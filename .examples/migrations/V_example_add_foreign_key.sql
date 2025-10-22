-- Example: Adding a foreign key relationship
-- File name: V{NEXT_NUMBER}__add_created_by_to_contacts.sql

-- Add the foreign key column
ALTER TABLE contacts 
ADD COLUMN created_by_user_id UUID;

-- Add the foreign key constraint
ALTER TABLE contacts
ADD CONSTRAINT fk_contacts_created_by 
FOREIGN KEY (created_by_user_id) 
REFERENCES users(id) 
ON DELETE SET NULL;

-- Add index for performance
CREATE INDEX idx_contacts_created_by_user_id ON contacts(created_by_user_id);

