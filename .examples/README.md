# Migration Examples

This folder contains example migration scripts for common database operations.

## Usage

These files are **examples only** - they will NOT be executed by Flyway.

When you need to create a migration:

1. Copy the relevant example from this folder
2. Rename it with the next version number
3. Move it to `src/main/resources/db/migration/`
4. Modify the SQL to match your needs
5. Run the application to apply the migration

## Available Examples

- **V_example_add_new_table.sql** - Creating a new table with indexes
- **V_example_add_column.sql** - Adding a column to an existing table
- **V_example_add_foreign_key.sql** - Adding foreign key relationships
- **V_example_add_enum_constraint.sql** - Adding status/enum columns with constraints
- **V_example_add_timestamps.sql** - Adding audit timestamps with auto-update triggers

## Quick Reference

### Naming Pattern
```
V{VERSION}__{DESCRIPTION}.sql
```

### Examples
- `V4__create_patients_table.sql`
- `V5__add_status_to_contacts.sql`
- `V6__add_created_at_to_users.sql`

### Remember
- Use TWO underscores `__` between version and description
- Never modify migrations after they're applied
- Test locally before committing

