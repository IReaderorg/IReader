# Discord Character Art Integration

This guide explains how to set up Discord webhooks for posting AI-generated character art directly to your Discord server, replacing the complex character art gallery system.

## Why Discord Webhooks?

| Feature | Discord Webhooks | R2 + Supabase Gallery |
|---------|------------------|----------------------|
| Setup Complexity | ⭐ Simple (5 minutes) | ⭐⭐⭐⭐⭐ Complex (hours) |
| Cost | 💰 Free | 💰 Free tier limits |
| Maintenance | ✅ None | ❌ Bucket, DB, moderation |
| Community Engagement | ✅ Immediate reactions | ❌ Separate gallery |
| Image Hosting | ✅ Discord CDN | ❌ Need R2/S3 |
| Moderation | ✅ Discord tools | ❌ Custom admin panel |

## Setup Steps

### 1. Create Discord Channel

1. Go to your Discord server
2. Create a new channel: `#character-art`
3. Set channel description: "AI-generated character art from IReader community"

### 2. Create Webhook

1. Right-click the `#character-art` channel
2. Click **Edit Channel**
3. Go to **Integrations** → **Webhooks**
4. Click **New Webhook**
5. Configure:
   - **Name:** IReader Character Art
   - **Avatar:** Upload IReader logo (optional)
6. Click **Copy Webhook URL**

### 3. Configure IReader

Add to your `local.properties`:

```properties
DISCORD_CHARACTER_ART_WEBHOOK_URL=https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz
```

Or in `config.properties`:

```properties
discord.characterArt.webhookUrl=https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz
```

### 4. Test the Integration

Run the app and generate a character art image. It should appear in your Discord channel!

## How It Works

1. User generates character art in IReader
2. App uploads image + metadata to Discord webhook
3. Discord posts to `#character-art` channel
4. Community can:
   - React with emojis
   - Comment and discuss
   - Share directly (Discord CDN URLs work everywhere)
   - Download images

## Discord Message Format

```
**New Character Art Generated!**

📖 **Character:** Frodo Baggins
📚 **From:** The Lord of the Rings by J.R.R. Tolkien
🤖 **AI Model:** Gemini Flash 2.0
💭 **Prompt:** A brave hobbit with curly hair...

[Image attached]
```

## Benefits

### For Users
- ✅ Instant sharing with community
- ✅ Get feedback via reactions/comments
- ✅ No need to navigate separate gallery
- ✅ Discord CDN = fast, reliable image hosting

### For Developers
- ✅ No R2 bucket setup
- ✅ No Supabase metadata tables
- ✅ No approval/moderation system
- ✅ No image storage costs
- ✅ Discord handles all the hard stuff

### For Community
- ✅ Centralized discussion
- ✅ Easy to browse history
- ✅ Can pin favorites
- ✅ Search by character/book name

## Removing Old Gallery System

Once Discord integration is working, you can remove:

1. **R2 Storage:**
   - `data/src/commonMain/kotlin/ireader/data/characterart/CloudflareR2DataSource.kt`
   - `data/src/commonMain/kotlin/ireader/data/characterart/R2ImageStorage.kt`

2. **Supabase Metadata:**
   - `data/src/commonMain/kotlin/ireader/data/characterart/SupabaseCharacterArtMetadata.kt`
   - `supabase/split/schema_11_character_art.sql`

3. **Gallery UI:**
   - `presentation/src/commonMain/kotlin/ireader/presentation/ui/characterart/CharacterArtGalleryScreen.kt`
   - `presentation/src/commonMain/kotlin/ireader/presentation/ui/characterart/CharacterArtDetailScreen.kt`
   - `presentation/src/commonMain/kotlin/ireader/presentation/ui/characterart/CharacterArtViewModel.kt`

4. **Navigation:**
   - Remove `characterArtGallery` route from `NavigationRoutes.kt`
   - Remove gallery link from `CommunityHubScreen.kt`

## Advanced: Multiple Webhooks

You can create separate webhooks for different purposes:

```properties
# Main character art channel
DISCORD_CHARACTER_ART_WEBHOOK_URL=https://discord.com/api/webhooks/...

# Pending approval channel (for moderation)
DISCORD_CHARACTER_ART_PENDING_WEBHOOK_URL=https://discord.com/api/webhooks/...

# Featured art channel (curated)
DISCORD_CHARACTER_ART_FEATURED_WEBHOOK_URL=https://discord.com/api/webhooks/...
```

## Rate Limits

Discord webhooks have rate limits:
- **30 requests per minute** per webhook
- **5 requests per second** per webhook

For most use cases, this is plenty. If you hit limits, implement a simple queue.

## Security

⚠️ **Important:** Keep your webhook URL secret!

- Don't commit it to git
- Use environment variables
- If leaked, regenerate the webhook in Discord settings

## Troubleshooting

### Webhook Not Working

1. Check webhook URL is correct
2. Verify channel still exists
3. Check bot permissions
4. Test with curl:

```bash
curl -X POST "YOUR_WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d '{"content": "Test message"}'
```

### Images Not Uploading

1. Check image size (Discord limit: 8MB for free, 50MB for Nitro)
2. Verify image format (JPEG, PNG, GIF, WebP)
3. Check file is not corrupted

### Rate Limit Errors

If you see 429 errors:
1. Implement exponential backoff
2. Add queue system
3. Consider multiple webhooks

## Example: Simple Upload Flow

```kotlin
// In your character art generation screen
val discordService = DiscordWebhookService(httpClient, webhookUrl)

scope.launch {
    val result = discordService.postCharacterArt(
        imageBytes = generatedImage,
        characterName = "Frodo Baggins",
        bookTitle = "The Lord of the Rings",
        bookAuthor = "J.R.R. Tolkien",
        aiModel = "Gemini Flash 2.0",
        prompt = userPrompt
    )
    
    result.onSuccess {
        showSnackbar("Posted to Discord! Check #character-art")
    }.onFailure { error ->
        showSnackbar("Failed to post: ${error.message}")
    }
}
```

## Migration Plan

1. ✅ Set up Discord webhook
2. ✅ Test with a few images
3. ✅ Announce to community
4. ⏳ Run both systems in parallel for 1 week
5. ⏳ Remove old gallery system
6. ⏳ Clean up database/storage

## Resources

- [Discord Webhooks Guide](https://discord.com/developers/docs/resources/webhook)
- [Discord CDN](https://discord.com/developers/docs/reference#image-formatting)
- [Rate Limits](https://discord.com/developers/docs/topics/rate-limits)
