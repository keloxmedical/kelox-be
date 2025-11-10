-- Make delivery_fee nullable
-- Delivery fee will be calculated by logistics system after order is created

-- Set any existing delivery fees to 0 if needed
UPDATE orders 
SET delivery_fee = 0 
WHERE delivery_fee IS NULL;

-- Make column nullable
ALTER TABLE orders 
ALTER COLUMN delivery_fee DROP NOT NULL;

-- Drop existing check constraint
ALTER TABLE orders 
DROP CONSTRAINT IF EXISTS chk_order_delivery_fee;

-- Add new check constraint that allows null
ALTER TABLE orders 
ADD CONSTRAINT chk_order_delivery_fee CHECK (delivery_fee IS NULL OR delivery_fee >= 0);

-- Update comment
COMMENT ON COLUMN orders.delivery_fee IS 'Delivery fee for this order (initially 0 or null, will be calculated by logistics system)';

