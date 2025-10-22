-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    wallet VARCHAR(255) NOT NULL UNIQUE
);

-- Create index on wallet for faster lookups
CREATE INDEX idx_users_wallet ON users(wallet);

