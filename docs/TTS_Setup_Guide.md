# Piper TTS Setup Guide

## Overview

IReader Desktop includes integrated Piper TTS (Text-to-Speech) functionality that allows you to listen to your books with natural-sounding AI-generated voices. Piper TTS works completely offline once you've downloaded voice models, ensuring privacy and availability without internet connectivity.

## Getting Started

### Prerequisites

- IReader Desktop application installed
- At least 100MB of free disk space for voice models
- Working audio output device

### First-Time Setup

1. **Open a Book**
   - Navigate to your library and open any book
   - Go to any chapter you want to read

2. **Access TTS Controls**
   - Look for the TTS button in the reader toolbar (speaker icon)
   - Click the TTS button to open the TTS control panel

3. **Download Your First Voice Model**
   - On first use, you'll see a message that no voice models are installed
   - Click "Manage Voice Models" or the settings icon
   - Browse the available voice models

## Downloading Voice Models

### Understanding Voice Models

Voice models are AI-trained neural networks that generate speech. Each model has different characteristics:

- **Language**: The language the voice speaks (English, Spanish, French, etc.)
- **Gender**: Male, Female, or Neutral voice
- **Quality**: 
  - **Low**: Smaller file size (~20-30MB), faster processing, good quality
  - **Medium**: Balanced size (~40-60MB), better naturalness
  - **High**: Larger size (~80-120MB), most natural and expressive

### How to Download

1. **Open Voice Model Manager**
   - Click the TTS settings icon in the reader
   - Select "Manage Voice Models"

2. **Browse Available Models**
   - Scroll through the list of available voices
   - Each model shows:
     - Voice name and language
     - Gender and quality level
     - File size
     - Download status

3. **Select and Download**
   - Click the "Download" button next to your preferred voice
   - A progress bar will show the download status
   - Wait for "Download Complete" message
   - The model is now ready to use

4. **Download Multiple Models** (Optional)
   - You can download multiple models for different languages
   - Downloaded models are stored locally and don't require re-downloading

### Recommended Models

For English speakers, we recommend starting with:
- **en_US-lessac-medium** - High-quality American English female voice
- **en_GB-alan-medium** - Natural British English male voice

For other languages, choose the "medium" quality variant for the best balance of quality and size.

## Selecting a Voice

### Setting Your Active Voice

1. **Open Voice Selection**
   - In the TTS control panel, click the voice dropdown
   - Or go to TTS Settings → Voice Selection

2. **Choose from Downloaded Models**
   - Only downloaded models appear in the selection list
   - Click on a voice to select it

3. **Apply Selection**
   - The voice is applied immediately
   - Your selection is saved for future sessions

### Switching Voices

You can switch voices at any time:
- Stop current playback (if playing)
- Select a different voice from the dropdown
- Resume playback with the new voice

## Adjusting TTS Settings

### Speech Rate

Control how fast the voice speaks:
- **Range**: 0.5x (half speed) to 2.0x (double speed)
- **Default**: 1.0x (normal speed)
- **How to adjust**: Use the speed slider in TTS controls
- **Tip**: Start at 1.0x and adjust based on your preference

### Volume

Control the playback volume:
- **Range**: 0% (mute) to 100% (maximum)
- **Default**: 80%
- **How to adjust**: Use the volume slider in TTS controls
- **Note**: This is separate from your system volume

### Pitch (If Available)

Some voices support pitch adjustment:
- **Range**: -50% (lower) to +50% (higher)
- **Default**: 0% (natural pitch)
- **How to adjust**: Use the pitch slider if available

## Using TTS Features

### Basic Playback Controls

- **Play**: Click the play button to start reading from current position
- **Pause**: Click pause to temporarily stop (resume from same position)
- **Stop**: Click stop to end playback and return to beginning

### Navigation During Playback

- **Next Paragraph**: Skip to the next paragraph
- **Previous Paragraph**: Go back to the previous paragraph
- **Next Chapter**: Jump to the next chapter
- **Previous Chapter**: Return to the previous chapter

### Word Highlighting

When TTS is active, the currently spoken word is highlighted in the text:
- Helps you follow along visually
- Highlight moves automatically as words are spoken
- You can scroll freely without disrupting the highlight
- Highlight disappears when playback is paused or stopped

### Reading Modes

- **Continuous Reading**: Automatically progresses through paragraphs and chapters
- **Paragraph Mode**: Stops after each paragraph (enable in settings)
- **Chapter Mode**: Stops at the end of each chapter

## Managing Voice Models

### Viewing Downloaded Models

1. Open Voice Model Manager
2. Downloaded models show a checkmark or "Downloaded" badge
3. View storage space used by each model

### Deleting Models

To free up disk space:
1. Open Voice Model Manager
2. Find the model you want to remove
3. Click the delete/trash icon
4. Confirm deletion
5. The model files are permanently removed

**Note**: You cannot delete the currently active voice model. Switch to a different voice first.

### Storage Management

- Each voice model uses 20-120MB depending on quality
- Models are stored in: `[AppData]/ireader/piper_models/`
- Total storage usage is displayed in the Voice Model Manager
- Consider keeping only the voices you regularly use

## Offline Usage

### How Offline Mode Works

Once you've downloaded voice models:
- TTS works completely offline
- No internet connection required
- No data sent to external servers
- Full privacy and control

### Preparing for Offline Use

1. Download all voice models you need while online
2. Test each voice to ensure it works
3. Disconnect from internet (optional)
4. TTS continues to function normally

## Persistence and Preferences

### Saved Settings

The following settings are automatically saved:
- Selected voice model
- Speech rate
- Volume level
- Pitch adjustment
- Reading mode preferences

### Settings Location

Settings are stored in your application preferences and persist across:
- Application restarts
- System reboots
- Application updates

### Resetting to Defaults

To reset TTS settings:
1. Open TTS Settings
2. Click "Reset to Defaults"
3. Confirm the reset
4. All settings return to factory defaults (voice selection is preserved)

## Tips for Best Experience

### Performance Tips

- **Use Medium Quality**: Best balance of quality and performance
- **Close Other Apps**: Free up system resources for smoother playback
- **SSD Storage**: Store models on SSD for faster loading

### Audio Quality Tips

- **Use Good Speakers/Headphones**: Neural TTS quality shines with good audio equipment
- **Adjust Speech Rate**: Find your comfortable listening speed
- **Try Different Voices**: Each voice has unique characteristics

### Reading Tips

- **Enable Word Highlighting**: Helps maintain focus and comprehension
- **Use Paragraph Mode**: For technical or complex content
- **Adjust Volume**: Set TTS volume lower than music/media for comfortable listening

## Platform-Specific Notes

### Windows

- Uses WASAPI for low-latency audio
- Models stored in: `%APPDATA%\ireader\piper_models\`
- Requires Windows 10 or later

### macOS

- Uses Core Audio for playback
- Models stored in: `~/Library/Application Support/ireader/piper_models/`
- Requires macOS 10.15 (Catalina) or later

### Linux

- Supports both ALSA and PulseAudio
- Models stored in: `~/.local/share/ireader/piper_models/`
- Requires compatible audio server

## Next Steps

- Try different voice models to find your favorite
- Experiment with speech rate settings
- Explore the troubleshooting guide if you encounter issues
- Check the main README for additional features

## Support

For issues or questions:
- See the [Troubleshooting Guide](TTS_Troubleshooting_Guide.md)
- Check the [GitHub Issues](https://github.com/IReaderorg/IReader/issues)
- Join the community discussions
