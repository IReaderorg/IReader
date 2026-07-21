# 🚀 Quick Start: 7-Project Supabase Configuration

## For Single-Project Users (500MB)

**Want to use just one Supabase project? Here's how:**

1. Open the app and go to **Settings → Sync → Supabase Configuration**

2. Look for the **"Quick Fill"** section at the top

3. Enter your Supabase details:
   - **Supabase URL**: `https://your-project.supabase.co`
   - **API Key**: Your anon/public key

4. Click **"Fill All Projects"** button
   - This automatically fills all 7 project fields with the same URL

5. Click **"Test"** to verify connection (optional)

6. Click **"Save Configuration"**

7. Done! ✅ All sync features will use your single project

### What Happens?
- All 7 "projects" point to your one database
- Works exactly like a single-project setup
- Uses only 500MB of storage
- Perfect for personal use

---

## For Multi-Project Users (3.5GB)

**Want maximum storage with 7 separate projects? Here's how:**

1. Create 7 Supabase projects (free tier):
   - Project 1: Auth
   - Project 2: Reading
   - Project 3: Library
   - Project 4: Book Reviews
   - Project 5: Chapter Reviews
   - Project 6: Badges
   - Project 7: Analytics

2. Deploy the appropriate schema to each project
   - See `supabase/` folder for SQL migration files

3. Open the app and go to **Settings → Sync → Supabase Configuration**

4. Skip the "Quick Fill" section

5. Enter each project's URL and API key individually:
   ```
   Project 1 - Auth
   URL: https://auth-project.supabase.co
   Key: eyJhbGciOiJIUzI1NiIs...
   
   Project 2 - Reading
   URL: https://reading-project.supabase.co
   Key: eyJhbGciOiJIUzI1NiIs...
   
   ... and so on for all 7 projects
   ```

6. Click **"Test"** to verify connections (optional)

7. Click **"Save Configuration"**

8. Done! ✅ You now have 3.5GB total storage

### What Happens?
- Each feature uses its dedicated database
- Total storage: 3.5GB (7 × 500MB)
- Better performance and scalability
- Perfect for power users or communities

---

## 🔄 Switching Between Setups

### From Single to Multi-Project
1. Create additional Supabase projects
2. Deploy schemas to new projects
3. Update configuration with new URLs
4. Save configuration
5. Data remains in original project (migrate if needed)

### From Multi to Single-Project
1. Consolidate all schemas into one project
2. Use "Quick Fill" with the consolidated project URL
3. Save configuration
4. Migrate data from other projects (if needed)

---

## 🎯 Which Setup Should I Use?

### Use Single-Project (500MB) if:
- ✅ Personal use only
- ✅ Small library (< 1000 books)
- ✅ Don't need reviews/badges/analytics
- ✅ Want simplest setup
- ✅ Free tier is enough

### Use Multi-Project (3.5GB) if:
- ✅ Community/shared use
- ✅ Large library (> 1000 books)
- ✅ Want all features (reviews, badges, leaderboard)
- ✅ Need more storage
- ✅ Want better performance
- ✅ Plan to scale

---

## 🛠️ Troubleshooting

### "Connection failed" error
- ✅ Check URL format: `https://xxx.supabase.co` (no trailing slash)
- ✅ Verify API key is the **anon/public** key (not service key)
- ✅ Ensure project is not paused
- ✅ Check internet connection

### "Save failed" error
- ✅ Ensure at least Project 1 (Auth) is configured
- ✅ Check URL and key are not empty
- ✅ Restart app and try again

### Sync not working
- ✅ Test connection first
- ✅ Verify you're signed in
- ✅ Check auto-sync is enabled
- ✅ Ensure tables exist in database

---

## 📚 Database Schemas

### Single-Project Setup
Deploy all schemas to one project:
```sql
-- Run all migration files in your single project:
- migration_create_users.sql
- migration_create_reading_progress.sql
- migration_create_synced_books.sql
- migration_create_synced_chapters.sql
- migration_add_book_reviews.sql
- migration_add_chapter_reviews.sql
- migration_add_donation_badges.sql
- migration_add_leaderboard.sql
```

### Multi-Project Setup
Deploy specific schemas to each project:

**Project 1 - Auth**
```sql
- migration_create_users.sql
```

**Project 2 - Reading**
```sql
- migration_create_reading_progress.sql
```

**Project 3 - Library**
```sql
- migration_create_synced_books.sql
- migration_create_synced_chapters.sql
```

**Project 4 - Book Reviews**
```sql
- migration_add_book_reviews.sql
```

**Project 5 - Chapter Reviews**
```sql
- migration_add_chapter_reviews.sql
```

**Project 6 - Badges**
```sql
- migration_add_donation_badges.sql
```

**Project 7 - Analytics**
```sql
- migration_add_leaderboard.sql
```

---

## 💡 Pro Tips

1. **Start Simple**: Begin with single-project, upgrade to multi-project later if needed

2. **Test First**: Always use the "Test" button before saving

3. **Backup Keys**: Save your API keys securely (password manager)

4. **Monitor Usage**: Check Supabase dashboard for storage usage

5. **Free Tier Limits**: Each project has 500MB, 2GB bandwidth/month

6. **Same URL Works**: Using same URL for all 7 "projects" is perfectly fine!

---

## 🎉 That's It!

The configuration is now much simpler:
- **One screen** for all settings
- **One button** for single-project setup
- **Flexible** for both use cases
- **No confusing toggles** or modes

Enjoy your simplified Supabase sync! 🚀

---

**Need Help?**
- Check `SIMPLIFIED_7PROJECT_COMPLETE.md` for technical details
- See `supabase/` folder for database schemas
- Open an issue on GitHub for support
