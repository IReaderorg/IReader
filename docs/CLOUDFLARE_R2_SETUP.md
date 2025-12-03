# Cloudflare R2 Setup Guide for Character Art Storage

This guide explains how to set up Cloudflare R2 for storing AI-generated character art images in IReader.

## Why Cloudflare R2?

| Feature | Cloudflare R2 | AWS S3 | Supabase Storage |
|---------|---------------|--------|------------------|
| Free Storage | 10 GB | 5 GB (12 months) | 1 GB |
| Free Reads/month | 10 million | Limited | 2 GB bandwidth |
| Free Writes/month | 1 million | Limited | Included |
| Egress Fees | **$0 (Free!)** | $0.09/GB | $0.09/GB after limit |
| S3 Compatible | ✅ Yes | ✅ Yes | ❌ No |

**Key advantage:** No egress fees means users can view images unlimited times without cost.

---

## Step 1: Create Cloudflare Account

1. Go to [cloudflare.com](https://cloudflare.com)
2. Click **Sign Up** (free account)
3. Verify your email

---

## Step 2: Create R2 Bucket

1. Log into Cloudflare Dashboard
2. In the left sidebar, click **R2 Object Storage**
3. Click **Create bucket**
4. Configure:
   - **Bucket name:** `ireader-character-art` (or your preferred name)
   - **Location:** Choose closest to your users (Auto is fine)
5. Click **Create bucket**

---

## Step 3: Enable Public Access (for image viewing)

1. Go to your bucket → **Settings**
2. Under **Public access**, click **Allow Access**
3. You'll get a public URL like: `https://pub-xxxx.r2.dev`
4. Save this URL - it's your `R2_PUBLIC_URL`

Alternatively, connect a custom domain:
1. Go to **Settings** → **Custom Domains**
2. Add your domain (e.g., `art.yourapp.com`)
3. Cloudflare handles SSL automatically

---

## Step 4: Create API Token

There are two types of API tokens. Choose based on your use case:

### Option A: Account API Token (Recommended for Production) ⭐

Best for: Production apps, CI/CD, long-term use

1. Go to **R2 Object Storage** → **Manage R2 API Tokens**
2. Under **Account API Tokens**, click **Create API token**
3. Configure:
   - **Token name:** `ireader-production`
   - **Permissions:** `Object Read & Write`
   - **Specify bucket(s):** Select `ireader-character-art`
   - **TTL (optional):** Leave empty for no expiration, or set expiry date
4. Click **Create API Token**

**Why Account API Token?**
- ✅ Stays active even if you leave the organization
- ✅ Ideal for production systems
- ✅ Can be scoped to specific buckets
- ✅ More stable for long-running applications

### Option B: User API Token (For Development/Testing)

Best for: Personal development, testing, temporary access

1. Go to **R2 Object Storage** → **Manage R2 API Tokens**
2. Under **User API Tokens**, click **Create API token**
3. Configure same as above

**Why User API Token?**
- ⚠️ Becomes inactive if you leave the organization
- ✅ Good for personal/development work
- ✅ Easier to revoke when done testing

### After Creating Token

5. **IMPORTANT:** Copy and save these values immediately (shown only once!):
   - `Access Key ID` → This is your `R2_ACCESS_KEY_ID`
   - `Secret Access Key` → This is your `R2_SECRET_ACCESS_KEY`

> ⚠️ **Warning:** The Secret Access Key is only shown once. If you lose it, you'll need to create a new token.

---

## Step 5: Get Account ID and Endpoint

1. Go to **R2 Object Storage** overview page
2. On the right side, find **Account ID**
3. Copy it - this is your `R2_ACCOUNT_ID`
4. Your endpoint URL is: `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`

---

## Step 6: Configure IReader

Add these to your `local.properties` or environment variables:

```properties
# Cloudflare R2 Configuration
R2_ACCOUNT_ID=your_account_id_here
R2_ACCESS_KEY_ID=your_access_key_id_here
R2_SECRET_ACCESS_KEY=your_secret_access_key_here
R2_BUCKET_NAME=ireader-character-art
R2_PUBLIC_URL=https://pub-xxxx.r2.dev
```

Or in `config.properties`:

```properties
cloudflare.r2.accountId=your_account_id_here
cloudflare.r2.accessKeyId=your_access_key_id_here
cloudflare.r2.secretAccessKey=your_secret_access_key_here
cloudflare.r2.bucketName=ireader-character-art
cloudflare.r2.publicUrl=https://pub-xxxx.r2.dev
```

---

## Step 7: Set Up CORS (for web/browser access)

1. Go to your bucket → **Settings**
2. Under **CORS Policy**, add:

```json
[
  {
    "AllowedOrigins": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "HEAD"],
    "AllowedHeaders": ["*"],
    "MaxAgeSeconds": 3600
  }
]
```

For production, replace `"*"` with your actual domains.

---

## Folder Structure in R2

Organize uploads like this:

```
ireader-character-art/
├── character-art/
│   ├── pending/
│   │   └── {user_id}/{timestamp}_{filename}.jpg
│   ├── approved/
│   │   └── {art_id}.jpg
│   └── thumbnails/
│       └── {art_id}_thumb.jpg
```

---

## Usage Estimates

With the free tier (10GB storage):

| Image Size | Estimated Count |
|------------|-----------------|
| 500 KB (compressed) | ~20,000 images |
| 1 MB (high quality) | ~10,000 images |
| 2 MB (original) | ~5,000 images |

**Recommendation:** Compress images to ~500KB-1MB before upload.

---

## Cost After Free Tier

If you exceed free limits:

| Resource | Price |
|----------|-------|
| Storage | $0.015/GB/month |
| Class A ops (writes) | $4.50/million |
| Class B ops (reads) | $0.36/million |
| Egress | **$0 (always free!)** |

Example: 50GB storage + 5M reads + 500K writes = ~$2.55/month

---

## Security Best Practices

1. **Never commit API keys** to git
2. **Use environment variables** for secrets
3. **Restrict bucket permissions** to specific operations
4. **Enable access logs** for monitoring
5. **Set up lifecycle rules** to auto-delete old pending uploads

### Lifecycle Rule Example

Auto-delete pending uploads after 7 days:

1. Go to bucket → **Settings** → **Object lifecycle rules**
2. Add rule:
   - **Prefix:** `character-art/pending/`
   - **Action:** Delete after 7 days

---

## Testing Your Setup

Use this curl command to test upload:

```bash
# Upload test
curl -X PUT "https://<ACCOUNT_ID>.r2.cloudflarestorage.com/ireader-character-art/test.txt" \
  -H "Authorization: AWS4-HMAC-SHA256 ..." \
  -d "Hello R2!"

# Or use AWS CLI (R2 is S3-compatible)
aws s3 cp test.jpg s3://ireader-character-art/test.jpg \
  --endpoint-url https://<ACCOUNT_ID>.r2.cloudflarestorage.com
```

---

## Troubleshooting

### "Access Denied" Error
- Check API token permissions
- Verify bucket name is correct
- Ensure token hasn't expired

### "Bucket Not Found"
- Verify bucket name matches exactly (case-sensitive)
- Check account ID is correct

### Images Not Loading
- Verify public access is enabled
- Check CORS settings
- Ensure public URL is correct

### Slow Uploads
- Enable Cloudflare CDN for your domain
- Compress images before upload
- Use multipart upload for large files

---

## Next Steps

1. ✅ Set up R2 bucket
2. ✅ Configure API credentials
3. 🔲 Implement `CloudflareR2DataSource` in the app
4. 🔲 Add image compression before upload
5. 🔲 Set up thumbnail generation

See `data/src/commonMain/kotlin/ireader/data/characterart/` for implementation.

---

## Resources

- [Cloudflare R2 Documentation](https://developers.cloudflare.com/r2/)
- [R2 Pricing](https://developers.cloudflare.com/r2/pricing/)
- [S3 API Compatibility](https://developers.cloudflare.com/r2/api/s3/)
- [Workers for image processing](https://developers.cloudflare.com/images/)
