-- Example: Adding a column with enum-like values
-- File name: V{NEXT_NUMBER}__add_status_to_hospital_profiles.sql

-- Add status column with default value
ALTER TABLE hospital_profiles 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- Add check constraint to enforce enum values
ALTER TABLE hospital_profiles 
ADD CONSTRAINT chk_hospital_profile_status 
CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED', 'ARCHIVED'));

-- Add index for filtering
CREATE INDEX idx_hospital_profiles_status ON hospital_profiles(status);

-- Add comment
COMMENT ON COLUMN hospital_profiles.status IS 'Status of the hospital profile: PENDING, ACTIVE, INACTIVE, SUSPENDED, or ARCHIVED';

