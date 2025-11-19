-- Drop existing check constraint first
ALTER TABLE offer_messages 
DROP CONSTRAINT IF EXISTS chk_offer_message_type;

-- Update message type from REJECT to REJECTED
UPDATE offer_messages 
SET type = 'REJECTED' 
WHERE type = 'REJECT';

-- Add new check constraint with REJECTED
ALTER TABLE offer_messages 
ADD CONSTRAINT chk_offer_message_type CHECK (type IS NULL OR type = 'REJECTED');

-- Add comment
COMMENT ON COLUMN offer_messages.type IS 'Message type: null for normal messages, REJECTED for rejection messages';

