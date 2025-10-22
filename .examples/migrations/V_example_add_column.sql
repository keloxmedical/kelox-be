-- Example: Adding a new column to existing table
-- File name: V{NEXT_NUMBER}__add_phone_to_hospital_profiles.sql

-- Add nullable column first
ALTER TABLE hospital_profiles 
ADD COLUMN phone VARCHAR(20);

-- Optionally set a default value for existing rows
UPDATE hospital_profiles 
SET phone = 'N/A' 
WHERE phone IS NULL;

-- If you want to make it NOT NULL after setting defaults
-- ALTER TABLE hospital_profiles 
-- ALTER COLUMN phone SET NOT NULL;

-- Add index if needed
CREATE INDEX idx_hospital_profiles_phone ON hospital_profiles(phone);

