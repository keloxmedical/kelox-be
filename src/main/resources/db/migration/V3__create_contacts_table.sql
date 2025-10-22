-- Create contacts table
CREATE TABLE contacts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    hospital_profile_id BIGINT NOT NULL,
    CONSTRAINT fk_contacts_hospital_profile FOREIGN KEY (hospital_profile_id) REFERENCES hospital_profiles(id) ON DELETE CASCADE
);

-- Create index on hospital_profile_id for faster lookups
CREATE INDEX idx_contacts_hospital_profile_id ON contacts(hospital_profile_id);

-- Create index on email for potential searches
CREATE INDEX idx_contacts_email ON contacts(email);

