# AI Translation Guide

IReader features a powerful AI-driven translation system that allows you to read novels in your preferred language using state-of-the-art language models.

## Supported Engines

### 1. OpenAI (ChatGPT)
Uses OpenAI's GPT models for high-quality translation.
- **Requires**: API Key from [OpenAI Platform](https://platform.openai.com/).
- **Features**: Context-aware translation, customizable tone.

### 2. DeepSeek
A powerful and cost-effective alternative to OpenAI.
- **Requires**: API Key from [DeepSeek](https://deepseek.com/).
- **Features**: Excellent performance on literary text.

### 3. Google Gemini
Uses Google's Gemini Pro models.
- **Requires**: API Key from [Google AI Studio](https://aistudio.google.com/).
- **Features**: Fast and accurate translations.

### 4. Ollama (Local)
Run LLMs locally on your device or network for privacy and free translations.
- **Requires**: [Ollama](https://ollama.com/) installed and running.
- **Configuration**:
    - **URL**: Your Ollama server URL (default: `http://localhost:11434`).
    - **Model**: The model name to use (e.g., `mistral`, `llama3`).
    - **Note**: You must pull the model first using `ollama pull <model_name>`.

### 5. WebView Engines (ChatGPT / DeepSeek)
These engines use a hidden browser window to access the web interfaces of ChatGPT or DeepSeek directly.
- **Pros**: Free (uses your account's free tier).
- **Cons**: Slower, requires manual login, may encounter CAPTCHAs.
- **Setup**: Select the engine and click "Sign in" to log in with your account.

## Configuration

Go to **Settings → Translation** to configure your preferred engine.

### API Keys
For API-based engines (OpenAI, DeepSeek, Gemini), enter your API key in the respective field.
> [!SECURITY]
> Your API keys are stored securely on your device and are never shared.

### Advanced Settings
When using AI engines, you can customize the translation output:

- **Content Type**: Tell the AI what kind of text it's translating (e.g., "Novel", "General").
- **Tone**: Set the desired tone (e.g., "Neutral", "Formal", "Casual").
- **Preserve Style**: Attempt to keep the author's original writing style.

## Troubleshooting

- **Rate Limits**: If you see "Rate limit exceeded", wait a few moments or upgrade your API tier.
- **Login Issues**: For WebView engines, if translation fails, try signing out and signing back in.
- **Ollama Connection**: Ensure your Ollama server is accessible from the device running IReader.
