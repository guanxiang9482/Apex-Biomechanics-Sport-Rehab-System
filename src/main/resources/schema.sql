USE apex_db;

-- ============================================================
-- TIER 1: Users (Central auth table - LSP Table-per-Type)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    role          ENUM('ADMIN','ATHLETE','THERAPIST') NOT NULL,
    is_active     BOOLEAN DEFAULT TRUE,
    last_active   TIMESTAMP NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TIER 2: Role-specific tables (Table-per-Type inheritance)
-- ============================================================
CREATE TABLE IF NOT EXISTS athletes (
    athlete_id     INT PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    date_of_birth  DATE,
    phone          VARCHAR(20),
    injury_status  VARCHAR(100) DEFAULT 'None',
    body_weight_kg DOUBLE,
    height_cm      DOUBLE,
    posture_notes  TEXT,
    FOREIGN KEY (athlete_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS physiotherapists (
    therapist_id   INT PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    phone          VARCHAR(20),
    license_number VARCHAR(50),
    FOREIGN KEY (therapist_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS administrators (
    admin_id   INT PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    department VARCHAR(100),
    FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ============================================================
-- TIER 3: Facilities
-- ============================================================
CREATE TABLE IF NOT EXISTS facilities (
    facility_id   INT AUTO_INCREMENT PRIMARY KEY,
    facility_name VARCHAR(100) NOT NULL,
    facility_type VARCHAR(50),
    is_available  BOOLEAN DEFAULT TRUE,
    notes         TEXT
);

-- ============================================================
-- TIER 4: Sessions (Core relational hub)
-- Bridges Athlete + Physiotherapist + Facility
-- Supports UC7, UC9, UC11
-- ============================================================
CREATE TABLE IF NOT EXISTS sessions (
    session_id    INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id    INT NOT NULL,
    therapist_id  INT,
    facility_id   INT,
    session_date  DATETIME NOT NULL,
    duration_mins INT DEFAULT 60,
    session_type  VARCHAR(100),
    status        ENUM('SCHEDULED','COMPLETED','CANCELLED','PENDING_FOLLOWUP')
                  DEFAULT 'SCHEDULED',
    notes         TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (athlete_id)  REFERENCES athletes(athlete_id),
    FOREIGN KEY (therapist_id) REFERENCES physiotherapists(therapist_id),
    FOREIGN KEY (facility_id)  REFERENCES facilities(facility_id)
);

-- ============================================================
-- TIER 5: BiomechanicalRecords
-- Linked to session_id for audit trail (UC12)
-- ============================================================
CREATE TABLE IF NOT EXISTS biomechanical_records (
    record_id      INT AUTO_INCREMENT PRIMARY KEY,
    session_id     INT NOT NULL,
    jump_power     DOUBLE,
    joint_mobility DOUBLE,
    posture_score  DOUBLE,
    notes          TEXT,
    recorded_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(session_id)
                             ON DELETE CASCADE
);

-- ============================================================
-- TIER 6: ClinicalReports
-- Finalized summaries by therapists (UC14)
-- ============================================================
CREATE TABLE IF NOT EXISTS clinical_reports (
    report_id    INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id   INT NOT NULL,
    therapist_id INT NOT NULL,
    report_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    summary      TEXT NOT NULL,
    status       ENUM('DRAFT','FINALIZED') DEFAULT 'DRAFT',
    FOREIGN KEY (athlete_id)  REFERENCES athletes(athlete_id),
    FOREIGN KEY (therapist_id) REFERENCES physiotherapists(therapist_id)
);

-- ============================================================
-- TIER 7: Invoices (Strategy pattern persistence - UC18, UC20)
-- ============================================================
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id   INT AUTO_INCREMENT PRIMARY KEY,
    session_id   INT NOT NULL,
    athlete_id   INT NOT NULL,
    billing_type ENUM('STANDARD','INSURANCE','SPONSORSHIP') NOT NULL,
    amount       DOUBLE NOT NULL,
    status       ENUM('PENDING','PAID','CANCELLED') DEFAULT 'PENDING',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(session_id),
    FOREIGN KEY (athlete_id) REFERENCES athletes(athlete_id)
);

-- ============================================================
-- TIER 8: NotificationLog (Observer pattern persistence - UC21)
-- ============================================================
CREATE TABLE IF NOT EXISTS notification_log (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_id    INT NOT NULL,
    event_message   VARCHAR(500) NOT NULL,
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES users(user_id)
);

-- ============================================================
-- Seed Data
-- ============================================================
INSERT IGNORE INTO facilities (facility_name, facility_type, is_available) VALUES
    ('Gait Analysis Lab A',      'Biomechanics',  TRUE),
    ('Strength Assessment Room', 'Performance',   TRUE),
    ('Recovery Pool',            'Hydrotherapy',  TRUE);
