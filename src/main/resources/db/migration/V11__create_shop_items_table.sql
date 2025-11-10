-- Create shop_items table
CREATE TABLE shop_items (
    id BIGSERIAL PRIMARY KEY,
    shopping_cart_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    CONSTRAINT fk_shop_items_shopping_cart FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_shop_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_shop_item_price CHECK (price >= 0),
    CONSTRAINT uk_shop_items_cart_product UNIQUE (shopping_cart_id, product_id)
);

-- Create indexes for faster lookups
CREATE INDEX idx_shop_items_shopping_cart_id ON shop_items(shopping_cart_id);
CREATE INDEX idx_shop_items_product_id ON shop_items(product_id);

-- Add comments
COMMENT ON TABLE shop_items IS 'Items in shopping carts';
COMMENT ON COLUMN shop_items.shopping_cart_id IS 'Shopping cart this item belongs to';
COMMENT ON COLUMN shop_items.product_id IS 'Product in the shopping cart';
COMMENT ON COLUMN shop_items.quantity IS 'Quantity of the product';
COMMENT ON COLUMN shop_items.price IS 'Price of the product at the time it was added to cart';
COMMENT ON CONSTRAINT uk_shop_items_cart_product ON shop_items IS 'Ensures a product can only appear once per shopping cart';

