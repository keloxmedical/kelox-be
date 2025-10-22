-- Example: Creating a new table
-- File name: V{NEXT_NUMBER}__create_patients_table.sql

CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    ssn VARCHAR(20) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes
CREATE INDEX idx_patients_last_name ON patients(last_name);
CREATE INDEX idx_patients_created_at ON patients(created_at);

-- Add comments
COMMENT ON TABLE patients IS 'Patient information';
COMMENT ON COLUMN patients.ssn IS 'Social Security Number - encrypted';

