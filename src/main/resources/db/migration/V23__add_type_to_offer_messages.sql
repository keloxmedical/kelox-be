-- Add type column to offer_messages table
ALTER TABLE offer_messages 
ADD COLUMN type VARCHAR(20) NULL;

-- Add check constraint for type
ALTER TABLE offer_messages 
ADD CONSTRAINT chk_offer_message_type CHECK (type IS NULL OR type = 'REJECT');

-- Create index for faster lookups
CREATE INDEX idx_offer_messages_type ON offer_messages(type);

-- Add comment
COMMENT ON COLUMN offer_messages.type IS 'Message type: null for normal messages, REJECT for rejection messages';

