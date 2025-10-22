-- Allow hospital profiles to exist without an owner initially
ALTER TABLE hospital_profiles 
ALTER COLUMN owner_id DROP NOT NULL;

-- Add comment
COMMENT ON COLUMN hospital_profiles.owner_id IS 'User who owns this hospital profile - can be assigned later by admin';

