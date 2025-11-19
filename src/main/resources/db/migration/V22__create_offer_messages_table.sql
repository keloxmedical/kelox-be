-- Create offer_messages table
CREATE TABLE offer_messages (
    id BIGSERIAL PRIMARY KEY,
    offer_id UUID NOT NULL,
    message VARCHAR(255) NOT NULL,
    sender_hospital_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offer_messages_offer FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_offer_messages_offer_id ON offer_messages(offer_id);
CREATE INDEX idx_offer_messages_created_at ON offer_messages(created_at);

-- Add comments
COMMENT ON TABLE offer_messages IS 'Chat messages between buyer and seller for offers';
COMMENT ON COLUMN offer_messages.offer_id IS 'Offer this message belongs to';
COMMENT ON COLUMN offer_messages.message IS 'Message text (max 255 characters)';
COMMENT ON COLUMN offer_messages.sender_hospital_name IS 'Name of the hospital that sent the message';
COMMENT ON COLUMN offer_messages.created_at IS 'Timestamp when the message was sent';

