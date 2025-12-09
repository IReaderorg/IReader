# Free Cloud Ollama Setup for IReader Translation

This guide explains how to run Ollama for free in the cloud using Hugging Face Spaces, Google Colab, or other free cloud services.

## Option 1: Hugging Face Spaces (Recommended)

Hugging Face Spaces offers free GPU access for running Ollama.

### Step 1: Create a Hugging Face Account

1. Go to [huggingface.co](https://huggingface.co)
2. Click **Sign Up** and create a free account
3. Verify your email

### Step 2: Create a New Space

1. Click your profile icon → **New Space**
2. Configure the Space:
   - **Space name**: `ollama-server` (or any name)
   - **License**: Apache 2.0
   - **SDK**: Docker
   - **Hardware**: CPU basic (free) or GPU if available
   - **Visibility**: Private (recommended)
3. Click **Create Space**

### Step 3: Add the Dockerfile

Create a file named `Dockerfile` in your Space with this content:

```dockerfile
FROM ubuntu:22.04

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install Ollama
RUN curl -fsSL https://ollama.com/install.sh | sh

# Expose the Ollama port
EXPOSE 7860

# Set environment variables
ENV OLLAMA_HOST=0.0.0.0:7860
ENV OLLAMA_MODELS=/data/models

# Create models directory
RUN mkdir -p /data/models

# Copy and set up start script
COPY start.sh /start.sh
RUN chmod +x /start.sh

ENTRYPOINT ["/bin/bash", "/start.sh"]
```

### Step 4: Add the Start Script

Create a file named `start.sh`:

```bash
#!/bin/bash

# Start Ollama server in background
ollama serve &

# Wait for server to start
sleep 10

# Pull the translation model (use smaller models for free tier)
ollama pull qwen2.5:1.5b

# Keep container running by waiting for the background process
wait
```

### Step 5: Configure Your Space

Create a `README.md` file with the required Hugging Face Space configuration:

```markdown
---
title: Ollama Server
emoji: 🦙
colorFrom: blue
colorTo: purple
sdk: docker
app_port: 7860
pinned: false
license: apache-2.0
---

Ollama server for IReader translation.
```

**Required fields:**
- `title`: Display name for your Space
- `sdk`: Must be `docker` for this setup
- `app_port`: Must match the port in your Dockerfile (7860)

**Note:** Do not use `app_file` for Docker SDK - it's only for Gradio/Streamlit SDKs.

### Step 6: Get Your Space URL

After deployment, your Ollama server will be available at:
```
https://<your-username>-ollama-server.hf.space
```

### Step 7: Configure IReader

1. Open IReader → **Settings** → **Reader** → **Translation**
2. Select **Ollama (Local LLM)**
3. Set **Ollama Server URL** to your Space URL:
   ```
   https://your-username-ollama-server.hf.space
   ```
4. Set **Ollama Model** to `qwen2.5:1.5b`

### Recommended Models for Free Tier

| Model | Size | Quality | Notes |
|-------|------|---------|-------|
| `qwen2.5:0.5b` | 400MB | Basic | Fastest, lowest quality |
| `qwen2.5:1.5b` | 1GB | Good | Best balance for free tier |
| `phi3:mini` | 2.3GB | Good | Microsoft's small model |
| `gemma2:2b` | 1.6GB | Good | Google's small model |

---

## Option 2: Google Colab (Free GPU)

Google Colab provides free GPU access with some limitations.

### Step 1: Open Colab Notebook

1. Go to [colab.research.google.com](https://colab.research.google.com)
2. Create a new notebook
3. Change runtime to GPU: **Runtime** → **Change runtime type** → **T4 GPU**

### Step 2: Install and Run Ollama

Add these cells to your notebook:

**Cell 1: Install Ollama**
```python
!curl -fsSL https://ollama.com/install.sh | sh
```

**Cell 2: Start Ollama Server with ngrok**
```python
# Install ngrok for public URL
!pip install pyngrok -q

from pyngrok import ngrok
import subprocess
import time
import os

# Set Ollama to listen on all interfaces
os.environ['OLLAMA_HOST'] = '0.0.0.0:11434'

# Start Ollama server in background
subprocess.Popen(['ollama', 'serve'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
time.sleep(5)

# Create public URL with ngrok
public_url = ngrok.connect(11434)
print(f"🦙 Ollama is running at: {public_url}")
print(f"Use this URL in IReader: {public_url}")
```

**Cell 3: Pull Translation Model**
```python
!ollama pull mistral
```

**Cell 4: Keep Session Alive**
```python
import time
while True:
    time.sleep(60)
    print(".", end="", flush=True)
```

### Step 3: Get Your URL

After running Cell 2, you'll see a URL like:
```
https://xxxx-xx-xxx-xxx-xxx.ngrok-free.app
```

### Step 4: Configure IReader

Use the ngrok URL as your Ollama Server URL in IReader.

### Limitations

- Colab sessions timeout after ~12 hours of inactivity
- Free tier has limited GPU hours per day
- You need to re-run the notebook each session

---

## Option 3: Kaggle Notebooks (Free GPU)

Kaggle offers 30 hours/week of free GPU.

### Step 1: Create Kaggle Account

1. Go to [kaggle.com](https://kaggle.com)
2. Sign up for free
3. Verify your phone number (required for GPU access)

### Step 2: Create New Notebook

1. Click **Create** → **New Notebook**
2. Enable GPU: **Settings** → **Accelerator** → **GPU T4 x2**
3. Enable Internet: **Settings** → **Internet** → **On**

### Step 3: Run Ollama

```python
# Install Ollama
!curl -fsSL https://ollama.com/install.sh | sh

# Install ngrok
!pip install pyngrok -q

import subprocess
import time
import os
from pyngrok import ngrok

# Configure Ollama
os.environ['OLLAMA_HOST'] = '0.0.0.0:11434'

# Start server
subprocess.Popen(['ollama', 'serve'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
time.sleep(5)

# Pull model
!ollama pull mistral

# Create public URL
public_url = ngrok.connect(11434)
print(f"🦙 Use this URL in IReader: {public_url}")

# Keep alive
while True:
    time.sleep(60)
```

---

## Option 4: Render.com (Free Tier)

Render offers a free tier for web services.

### Step 1: Create Account

1. Go to [render.com](https://render.com)
2. Sign up with GitHub

### Step 2: Create Web Service

1. Click **New** → **Web Service**
2. Select **Deploy from a Git repository** or **Deploy an existing image**
3. Use Docker image: `ollama/ollama`
4. Configure:
   - **Name**: `ollama-server`
   - **Region**: Choose closest to you
   - **Instance Type**: Free
   - **Environment Variables**:
     ```
     OLLAMA_HOST=0.0.0.0:10000
     ```

### Limitations

- Free tier has limited resources
- Services spin down after inactivity
- Cold starts can be slow

---

## Option 5: Railway.app

Railway offers $5 free credit monthly.

### Step 1: Create Account

1. Go to [railway.app](https://railway.app)
2. Sign up with GitHub

### Step 2: Deploy Ollama

1. Click **New Project** → **Deploy from Docker Image**
2. Enter: `ollama/ollama`
3. Add environment variable:
   ```
   OLLAMA_HOST=0.0.0.0:$PORT
   ```
4. Deploy

### Step 3: Get URL

Railway will provide a URL like:
```
https://ollama-production-xxxx.up.railway.app
```

---

## Troubleshooting

### "Connection Timeout" Error

- Check if your cloud service is still running
- Verify the URL doesn't include `/api/chat`
- Try refreshing/restarting the cloud service

### "Model Not Found" Error

- Make sure you pulled the model in your cloud setup
- Check the model name matches exactly (e.g., `mistral` not `Mistral`)

### Slow Response Times

- Cloud free tiers have limited resources
- Use smaller models (`qwen2.5:1.5b` instead of `llama3`)
- Consider upgrading to paid tier for better performance

### Session Expired (Colab/Kaggle)

- Re-run the notebook
- The ngrok URL will change each session
- Update the URL in IReader settings

## Comparison Table

| Service | Free GPU | Always On | Setup Difficulty |
|---------|----------|-----------|------------------|
| Hugging Face | Limited | Yes | Medium |
| Google Colab | Yes (T4) | No | Easy |
| Kaggle | Yes (T4) | No | Easy |
| Render | No | Yes* | Medium |
| Railway | No | Yes | Easy |

*Spins down after inactivity

## Security Notes

- ngrok URLs are public - anyone with the URL can access your Ollama
- For sensitive translations, use private/authenticated services
- Don't share your ngrok URLs publicly
- Consider using Hugging Face private Spaces for better security
