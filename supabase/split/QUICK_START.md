# Quick Start Guide - 7-Project Supabase Setup

Get your IReader app running with 3.5GB of free storage in 15 minutes!

## Prerequisites

- Supabase account (free tier)
- IReader app installed
- 15 minutes of your time

## Step 1: Create Projects (5 minutes)

Go to https://supabase.com/dashboard and create 7 projects:

1. **ireader-auth** - For user authentication
2. **ireader-reading** - For reading progress
3. **ireader-library** - For synced books
4. **ireader-book-reviews** - For book reviews
5. **ireader-chapter-reviews** - For chapter reviews
6. **ireader-badges** - For badges & NFT
7. **ireader-analytics** - For leaderboard

💡 **Tip**: Use the same password for all projects to make it easier!

## Step 2: Deploy Schemas (5 minutes)

For each project, go to **SQL Editor** and run the corresponding schema:

### Project 1 - Auth
```sql
-- Copy and paste contents of schema_1_auth.sql
-- Click "Run" button
```

### Project 2 - Reading
```sql
-- Copy and paste contents of schema_2_reading.sql
-- Click "Run" button
```

### Project 3 - Library
```sql
-- Copy and paste contents of schema_3_library.sql
-- Click "Run" button
```

### Project 4 - Book Reviews
```sql
-- Copy and paste contents of schema_4_book_reviews.sql
-- Click "Run" button
```

### Project 5 - Chapter Reviews
```sql
-- Copy and paste contents of schema_5_chapter_reviews.sql
-- Click "Run" button
```

### Project 6 - Badges
```sql
-- Copy and paste contents of schema_6_badges.sql
-- Click "Run" button
```

### Project 7 - Analytics
```sql
-- Copy and paste contents of schema_7_analytics.sql
-- Click "Run" button
```

## Step 3: Get Credentials (2 minutes)

For each project, go to **Settings → API**:

1. Copy the **Project URL** (looks like `https://xxx.supabase.co`)
2. Copy the **anon/public key** (long JWT token)

💡 **Tip**: Keep a text file with all credentials organized!

```
Project 1 - Auth:
URL: https://xxx1.supabase.co
Key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Project 2 - Reading:
URL: https://xxx2.supabase.co
Key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

... and so on
```

## Step 4: Configure in App (3 minutes)

1. Open **IReader app**
2. Go to **Settings → Sync → Supabase Configuration**
3. Enable **"Use Custom Supabase"** toggle
4. Enable **"7-Project Configuration"** toggle
5. Enter credentials for all 7 projects:

```
Project 1 - Auth:
URL: [paste URL]
API Key: [paste key]

Project 2 - Reading:
URL: [paste URL]
API Key: [paste key]

... continue for all 7 projects
```

6. Click **"Save 7-Project Configuration"**
7. You should see: ✅ "7-Project configuration saved successfully! Total storage: 3.5GB"

## Step 5: Test Connection (1 minute)

1. Go back to main Supabase config
2. Click **"Test Connection"**
3. You should see: ✅ "Connection successful! Supabase is configured correctly."

## Step 6: Start Syncing!

1. Sign up or sign in to your account
2. Add some books to your library
3. Click **"Sync Now"**
4. Your data is now synced across 7 projects!

## Verification Checklist

- [ ] All 7 projects created
- [ ] All 7 schemas deployed successfully
- [ ] All 7 credentials entered in app
- [ ] Configuration saved
- [ ] Test connection successful
- [ ] First sync completed

## Troubleshooting

### "Connection failed"
- Check that URL starts with `https://` and ends with `.supabase.co`
- Verify the API key is the **anon/public** key, not service role key
- Make sure you copied the full key (it's very long!)

### "Table does not exist"
- Go back to SQL Editor
- Run the schema file again
- Check for any error messages in SQL Editor

### "Authentication failed"
- Make sure you're using Project 1 (Auth) credentials for sign in
- Try signing up with a new account
- Check that the users table exists in Project 1

### "Sync failed"
- Verify all 7 projects are configured
- Check that each schema was deployed successfully
- Try syncing one book at a time first

## What's Next?

### Explore Features
- ✅ **Reading Progress** - Synced across devices
- ✅ **Favorite Books** - Access from anywhere
- ✅ **Reviews** - Share your thoughts
- ✅ **Badges** - Earn achievements
- ✅ **Leaderboard** - Compete with others

### Monitor Usage
- Check storage usage in each project's dashboard
- You have 500MB per project = 3.5GB total
- Free tier includes 2 concurrent connections per project

### Upgrade (Optional)
If you need more storage:
- **Pro Plan**: $25/month per project
- **8GB per project** = 56GB total for 7 projects
- More connections and better performance

## Cost Breakdown

### Free Tier (Current Setup)
- **Projects**: 7
- **Storage**: 3.5GB total
- **Cost**: $0/month ✅
- **Limitations**: 500MB per project, 2 concurrent connections

### If You Upgrade
- **Pro Plan**: $25/month × 7 = $175/month
- **Storage**: 56GB total (8GB × 7)
- **Benefits**: More connections, better performance

## Support

Need help?
1. Check `IMPLEMENTATION_COMPLETE.md` for detailed info
2. Review `MIGRATION_GUIDE.md` for data migration
3. See `MultiSupabaseClient.kt` for code examples
4. Open an issue on GitHub

## Success! 🎉

You now have:
- ✅ 3.5GB of free storage
- ✅ 7 independent Supabase projects
- ✅ Full sync functionality
- ✅ All features enabled

Enjoy your expanded storage capacity!

---

**Time to complete**: ~15 minutes
**Cost**: $0/month (free tier)
**Storage**: 3.5GB (7 × 500MB)
**Status**: Ready to use! 🚀
