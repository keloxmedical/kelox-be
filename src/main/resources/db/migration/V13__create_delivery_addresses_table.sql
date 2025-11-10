-- Create delivery_addresses table
CREATE TABLE delivery_addresses (
    id BIGSERIAL PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    street_address VARCHAR(500) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NULL,
    postal_code VARCHAR(50) NOT NULL,
    country VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_addresses_hospital FOREIGN KEY (hospital_id) REFERENCES hospital_profiles(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_delivery_addresses_hospital_id ON delivery_addresses(hospital_id);
CREATE INDEX idx_delivery_addresses_is_default ON delivery_addresses(hospital_id, is_default);

-- Add comments
COMMENT ON TABLE delivery_addresses IS 'Delivery addresses for hospitals (max 5 per hospital)';
COMMENT ON COLUMN delivery_addresses.hospital_id IS 'Hospital that owns this delivery address';
COMMENT ON COLUMN delivery_addresses.street_address IS 'Full street address';
COMMENT ON COLUMN delivery_addresses.city IS 'City name';
COMMENT ON COLUMN delivery_addresses.state IS 'State/Province (optional)';
COMMENT ON COLUMN delivery_addresses.postal_code IS 'Postal/ZIP code';
COMMENT ON COLUMN delivery_addresses.country IS 'Country name';
COMMENT ON COLUMN delivery_addresses.is_default IS 'Whether this is the default delivery address for the hospital';

