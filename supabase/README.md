# IReader Supabase Setup

This directory contains the database schema and configuration for IReader's sync feature.

## 🔒 Security Notice

**This is an open-source project.** API keys and credentials are NOT included in the repository. Each developer must configure their own Supabase instance.

## Quick Setup

### Fresh Installation

1. **Create Supabase Project**: https://supabase.com
2. **Run Complete Schema**: 
   - Open Supabase SQL Editor
   - Copy entire `schema.sql` file
   - Execute (creates all tables, policies, functions)
3. **Get Credentials**: Go to Settings → API and copy your **anon** key (NOT service_role!)
4. **Configure Locally**:
   ```bash
   # Copy example files
   cp config.properties.example config.properties
   cp local.properties.example local.properties
   
   # Edit both files and add your credentials
   ```
5. **Build**: `./gradlew clean build`

### Migrating from Old Schema

If you already have an older database:
- **Option 1**: Run the complete `schema.sql` (recommended for fresh start)
- **Option 2**: Your existing data will work, but you may need to:
  - Remove unique constraints on reviews (see Migration Notes in schema.sql)
  - Ensure book_url column exists in synced_books

## Files

- `schema.sql` - **Complete database schema** (includes all migrations)
  - All tables, indexes, RLS policies, functions, triggers
  - Book and chapter review system
  - Badge system for users
  - All migrations already applied
- ~~`migration_*.sql`~~ - **Deprecated** (merged into schema.sql)

## Documentation

- `../SECURE_CONFIGURATION_GUIDE.md` - Detailed security setup
- `../SUPABASE_API_KEY_FIX.md` - API key security explanation
- `../SYNC_SETUP_FINAL.md` - Complete sync setup guide

## Important

- ✅ Use **anon** key (safe for client apps)
- ❌ Never use **service_role** key in client apps
- ✅ Config files are gitignored
- ❌ Never commit credentials to git
