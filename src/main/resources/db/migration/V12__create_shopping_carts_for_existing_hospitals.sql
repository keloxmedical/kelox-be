-- Create shopping carts for all existing hospitals that don't have one yet
INSERT INTO shopping_carts (id, hospital_id, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    hp.id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM hospital_profiles hp
WHERE NOT EXISTS (
    SELECT 1 FROM shopping_carts sc WHERE sc.hospital_id = hp.id
);

-- Add comment
COMMENT ON TABLE shopping_carts IS 'Shopping carts automatically created for each hospital';

