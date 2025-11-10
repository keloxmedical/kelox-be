-- Create order_items table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    type VARCHAR(20) NOT NULL,
    offer_id UUID NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_price CHECK (price >= 0),
    CONSTRAINT chk_order_item_type CHECK (type IN ('SINGLE', 'OFFER'))
);

-- Create indexes for faster lookups
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_offer_id ON order_items(offer_id);
CREATE INDEX idx_order_items_type ON order_items(type);

-- Add comments
COMMENT ON TABLE order_items IS 'Items in orders, created from shopping cart items';
COMMENT ON COLUMN order_items.order_id IS 'Order this item belongs to';
COMMENT ON COLUMN order_items.product_id IS 'Product in the order';
COMMENT ON COLUMN order_items.quantity IS 'Quantity of the product ordered';
COMMENT ON COLUMN order_items.price IS 'Price of the product at the time of order';
COMMENT ON COLUMN order_items.type IS 'Type of item: SINGLE (added directly) or OFFER (from accepted offer)';
COMMENT ON COLUMN order_items.offer_id IS 'Offer ID if this item was from an accepted offer';

