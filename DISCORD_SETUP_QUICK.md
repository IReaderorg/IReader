# Discord Character Art - Quick Setup (5 Minutes)

## What Changed?

Character art now posts directly to Discord instead of R2+Supabase. This is:
- ✅ Simpler (no bucket, no database)
- ✅ Free (Discord CDN)
- ✅ Better engagement (community sees art immediately)
- ✅ No 404 errors (Discord URLs work everywhere)

## Setup Steps

### 1. Create Discord Webhook (2 minutes)

1. Go to your Discord server
2. Create or select a channel (e.g., `#character-art`)
3. Right-click channel → **Edit Channel**
4. Go to **Integrations** → **Webhooks**
5. Click **New Webhook**
6. Name it "IReader Character Art"
7. Click **Copy Webhook URL**

### 2. Configure IReader (1 minute)

Add to your `local.properties`:

```properties
DISCORD_CHARACTER_ART_WEBHOOK_URL=https://discord.com/api/webhooks/YOUR_WEBHOOK_URL_HERE
```

### 3. Test It! (2 minutes)

1. Run the app
2. Generate a character art image
3. Click "Submit" or "Post"
4. Check your Discord channel - the art should appear!

## What Gets Posted?

```
**New Character Art Generated!**

📖 **Character:** Frodo Baggins
📚 **From:** The Lord of the Rings by J.R.R. Tolkien
🤖 **AI Model:** Gemini Flash 2.0
💭 **Prompt:** A brave hobbit with curly hair...

[Image attached]
```

## Benefits

| Before (R2+Supabase) | After (Discord) |
|---------------------|-----------------|
| Setup: Hours | Setup: 5 minutes |
| Cost: R2 + Supabase | Cost: Free |
| Sharing: 404 errors | Sharing: Works everywhere |
| Moderation: Custom system | Moderation: Discord tools |
| Engagement: Separate gallery | Engagement: Immediate reactions |

## Troubleshooting

**Webhook not working?**
- Check the URL is correct (starts with `https://discord.com/api/webhooks/`)
- Verify the channel still exists
- Make sure you copied the full URL

**Images not appearing?**
- Check image size (Discord limit: 8MB)
- Verify image format (JPEG, PNG, WebP, GIF)

**Want to keep R2+Supabase?**
- Just don't set the Discord webhook URL
- The app will automatically fall back to R2+Supabase

## Next Steps

After testing Discord integration:
1. Announce to your community
2. Consider removing the old gallery UI (optional)
3. Enjoy simpler character art sharing!

## Full Documentation

See `docs/DISCORD_CHARACTER_ART_SETUP.md` for detailed information.
