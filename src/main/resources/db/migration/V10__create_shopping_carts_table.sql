-- Create shopping_carts table
CREATE TABLE shopping_carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shopping_carts_hospital FOREIGN KEY (hospital_id) REFERENCES hospital_profiles(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_shopping_carts_hospital_id ON shopping_carts(hospital_id);

-- Add comments
COMMENT ON TABLE shopping_carts IS 'Shopping carts for hospitals';
COMMENT ON COLUMN shopping_carts.hospital_id IS 'Hospital that owns this shopping cart';
COMMENT ON COLUMN shopping_carts.created_at IS 'Timestamp when the shopping cart was created';
COMMENT ON COLUMN shopping_carts.updated_at IS 'Timestamp when the shopping cart was last updated';

