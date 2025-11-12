# Quick Reference Guide

## Files Created

### Configuration Templates
- `.kiro/specs/supabase-web3-backend/config/local.properties.template` - Android config template
- `.kiro/specs/supabase-web3-backend/config/config.properties.template` - Desktop config template

### Supabase Infrastructure
- `.kiro/specs/supabase-web3-backend/supabase/schema.sql` - Database schema with RLS policies
- `.kiro/specs/supabase-web3-backend/supabase/functions/verify-wallet-signature/index.ts` - Edge Function

### Documentation
- `.kiro/specs/supabase-web3-backend/SETUP_INSTRUCTIONS.md` - Complete setup guide
- `.kiro/specs/supabase-web3-backend/supabase/DEPLOYMENT_GUIDE.md` - Deployment guide

## Dependencies Added

### gradle/libs.versions.toml
```toml
[versions]
supabase = "2.0.0"

[libraries]
supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt", version.ref = "supabase" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt", version.ref = "supabase" }
supabase-realtime = { module = "io.github.jan-tennert.supabase:realtime-kt", version.ref = "supabase" }
supabase-functions = { module = "io.github.jan-tennert.supabase:functions-kt", version.ref = "supabase" }

[bundles]
supabase = ["supabase-postgrest", "supabase-auth", "supabase-realtime", "supabase-functions"]
```

### data/build.gradle.kts
```kotlin
commonMain {
    dependencies {
        // ... existing dependencies
        implementation(libs.bundles.supabase)
    }
}
```

### android/build.gradle.kts
```kotlin
defaultConfig {
    // ... existing config
    buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("supabase.url", "")}\"")
    buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("supabase.anon.key", "")}\"")
}
```

## Configuration Files to Create

### local.properties (Project Root)
```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key-here
```

### config.properties (Project Root)
```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key-here
supabase.realtime.enabled=true
supabase.sync.interval=30000
```

## Database Schema

### Tables Created
1. **users**
   - `wallet_address` (TEXT, PRIMARY KEY)
   - `username` (TEXT, nullable)
   - `created_at` (TIMESTAMPTZ)
   - `is_supporter` (BOOLEAN)

2. **reading_progress**
   - `id` (UUID, PRIMARY KEY)
   - `user_wallet_address` (TEXT, FOREIGN KEY → users)
   - `book_id` (TEXT)
   - `last_chapter_slug` (TEXT)
   - `last_scroll_position` (REAL)
   - `updated_at` (TIMESTAMPTZ)

### RLS Policies
- Users can read all user profiles
- Users can only update their own profile
- Users can only access their own reading progress

**Note**: RLS policies use `auth.jwt() ->> 'wallet_address'` to extract wallet address from JWT claims. Your authentication implementation must include the wallet address in the JWT token payload.

## Edge Function

**Name**: `verify-wallet-signature`

**Purpose**: Verifies wallet ownership by validating cryptographic signatures

**Endpoint**: `https://your-project.supabase.co/functions/v1/verify-wallet-signature`

**Request Body**:
```json
{
  "walletAddress": "0x...",
  "signature": "0x...",
  "message": "Sign this message to authenticate with IReader: {timestamp}"
}
```

**Success Response**:
```json
{
  "verified": true,
  "walletAddress": "0x...",
  "timestamp": 1234567890
}
```

## Quick Setup Commands

```bash
# 1. Copy configuration templates
cp .kiro/specs/supabase-web3-backend/config/local.properties.template local.properties
cp .kiro/specs/supabase-web3-backend/config/config.properties.template config.properties

# 2. Edit the files and add your Supabase credentials
# (Use your text editor to fill in the values)

# 3. Deploy Edge Function (requires Supabase CLI)
cd .kiro/specs/supabase-web3-backend/supabase
supabase functions deploy verify-wallet-signature

# 4. Sync Gradle
./gradlew build
```

## Accessing Configuration in Code

### Android
```kotlin
val supabaseUrl = BuildConfig.SUPABASE_URL
val supabaseKey = BuildConfig.SUPABASE_ANON_KEY
```

### Desktop
```kotlin
val properties = Properties()
properties.load(FileInputStream("config.properties"))
val supabaseUrl = properties.getProperty("supabase.url")
val supabaseKey = properties.getProperty("supabase.anon.key")
```

## Important Notes

- ✅ Configuration files are in `.gitignore` - never commit them
- ✅ Use `anon` key in client apps (safe, protected by RLS)
- ❌ Never use `service_role` key in client apps (bypasses RLS)
- 🔄 Realtime must be enabled for reading_progress table
- 🔒 RLS policies must be enabled for security

## Next Steps

1. Create Supabase project at https://app.supabase.com
2. Deploy schema using SQL Editor
3. Deploy Edge Function
4. Create and configure local.properties and config.properties
5. Proceed to Task 2: Implement domain layer

## Useful Links

- Supabase Dashboard: https://app.supabase.com
- Supabase Docs: https://supabase.com/docs
- Supabase Kotlin SDK: https://github.com/supabase-community/supabase-kt
