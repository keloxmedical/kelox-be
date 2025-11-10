-- Add type and offer_id columns to shop_items table
ALTER TABLE shop_items 
ADD COLUMN type VARCHAR(20),
ADD COLUMN offer_id UUID;

-- Set default value for existing rows
UPDATE shop_items 
SET type = 'SINGLE' 
WHERE type IS NULL;

-- Make type column non-nullable
ALTER TABLE shop_items 
ALTER COLUMN type SET NOT NULL;

-- Add foreign key constraint for offer_id
ALTER TABLE shop_items 
ADD CONSTRAINT fk_shop_items_offer FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE SET NULL;

-- Add check constraint for type
ALTER TABLE shop_items 
ADD CONSTRAINT chk_shop_item_type CHECK (type IN ('SINGLE', 'OFFER'));

-- Create indexes for faster lookups
CREATE INDEX idx_shop_items_offer_id ON shop_items(offer_id);
CREATE INDEX idx_shop_items_type ON shop_items(type);

-- Add comments
COMMENT ON COLUMN shop_items.type IS 'Type of shop item: SINGLE (added directly) or OFFER (from accepted offer)';
COMMENT ON COLUMN shop_items.offer_id IS 'Offer ID if this item was added from an accepted offer';

