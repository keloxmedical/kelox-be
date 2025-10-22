# Database Migration Guide - Flyway

This project uses **Flyway** for database version control and migrations. This ensures all database changes are tracked, versioned, and can be applied consistently across all environments.

## 📁 Migration Files Location

All migration files are located in: `src/main/resources/db/migration/`

## 📝 Naming Convention

Migration files **must** follow this pattern:

```
V{VERSION}__{DESCRIPTION}.sql
```

**Examples:**
- `V1__create_users_table.sql`
- `V2__create_hospital_profiles_table.sql`
- `V3__add_status_to_hospital_profiles.sql`
- `V4__alter_contacts_add_mobile.sql`

**Important Rules:**
- Version numbers must be unique and sequential
- Use **TWO** underscores `__` between version and description
- Use **ONE** underscore `_` between words in the description
- Never modify a migration file after it has been applied
- Use lowercase with underscores for descriptions

## 🔄 Development Workflow

### 1. Creating a New Entity

When you create a new JPA entity, create a corresponding migration:

**Step 1:** Create the entity class (e.g., `Patient.java`)

**Step 2:** Create migration file: `V4__create_patients_table.sql`

```sql
CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patients_last_name ON patients(last_name);
```

**Step 3:** Run the application - Flyway will automatically apply the migration

### 2. Modifying an Existing Entity

**Never modify old migration files!** Always create a new migration.

**Example:** Adding a field to HospitalProfile

**Step 1:** Update the entity class:
```java
@Column(nullable = true)
private String website;
```

**Step 2:** Create migration: `V4__add_website_to_hospital_profiles.sql`
```sql
ALTER TABLE hospital_profiles 
ADD COLUMN website VARCHAR(500);
```

### 3. Adding a Relationship

**Example:** Adding a many-to-one relationship

Create migration: `V5__add_created_by_to_contacts.sql`
```sql
ALTER TABLE contacts 
ADD COLUMN created_by UUID,
ADD CONSTRAINT fk_contacts_created_by FOREIGN KEY (created_by) REFERENCES users(id);

CREATE INDEX idx_contacts_created_by ON contacts(created_by);
```

### 4. Renaming a Column

**Step 1:** Create migration: `V6__rename_company_name_to_organization_name.sql`
```sql
ALTER TABLE hospital_profiles 
RENAME COLUMN company_name TO organization_name;
```

**Step 2:** Update the Java entity field name accordingly

### 5. Adding Data (Seeds)

Create migration: `V7__insert_default_data.sql`
```sql
-- Insert default admin user
INSERT INTO users (id, wallet) 
VALUES ('550e8400-e29b-41d4-a716-446655440000', '0x0000000000000000000000000000000000000000');
```

## 🚀 Running Migrations

### Automatic (Recommended for Development)

Migrations run automatically when you start the application:
```bash
./gradlew bootRun
```

### Manual via Gradle

```bash
# Apply all pending migrations
./gradlew flywayMigrate

# Get migration status
./gradlew flywayInfo

# Validate applied migrations
./gradlew flywayValidate

# Clean database (⚠️ DANGEROUS - removes all objects)
./gradlew flywayClean
```

## 📊 Flyway Metadata

Flyway tracks applied migrations in a table called `flyway_schema_history`.

You can query it in DataGrip:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

This shows:
- Which migrations have been applied
- When they were applied
- Execution time
- Checksum (detects if file was modified)

## ⚠️ Important Rules

### ✅ DO:
- Create a new migration for every schema change
- Use descriptive names
- Test migrations on a local database first
- Keep migrations small and focused
- Add indexes for foreign keys and frequently queried columns
- Use transactions when possible
- Add comments to complex migrations

### ❌ DON'T:
- Never modify a migration file after it's been applied
- Never delete old migration files
- Don't skip version numbers
- Don't use the same version number twice
- Never use `flyway.clean` in production

## 🔧 Common Migration Patterns

### Adding NOT NULL Column with Default Value

```sql
-- Add column as nullable first
ALTER TABLE hospital_profiles ADD COLUMN status VARCHAR(50);

-- Set default value for existing rows
UPDATE hospital_profiles SET status = 'ACTIVE' WHERE status IS NULL;

-- Now make it NOT NULL
ALTER TABLE hospital_profiles ALTER COLUMN status SET NOT NULL;
```

### Creating Enum-like Constraints

```sql
ALTER TABLE hospital_profiles 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
ADD CONSTRAINT chk_hospital_profile_status 
    CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED'));
```

### Adding Timestamps

```sql
ALTER TABLE contacts 
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create trigger to auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_contacts_updated_at 
    BEFORE UPDATE ON contacts 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
```

## 🐛 Troubleshooting

### Migration Failed

If a migration fails:
1. Fix the issue in the migration file
2. Manually delete the failed entry from `flyway_schema_history`
3. Restart the application

```sql
-- Find the failed migration
SELECT * FROM flyway_schema_history WHERE success = false;

-- Delete it (replace version with actual version)
DELETE FROM flyway_schema_history WHERE version = '4';
```

### Out of Order Migration

If you accidentally create `V6` before `V5` was applied:
- Set `spring.flyway.out-of-order=true` in `application.yml` (temporarily)
- Or rename files to fix the order

### Checksum Mismatch

If you see "Migration checksum mismatch":
- Someone modified an already-applied migration
- Find the entry in `flyway_schema_history` and update its checksum
- Or delete it and rerun (only in development!)

## 📝 Best Practices

1. **Review Before Committing**: Always review migration SQL before committing
2. **Test Locally**: Run migrations on your local database first
3. **Backup Production**: Always backup before running migrations in production
4. **Incremental Changes**: Make small, incremental changes rather than large migrations
5. **Documentation**: Add comments explaining complex logic
6. **Rollback Plan**: Know how to rollback (create a down migration if needed)

## 🔄 Current Migration Status

Check the current state:
```bash
./gradlew flywayInfo
```

View in database:
```sql
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

