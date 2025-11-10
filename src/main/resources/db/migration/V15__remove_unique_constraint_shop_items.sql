-- Remove unique constraint on (shopping_cart_id, product_id)
-- This allows the same product to exist multiple times in cart with different types (SINGLE vs OFFER)
ALTER TABLE shop_items 
DROP CONSTRAINT IF EXISTS uk_shop_items_cart_product;

-- Add comment
COMMENT ON TABLE shop_items IS 'Items in shopping carts - same product can exist as SINGLE and OFFER types';

