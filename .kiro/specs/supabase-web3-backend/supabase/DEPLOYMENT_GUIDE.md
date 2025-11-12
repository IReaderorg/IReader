# Supabase Deployment Guide

This guide walks you through setting up the Supabase backend for IReader with Web3 wallet authentication.

## Prerequisites

- Supabase account (sign up at https://supabase.com)
- Supabase CLI installed (https://supabase.com/docs/guides/cli)
- Node.js and npm (for Edge Functions)

## Step 1: Create Supabase Project

1. Go to https://app.supabase.com
2. Click "New Project"
3. Fill in project details:
   - **Name**: ireader-backend (or your preferred name)
   - **Database Password**: Generate a strong password and save it securely
   - **Region**: Choose the region closest to your users
4. Click "Create new project"
5. Wait for the project to be provisioned (takes 1-2 minutes)

## Step 2: Get Project Credentials

After your project is created:

1. Go to **Settings** → **API**
2. Note down the following values:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon/public key**: `eyJhbGc...` (this is safe to use in client apps)
   - **service_role key**: `eyJhbGc...` (keep this secret, only for server-side)

## Step 3: Set Up Database Schema

### Option A: Using Supabase Dashboard (Recommended for beginners)

1. Go to **SQL Editor** in your Supabase dashboard
2. Click "New Query"
3. Copy the entire contents of `schema.sql` from this directory
4. Paste into the SQL editor
5. Click "Run" to execute the schema

### Option B: Using Supabase CLI

```bash
# Login to Supabase
supabase login

# Link to your project
supabase link --project-ref your-project-ref

# Run migrations
supabase db push
```

## Step 4: Verify Database Setup

1. Go to **Table Editor** in your Supabase dashboard
2. Verify that you see two tables:
   - `users`
   - `reading_progress`
3. Click on each table and verify the columns match the schema
4. Go to **Authentication** → **Policies** and verify RLS policies are enabled

## Step 5: Deploy Edge Function

### Using Supabase CLI

```bash
# Navigate to the supabase directory
cd .kiro/specs/supabase-web3-backend/supabase

# Deploy the Edge Function
supabase functions deploy verify-wallet-signature

# Test the function
supabase functions invoke verify-wallet-signature --body '{
  "walletAddress": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "signature": "0x...",
  "message": "Sign this message to authenticate with IReader: 1234567890"
}'
```

### Manual Deployment (Alternative)

1. Go to **Edge Functions** in your Supabase dashboard
2. Click "Create a new function"
3. Name it: `verify-wallet-signature`
4. Copy the contents of `functions/verify-wallet-signature/index.ts`
5. Paste into the editor
6. Click "Deploy"

## Step 6: Configure Environment Variables

### For Android

1. Open `android/build.gradle.kts`
2. Add the following to the `defaultConfig` block:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://your-project.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key-here\"")
```

3. Sync Gradle

### For Desktop

1. Create a file at the project root: `config.properties`
2. Add the following content:

```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key-here
```

3. Add `config.properties` to `.gitignore` to keep credentials secure

## Step 7: Configure Custom JWT Claims (Important!)

Since we're using Web3 wallet addresses instead of Supabase's default UUID-based auth, we need to configure custom JWT claims.

### Option A: Using Supabase Auth with Custom Claims

When generating JWT tokens for authenticated users, include the wallet address in the JWT payload:

```json
{
  "wallet_address": "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
  "aud": "authenticated",
  "role": "authenticated"
}
```

### Option B: Simplified RLS for Development

For development/testing, you can temporarily disable RLS or use simpler policies:

```sql
-- Temporarily disable RLS for testing (NOT for production!)
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE reading_progress DISABLE ROW LEVEL SECURITY;

-- OR use service_role key for testing (server-side only)
```

### Option C: Use Service Role Key (Server-Side Only)

For server-side operations, use the service_role key which bypasses RLS. Never expose this key in client applications.

**Note**: The RLS policies in the schema use `auth.jwt() ->> 'wallet_address'` to extract the wallet address from JWT claims. Your authentication flow must include the wallet address in the JWT token.

## Step 8: Test the Setup

### Test Database Connection

Run this SQL query in the SQL Editor:

```sql
-- Insert a test user
INSERT INTO users (wallet_address, username) 
VALUES ('0xTEST123', 'testuser');

-- Verify insertion
SELECT * FROM users WHERE wallet_address = '0xTEST123';

-- Clean up
DELETE FROM users WHERE wallet_address = '0xTEST123';
```

### Test Edge Function

Use curl or Postman to test the Edge Function:

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

## Step 9: Enable Realtime (Optional but Recommended)

1. Go to **Database** → **Replication**
2. Find the `reading_progress` table
3. Toggle "Enable Realtime" to ON
4. This allows real-time sync across devices

## Step 10: Security Checklist

Before going to production, verify:

- [ ] RLS policies are enabled on all tables
- [ ] Service role key is never exposed in client code
- [ ] Anon key is used in client applications
- [ ] Edge Function validates signatures correctly
- [ ] Database indexes are created for performance
- [ ] Realtime is enabled for reading_progress table
- [ ] Environment variables are not committed to git

## Troubleshooting

### "relation does not exist" error
- Make sure you ran the schema.sql file completely
- Check that you're connected to the correct project

### Edge Function deployment fails
- Ensure Supabase CLI is up to date: `supabase update`
- Check that you're logged in: `supabase login`
- Verify project link: `supabase link --project-ref your-ref`

### RLS policies blocking requests
- Verify that `auth.uid()` matches the wallet_address
- Check that the user is authenticated before making requests
- Test policies in the SQL Editor with different user contexts

## Next Steps

After completing the deployment:

1. Update your app configuration with the Supabase credentials
2. Test authentication flow with a real wallet
3. Test reading progress sync across devices
4. Monitor usage in the Supabase dashboard
5. Set up database backups (automatic in Supabase Pro)

## Support

- Supabase Documentation: https://supabase.com/docs
- Supabase Discord: https://discord.supabase.com
- IReader Issues: [Your GitHub repo]
