-- SQLite initialization script for pharmacy database
-- Based on the Oracle schema but adapted for SQLite

-- Note: SQLite will automatically create the database file when this script runs
-- This script provides the basic structure, but Hibernate will handle table creation
-- with the hbm2ddl.auto=update setting

-- Create a basic configuration table if needed
CREATE TABLE IF NOT EXISTS CONFIGURABLE_AMOUNT (
    ID_CONFIGURABLE_AMOUNT INTEGER PRIMARY KEY AUTOINCREMENT,
    PRESCRIPTION_AMOUNT REAL DEFAULT 250.00 NOT NULL
);

-- Insert default configuration if table is empty
INSERT OR IGNORE INTO CONFIGURABLE_AMOUNT (ID_CONFIGURABLE_AMOUNT, PRESCRIPTION_AMOUNT) 
VALUES (1, 250.00);

-- Create prescription approvals table (adapted from Oracle version)
CREATE TABLE IF NOT EXISTS PRESCRIPTION_APPROVALS (
    ID_APPROVAL INTEGER PRIMARY KEY AUTOINCREMENT,
    AUTHORIZATION_NUMBER VARCHAR(50) NOT NULL UNIQUE,
    ID_USER INTEGER NOT NULL,
    PRESCRIPTION_ID_HOSPITAL VARCHAR(100),
    PRESCRIPTION_DETAILS TEXT, -- Using TEXT instead of CLOB
    PRESCRIPTION_COST REAL NOT NULL,
    APPROVAL_DATE DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    STATUS VARCHAR(20) NOT NULL, -- APPROVED, REJECTED
    REJECTION_REASON VARCHAR(255),
    FOREIGN KEY (ID_USER) REFERENCES USERS(ID_USER)
);

-- SQLite doesn't support comments on columns, but we can add them as table comments
-- PRESCRIPTION_ID_HOSPITAL: Optional ID of the prescription in the hospital system
-- PRESCRIPTION_DETAILS: Prescription details, possibly in JSON format
-- STATUS: Approval status: APPROVED, REJECTED

PRAGMA foreign_keys = ON; 