# IReader Supabase Setup

This directory contains the database schema and configuration for IReader's sync feature.

## 🔒 Security Notice

**This is an open-source project.** API keys and credentials are NOT included in the repository. Each developer must configure their own Supabase instance.

## Quick Setup

1. **Create Supabase Project**: https://supabase.com
2. **Run Schema**: Copy `schema.sql` into Supabase SQL Editor and execute
3. **Get Credentials**: Go to Settings → API and copy your **anon** key (NOT service_role!)
4. **Configure Locally**:
   ```bash
   # Copy example files
   cp config.properties.example config.properties
   cp local.properties.example local.properties
   
   # Edit both files and add your credentials
   ```
5. **Build**: `./gradlew clean build`

## Files

- `schema.sql` - Complete database schema with RLS policies
- `migration_add_book_url.sql` - Migration for existing databases

## Documentation

- `../SECURE_CONFIGURATION_GUIDE.md` - Detailed security setup
- `../SUPABASE_API_KEY_FIX.md` - API key security explanation
- `../SYNC_SETUP_FINAL.md` - Complete sync setup guide

## Important

- ✅ Use **anon** key (safe for client apps)
- ❌ Never use **service_role** key in client apps
- ✅ Config files are gitignored
- ❌ Never commit credentials to git
