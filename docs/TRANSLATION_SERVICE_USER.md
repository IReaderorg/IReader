# Mass Translation Feature - User Guide

## Overview

IReader now supports mass translation of chapters, allowing you to translate multiple chapters at once. Translations are stored separately from original content, so you can always switch back to the original.

## How to Use

### Translating Multiple Chapters (Detail Screen)

1. **Open a book** from your library
2. **Select chapters** you want to translate:
   - Long press a chapter to enter selection mode
   - Tap additional chapters to select them
   - Use "Select All" to select all chapters
3. **Tap the translate icon** (🌐) in the bottom bar
   - **Single tap**: Quick translate with your saved settings
   - **Long press**: Open options dialog to change settings
4. **Configure translation** (if using options dialog):
   - Select translation engine
   - Choose source and target languages
   - Tap "Translate" to start

### Translating Single Chapter (Reader)

1. **Open a chapter** in the reader
2. **Access translation** from the reader menu
3. **Select options** and translate

### Translating for TTS

When using Text-to-Speech, you can translate the current chapter before listening.

## Translation Engines

### Offline Engines (No Internet Required)
- **Google ML Kit** - Fast, works offline, good for common languages
- **LibreTranslate** - Self-hosted option
- **Ollama** - Local AI models

### Online Engines (Requires Internet)
- **Google Translate** - Wide language support
- **OpenAI GPT** - High quality, requires API key
- **DeepSeek API** - Good for Asian languages, requires API key
- **ChatGPT WebView** - Uses browser session
- **DeepSeek WebView** - Uses browser session
- **Gemini API** - Google's AI, requires API key

## Rate Limiting

When translating many chapters with online engines, you may see a warning:

> "Translating X chapters may take approximately Y minutes and could exhaust API credits or result in IP blocking."

This is to protect you from:
- **API credit exhaustion** - Paid services charge per request
- **IP blocking** - Free services may block excessive requests

### Options:
- **Continue** - Proceed with translation (with automatic delays)
- **Cancel** - Go back and select fewer chapters
- **Bypass Warning** - Enable in Settings to skip this warning

## Settings

Go to **Settings → General** to configure:

### Bypass Translation Warning
When enabled, skips the rate limit warning for large translations. Use with caution.

### Rate Limit Delay
Time between translation requests (default: 3 seconds). Increase if you're getting blocked.

### Warning Threshold
Number of chapters that triggers the warning (default: 10).

## Progress Tracking

During translation, you'll see:
- **Progress bar** - Overall completion percentage
- **Chapter count** - "X / Y chapters"
- **Current chapter** - Name of chapter being translated
- **Pause/Resume** - Control translation flow
- **Cancel** - Stop all translations

## Viewing Translations

Translated chapters are automatically used when:
- The translation matches your current language settings
- The translation was made with your selected engine

To switch between original and translated:
- Change the target language in reader settings
- The app will show original if no translation exists

## Troubleshooting

### Translation Failed
- Check your internet connection
- Verify API keys are correct (for paid services)
- Try a different translation engine
- Reduce batch size

### Slow Translation
- Online engines have rate limits
- Increase delay in settings
- Use offline engines for faster results

### Missing Translations
- Ensure chapter content was downloaded first
- Check if translation completed successfully
- Verify language settings match

## Tips

1. **Start small** - Test with a few chapters first
2. **Use offline engines** for large batches
3. **Download chapters first** before translating
4. **Check API limits** for paid services
5. **Be patient** - Quality translation takes time

## Storage

Translations are stored in a separate database table:
- Original chapters remain unchanged
- Multiple translations per chapter (different languages/engines)
- Automatically deleted when chapter/book is removed
