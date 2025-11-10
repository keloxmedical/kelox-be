-- Add paid field to orders table
ALTER TABLE orders 
ADD COLUMN paid BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index for faster lookups
CREATE INDEX idx_orders_paid ON orders(paid);

-- Add comment
COMMENT ON COLUMN orders.paid IS 'Payment status - false by default, only admin can set to true';

