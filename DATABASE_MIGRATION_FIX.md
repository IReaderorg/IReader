# Database Migration Fix

## Problem
The application was experiencing a database error:
```
Error creating database schema (may already exist): [SQLITE_ERROR] SQL error or missing database (table sync_queue already exists)
Database Version: Current=8, Target=8
No database upgrade needed, but ensuring views are initialized
```

## Root Cause
The database was at version 8, but the `sync_queue` table schema was inconsistent:
- The table definition in `sync_queue.sq` had one schema (with `book_id`, `data`, `timestamp`, `retry_count`)
- Migration 7 (`7.sqm`) had a different schema (with `operation`, `entity_type`, `entity_id`, `data`, `created_at`, `retry_count`, `last_error`, `status`)
- The Kotlin migration code was missing migrations from v6→v7 and v7→v8

## Solution

### 1. Added Missing Migration Functions
Created three new migration functions in `DatabaseMigrations.kt`:

- **migrateV6toV7**: Transitions from the v5 schema to the v7 schema (with extended fields)
- **migrateV7toV8**: Reverts to the simpler schema matching `sync_queue.sq`
- **migrateV8toV9**: Ensures the correct schema exists (fixes any inconsistencies)

### 2. Created Migration File
Added `migrations/8.sqm` to handle the schema fix at the SQL level.

### 3. Updated Database Version
- Changed `CURRENT_VERSION` from 8 to 9
- Added case for version 8 migration in `applyMigration()`

### 4. Created Standalone Migration Module
Created `DatabaseMigration_V8toV9.kt` as a separate file for better organization.

## How It Works

When the app starts:
1. It detects the database is at version 8, target is version 9
2. Runs `migrateV8toV9()` which:
   - Checks if `sync_queue` table exists
   - Verifies the schema by checking for the `book_id` column
   - If the schema is incorrect, drops and recreates the table
   - Creates proper indexes

## Files Modified
- `data/src/commonMain/kotlin/data/DatabaseMigrations.kt` - Added migration functions and updated version
- `data/src/commonMain/kotlin/data/DatabaseMigration_V8toV9.kt` - New standalone migration
- `data/src/commonMain/sqldelight/migrations/8.sqm` - New SQL migration file

## Testing
After this fix:
- Users with existing databases will automatically migrate from v8 to v9
- The `sync_queue` table will have the correct schema
- No more "table already exists" errors
- Database initialization will complete successfully
