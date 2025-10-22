-- Create hospital_profiles table
CREATE TABLE hospital_profiles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    owner_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_hospital_profiles_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on owner_id for faster lookups
CREATE INDEX idx_hospital_profiles_owner_id ON hospital_profiles(owner_id);

