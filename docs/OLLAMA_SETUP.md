# Ollama Setup Guide for IReader Translation

This guide explains how to set up Ollama on Windows and connect to it from your Android device for local LLM translation.

## What is Ollama?

Ollama is a tool that lets you run large language models (LLMs) locally on your computer. This means:
- **Free**: No API costs or subscriptions
- **Private**: Your text never leaves your network
- **Fast**: Local processing, no internet latency (depends on your hardware)

## Requirements

- Windows 10/11 PC with at least 8GB RAM (16GB+ recommended)
- GPU with 4GB+ VRAM recommended (NVIDIA, AMD, or Intel)
- Android device on the same WiFi network as your PC

## Step 1: Install Ollama on Windows

1. Download Ollama from [ollama.com/download](https://ollama.com/download)
2. Run the installer
3. Ollama will start automatically and run in the system tray

## Step 2: Download a Translation Model

Open PowerShell or Command Prompt and run:

```cmd
ollama pull mistral
```

Recommended models for translation:
| Model | Size | Quality | Speed |
|-------|------|---------|-------|
| `mistral` | 4GB | Good | Fast |
| `llama2` | 4GB | Good | Fast |
| `llama3` | 4.7GB | Better | Medium |
| `qwen2.5:7b` | 4.7GB | Excellent for Asian languages | Medium |
| `gemma2:9b` | 5.4GB | Very Good | Slower |

To download a different model:
```cmd
ollama pull llama3
```

## Step 3: Configure Ollama for Network Access

By default, Ollama only accepts connections from localhost. To allow your Android device to connect:

### Option A: Temporary (for testing)

Open PowerShell and run:
```powershell
$env:OLLAMA_HOST="0.0.0.0:11434"; ollama serve
```

### Option B: Permanent (recommended)

1. Open PowerShell as Administrator
2. Run:
```powershell
[System.Environment]::SetEnvironmentVariable("OLLAMA_HOST", "0.0.0.0:11434", "User")
```
3. Restart Ollama (right-click tray icon → Quit, then start again)

## Step 4: Configure Windows Firewall

Allow Ollama through the firewall:

1. Open **Windows Security** → **Firewall & network protection**
2. Click **Allow an app through firewall**
3. Click **Change settings** → **Allow another app**
4. Browse to `C:\Users\<YourUsername>\AppData\Local\Programs\Ollama\ollama.exe`
5. Check both **Private** and **Public** networks
6. Click **OK**

Or via PowerShell (Admin):
```powershell
New-NetFirewallRule -DisplayName "Ollama" -Direction Inbound -LocalPort 11434 -Protocol TCP -Action Allow
```

## Step 5: Find Your PC's IP Address

Open PowerShell and run:
```powershell
ipconfig
```

Look for **IPv4 Address** under your WiFi or Ethernet adapter. It will look like `192.168.x.x` or `10.0.x.x`.

## Step 6: Configure IReader on Android

1. Open IReader
2. Go to **Settings** → **Reader** → **Translation**
3. Select **Ollama (Local LLM)** as the translation engine
4. Set **Ollama Server URL** to your PC's IP:
   ```
   http://192.168.x.x:11434
   ```
   (Replace `192.168.x.x` with your actual IP from Step 5)
5. Set **Ollama Model** to the model you downloaded (e.g., `mistral`)

## Step 7: Test the Connection

1. Open a book in IReader
2. Select some text and choose **Translate**
3. The translation should appear after a few seconds

## Troubleshooting

### Connection Timeout
- Verify Ollama is running (check system tray)
- Confirm `OLLAMA_HOST` is set to `0.0.0.0:11434`
- Check Windows Firewall allows port 11434
- Ensure both devices are on the same WiFi network

### "404 Not Found" Error
- Make sure the URL doesn't include `/api/chat` - just use the base URL
- Correct: `http://192.168.1.100:11434`
- Wrong: `http://192.168.1.100:11434/api/chat`

### Slow Translation
- Use a smaller model (`mistral` instead of `llama3`)
- Ensure your GPU is being used (check with `ollama ps`)
- Close other GPU-intensive applications

### Model Not Found
- Run `ollama list` to see installed models
- Download the model: `ollama pull <model-name>`

## Running Ollama with Debug Logging

To see detailed logs for troubleshooting:

```powershell
$env:OLLAMA_DEBUG="1"; ollama serve
```

Or set permanently:
```powershell
[System.Environment]::SetEnvironmentVariable("OLLAMA_DEBUG", "1", "User")
```

## Advanced: Running Ollama as a Windows Service

To have Ollama start automatically with Windows:

1. Download [NSSM](https://nssm.cc/download)
2. Extract and open PowerShell in the folder
3. Run:
```powershell
.\nssm.exe install Ollama "C:\Users\<YourUsername>\AppData\Local\Programs\Ollama\ollama.exe" serve
.\nssm.exe set Ollama AppEnvironmentExtra OLLAMA_HOST=0.0.0.0:11434
.\nssm.exe start Ollama
```

## Recommended Settings

For best translation quality:
- **Model**: `mistral` or `qwen2.5:7b` (for Chinese/Japanese/Korean)
- **Content Type**: Literary (for novels)
- **Preserve Style**: Enabled

## Security Note

When you set `OLLAMA_HOST=0.0.0.0`, Ollama accepts connections from any device on your network. This is fine for home networks but be cautious on public WiFi. You can restrict access by:
- Using your PC's firewall to only allow your phone's IP
- Only enabling network access when needed
