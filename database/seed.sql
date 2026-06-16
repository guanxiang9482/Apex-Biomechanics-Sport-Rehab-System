USE apex_db;

-- Facilities
INSERT INTO facilities (name, type, status, location) VALUES
('Gait Analysis Lab A', 'Biomechanics', 'AVAILABLE', 'Block A, Level 1'),
('Strength Assessment Room', 'Performance', 'AVAILABLE', 'Block A, Level 2'),
('Recovery Pool', 'Hydrotherapy', 'AVAILABLE', 'Block B, Level 1');

-- Equipment
INSERT INTO equipments (facility_id, item_name, item_status, item_quantity) VALUES
(1, 'Force Plate', 'AVAILABLE', 2),
(1, 'Motion Capture Camera', 'AVAILABLE', 4),
(2, 'Resistance Bands', 'AVAILABLE', 10),
(2, 'Barbell Set', 'AVAILABLE', 3),
(3, 'Underwater Treadmill', 'AVAILABLE', 1);

-- Admin account (password: Admin@2026)
INSERT INTO users (username, password, email, role, fullname, contact, is_active)
VALUES ('admin', '$2a$10$QNDumlEXyS7e8E8QpK7PDOjNEE7pAjvH39PkcDAIA2CXP.9OiNK9S',
        'admin@apex.com', 'ADMIN', 'System Administrator', '0123456789', TRUE);

SET @admin_user_id = LAST_INSERT_ID();
INSERT INTO administrators (user_id, department)
VALUES (@admin_user_id, 'Management');

-- Therapist account (password: Admin@2026)
INSERT INTO users (username, password, email, role, fullname, contact, is_active)
VALUES ('therapist1', '$2a$10$Eg0qTieY9CPFksjD6l9NT.9OkgNcTFO8d6/MbuiW0Uq6pOe8ZptlC',
        'therapist1@apex.com', 'THERAPIST', 'Dr. Sarah Brown', '0123456780', TRUE);

SET @therapist_user_id = LAST_INSERT_ID();
INSERT INTO physiotherapists (user_id, specialization, license_number)
VALUES (@therapist_user_id, 'Sports Rehabilitation', 'PT-2024-001');
