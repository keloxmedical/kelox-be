-- Create offer_products table
CREATE TABLE offer_products (
    id BIGSERIAL PRIMARY KEY,
    offer_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    CONSTRAINT fk_offer_products_offer FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE,
    CONSTRAINT fk_offer_products_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_offer_product_quantity CHECK (quantity > 0),
    CONSTRAINT chk_offer_product_price CHECK (price >= 0)
);

-- Create indexes for faster lookups
CREATE INDEX idx_offer_products_offer_id ON offer_products(offer_id);
CREATE INDEX idx_offer_products_product_id ON offer_products(product_id);

-- Add comments
COMMENT ON TABLE offer_products IS 'Products included in an offer with custom quantity and price';
COMMENT ON COLUMN offer_products.offer_id IS 'Offer this product belongs to';
COMMENT ON COLUMN offer_products.product_id IS 'Product being offered';
COMMENT ON COLUMN offer_products.quantity IS 'Custom quantity for this product in the offer';
COMMENT ON COLUMN offer_products.price IS 'Custom price for this product in the offer';

