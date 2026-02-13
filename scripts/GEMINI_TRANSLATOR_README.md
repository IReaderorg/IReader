# Gemini Translator - Multi-Key Support

The Gemini translator script now supports multiple API keys with automatic rotation when rate-limited.

## Configuration

### Option 1: Comma-Separated Keys (Recommended)

Set a single environment variable with comma-separated API keys:

```bash
# Linux/Mac
export GEMINI_API_KEY='key1,key2,key3'

# Windows (CMD)
set GEMINI_API_KEY=key1,key2,key3

# Windows (PowerShell)
$env:GEMINI_API_KEY='key1,key2,key3'
```

### Option 2: Numbered Keys

Set multiple environment variables with numbered suffixes:

```bash
# Linux/Mac
export GEMINI_API_KEY_1='first_key'
export GEMINI_API_KEY_2='second_key'
export GEMINI_API_KEY_3='third_key'

# Windows (CMD)
set GEMINI_API_KEY_1=first_key
set GEMINI_API_KEY_2=second_key
set GEMINI_API_KEY_3=third_key

# Windows (PowerShell)
$env:GEMINI_API_KEY_1='first_key'
$env:GEMINI_API_KEY_2='second_key'
$env:GEMINI_API_KEY_3='third_key'
```

### Option 3: Mix Both Methods

You can combine both methods - the script will load all keys:

```bash
export GEMINI_API_KEY='key1,key2'
export GEMINI_API_KEY_1='key3'
export GEMINI_API_KEY_2='key4'
# Result: 4 keys total (key1, key2, key3, key4)
```

## How It Works

1. **Key Rotation**: The script automatically rotates through available API keys
2. **Rate Limit Detection**: When a key hits rate limit (HTTP 429), it's marked as exhausted
3. **Automatic Switching**: Script immediately switches to the next available key
4. **Smart Retry**: Each key gets 2 retry attempts before switching
5. **Cooldown Period**: If all keys are rate-limited, waits 60s and resets tracking

## Usage

```bash
# Run the translator
python scripts/gemini_translator.py
```

## Output Example

```
✓ Loaded 3 API key(s)
Loading source strings...
Loaded 450 strings

[1/10] Processing ar...
    [Batch 1/2] Translating 200 strings to Arabic...
    [Batch 1/2] Using API key #1
    [Batch 1/2] Response received in 3.2s (status: 200)
    [Batch 1/2] ✓ Successfully parsed 200 translations using key #1
    
    [Batch 2/2] Translating 50 strings to Arabic...
    [Batch 2/2] Using API key #1
    [Batch 2/2] Rate limited on key #1! Waiting 3s...
    [Key 1] Marked as rate-limited (1/3 keys exhausted)
    [Batch 2/2] Switching to next API key...
    [Batch 2/2] Using API key #2
    [Batch 2/2] ✓ Successfully parsed 50 translations using key #2

Summary: 10 updated, 0 skipped, 0 failed
API Keys: 3 total, 1 rate-limited
```

## Benefits

- **Higher Throughput**: Process more translations before hitting rate limits
- **Automatic Failover**: No manual intervention needed when keys are exhausted
- **Cost Distribution**: Spread API usage across multiple free-tier accounts
- **Resilience**: Continue working even if some keys fail

## Tips

1. **Free Tier**: Each Gemini API key has 15 requests/minute free tier
2. **Multiple Accounts**: Create multiple Google accounts for more free keys
3. **Key Management**: Keep keys in a secure location (e.g., password manager)
4. **Monitoring**: Watch the output to see which keys are being used

## Troubleshooting

### "No API keys configured"
- Check that environment variables are set correctly
- Verify no extra spaces in comma-separated keys
- Try using numbered keys instead

### "All API keys are rate-limited"
- Script will automatically wait 60s and retry
- Consider adding more API keys
- Reduce BATCH_SIZE if hitting limits too quickly

### Keys not rotating
- Check that keys are valid and different
- Verify all keys have API access enabled
- Ensure keys aren't expired
