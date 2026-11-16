-- Hospital Management System - database setup

CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

CREATE TABLE IF NOT EXISTS patients (
    patient_id      INT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    age             INT          NOT NULL,
    gender          VARCHAR(20)  NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    diagnosis       VARCHAR(255) NOT NULL,
    admission_date  DATE         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id),
    CHECK (age BETWEEN 0 AND 125)
);

-- Optional fictional sample rows
INSERT INTO patients (name, age, gender, phone, diagnosis, admission_date) VALUES
    ('Alex Rivera',  34, 'Male',              '647-555-0101', 'Seasonal flu',          '2026-10-02'),
    ('Maya Chen',    27, 'Female',            '437-555-0102', 'Sprained ankle',        '2026-10-15'),
    ('Sam Taylor',   61, 'Non-binary',        '416-555-0103', 'Routine blood test',    '2026-10-28'),
    ('Jordan Patel', 45, 'Prefer not to say', '647-555-0104', 'Mild allergic reaction', '2026-11-03');

-- Check the contents of the table
SELECT * FROM patients;
