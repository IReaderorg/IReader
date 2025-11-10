# Piper TTS User Guide

## Welcome to Piper Text-to-Speech

IReader includes powerful offline text-to-speech (TTS) powered by Piper, allowing you to listen to your books with natural-sounding AI voices. This guide will help you get started and make the most of this feature.

## What is Piper TTS?

Piper is a neural text-to-speech system that converts written text into spoken audio. Unlike traditional TTS systems, Piper uses advanced AI models to produce natural, human-like speech.

### Key Features

- **Completely Offline**: Works without internet connection once voice models are downloaded
- **Privacy-First**: All processing happens on your device, no data sent to servers
- **High Quality**: Neural voices sound natural and expressive
- **Multi-Language**: Support for 20+ languages including English, Spanish, French, German, Chinese, Japanese, and more
- **Customizable**: Adjust speed, pitch, and other parameters to your preference
- **Lightweight**: Efficient models that run smoothly on most computers

## Getting Started

### Step 1: Access TTS Controls

1. Open any book in IReader
2. Navigate to a chapter you want to read
3. Look for the **speaker icon** (🔊) in the reader toolbar
4. Click the icon to open the TTS control panel

### Step 2: Download Your First Voice

On first use, you'll need to download a voice model:

1. Click **"Manage Voices"** or the settings icon in the TTS panel
2. Browse the available voice models
3. Choose a voice that matches your preferred language
4. Click the **Download** button
5. Wait for the download to complete (typically 30-120 seconds depending on model size)

**Recommended First Voices:**
- **English (US)**: Amy or Ryan - Medium quality
- **English (UK)**: Alan - Medium quality
- **Spanish**: Carla or Diego - Medium quality
- **French**: Siwis - Medium quality
- **German**: Thorsten - Medium quality

### Step 3: Start Listening

1. Select your downloaded voice from the voice dropdown
2. Click the **Play** button (▶️)
3. The text will be read aloud starting from your current position
4. The currently spoken text will be highlighted in the reader

Congratulations! You're now using Piper TTS.


## Understanding Voice Models

### Voice Characteristics

Each voice model has several characteristics:

#### Language and Locale
- **Language**: The primary language (e.g., English, Spanish)
- **Locale**: Regional variant (e.g., en-US, en-GB, es-MX)
- Choose a locale that matches your content or preference

#### Gender
- **Male**: Deeper, masculine voice
- **Female**: Higher, feminine voice
- **Neutral**: Gender-neutral voice (available for some languages)

#### Quality Levels

| Quality | File Size | Speed | Naturalness | Best For |
|---------|-----------|-------|-------------|----------|
| **Low** | 20-30 MB | Fast | Good | Quick reading, older devices |
| **Medium** | 40-60 MB | Balanced | Very Good | General use (recommended) |
| **High** | 80-120 MB | Slower | Excellent | Audiobook production, critical listening |

**Recommendation**: Start with **Medium quality** voices for the best balance of quality and performance.

### Downloading Voice Models

#### How to Download

1. Open the **Voice Manager** from TTS settings
2. Use the **language filter** to find voices in your preferred language
3. Click on a voice card to see details:
   - Voice name and description
   - Language and locale
   - Gender and quality
   - File size
   - Sample audio (preview)
4. Click **Download** to begin downloading
5. Monitor the progress bar
6. Once complete, the voice is ready to use

#### Download Tips

- **WiFi Recommended**: Voice models range from 20-120 MB
- **Storage Space**: Ensure you have sufficient disk space
- **Multiple Downloads**: You can download multiple voices for different languages
- **Resume Support**: Interrupted downloads can be resumed

#### Managing Storage

Voice models are stored on your device. To manage storage:

1. Open **Voice Manager**
2. View total storage used at the top
3. See individual model sizes
4. Delete unused voices by clicking the **trash icon**
5. You can re-download deleted voices anytime

**Storage Locations:**
- Windows: `%APPDATA%\ireader\piper_models\`
- macOS: `~/Library/Application Support/ireader/piper_models/`
- Linux: `~/.local/share/ireader/piper_models/`

## Using TTS Controls

### Playback Controls

#### Basic Controls

- **Play (▶️)**: Start reading from current position
- **Pause (⏸️)**: Temporarily stop (resume from same position)
- **Stop (⏹️)**: End playback and return to beginning

#### Navigation Controls

- **Skip Forward (⏭️)**: Jump to next paragraph or sentence
- **Skip Backward (⏮️)**: Go back to previous paragraph or sentence
- **Next Chapter**: Automatically advance to next chapter when current chapter ends
- **Previous Chapter**: Jump back to previous chapter

#### Keyboard Shortcuts

| Action | Windows/Linux | macOS |
|--------|---------------|-------|
| Play/Pause | `Space` | `Space` |
| Stop | `Esc` | `Esc` |
| Skip Forward | `→` or `Ctrl+→` | `→` or `⌘→` |
| Skip Backward | `←` or `Ctrl+←` | `←` or `⌘←` |
| Speed Up | `Ctrl++` | `⌘+` |
| Speed Down | `Ctrl+-` | `⌘-` |
| Next Chapter | `Ctrl+Shift+→` | `⌘⇧→` |
| Previous Chapter | `Ctrl+Shift+←` | `⌘⇧←` |

### Adjusting Speech Settings

#### Speech Rate (Speed)

Control how fast the voice speaks:

1. Locate the **speed slider** in TTS controls
2. Drag left to slow down (0.5x = half speed)
3. Drag right to speed up (2.0x = double speed)
4. Default is 1.0x (normal speed)

**Tips:**
- Start at 1.0x and adjust to your comfort
- Slower speeds (0.7x - 0.9x) help with complex content or language learning
- Faster speeds (1.2x - 1.5x) are great for familiar content
- Very fast speeds (1.6x+) take practice but can significantly increase reading speed

#### Volume Control

Adjust TTS volume independently from system volume:

1. Use the **volume slider** in TTS controls
2. Range: 0% (mute) to 100% (maximum)
3. Default: 80%

**Note**: This controls TTS output only, not your system volume.

#### Voice Selection

Switch between downloaded voices:

1. Click the **voice dropdown** in TTS controls
2. Select from your downloaded voices
3. The change applies immediately
4. Your selection is saved for next time

### Progress Tracking

The TTS control panel shows:

- **Progress Bar**: Visual indicator of position in current chapter
- **Time Elapsed**: How long you've been listening
- **Time Remaining**: Estimated time to finish current chapter
- **Text Highlighting**: Currently spoken text is highlighted in the reader

## Advanced Features

### Reading Modes

#### Continuous Reading Mode (Default)

- Automatically progresses through paragraphs
- Continues to next chapter when current chapter ends
- Best for uninterrupted listening

#### Paragraph Mode

- Pauses after each paragraph
- Useful for studying or taking notes
- Enable in TTS Settings → Reading Mode

#### Sentence Mode

- Pauses after each sentence
- Ideal for language learning
- Enable in TTS Settings → Reading Mode

### Text Highlighting

As text is read aloud, the current word or sentence is highlighted:

- **Word Highlighting**: Highlights each word as it's spoken (default)
- **Sentence Highlighting**: Highlights the entire current sentence
- **Paragraph Highlighting**: Highlights the current paragraph
- **No Highlighting**: Disable highlighting if preferred

Configure in: TTS Settings → Highlighting

### Auto-Scroll

The reader automatically scrolls to keep the spoken text visible:

- **Enabled by default**
- Smooth scrolling follows the voice
- You can manually scroll without interrupting playback
- Auto-scroll resumes when you stop scrolling

Disable in: TTS Settings → Auto-Scroll

### Voice Bookmarks

Save your position for later:

1. While listening, click **Bookmark** (🔖)
2. Add a note (optional)
3. Resume from bookmarks anytime
4. TTS automatically resumes from bookmarked position


## Multi-Language Support

### Available Languages

Piper TTS supports 20+ languages with high-quality voices:

| Language | Locales Available | Sample Voices |
|----------|-------------------|---------------|
| **English** | US, UK, Australia, India | Amy, Ryan, Alan, Jenny |
| **Spanish** | Spain, Mexico, Argentina | Carla, Diego, Maria |
| **French** | France, Canada | Siwis, Marie |
| **German** | Germany, Austria | Thorsten, Eva |
| **Chinese** | Mandarin, Cantonese | Huayan, Xiaoxiao |
| **Japanese** | Japan | Hikari, Takeshi |
| **Korean** | South Korea | Minho, Sora |
| **Italian** | Italy | Riccardo, Paola |
| **Portuguese** | Brazil, Portugal | Faber, Cristiano |
| **Russian** | Russia | Dmitri, Irina |
| **Arabic** | Egypt, Saudi Arabia | Amira, Hassan |
| **Hindi** | India | Aarav, Diya |
| **Dutch** | Netherlands | Nienke, Maarten |
| **Polish** | Poland | Agnieszka, Bartosz |
| **Turkish** | Turkey | Aylin, Mehmet |
| **Swedish** | Sweden | Elin, Erik |
| **Norwegian** | Norway | Ingrid, Lars |
| **Danish** | Denmark | Sofie, Mikkel |
| **Finnish** | Finland | Aino, Eero |
| **Greek** | Greece | Maria, Nikos |

### Using Multiple Languages

#### Downloading Multiple Voices

You can download voices for multiple languages:

1. Open Voice Manager
2. Download voices for each language you need
3. Switch between voices as needed

#### Automatic Language Detection

When enabled, IReader can automatically detect the language of your content and suggest appropriate voices:

1. Enable in: Settings → TTS → Auto Language Detection
2. Open a book in a different language
3. IReader suggests voices matching the detected language
4. Accept the suggestion or choose manually

#### Manual Voice Switching

For multilingual content:

1. Pause playback
2. Select a different voice from the dropdown
3. Resume playback
4. The new voice continues from where you paused

### Language Learning Tips

TTS is excellent for language learning:

- **Slow Speed**: Use 0.6x - 0.8x speed to hear pronunciation clearly
- **Sentence Mode**: Pause after each sentence to practice
- **Repeat**: Use skip backward to repeat difficult passages
- **Multiple Voices**: Try different voices to hear pronunciation variations
- **Text Highlighting**: Follow along visually while listening

## Accessibility Features

### For Visual Impairments

- **Screen Reader Compatible**: Works with NVDA, JAWS, VoiceOver
- **Keyboard Navigation**: Full keyboard control without mouse
- **High Contrast Mode**: Enhanced visibility for TTS controls
- **Large Text**: Adjustable UI text size

### For Reading Difficulties

- **Dyslexia Support**: 
  - Slower default speeds
  - Clear, articulate voices
  - Word-by-word highlighting
  - Customizable fonts and spacing

- **ADHD Support**:
  - Paragraph mode for breaks
  - Progress tracking
  - Bookmarks for resuming
  - Distraction-free reading mode

### For Hearing Impairments

- **Visual Feedback**: 
  - Waveform visualization (optional)
  - Text highlighting synchronized with audio
  - Progress indicators

- **Audio Enhancement**:
  - Volume normalization
  - Frequency adjustment (if supported by voice)
  - Compatibility with hearing aid devices

### Keyboard-Only Navigation

Complete TTS control without mouse:

1. `Tab` to navigate between controls
2. `Space` to activate buttons
3. `Arrow keys` for sliders
4. `Enter` to confirm selections
5. `Esc` to close dialogs

## Troubleshooting

### Common Issues

#### No Sound

**Check:**
1. System volume is not muted
2. TTS volume slider is not at 0%
3. Correct audio output device is selected
4. Audio device is working (test with other apps)

**Solution:**
- Adjust system volume
- Increase TTS volume slider
- Select correct output device in system settings
- Restart audio service

#### Voice Sounds Robotic or Distorted

**Possible Causes:**
- Low-quality voice model
- Incorrect audio settings
- System resource constraints

**Solutions:**
- Try a Medium or High quality voice
- Check audio format settings
- Close other resource-intensive applications
- Restart IReader

#### Download Fails

**Check:**
1. Internet connection is stable
2. Sufficient disk space available
3. No firewall blocking downloads

**Solutions:**
- Retry the download
- Free up disk space
- Check firewall settings
- Try a different network

#### TTS Stops Unexpectedly

**Possible Causes:**
- System went to sleep
- Audio device disconnected
- Application lost focus

**Solutions:**
- Disable system sleep during TTS
- Keep audio device connected
- Enable "Continue in background" in settings

#### Text Not Highlighting

**Check:**
1. Highlighting is enabled in settings
2. Reader view is visible
3. Text is not in an unsupported format

**Solutions:**
- Enable highlighting in TTS Settings
- Ensure reader view is active
- Try a different book format

### Performance Issues

#### Slow Synthesis

**Solutions:**
- Use Low or Medium quality voices
- Close other applications
- Ensure sufficient RAM available
- Update to latest version

#### High CPU Usage

**Solutions:**
- Use lower quality voice models
- Reduce speech rate
- Enable hardware acceleration (if available)
- Check for background processes

### Getting Help

If you encounter issues not covered here:

1. Check the [Troubleshooting Guide](../TTS_Troubleshooting_Guide.md)
2. Visit [GitHub Issues](https://github.com/IReaderorg/IReader/issues)
3. Search for similar problems
4. Create a new issue with details:
   - Your operating system and version
   - IReader version
   - Voice model being used
   - Steps to reproduce the issue
   - Error messages (if any)

## Frequently Asked Questions (FAQ)

### General Questions

**Q: Is Piper TTS free?**
A: Yes, Piper TTS is completely free and open-source. All voice models are also free to use.

**Q: Do I need internet to use TTS?**
A: You need internet only to download voice models. Once downloaded, TTS works completely offline.

**Q: How much storage do voice models use?**
A: Voice models range from 20 MB (low quality) to 120 MB (high quality). Most users need 100-300 MB total for 2-3 voices.

**Q: Can I use TTS on multiple devices?**
A: Yes, but you need to download voice models on each device separately.

**Q: Is my reading data sent to any servers?**
A: No. All TTS processing happens locally on your device. No data is sent to external servers.

### Voice Models

**Q: Which voice should I choose?**
A: Start with a Medium quality voice in your preferred language. Try a few to find one you like.

**Q: Can I use multiple voices?**
A: Yes, download as many as you want and switch between them anytime.

**Q: How do I delete a voice I don't use?**
A: Open Voice Manager, find the voice, and click the delete/trash icon.

**Q: Can I create my own voice?**
A: Custom voice creation is not currently supported, but may be added in future versions.

**Q: Why do some voices sound better than others?**
A: Voice quality depends on the training data and model size. Higher quality models generally sound more natural.

### Performance

**Q: Why is synthesis slow on my computer?**
A: Try using a lower quality voice model, or close other applications to free up resources.

**Q: Can I speed up the voice?**
A: Yes, use the speech rate slider to adjust from 0.5x to 2.0x speed.

**Q: Does TTS drain battery on laptops?**
A: TTS uses moderate CPU, similar to playing audio. Lower quality voices use less power.

### Compatibility

**Q: What file formats are supported?**
A: TTS works with all text-based formats supported by IReader (EPUB, PDF, TXT, etc.).

**Q: Does TTS work with all languages?**
A: TTS works best with languages that have available voice models. Text in unsupported languages may not sound natural.

**Q: Can I use TTS with audiobooks?**
A: TTS is for text-to-speech conversion. For existing audiobooks, use the built-in audio player.

### Privacy and Security

**Q: Is my reading data private?**
A: Yes, all TTS processing happens on your device. No data is transmitted.

**Q: Are voice models safe to download?**
A: Yes, all voice models are verified and come from trusted sources.

**Q: Can I use TTS offline?**
A: Yes, once voice models are downloaded, TTS works completely offline.

## Tips and Best Practices

### For Best Audio Quality

1. **Use Good Headphones**: Neural TTS quality shines with quality audio equipment
2. **Choose Medium or High Quality Voices**: Better models sound more natural
3. **Adjust Speech Rate**: Find your comfortable listening speed
4. **Quiet Environment**: Minimize background noise for better experience

### For Productivity

1. **Use Faster Speeds**: Gradually increase to 1.3x - 1.5x to read more in less time
2. **Paragraph Mode**: Take breaks between paragraphs for better retention
3. **Bookmarks**: Mark important sections for later review
4. **Keyboard Shortcuts**: Learn shortcuts for efficient control

### For Language Learning

1. **Start Slow**: Use 0.7x - 0.8x speed initially
2. **Sentence Mode**: Pause after each sentence to practice
3. **Multiple Voices**: Compare pronunciation across different voices
4. **Repeat**: Use skip backward to hear difficult words again
5. **Follow Along**: Enable text highlighting to connect written and spoken forms

### For Long Listening Sessions

1. **Take Breaks**: Rest your ears every 30-60 minutes
2. **Vary Speed**: Change speed occasionally to maintain focus
3. **Switch Voices**: Try different voices to prevent monotony
4. **Adjust Volume**: Keep volume at comfortable levels

## Advanced Settings

### Audio Output

Configure audio output settings:

- **Output Device**: Select specific audio device
- **Sample Rate**: Match your audio device (usually automatic)
- **Buffer Size**: Adjust for latency vs stability
- **Volume Normalization**: Maintain consistent volume

Access: Settings → TTS → Audio Output

### Synthesis Options

Fine-tune synthesis behavior:

- **Noise Scale**: Adjust voice variation (0.0 - 1.0)
- **Length Scale**: Adjust phoneme duration
- **Sentence Silence**: Pause duration between sentences
- **Paragraph Silence**: Pause duration between paragraphs

Access: Settings → TTS → Advanced

### Performance Tuning

Optimize for your system:

- **Cache Size**: Number of pre-synthesized chunks to cache
- **Thread Count**: CPU threads for synthesis (auto-detect recommended)
- **Memory Limit**: Maximum memory for voice models
- **Preload**: Preload next chapter for seamless transitions

Access: Settings → TTS → Performance

## What's Next?

Now that you're familiar with Piper TTS:

1. **Explore Different Voices**: Try voices in different languages
2. **Customize Settings**: Adjust speed and other parameters to your preference
3. **Learn Shortcuts**: Master keyboard shortcuts for efficient control
4. **Share Feedback**: Help improve TTS by reporting issues or suggestions

## Additional Resources

- [Developer Guide](Developer_Guide.md) - For technical integration
- [TTS Setup Guide](../TTS_Setup_Guide.md) - Detailed setup instructions
- [Troubleshooting Guide](../TTS_Troubleshooting_Guide.md) - Solve common problems
- [GitHub Repository](https://github.com/IReaderorg/IReader) - Source code and issues
- [Community Forum](https://github.com/IReaderorg/IReader/discussions) - Ask questions and share tips

## Support the Project

Piper TTS in IReader is free and open-source. If you find it useful:

- ⭐ Star the project on GitHub
- 🐛 Report bugs and issues
- 💡 Suggest new features
- 📖 Improve documentation
- 🌍 Help translate to more languages
- ☕ Support the developers

Thank you for using IReader with Piper TTS!
