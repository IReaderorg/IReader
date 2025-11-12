# Supabase Web3 Backend Setup Instructions

This document provides complete instructions for setting up the Supabase backend infrastructure for IReader with Web3 wallet authentication.

## Overview

This setup includes:
1. Adding Supabase Kotlin SDK dependencies to the project
2. Creating and configuring a Supabase project
3. Deploying database schema with Row Level Security (RLS)
4. Deploying Edge Function for wallet signature verification
5. Configuring environment variables for Android and Desktop

## Part 1: Project Dependencies (✅ COMPLETED)

The following dependencies have been added to the project:

### gradle/libs.versions.toml
- Added `supabase = "2.0.0"` version
- Added Supabase libraries:
  - `supabase-postgrest` - Database operations
  - `supabase-auth` - Authentication
  - `supabase-realtime` - Real-time subscriptions
  - `supabase-functions` - Edge Functions
- Created `supabase` bundle for easy dependency management

### data/build.gradle.kts
- Added `implementation(libs.bundles.supabase)` to commonMain dependencies

## Part 2: Supabase Project Setup

### Step 1: Create Supabase Project

1. Visit https://app.supabase.com and sign in (or create an account)
2. Click **"New Project"**
3. Fill in the project details:
   - **Organization**: Select or create an organization
   - **Name**: `ireader-backend` (or your preferred name)
   - **Database Password**: Generate a strong password (save it securely!)
   - **Region**: Choose the region closest to your target users
   - **Pricing Plan**: Free tier is sufficient for development
4. Click **"Create new project"**
5. Wait 1-2 minutes for provisioning to complete

### Step 2: Get Project Credentials

Once your project is ready:

1. Navigate to **Settings** → **API** in the left sidebar
2. Copy and save the following values:
   - **Project URL**: `https://xxxxxxxxxxxxx.supabase.co`
   - **anon public key**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   - **service_role key**: (keep this secret - only for server-side use)

## Part 3: Database Schema Deployment

### Option A: Using Supabase Dashboard (Recommended)

1. In your Supabase project, go to **SQL Editor** (left sidebar)
2. Click **"New Query"**
3. Open the file: `.kiro/specs/supabase-web3-backend/supabase/schema.sql`
4. Copy the entire contents
5. Paste into the SQL Editor
6. Click **"Run"** (or press Ctrl+Enter)
7. Verify success - you should see "Success. No rows returned"

### Option B: Using Supabase CLI

```bash
# Install Supabase CLI (if not already installed)
npm install -g supabase

# Login to Supabase
supabase login

# Link to your project (replace with your project ref)
supabase link --project-ref your-project-ref

# Apply the schema
supabase db push
```

### Verify Database Setup

1. Go to **Table Editor** in the Supabase dashboard
2. Confirm you see these tables:
   - **users** (columns: wallet_address, username, created_at, is_supporter)
   - **reading_progress** (columns: id, user_wallet_address, book_id, last_chapter_slug, last_scroll_position, updated_at)
3. Go to **Authentication** → **Policies**
4. Verify RLS policies are enabled for both tables

### Important Note on RLS Policies

The RLS policies use `auth.jwt() ->> 'wallet_address'` to extract the wallet address from JWT claims. This means:

- Your authentication implementation must include the wallet address in JWT tokens
- For development/testing, you may need to temporarily disable RLS or use the service_role key
- The actual JWT integration will be implemented in later tasks (Task 2-4)

**For now, you can test database operations using the service_role key (server-side only) or by temporarily disabling RLS for development.**

## Part 4: Edge Function Deployment

The Edge Function verifies wallet signatures to authenticate users without requiring passwords.

### Using Supabase CLI (Recommended)

```bash
# Navigate to the functions directory
cd .kiro/specs/supabase-web3-backend/supabase

# Deploy the function
supabase functions deploy verify-wallet-signature

# Test the function (optional)
supabase functions invoke verify-wallet-signature \
  --body '{"walletAddress":"0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb","signature":"0x...","message":"Sign this message to authenticate with IReader: 1234567890"}'
```

### Using Supabase Dashboard (Alternative)

1. Go to **Edge Functions** in the Supabase dashboard
2. Click **"Create a new function"**
3. Name: `verify-wallet-signature`
4. Open `.kiro/specs/supabase-web3-backend/supabase/functions/verify-wallet-signature/index.ts`
5. Copy the entire file contents
6. Paste into the function editor
7. Click **"Deploy"**

### Verify Edge Function

Test the function using curl:

```bash
curl -X POST 'https://your-project.supabase.co/functions/v1/verify-wallet-signature' \
  -H 'Authorization: Bearer YOUR_ANON_KEY' \
  -H 'Content-Type: application/json' \
  -d '{
    "walletAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
    "signature": "0x...",
    "message": "Sign this message to authenticate with IReader: 1234567890"
  }'
```

Expected response (with invalid signature):
```json
{
  "error": "Signature verification failed",
  "details": "The signature does not match the provided wallet address"
}
```

## Part 5: Environment Configuration

### For Android

1. Copy the template file:
   ```bash
   cp .kiro/specs/supabase-web3-backend/config/local.properties.template local.properties
   ```

2. Edit `local.properties` in the project root and add your credentials:
   ```properties
   supabase.url=https://your-project.supabase.co
   supabase.anon.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

3. The Android build is already configured to read these values and inject them as BuildConfig fields

4. Verify the configuration is working:
   - Open `android/build.gradle.kts`
   - Look for the BuildConfig fields: `SUPABASE_URL` and `SUPABASE_ANON_KEY`

### For Desktop

1. Copy the template file:
   ```bash
   cp .kiro/specs/supabase-web3-backend/config/config.properties.template config.properties
   ```

2. Edit `config.properties` in the project root and add your credentials:
   ```properties
   supabase.url=https://your-project.supabase.co
   supabase.anon.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   supabase.realtime.enabled=true
   supabase.sync.interval=30000
   ```

3. The Desktop app will load this configuration at runtime

### Security Notes

- ✅ Both `local.properties` and `config.properties` are in `.gitignore`
- ✅ Never commit these files to version control
- ✅ The `anon` key is safe to use in client apps (protected by RLS)
- ❌ Never use the `service_role` key in client applications

## Part 6: Enable Realtime (Optional but Recommended)

Realtime enables instant sync of reading progress across devices.

1. Go to **Database** → **Replication** in Supabase dashboard
2. Find the `reading_progress` table
3. Toggle **"Enable Realtime"** to ON
4. This allows the app to receive live updates when progress changes on other devices

## Part 7: Verification Checklist

Before proceeding to implementation, verify:

- [ ] Supabase project is created and accessible
- [ ] Database schema is deployed (users and reading_progress tables exist)
- [ ] RLS policies are enabled on both tables
- [ ] Edge Function is deployed and accessible
- [ ] `local.properties` exists with correct Supabase credentials (Android)
- [ ] `config.properties` exists with correct Supabase credentials (Desktop)
- [ ] Both config files are in `.gitignore`
- [ ] Realtime is enabled for reading_progress table
- [ ] You can access the Supabase dashboard

## Part 8: Testing the Infrastructure

### Test Database Access

Run this in the SQL Editor:

```sql
-- Insert a test user
INSERT INTO users (wallet_address, username) 
VALUES ('0xTEST123', 'testuser');

-- Insert test reading progress
INSERT INTO reading_progress (user_wallet_address, book_id, last_chapter_slug, last_scroll_position)
VALUES ('0xTEST123', 'test-book', 'chapter-1', 0.5);

-- Query the data
SELECT * FROM users WHERE wallet_address = '0xTEST123';
SELECT * FROM reading_progress WHERE user_wallet_address = '0xTEST123';

-- Clean up
DELETE FROM reading_progress WHERE user_wallet_address = '0xTEST123';
DELETE FROM users WHERE wallet_address = '0xTEST123';
```

### Test Edge Function

Use the Supabase dashboard or curl to test the Edge Function with sample data.

## Troubleshooting

### "Cannot find BuildConfig" error
- Make sure you've synced Gradle after adding the configuration
- Verify `local.properties` exists in the project root
- Check that the file contains valid `supabase.url` and `supabase.anon.key` entries

### "relation does not exist" error
- The schema wasn't applied correctly
- Re-run the schema.sql file in the SQL Editor
- Check for any error messages during schema execution

### Edge Function returns 404
- Function wasn't deployed successfully
- Verify the function name is exactly `verify-wallet-signature`
- Check the function logs in the Supabase dashboard

### RLS policy errors
- Make sure RLS is enabled on the tables
- Verify the policies were created correctly
- Test with authenticated requests (include auth token)

## Next Steps

Now that the infrastructure is set up, you can proceed to:

1. **Task 2**: Implement the domain layer (interfaces, models, use cases)
2. **Task 3**: Implement the data layer (Supabase repository)
3. **Task 4**: Implement platform-specific wallet managers
4. **Task 5**: Wire up dependency injection

## Additional Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Kotlin SDK](https://github.com/supabase-community/supabase-kt)
- [Row Level Security Guide](https://supabase.com/docs/guides/auth/row-level-security)
- [Edge Functions Guide](https://supabase.com/docs/guides/functions)
- [Realtime Guide](https://supabase.com/docs/guides/realtime)

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review the Supabase dashboard logs
3. Consult the Supabase documentation
4. Ask in the Supabase Discord community
