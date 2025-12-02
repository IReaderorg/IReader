# 🎙️ Coqui TTS - Hugging Face Space Tutorial

A high-quality neural text-to-speech service powered by [Coqui TTS](https://github.com/coqui-ai/TTS), deployable on Hugging Face Spaces.

## Overview

This Space provides a free, open-source TTS API that IReader can use to read books aloud with natural-sounding voices.

**Features:**
- 🎯 Natural-sounding English voice (LJSpeech FastPitch model)
- ⚡ Fast synthesis
- 🎚️ Adjustable speed (0.5x - 2.0x)
- 📝 Up to 5000 characters per request
- 🆓 Free and open source

---

## Quick Start

### Option 1: Deploy to Hugging Face Spaces (Recommended)

1. **Create a Hugging Face account** at [huggingface.co](https://huggingface.co)

2. **Create a new Space:**
   - Go to [huggingface.co/new-space](https://huggingface.co/new-space)
   - Choose a name (e.g., `coqui-tts`)
   - Select **Gradio** as the SDK
   - Choose **CPU basic** (free tier works fine)
   - Click "Create Space"

3. **Upload the files:**
   - Upload `app.py` and `requirements.txt` from this directory
   - Or use Git to push the files:
   ```bash
   git clone https://huggingface.co/spaces/YOUR_USERNAME/coqui-tts
   cd coqui-tts
   cp /path/to/huggingface_space_coqui/* .
   git add .
   git commit -m "Initial commit"
   git push
   ```

4. **Wait for deployment** (~2-5 minutes for first build)

5. **Access your Space** at `https://YOUR_USERNAME-coqui-tts.hf.space`

### Option 2: Run Locally

```bash
# Navigate to the directory
cd huggingface_space_coqui

# Create virtual environment (recommended)
python -m venv venv
source venv/bin/activate  # Linux/Mac
# or: venv\Scripts\activate  # Windows

# Install dependencies
pip install -r requirements.txt

# Run the app
python app.py
```

The app will be available at `http://localhost:7860`

---

## API Usage

### Web Interface

Simply visit your Space URL and:
1. Enter text in the textbox
2. Adjust speed slider if needed
3. Click "Submit"
4. Play or download the generated audio

### Programmatic API (Python)

```python
from gradio_client import Client

# Connect to your Space
client = Client("YOUR_USERNAME/coqui-tts")

# Generate speech
result = client.predict(
    text="Hello, this is a test.",
    speed=1.0,
    api_name="/predict"
)

# result contains the path to the generated audio file
print(f"Audio saved to: {result}")
```

### REST API (cURL)

```bash
curl -X POST "https://YOUR_USERNAME-coqui-tts.hf.space/api/predict" \
  -H "Content-Type: application/json" \
  -d '{"data": ["Hello, this is a test.", 1.0]}'
```

---

## Integration with IReader

To use this TTS service with IReader:

1. Deploy your Space (see Quick Start above)
2. Note your Space URL: `https://YOUR_USERNAME-coqui-tts.hf.space`
3. Configure IReader to use this endpoint for TTS

---

## Configuration Options

### Available TTS Models

The default model is `tts_models/en/ljspeech/fast_pitch`. You can change it in `app.py`:

| Model | Quality | Speed | Notes |
|-------|---------|-------|-------|
| `tts_models/en/ljspeech/fast_pitch` | Good | Fast | Default, balanced |
| `tts_models/en/ljspeech/tacotron2-DDC` | Higher | Slower | Better quality |
| `tts_models/en/vctk/vits` | Good | Fast | Multi-speaker |

To change the model, edit line 14 in `app.py`:
```python
tts = TTS("tts_models/en/ljspeech/tacotron2-DDC")
```

### Speed Settings

- `0.5` - Slow (good for learning/accessibility)
- `1.0` - Normal speed
- `1.5` - Fast
- `2.0` - Maximum speed

---

## Troubleshooting

### "Model loading failed"
- Ensure you have enough disk space (~500MB for models)
- Check your internet connection (models are downloaded on first run)

### "Out of memory"
- Reduce text length
- Use CPU basic tier on HF Spaces (GPU not required for this model)

### "Slow generation"
- First request is slower (model warm-up)
- Subsequent requests are faster
- Consider using `fast_pitch` model for speed

### Local installation issues

**Windows:**
```bash
# If TTS installation fails, try:
pip install TTS --no-build-isolation
```

**Linux/Mac:**
```bash
# Install system dependencies first:
# Ubuntu/Debian:
sudo apt-get install libsndfile1

# Mac:
brew install libsndfile
```

---

## File Structure

```
huggingface_space_coqui/
├── app.py              # Main Gradio application
├── requirements.txt    # Python dependencies
└── README.md          # This tutorial
```

---

## Cost & Limits

**Hugging Face Spaces (Free Tier):**
- CPU basic: Free
- 2 vCPU, 16GB RAM
- Sleeps after 48h of inactivity (wakes on request)
- No rate limits (but be reasonable)

**Upgrade options:**
- CPU upgrade: $0.03/hour
- GPU: $0.60/hour (not needed for this app)

---

## License

This project uses:
- [Coqui TTS](https://github.com/coqui-ai/TTS) - MPL-2.0 License
- [Gradio](https://gradio.app/) - Apache 2.0 License

---

## Contributing

Feel free to:
- Add support for more languages/voices
- Improve the UI
- Add batch processing
- Optimize performance

Submit PRs to the main repository!
