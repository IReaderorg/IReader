# IReader AI Translation Guide

## Available AI Translation Engines

IReader supports several AI-powered translation engines to enhance your reading experience:

1. **Google Gemini API** - High-quality translations using Google's AI models
2. **ChatGPT WebView** - Uses ChatGPT directly through a web interface (no API key required)
3. **DeepSeek WebView** - Uses DeepSeek AI through a web interface (no API key required)
4. **DeepSeek API** - Direct API access to DeepSeek models
5. **Ollama** - Run local AI models on your device
6. **LibreTranslate** - Free and open-source translation service

## Getting API Keys

### Google Gemini API Key (Recommended)

1. **Create Google AI Studio Account**:
   - Visit [Google AI Studio](https://aistudio.google.com/)
   - Sign in with your Google account

2. **Generate an API Key**:
   - Go to [API Keys](https://aistudio.google.com/app/apikey)
   - Click "Create API Key"
   - Give it a name like "IReader Translation"
   - Copy the generated API key

3. **Use in IReader**:
   - Open IReader app
   - Go to Settings → Translation Settings
   - Select "Google Gemini API" as your translation engine
   - Paste your API key in the Gemini API Key field

### Using ChatGPT or DeepSeek WebView (No API Key)

If you prefer not to use an API key, you can use the WebView options:

1. **ChatGPT WebView**:
   - Select "ChatGPT WebView" as your translation engine
   - Click "Sign in to ChatGPT"
   - Log in with your ChatGPT account in the webview
   - The app will save your session cookies for future translations

2. **DeepSeek WebView**:
   - Select "DeepSeek WebView" as your translation engine
   - Click "Sign in to DeepSeek"
   - Log in with your DeepSeek account in the webview
   - The app will save your session cookies for future translations

## Advanced Translation Settings

When using AI-powered engines, you can customize your translation:

1. **Content Type**:
   - General - For most content
   - Literary - For novels and stories
   - Technical - For manuals and technical documents
   - Conversation - For dialogue
   - Poetry - For poems and lyrics

2. **Tone Type**:
   - Neutral - Standard translation
   - Formal - More professional language
   - Casual - More relaxed language
   - Humorous - Adds slight humor
   - Professional - Business-appropriate language

3. **Preserve Style**:
   - Enable this to maintain the original writing style

## Tips for Using AI Translation

1. **Google Gemini API (Recommended)**:
   - Provides the best balance of quality and ease of use
   - Free tier includes generous quota
   - Will automatically try different models if one exceeds quota

2. **For Desktop Users**:
   - Make sure Java is properly installed for desktop version
   - If you encounter "resource not found" errors, try reinstalling the app
   - Use the standalone version with packaged JRE for best compatibility

3. **For Mobile Users**:
   - WebView options work best on newer devices
   - If you encounter CAPTCHA, you'll need to complete it manually
   - API-based options (Gemini, DeepSeek API) are more reliable on mobile

4. **Handling Quota Limits**:
   - If you see "quota exceeded" messages, try waiting a day or switching engines
   - For frequent use, consider upgrading to a paid API tier

## Troubleshooting

- **Translation Fails**: Check your internet connection and API key validity
- **Slow Translation**: Large texts are translated in batches; be patient
- **WebView Issues**: Try clearing cookies and logging in again
- **Empty Responses**: Try switching to a different AI model or engine

Need more help? Visit the [IReader GitHub repository](https://github.com/IReaderorg/IReader) or Discord channel for assistance. 