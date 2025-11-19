-- Drop existing check constraint
ALTER TABLE offer_messages 
DROP CONSTRAINT IF EXISTS chk_offer_message_type;

-- Add new check constraint with SYSTEM type
ALTER TABLE offer_messages 
ADD CONSTRAINT chk_offer_message_type CHECK (type IS NULL OR type IN ('REJECTED', 'SYSTEM'));

-- Add comment
COMMENT ON COLUMN offer_messages.type IS 'Message type: null for normal messages, REJECTED for rejection messages, SYSTEM for system-generated messages';

