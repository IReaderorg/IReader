# Cloudflare D1 + R2 Setup for Community Translations

This guide explains how to set up Cloudflare D1 (database) and R2 (object storage) for the IReader community translation sharing feature.

## Overview

The community translation feature uses:
- **Cloudflare D1**: SQLite database for translation metadata (book info, chapter info, contributor, ratings)
- **Cloudflare R2**: Object storage for compressed translation content (cheap, fast, no egress fees)

## Prerequisites

1. A Cloudflare account (free tier works)
2. Access to Cloudflare Dashboard

## Step 1: Create API Token

1. Go to [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Click on your profile icon → **My Profile** → **API Tokens**
3. Click **Create Token**
4. Use **Create Custom Token**:
   - **Token name**: `IReader Community Translations`
   - **Permissions**:
     - `Account` → `D1` → `Edit`
     - `Account` → `Workers R2 Storage` → `Edit`
   - **Account Resources**: Include your account
5. Click **Continue to summary** → **Create Token**
6. **Copy the token** (you won't see it again!)

## Step 2: Get Account ID

1. Go to [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Click on any domain or **Workers & Pages**
3. Look at the URL: `https://dash.cloudflare.com/ACCOUNT_ID/...`
4. Or find it in the right sidebar under **Account ID**

## Step 3: Create D1 Database

### Option A: Via Dashboard
1. Go to **Workers & Pages** → **D1**
2. Click **Create database**
3. Name it: `ireader-community-translations`
4. Click **Create**
5. Copy the **Database ID** from the database details page

### Option B: Via API (PowerShell)
```powershell
$accountId = "YOUR_ACCOUNT_ID"
$apiToken = "YOUR_API_TOKEN"

$response = Invoke-WebRequest -Uri "https://api.cloudflare.com/client/v4/accounts/$accountId/d1/database" `
    -Method Post `
    -Headers @{ "Authorization" = "Bearer $apiToken"; "Content-Type" = "application/json" } `
    -Body '{"name": "ireader-community-translations"}' `
    -UseBasicParsing

$response.Content
# Copy the "uuid" from the response - this is your Database ID
```

## Step 4: Create D1 Tables

Run this SQL to create the translations table:

```powershell
$accountId = "YOUR_ACCOUNT_ID"
$apiToken = "YOUR_API_TOKEN"
$databaseId = "YOUR_DATABASE_ID"

$url = "https://api.cloudflare.com/client/v4/accounts/$accountId/d1/database/$databaseId/query"

# Create table
$createTableSql = @"
CREATE TABLE IF NOT EXISTS translations (
    id TEXT PRIMARY KEY,
    content_hash TEXT NOT NULL,
    book_hash TEXT NOT NULL,
    book_title TEXT NOT NULL,
    book_author TEXT DEFAULT '',
    chapter_name TEXT NOT NULL,
    chapter_number REAL DEFAULT -1,
    source_language TEXT NOT NULL,
    target_language TEXT NOT NULL,
    engine_id TEXT NOT NULL,
    r2_object_key TEXT NOT NULL,
    original_size INTEGER NOT NULL,
    compressed_size INTEGER NOT NULL,
    compression_ratio REAL NOT NULL,
    contributor_id TEXT DEFAULT '',
    contributor_name TEXT DEFAULT '',
    rating REAL DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    download_count INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
)
"@

$body = @{ sql = $createTableSql } | ConvertTo-Json -Compress
Invoke-WebRequest -Uri $url -Method Post -Headers @{
    "Authorization" = "Bearer $apiToken"
    "Content-Type" = "application/json"
} -Body $body -UseBasicParsing

# Create indexes
$indexes = @(
    "CREATE INDEX IF NOT EXISTS idx_translations_content_hash ON translations(content_hash)",
    "CREATE INDEX IF NOT EXISTS idx_translations_book_hash ON translations(book_hash)",
    "CREATE INDEX IF NOT EXISTS idx_translations_languages ON translations(source_language, target_language)",
    "CREATE INDEX IF NOT EXISTS idx_translations_book_chapter ON translations(book_hash, chapter_number, target_language)",
    "CREATE INDEX IF NOT EXISTS idx_translations_rating ON translations(rating DESC)",
    "CREATE INDEX IF NOT EXISTS idx_translations_downloads ON translations(download_count DESC)"
)

foreach ($sql in $indexes) {
    $body = @{ sql = $sql } | ConvertTo-Json -Compress
    Invoke-WebRequest -Uri $url -Method Post -Headers @{
        "Authorization" = "Bearer $apiToken"
        "Content-Type" = "application/json"
    } -Body $body -UseBasicParsing
    Write-Host "Index created"
}
```

## Step 5: Create R2 Bucket

### Option A: Via Dashboard
1. Go to **R2** in the sidebar
2. Click **Create bucket**
3. Name it: `ireader-community-translations`
4. Click **Create bucket**

### Option B: Via API
```powershell
$accountId = "YOUR_ACCOUNT_ID"
$apiToken = "YOUR_API_TOKEN"

Invoke-WebRequest -Uri "https://api.cloudflare.com/client/v4/accounts/$accountId/r2/buckets" `
    -Method Post `
    -Headers @{ "Authorization" = "Bearer $apiToken"; "Content-Type" = "application/json" } `
    -Body '{"name": "ireader-community-translations"}' `
    -UseBasicParsing
```

## Step 6: (Optional) Enable Public Access for R2

If you want faster downloads without authentication:

1. Go to **R2** → your bucket → **Settings**
2. Under **Public access**, click **Allow Access**
3. Copy the **Public URL** (e.g., `https://pub-xxx.r2.dev`)

## Step 7: Configure IReader

### For Development (config.properties)

Add to your `config.properties`:

```properties
# Cloudflare D1 + R2 (Community Translations)
community.cloudflare.accountId=YOUR_ACCOUNT_ID
community.cloudflare.apiToken=YOUR_API_TOKEN
community.d1.databaseId=YOUR_DATABASE_ID
community.r2.bucketName=ireader-community-translations
community.r2.publicUrl=https://pub-xxx.r2.dev  # Optional, for faster downloads
```

### For Users (In-App Settings)

Users can configure their own Cloudflare in:
**Settings** → **Community Hub** → **Community Source**

Required fields:
- Account ID
- API Token
- D1 Database ID
- R2 Bucket Name

## Verification

Test your setup:

```powershell
# Test D1
$url = "https://api.cloudflare.com/client/v4/accounts/$accountId/d1/database/$databaseId/query"
$body = '{"sql": "SELECT COUNT(*) FROM translations"}'
Invoke-WebRequest -Uri $url -Method Post -Headers @{
    "Authorization" = "Bearer $apiToken"
    "Content-Type" = "application/json"
} -Body $body -UseBasicParsing

# Test R2
$url = "https://api.cloudflare.com/client/v4/accounts/$accountId/r2/buckets/ireader-community-translations/objects"
Invoke-WebRequest -Uri $url -Method Get -Headers @{
    "Authorization" = "Bearer $apiToken"
} -UseBasicParsing
```

## How It Works

1. **Translation Sharing**: When a user translates a chapter with an AI engine (OpenAI, Gemini, DeepSeek), the translation is automatically shared if:
   - Auto-share is enabled (default: true)
   - Contributor name is set
   - Cloudflare is configured

2. **Deduplication**: Translations are deduplicated by content hash to avoid storing duplicates.

3. **Compression**: Large translations are compressed before storage to save space.

4. **Community Lookup**: Before translating, the app checks if a community translation already exists.

## Costs

Cloudflare's free tier includes:
- **D1**: 5GB storage, 5M reads/day, 100K writes/day
- **R2**: 10GB storage, 10M reads/month, 1M writes/month, **no egress fees**

This is more than enough for personal use and small communities.

## Troubleshooting

### "The specified bucket does not exist"
- Create the R2 bucket (Step 5)

### "no such table: translations"
- Run the table creation SQL (Step 4)

### "Authentication error"
- Verify your API token has D1 and R2 permissions
- Check the token hasn't expired

### Translations not being shared
Check the logs for:
```
adb logcat -s "IReader" | grep -i "AutoShare\|CommunityRepo\|R2Client\|D1Client"
```

Common issues:
- Contributor name not set
- Auto-share disabled
- Non-AI translation engine used
