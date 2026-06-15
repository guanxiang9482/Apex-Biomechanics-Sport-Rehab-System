USE apex_db;

-- ============================================================
-- APEX BIOMECHANICS & SPORTS REHAB SYSTEM
-- Schema v2.0 — Aligned to submitted ERD
-- ============================================================

-- Users (central auth, fullname stored here per ERD)
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) UNIQUE NOT NULL,
    password      VARCHAR(255) NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    role          ENUM('ADMIN','ATHLETE','THERAPIST') NOT NULL,
    fullname      VARCHAR(100) NOT NULL,
    last_login_at TIMESTAMP NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active     BOOLEAN DEFAULT TRUE
);

-- Administrators (separate PK per ERD)
CREATE TABLE IF NOT EXISTS administrators (
    admin_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL UNIQUE,
    department VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
                          ON DELETE CASCADE
);

-- Physiotherapists (separate PK per ERD)
CREATE TABLE IF NOT EXISTS physiotherapists (
    therapist_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL UNIQUE,
    specialization VARCHAR(100),
    license_number VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
                          ON DELETE CASCADE
);

-- Athletes (separate PK per ERD)
CREATE TABLE IF NOT EXISTS athletes (
    athlete_id   INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT NOT NULL UNIQUE,
    date_of_birth DATE,
    sport        VARCHAR(100),
    injury_status VARCHAR(100) DEFAULT 'None',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
                          ON DELETE CASCADE
);

-- Facilities
CREATE TABLE IF NOT EXISTS facilities (
    facility_id          INT AUTO_INCREMENT PRIMARY KEY,
    last_used_by_therapist INT NULL,
    name                 VARCHAR(100) NOT NULL,
    type                 VARCHAR(50),
    status               ENUM('AVAILABLE','MAINTENANCE',
                              'RESERVED') DEFAULT 'AVAILABLE',
    location             VARCHAR(100),
    FOREIGN KEY (last_used_by_therapist)
        REFERENCES physiotherapists(therapist_id)
        ON DELETE SET NULL
);

-- Equipment (per ERD — contained in Facilities)
CREATE TABLE IF NOT EXISTS equipments (
    item_id       INT AUTO_INCREMENT PRIMARY KEY,
    facility_id   INT NOT NULL,
    item_name     VARCHAR(100) NOT NULL,
    item_status   ENUM('AVAILABLE','IN_USE','MAINTENANCE')
                  DEFAULT 'AVAILABLE',
    item_quantity INT DEFAULT 1,
    FOREIGN KEY (facility_id) REFERENCES facilities(facility_id)
                              ON DELETE CASCADE
);

-- Sessions (core relational hub per ERD)
CREATE TABLE IF NOT EXISTS sessions (
    session_id     INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id     INT NOT NULL,
    therapist_id   INT NOT NULL,
    facility_id    INT,
    session_type   VARCHAR(100),
    scheduled_date DATETIME NOT NULL,
    status         ENUM('SCHEDULED','COMPLETED','CANCELLED',
                        'PENDING_FOLLOWUP') DEFAULT 'SCHEDULED',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (athlete_id)
        REFERENCES athletes(athlete_id),
    FOREIGN KEY (therapist_id)
        REFERENCES physiotherapists(therapist_id),
    FOREIGN KEY (facility_id)
        REFERENCES facilities(facility_id)
        ON DELETE SET NULL
);

-- Medical Records (per ERD — owned by athlete)
CREATE TABLE IF NOT EXISTS medical_records (
    record_id           INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id          INT NOT NULL,
    created_by_therapist INT NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    diagnosis_notes     TEXT,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (athlete_id)
        REFERENCES athletes(athlete_id)
        ON DELETE CASCADE,
    FOREIGN KEY (created_by_therapist)
        REFERENCES physiotherapists(therapist_id)
);

-- Biomechanical Records (per ERD — has athlete_id + therapist_id)
CREATE TABLE IF NOT EXISTS biomechanical_records (
    record_id      INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id     INT NOT NULL,
    therapist_id   INT NOT NULL,
    session_id     INT NOT NULL,
    jump_power     DOUBLE,
    joint_mobility DOUBLE,
    posture_score  DOUBLE,
    recorded_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    treatment_note TEXT,
    FOREIGN KEY (athlete_id)
        REFERENCES athletes(athlete_id),
    FOREIGN KEY (therapist_id)
        REFERENCES physiotherapists(therapist_id),
    FOREIGN KEY (session_id)
        REFERENCES sessions(session_id)
        ON DELETE CASCADE
);

-- Invoices (per ERD — base_amount, discount_rate, final_amount)
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id     INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id     INT NOT NULL,
    session_id     INT NOT NULL,
    base_amount    DOUBLE NOT NULL,
    discount_rate  DOUBLE DEFAULT 0.0,
    final_amount   DOUBLE NOT NULL,
    billing_type   ENUM('STANDARD','INSURANCE','SPONSORSHIP')
                   NOT NULL,
    payment_method ENUM('CASH','CARD','INSURANCE_CLAIM',
                        'SPONSORED','PENDING')
                   DEFAULT 'PENDING',
    issued_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (athlete_id)
        REFERENCES athletes(athlete_id),
    FOREIGN KEY (session_id)
        REFERENCES sessions(session_id)
);

-- Clinical Reports (per ERD)
CREATE TABLE IF NOT EXISTS clinical_reports (
    report_id          INT AUTO_INCREMENT PRIMARY KEY,
    submit_by_therapist INT NOT NULL,
    approve_by_admin    INT NULL,
    report_type        VARCHAR(100),
    description        TEXT,
    submitted_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at        TIMESTAMP NULL,
    status             ENUM('DRAFT','SUBMITTED','APPROVED',
                            'REJECTED') DEFAULT 'DRAFT',
    FOREIGN KEY (submit_by_therapist)
        REFERENCES physiotherapists(therapist_id),
    FOREIGN KEY (approve_by_admin)
        REFERENCES administrators(admin_id)
        ON DELETE SET NULL
);

-- Notification Log (per ERD)
CREATE TABLE IF NOT EXISTS notifications_log (
    notif_id     INT AUTO_INCREMENT PRIMARY KEY,
    recipient_id INT NOT NULL,
    message      VARCHAR(500) NOT NULL,
    is_read      BOOLEAN DEFAULT FALSE,
    timestamp    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- ============================================================
-- Seed Data
-- ============================================================
INSERT INTO facilities (name, type, status, location) VALUES
    ('Gait Analysis Lab A',      'Biomechanics',  'AVAILABLE',
     'Block A, Level 1'),
    ('Strength Assessment Room', 'Performance',   'AVAILABLE',
     'Block A, Level 2'),
    ('Recovery Pool',            'Hydrotherapy',  'AVAILABLE',
     'Block B, Level 1');

INSERT INTO equipments (facility_id, item_name, item_status,
                        item_quantity) VALUES
    (1, 'Force Plate',          'AVAILABLE', 2),
    (1, 'Motion Capture Camera','AVAILABLE', 4),
    (2, 'Resistance Bands',     'AVAILABLE', 10),
    (2, 'Barbell Set',          'AVAILABLE', 3),
    (3, 'Underwater Treadmill', 'AVAILABLE', 1);