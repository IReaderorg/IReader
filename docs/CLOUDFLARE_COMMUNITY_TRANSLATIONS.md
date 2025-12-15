# Cloudflare D1 + R2 Community Translations

This document explains how to set up Cloudflare D1 and R2 for community translation storage.

## Overview

The community translation feature allows users to share AI-generated translations with others. When you translate a chapter using an AI engine (OpenAI, Gemini, DeepSeek, etc.), the translation can be automatically saved to a shared database for others to use.

### Architecture

- **Cloudflare D1**: SQLite database for translation metadata and indexes
- **Cloudflare R2**: Object storage for compressed translation content
- **Text Compression**: Custom compression reduces storage by ~40%
- **Deduplication**: Content hashing prevents duplicate translations

### Free Tier Limits

| Service | Free Tier |
|---------|-----------|
| D1 | 5GB storage, 25B row reads/month |
| R2 | 10GB storage, 10M Class A ops, 1M Class B ops |

With compression and deduplication, this can store approximately:
- **50,000+ chapters** of translated content
- **1,000+ complete novels**

## Setup Instructions

### 1. Create Cloudflare Account

1. Go to [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Sign up for a free account
3. Note your **Account ID** from the dashboard URL or sidebar

### 2. Install Wrangler CLI

```bash
npm install -g wrangler
wrangler login
```

### 3. Create D1 Database

```bash
# Create the database
wrangler d1 create community-translations

# Note the database ID from the output
# Example: Created database 'community-translations' with id: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

### 4. Initialize Database Schema

```bash
# Run the schema file
wrangler d1 execute community-translations --file=supabase/split/cloudflare_d1_schema.sql
```

### 5. Create R2 Bucket

```bash
# Create the bucket
wrangler r2 bucket create community-translations

# Optional: Enable public access for faster reads
# Go to R2 dashboard > community-translations > Settings > Public Access
```

### 6. Create API Token

1. Go to [Cloudflare API Tokens](https://dash.cloudflare.com/profile/api-tokens)
2. Click "Create Token"
3. Use "Custom token" template
4. Add permissions:
   - **D1**: Edit
   - **R2**: Edit
5. Save the token securely

### 7. Configure in IReader

#### Option A: User Configuration (In-App)

1. Open IReader Settings
2. Go to Community Source
3. Expand "Cloudflare D1 + R2" section
4. Enter:
   - Account ID
   - API Token
   - D1 Database ID
   - R2 Bucket Name
5. Click "Save"

#### Option B: Developer Default Configuration (Build-Time)

Developers can pre-configure Cloudflare credentials so the app ships with community translations enabled by default. Users can still override these in settings.

Add to `local.properties`:

```properties
# Cloudflare D1 + R2 (Community Translations)
community.cloudflare.accountId=your-account-id
community.cloudflare.apiToken=your-api-token
community.d1.databaseId=your-d1-database-id
community.r2.bucketName=ireader-community-translations
community.r2.publicUrl=https://your-bucket.r2.dev
```

Or set environment variables:

```bash
export COMMUNITY_CLOUDFLARE_ACCOUNT_ID=your-account-id
export COMMUNITY_CLOUDFLARE_API_TOKEN=your-api-token
export COMMUNITY_D1_DATABASE_ID=your-d1-database-id
export COMMUNITY_R2_BUCKET_NAME=ireader-community-translations
export COMMUNITY_R2_PUBLIC_URL=https://your-bucket.r2.dev
```

The app will use these defaults when the user hasn't configured their own credentials.

## How It Works

### Auto-Share Flow

1. User translates a chapter using an AI engine
2. System checks if auto-share is enabled and engine is AI-powered
3. Original content is hashed for deduplication
4. If no duplicate exists:
   - Translation is compressed
   - Compressed content uploaded to R2
   - Metadata saved to D1
5. Translation saved locally as usual

### Lookup Flow

1. User requests translation for a chapter
2. System checks community database first (if enabled)
3. If found:
   - Metadata retrieved from D1
   - Compressed content downloaded from R2
   - Content decompressed and returned
   - Download count incremented
4. If not found:
   - Normal translation flow proceeds
   - Result may be auto-shared

### Compression Details

The text compressor uses:
1. **Dictionary replacement**: Common words/phrases replaced with short codes
2. **Run-length encoding**: Repeated characters compressed
3. **UTF-8 encoding**: Efficient byte representation

Typical compression ratios:
- English text: 50-60% of original size
- CJK text: 70-80% of original size

## API Reference

### D1 Endpoints

```
POST /client/v4/accounts/{account_id}/d1/database/{database_id}/query
```

### R2 Endpoints

```
PUT /client/v4/accounts/{account_id}/r2/buckets/{bucket_name}/objects/{key}
GET /client/v4/accounts/{account_id}/r2/buckets/{bucket_name}/objects/{key}
DELETE /client/v4/accounts/{account_id}/r2/buckets/{bucket_name}/objects/{key}
```

## Troubleshooting

### "D1 query failed"
- Check API token has D1 Edit permission
- Verify database ID is correct
- Ensure schema has been initialized

### "R2 upload failed"
- Check API token has R2 Edit permission
- Verify bucket name is correct
- Check bucket exists

### "Translation not found"
- Content hash may differ (whitespace, encoding)
- Different engine ID
- Different target language

## Privacy Considerations

- Translations are shared anonymously by default
- Contributor name is optional
- Original content is hashed, not stored
- Only translated content is stored
- Users can disable auto-share at any time

## Contributing

To contribute improvements to the community translation system:

1. Fork the IReader repository
2. Make changes to files in `domain/src/commonMain/kotlin/ireader/domain/community/cloudflare/`
3. Test with your own Cloudflare account
4. Submit a pull request

## Files

| File | Description |
|------|-------------|
| `CloudflareConfig.kt` | Configuration data classes |
| `CloudflareD1Client.kt` | D1 database client |
| `CloudflareR2Client.kt` | R2 storage client |
| `TextCompressor.kt` | Compression utilities |
| `CommunityTranslationRepository.kt` | Main repository |
| `AutoShareTranslationUseCase.kt` | Auto-share logic |
| `CommunityTranslationModule.kt` | Koin DI module |
