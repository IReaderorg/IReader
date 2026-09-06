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

## Files & Directory Structure

- `schema.sql` - **Default database schema** (lightweight, alias to `schema_lightweight.sql`)
- `schema_lightweight.sql` - **Option A (Lightweight - Recommended for Supabase Free Tier)**:
  - Synchronizes all book metadata, chapter reading state, bookmarks, scroll positions, and last read page.
  - Does **NOT** store full novel text/body content in the cloud, staying comfortably within the 500MB free quota.
- `schema_with_chapter_content.sql` - **Option B (Full Content - For Self-Hosted Supabase / PostgreSQL)**:
  - Includes a `content TEXT DEFAULT ''` column in `public.synced_chapters` and exposes it in `public.synced_chapters_view`.
  - Intended for self-hosters (TrueNAS, VPS, Docker, unlimited cloud) who enable "Sync Chapter Content (Full Text)" in the app.
- `migrations/` - **Versioned incremental migrations**
  - `001_profile_gamification.sql` to `004_security_advisor_fixes.sql`
  - `005_unified_sync_and_library_enhancements.sql` - Sync manifest document store & rich library metadata
  - `006_chapter_sync_and_manifest_updates.sql` - Synced chapters metadata table, GIN index, and dynamic view
  - `007_optional_chapter_content.sql` - Adds optional `content TEXT` column and updates `synced_chapters_view` for chapter body text backup
- `split/` - **Modular schemas for distributed multi-project setups**
  - Allows distributing tables across multiple free-tier Supabase projects (up to 5.5GB storage)

## Key Features

- **Two Storage Profiles (Option A vs Option B)**:
  - **Option A (Default / Lightweight)**: Chapter metadata (titles, numbers, reading state, bookmarks, last read page) synced seamlessly without chapter body text. Fast, private, and minimal storage consumption.
  - **Option B (Full Chapter Content)**: Offline novel text saved to `synced_chapters` for complete self-hosted backup.
- **In-App User Control**:
  - Setting: **"Sync Chapter Content (Full Text)"** under **Settings → Unified Sync** and **Settings → Supabase Configuration**.
  - Default: **Disabled (false)** so free-tier users never unintentionally exceed database storage quotas.
- **Unified Sync Manifest**: Stored as high-fidelity JSONB with GIN indexing (`idx_sync_manifest_gin`), queryable dynamically via `synced_chapters_view`.
- **Single-Project & Multi-Project Support**: Works with a single free Supabase project or distributed across multiple projects.

## Documentation

- `../docs/guides/supabase_setup_guide.md` - **Complete Step-by-Step Setup Guide**
- `migrations/README.md` - Detailed migration guide and reference
- `split/README.md` - Distributed multi-project architecture guide
- `../SECURE_CONFIGURATION_GUIDE.md` - Detailed security setup
- `../SUPABASE_API_KEY_FIX.md` - API key security explanation
- `../SYNC_SETUP_FINAL.md` - Complete sync setup guide

## Important

- ✅ Use **anon** key (safe for client apps)
- ❌ Never use **service_role** key in client apps
- ✅ Config files are gitignored
- ❌ Never commit credentials to git
