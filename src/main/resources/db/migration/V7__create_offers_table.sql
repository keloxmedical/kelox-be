-- Create offers table
CREATE TABLE offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id BIGINT NOT NULL,
    creator_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_offers_hospital FOREIGN KEY (hospital_id) REFERENCES hospital_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_offers_creator FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_offer_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED'))
);

-- Create indexes for faster lookups
CREATE INDEX idx_offers_hospital_id ON offers(hospital_id);
CREATE INDEX idx_offers_creator_id ON offers(creator_id);
CREATE INDEX idx_offers_status ON offers(status);
CREATE INDEX idx_offers_created_at ON offers(created_at);
CREATE INDEX idx_offers_hospital_status ON offers(hospital_id, status);
CREATE INDEX idx_offers_creator_status ON offers(creator_id, status);

-- Add comments
COMMENT ON TABLE offers IS 'Offers made by users to hospitals for products';
COMMENT ON COLUMN offers.hospital_id IS 'Hospital receiving the offer';
COMMENT ON COLUMN offers.creator_id IS 'User who created the offer';
COMMENT ON COLUMN offers.created_at IS 'Timestamp when the offer was created';
COMMENT ON COLUMN offers.status IS 'Current status of the offer (PENDING, ACCEPTED, REJECTED)';

