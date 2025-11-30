# Text-to-Speech (TTS) Guide

IReader offers powerful Text-to-Speech capabilities, allowing you to listen to your novels offline or online with high-quality voices.

## Available Engines

IReader supports three main TTS engines:

1.  **Piper TTS** (Recommended for most users)
    *   **Type**: Offline
    *   **Pros**: Very fast, low memory usage, good quality, many voices/languages.
    *   **Cons**: Voices are not as "human-like" as Kokoro or AI models.
2.  **Kokoro TTS**
    *   **Type**: Offline
    *   **Pros**: Extremely high quality, very natural and human-like prosody.
    *   **Cons**: Large download (~300MB+), slower than Piper, higher battery usage.
3.  **Native TTS**
    *   **Type**: Offline
    *   **Pros**: Uses your device's built-in engine (Google, Samsung, etc.), zero setup.
    *   **Cons**: Quality depends entirely on your device's system TTS.
4.  **Gradio TTS**
    *   **Type**: Online
    *   **Pros**: Access to massive AI models hosted on Hugging Face.
    *   **Cons**: Requires internet connection, may have latency.

---

## Piper TTS Setup

Piper is the balanced choice for offline reading.

1.  Go to **Settings** → **TTS Engine Manager**.
2.  Find the **Piper TTS** card.
3.  Tap **Install** (if not already installed).
4.  Once installed, scroll down to **Piper TTS Voices**.
5.  Browse the list of available voices.
6.  Tap the **Download** icon next to a voice you like.
7.  Once downloaded, tap the voice card to **Select** it.

> [!TIP]
> You can preview a voice before downloading by tapping the play button (if available) or testing it after download.

---

## Kokoro TTS Setup

Kokoro offers the best offline audio quality but requires a larger initial download.

1.  Go to **Settings** → **TTS Engine Manager**.
2.  Find the **Kokoro TTS** card.
3.  Tap **Install**.
4.  **Wait**: The first time you use Kokoro, it will download a large model file (~300MB). This can take 5-10 minutes depending on your connection.
5.  **Test**: Use the "Test" button to verify it's working.

> [!IMPORTANT]
> Ensure you have enough storage space before installing Kokoro.

---

## Gradio TTS (Online)

Use this if you want to connect to a custom AI TTS model hosted on Hugging Face Spaces.

1.  Go to **Settings** → **TTS Engine Manager**.
2.  Scroll to the **Gradio TTS** section.
3.  **Enable Gradio TTS**: Toggle the switch to ON.
4.  **Space URL**: Enter the URL of the Hugging Face Space (e.g., `https://huggingface.co/spaces/username/space-name`).
5.  **API Key**: (Optional) Enter your Hugging Face API key if the space is private.
6.  **Config ID**: Select a preset configuration if available.

---

## Using TTS in Reader

1.  Open a chapter in the Reader.
2.  Tap the screen to show controls.
3.  Tap the **Headphone** icon (or access via the 3-dot menu).
4.  The TTS player will appear at the bottom.
5.  **Play/Pause**: Control playback.
6.  **Speed**: Adjust reading speed (0.5x to 3.0x).
7.  **Timer**: Set a sleep timer (e.g., 15 min, 30 min).

---

## Troubleshooting

### No Sound
*   Check your device volume (Media volume).
*   Ensure the correct TTS engine is selected in settings.
*   If using Piper/Kokoro, ensure the voice model is fully downloaded.

### Installation Failed
*   Check your internet connection.
*   Ensure you have sufficient storage space.
*   Try clearing the app cache and retrying.

### "Initialization Failed" (Kokoro)
*   This usually happens if the model download was interrupted.
*   Uninstall Kokoro from the manager and reinstall it.
