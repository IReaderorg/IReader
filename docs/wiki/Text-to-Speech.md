# Text-to-Speech (TTS) Guide

IReader offers advanced Text-to-Speech capabilities, allowing you to listen to your novels with natural-sounding voices.

## Desktop TTS Engines

On the Desktop version, you can manage TTS engines via **Settings → Text to Speech → TTS Engine Manager**.

### 1. Piper TTS
A fast, local neural TTS engine.
- **Pros**: Very fast, low memory usage, works offline.
- **Voices**: 30+ voices in 20+ languages.
- **Status**: Pre-installed or easily downloadable.

### 2. Kokoro TTS
A premium quality local neural TTS.
- **Pros**: Extremely natural human-like voices.
- **Cons**: Larger download size (~300MB), requires more processing power.
- **Setup**: Click "Install" in the TTS Manager. The first run will download the model.

### 3. Maya TTS
A multilingual neural TTS system.
- **Pros**: Support for 16+ languages with high quality.
- **Setup**: Click "Install" in the TTS Manager.

## Online TTS (Coqui)

IReader supports connecting to a **Coqui TTS** server (hosted on Hugging Face Spaces or locally). This works on both **Android and Desktop**.

### Setup Guide
1. **Deploy a Server**:
   - You can duplicate our [Hugging Face Space](https://huggingface.co/spaces/kazemcodes/ireader) to your own account for better performance.
   - Or run a Coqui XTTS server locally.

2. **Configure IReader**:
   - Go to **Settings → Text to Speech**.
   - Enable **"Use Coqui TTS"**.
   - Enter your **Space URL** (e.g., `https://my-space.hf.space`).
   - (Optional) Enter your API Key if your space is private.

### Features
- **High Quality**: Uses the XTTS v2 model for state-of-the-art speech synthesis.
- **Multilingual**: Supports English, Spanish, French, German, Italian, Portuguese, Polish, Turkish, Russian, Dutch, Czech, Arabic, Chinese, Japanese, Hungarian, Korean.

## Android TTS
On Android, IReader uses the system's default TTS engine (e.g., Google TTS, Samsung TTS) by default.
- You can change the voice and speed in the player controls.
- For higher quality, use the **Coqui TTS** online integration described above.
