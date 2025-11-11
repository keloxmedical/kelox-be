-- Add balance column to hospital_profiles table
ALTER TABLE hospital_profiles 
ADD COLUMN balance REAL NOT NULL DEFAULT 0;

-- Add check constraint for non-negative balance
ALTER TABLE hospital_profiles 
ADD CONSTRAINT chk_hospital_balance CHECK (balance >= 0);

-- Create index for faster lookups
CREATE INDEX idx_hospital_profiles_balance ON hospital_profiles(balance);

-- Add comment
COMMENT ON COLUMN hospital_profiles.balance IS 'Hospital wallet balance - can be increased/decreased by admin';

