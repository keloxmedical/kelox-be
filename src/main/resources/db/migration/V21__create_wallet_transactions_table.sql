-- Create wallet_transactions table
CREATE TABLE wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount REAL NOT NULL,
    description TEXT,
    order_id UUID NULL,
    balance_before REAL NOT NULL,
    balance_after REAL NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_transactions_hospital FOREIGN KEY (hospital_id) REFERENCES hospital_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_wallet_transactions_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    CONSTRAINT chk_transaction_type CHECK (type IN ('DEPOSIT', 'WITHDRAW')),
    CONSTRAINT chk_transaction_amount CHECK (amount > 0),
    CONSTRAINT chk_transaction_balance_before CHECK (balance_before >= 0),
    CONSTRAINT chk_transaction_balance_after CHECK (balance_after >= 0)
);

-- Create indexes for faster lookups
CREATE INDEX idx_wallet_transactions_hospital_id ON wallet_transactions(hospital_id);
CREATE INDEX idx_wallet_transactions_order_id ON wallet_transactions(order_id);
CREATE INDEX idx_wallet_transactions_type ON wallet_transactions(type);
CREATE INDEX idx_wallet_transactions_created_at ON wallet_transactions(created_at);

-- Add comments
COMMENT ON TABLE wallet_transactions IS 'Wallet transactions for hospital balances';
COMMENT ON COLUMN wallet_transactions.hospital_id IS 'Hospital that this transaction belongs to';
COMMENT ON COLUMN wallet_transactions.type IS 'Transaction type: DEPOSIT (increase balance) or WITHDRAW (decrease balance)';
COMMENT ON COLUMN wallet_transactions.amount IS 'Transaction amount (always positive)';
COMMENT ON COLUMN wallet_transactions.description IS 'Admin-provided description of the transaction';
COMMENT ON COLUMN wallet_transactions.order_id IS 'Optional order ID if transaction is related to an order';
COMMENT ON COLUMN wallet_transactions.balance_before IS 'Hospital balance before this transaction';
COMMENT ON COLUMN wallet_transactions.balance_after IS 'Hospital balance after this transaction';
COMMENT ON COLUMN wallet_transactions.created_at IS 'Timestamp when transaction was created';

