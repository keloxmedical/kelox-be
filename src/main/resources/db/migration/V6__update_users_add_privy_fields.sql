-- Rename wallet column to solana_wallet
ALTER TABLE users 
RENAME COLUMN wallet TO solana_wallet;

-- Add new columns
ALTER TABLE users 
ADD COLUMN privy_id VARCHAR(255),
ADD COLUMN email VARCHAR(255),
ADD COLUMN ethereum_wallet VARCHAR(255);

-- For existing users, set default values (you may want to update these manually)
UPDATE users 
SET privy_id = CONCAT('legacy_', id),
    email = CONCAT('user_', id, '@legacy.kelox.com')
WHERE privy_id IS NULL;

-- Make columns non-nullable after setting defaults
ALTER TABLE users 
ALTER COLUMN privy_id SET NOT NULL,
ALTER COLUMN email SET NOT NULL;

-- Add unique constraints
ALTER TABLE users 
ADD CONSTRAINT uk_users_privy_id UNIQUE (privy_id);

ALTER TABLE users 
ADD CONSTRAINT uk_users_ethereum_wallet UNIQUE (ethereum_wallet);

-- Create indexes
CREATE INDEX idx_users_privy_id ON users(privy_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_solana_wallet ON users(solana_wallet);
CREATE INDEX idx_users_ethereum_wallet ON users(ethereum_wallet);

-- Add comments
COMMENT ON COLUMN users.privy_id IS 'Privy unique user identifier';
COMMENT ON COLUMN users.email IS 'User email from Privy';
COMMENT ON COLUMN users.solana_wallet IS 'Solana wallet address from Privy embedded wallet';
COMMENT ON COLUMN users.ethereum_wallet IS 'Ethereum wallet address (optional)';

