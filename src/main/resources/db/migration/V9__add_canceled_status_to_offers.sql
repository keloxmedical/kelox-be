-- Add CANCELED status to offers table check constraint
-- Drop the existing constraint
ALTER TABLE offers DROP CONSTRAINT IF EXISTS chk_offer_status;

-- Add new constraint with CANCELED status
ALTER TABLE offers 
ADD CONSTRAINT chk_offer_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED'));

-- Add comment
COMMENT ON CONSTRAINT chk_offer_status ON offers IS 'Valid offer statuses: PENDING, ACCEPTED, REJECTED, CANCELED';

