-- Create orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id BIGINT NOT NULL,
    delivery_address_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CALCULATING_LOGISTICS',
    products_cost REAL NOT NULL,
    platform_fee REAL NOT NULL,
    delivery_fee REAL NOT NULL,
    total_cost REAL NOT NULL,
    CONSTRAINT fk_orders_hospital FOREIGN KEY (hospital_id) REFERENCES hospital_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_delivery_address FOREIGN KEY (delivery_address_id) REFERENCES delivery_addresses(id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_status CHECK (status IN ('CALCULATING_LOGISTICS', 'CONFIRMING_PAYMENT', 'IN_TRANSIT', 'COMPLETED', 'CANCELED')),
    CONSTRAINT chk_order_products_cost CHECK (products_cost >= 0),
    CONSTRAINT chk_order_platform_fee CHECK (platform_fee >= 0),
    CONSTRAINT chk_order_delivery_fee CHECK (delivery_fee >= 0),
    CONSTRAINT chk_order_total_cost CHECK (total_cost >= 0)
);

-- Create indexes for faster lookups
CREATE INDEX idx_orders_hospital_id ON orders(hospital_id);
CREATE INDEX idx_orders_delivery_address_id ON orders(delivery_address_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_hospital_status ON orders(hospital_id, status);

-- Add comments
COMMENT ON TABLE orders IS 'Orders created from shopping cart items';
COMMENT ON COLUMN orders.hospital_id IS 'Hospital that placed this order';
COMMENT ON COLUMN orders.delivery_address_id IS 'Delivery address for this order';
COMMENT ON COLUMN orders.created_at IS 'Timestamp when the order was created';
COMMENT ON COLUMN orders.completed_at IS 'Timestamp when the order was completed (null until status is COMPLETED)';
COMMENT ON COLUMN orders.status IS 'Current status of the order';
COMMENT ON COLUMN orders.products_cost IS 'Total cost of all products in the order';
COMMENT ON COLUMN orders.platform_fee IS 'Platform fee charged for this order';
COMMENT ON COLUMN orders.delivery_fee IS 'Delivery fee for this order';
COMMENT ON COLUMN orders.total_cost IS 'Total cost including all fees';

