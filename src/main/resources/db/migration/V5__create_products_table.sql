-- Create products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    lot_number VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    description TEXT,
    price REAL NOT NULL,
    quantity INTEGER NOT NULL,
    unit VARCHAR(20) NOT NULL,
    seller_hospital_id BIGINT NOT NULL,
    CONSTRAINT fk_products_seller_hospital FOREIGN KEY (seller_hospital_id) REFERENCES hospital_profiles(id) ON DELETE CASCADE,
    CONSTRAINT chk_product_unit CHECK (unit IN ('BOX', 'PIECE')),
    CONSTRAINT chk_product_price CHECK (price >= 0),
    CONSTRAINT chk_product_quantity CHECK (quantity >= 0)
);

-- Create indexes for faster lookups
CREATE INDEX idx_products_seller_hospital_id ON products(seller_hospital_id);
CREATE INDEX idx_products_code ON products(code);
CREATE INDEX idx_products_lot_number ON products(lot_number);
CREATE INDEX idx_products_expiry_date ON products(expiry_date);

-- Add comments
COMMENT ON TABLE products IS 'Medical products sold by hospitals';
COMMENT ON COLUMN products.expiry_date IS 'Product expiration date';
COMMENT ON COLUMN products.seller_hospital_id IS 'Hospital that is selling this product';

