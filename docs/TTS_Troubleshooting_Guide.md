# Piper TTS Troubleshooting Guide

## Overview

This guide helps you resolve common issues with Piper TTS in IReader Desktop. Most problems can be solved by following the steps below.

## Quick Diagnostics

Before diving into specific issues, try these quick checks:

1. **Verify Voice Model**: Ensure you have at least one voice model downloaded
2. **Check Audio Device**: Confirm your speakers/headphones are connected and working
3. **Restart Application**: Close and reopen IReader Desktop
4. **Check System Volume**: Ensure system audio is not muted

## Common Issues and Solutions

### Issue: No Sound During Playback

**Symptoms:**
- TTS controls show playback is active
- Progress indicator moves
- No audio is heard

**Solutions:**

1. **Check System Audio**
   - Verify system volume is not muted
   - Test audio with another application (music player, video)
   - Ensure correct audio output device is selected in system settings

2. **Check TTS Volume**
   - Open TTS controls
   - Verify volume slider is not at 0%
   - Increase volume to 80% and test

3. **Verify Audio Device**
   - Go to system audio settings
   - Confirm the correct output device is selected
   - Try switching to a different audio device
   - Reconnect headphones/speakers if using external devices

4. **Restart Audio Service** (Windows)
   ```
   - Open Services (services.msc)
   - Find "Windows Audio"
   - Right-click → Restart
   ```

5. **Check PulseAudio** (Linux)
   ```bash
   pulseaudio --check
   pulseaudio --start
   ```

**Still Not Working?**
- See "Fallback Mode" section below
- Check application logs for audio device errors

---

### Issue: Voice Model Won't Download

**Symptoms:**
- Download starts but fails
- "Download failed" error message
- Progress bar stuck at 0%

**Solutions:**

1. **Check Internet Connection**
   - Verify you're connected to the internet
   - Test by opening a web browser
   - Try downloading a different model

2. **Check Disk Space**
   - Ensure you have at least 150MB free space
   - Voice models range from 20-120MB each
   - Check available space:
     - Windows: Right-click drive → Properties
     - macOS: Apple menu → About This Mac → Storage
     - Linux: `df -h`

3. **Clear Partial Downloads**
   - Navigate to models directory:
     - Windows: `%APPDATA%\ireader\piper_models\`
     - macOS: `~/Library/Application Support/ireader/piper_models/`
     - Linux: `~/.local/share/ireader/piper_models/`
   - Delete any incomplete model folders
   - Retry download

4. **Check Firewall/Antivirus**
   - Temporarily disable firewall/antivirus
   - Retry download
   - If successful, add IReader to firewall exceptions
   - Re-enable firewall/antivirus

5. **Try Different Network**
   - Switch to a different WiFi network
   - Try mobile hotspot
   - Some networks block large downloads

**Error: "Insufficient Storage"**
- Free up disk space (at least 150MB)
- Delete unused voice models
- Move files to external storage

---

### Issue: Voice Model Failed to Load

**Symptoms:**
- "Model load error" notification
- TTS falls back to simulation mode
- Selected voice doesn't work

**Solutions:**

1. **Verify Model Files**
   - Open Voice Model Manager
   - Check if model shows as "Downloaded"
   - If not, re-download the model

2. **Check File Integrity**
   - Navigate to models directory (see paths above)
   - Open the model folder (e.g., `en_US-lessac-medium`)
   - Verify two files exist:
     - `model.onnx` (should be 20-120MB)
     - `config.json` (should be a few KB)
   - If files are missing or 0 bytes, delete folder and re-download

3. **Delete and Re-download**
   - In Voice Model Manager, delete the problematic model
   - Download it again
   - Select the newly downloaded model

4. **Try a Different Model**
   - Download and select a different voice model
   - If other models work, the original model file may be corrupted

5. **Check Permissions**
   - Ensure IReader has read access to the models directory
   - On macOS/Linux: `chmod -R 755 [models_directory]`

**Still Not Working?**
- Application will automatically use fallback mode
- See "Understanding Fallback Mode" section

---

### Issue: Playback is Choppy or Stuttering

**Symptoms:**
- Audio cuts in and out
- Robotic or distorted sound
- Frequent pauses during playback

**Solutions:**

1. **Close Other Applications**
   - Close resource-intensive programs
   - Free up CPU and memory
   - Especially close other audio/video applications

2. **Lower Voice Quality**
   - Download and use a "Low" quality model instead of "High"
   - Low quality models process faster
   - Still provide good audio quality

3. **Adjust Buffer Settings** (Advanced)
   - Increase audio buffer size in settings
   - Trade-off: Slightly higher latency but smoother playback

4. **Check System Resources**
   - Open Task Manager (Windows) / Activity Monitor (macOS) / System Monitor (Linux)
   - Check CPU usage (should be below 80%)
   - Check RAM usage (should have at least 500MB free)
   - Close unnecessary background processes

5. **Update Audio Drivers**
   - Windows: Update via Device Manager
   - macOS: Update via System Preferences → Software Update
   - Linux: Update via package manager

6. **Disable Hardware Acceleration** (If Available)
   - Some systems have issues with GPU acceleration
   - Try disabling in TTS settings

---

### Issue: Word Highlighting Not Working

**Symptoms:**
- Audio plays correctly
- No words are highlighted in the text
- Highlight appears in wrong location

**Solutions:**

1. **Enable Word Highlighting**
   - Check TTS settings
   - Ensure "Word Highlighting" is enabled
   - Toggle off and on to reset

2. **Scroll to Current Position**
   - The highlight may be off-screen
   - Click "Jump to Current Position" in TTS controls
   - Or manually scroll to the playing paragraph

3. **Restart Playback**
   - Stop current playback
   - Start playing again
   - Highlighting should reinitialize

4. **Check Text Format**
   - Some special characters may interfere
   - Try a different chapter or book
   - Report persistent issues with specific books

---

### Issue: TTS Controls Not Responding

**Symptoms:**
- Buttons don't respond to clicks
- Playback won't start/stop
- UI appears frozen

**Solutions:**

1. **Wait for Processing**
   - Large chapters may take a few seconds to initialize
   - Look for loading indicator
   - Be patient on first playback of a chapter

2. **Stop and Restart**
   - Click Stop button
   - Wait 2 seconds
   - Click Play again

3. **Restart Application**
   - Save your reading position
   - Close IReader completely
   - Reopen and try again

4. **Check for Updates**
   - Ensure you're running the latest version
   - Update if available
   - Restart after updating

---

### Issue: Playback Speed Too Fast/Slow

**Symptoms:**
- Voice speaks too quickly or slowly
- Speed slider doesn't seem to work
- Speed resets unexpectedly

**Solutions:**

1. **Adjust Speech Rate**
   - Open TTS controls
   - Use the speed slider
   - Range: 0.5x (slow) to 2.0x (fast)
   - Default: 1.0x

2. **Reset to Default**
   - Click "Reset" next to speed slider
   - Returns to 1.0x normal speed

3. **Check if Setting Persists**
   - Adjust speed
   - Stop and restart playback
   - If speed resets, there may be a preferences issue
   - Try "Reset to Defaults" in settings

---

### Issue: TTS Stops at End of Paragraph

**Symptoms:**
- Playback stops after each paragraph
- Doesn't continue to next paragraph automatically
- Have to manually click play repeatedly

**Solutions:**

1. **Check Reading Mode**
   - Open TTS settings
   - Ensure "Continuous Reading" is enabled
   - Disable "Paragraph Mode" if enabled

2. **Verify Auto-Advance**
   - Check "Auto-advance to next paragraph" setting
   - Enable if disabled

3. **Check Sleep Timer**
   - Ensure sleep timer is not set
   - Or set to a longer duration

---

## Understanding Fallback Mode

### What is Fallback Mode?

When Piper TTS cannot initialize or encounters critical errors, IReader automatically switches to "Fallback Mode" (simulation mode). This ensures you can continue using the application even if TTS is not working.

### How to Identify Fallback Mode

- Notification: "TTS running in simulation mode"
- No actual audio playback
- Text highlighting still works
- Paragraph progression continues

### Why Fallback Mode Activates

Common reasons:
1. **No Voice Models Downloaded**: Download at least one model
2. **Model Load Failure**: Voice model files are corrupted
3. **Native Library Error**: Piper library failed to initialize
4. **Audio Device Unavailable**: No working audio output device
5. **Insufficient Resources**: Not enough memory or CPU

### Exiting Fallback Mode

1. **Resolve the Underlying Issue**
   - Download a voice model if none exist
   - Fix audio device problems
   - Free up system resources

2. **Restart TTS Service**
   - Stop playback completely
   - Close and reopen the book
   - Or restart the application

3. **Verify Normal Mode**
   - Start playback
   - Listen for actual audio
   - Check for "TTS Active" indicator (not "Simulation Mode")

### When to Use Fallback Mode

Fallback mode is useful when:
- You want to test TTS features without audio
- Audio device is temporarily unavailable
- You're in a quiet environment but want to track reading progress
- Troubleshooting other TTS issues

---

## Error Messages and Meanings

### "Model load error: File not found"
- **Cause**: Voice model files are missing
- **Solution**: Re-download the voice model

### "Audio playback error: Device unavailable"
- **Cause**: No audio output device detected
- **Solution**: Connect speakers/headphones, check system audio settings

### "Synthesis error: Out of memory"
- **Cause**: Insufficient RAM for processing
- **Solution**: Close other applications, use lower quality model

### "Download failed: Network error"
- **Cause**: Internet connection issue
- **Solution**: Check network connection, retry download

### "Insufficient storage: Need XXX MB"
- **Cause**: Not enough disk space
- **Solution**: Free up disk space, delete unused models

### "Model verification failed"
- **Cause**: Downloaded model file is corrupted
- **Solution**: Delete and re-download the model

---

## Platform-Specific Issues

### Windows

**Issue: "DLL not found" error**
- Ensure Visual C++ Redistributable is installed
- Download from Microsoft website
- Restart application after installation

**Issue: Audio crackling on Windows**
- Update audio drivers via Device Manager
- Try switching audio format in Windows Sound settings
- Disable audio enhancements

### macOS

**Issue: "App cannot be opened" on first launch**
- Right-click app → Open (instead of double-clicking)
- Or: System Preferences → Security & Privacy → Allow

**Issue: No audio on macOS**
- Check System Preferences → Sound → Output
- Grant audio permissions if prompted
- Restart Core Audio: `sudo killall coreaudiod`

### Linux

**Issue: "Audio server not found"**
- Install PulseAudio: `sudo apt install pulseaudio`
- Or configure ALSA properly
- Start PulseAudio: `pulseaudio --start`

**Issue: Permission denied errors**
- Check file permissions in models directory
- Run: `chmod -R 755 ~/.local/share/ireader/piper_models/`

---

## Advanced Troubleshooting

### Checking Application Logs

Logs contain detailed error information:

**Location:**
- Windows: `%APPDATA%\ireader\logs\`
- macOS: `~/Library/Logs/ireader/`
- Linux: `~/.local/share/ireader/logs/`

**What to Look For:**
- Lines containing "ERROR" or "WARN"
- TTS-related messages
- Audio device initialization messages
- Model loading errors

### Clearing Cache and Preferences

If all else fails, reset TTS completely:

1. **Backup Your Models** (Optional)
   - Copy models directory to safe location
   - Prevents re-downloading

2. **Clear TTS Preferences**
   - Close IReader
   - Delete preferences file (location varies by platform)
   - Restart IReader
   - Reconfigure TTS settings

3. **Reinstall Voice Models**
   - Delete all models
   - Download fresh copies
   - Test with a single model first

### Testing with Minimal Configuration

1. Download only one small voice model (Low quality)
2. Test with a short chapter (1-2 paragraphs)
3. Use default settings (no customization)
4. If this works, gradually add complexity

---

## Getting Help

### Before Requesting Support

Gather this information:
1. Operating system and version
2. IReader version
3. Voice model being used
4. Error messages (exact text)
5. Steps to reproduce the issue
6. Relevant log entries

### Where to Get Help

- **GitHub Issues**: [IReader Issues](https://github.com/IReaderorg/IReader/issues)
- **Community Forum**: Check for similar issues
- **Documentation**: Review setup guide and README

### Reporting Bugs

When reporting TTS bugs, include:
- System information
- Voice model details
- Error messages
- Log excerpts
- Steps to reproduce
- Expected vs actual behavior

---

## Prevention Tips

### Maintain Healthy TTS Setup

1. **Keep Models Updated**
   - Check for model updates periodically
   - Re-download if new versions available

2. **Monitor Disk Space**
   - Keep at least 500MB free
   - Delete unused voice models

3. **Regular Application Updates**
   - Update IReader when new versions release
   - Check changelog for TTS improvements

4. **Backup Your Configuration**
   - Note your preferred settings
   - Makes recovery easier if reset needed

### Best Practices

- Test new voice models before deleting old ones
- Don't interrupt model downloads
- Close IReader properly (don't force quit)
- Keep audio drivers updated
- Maintain adequate system resources

---

## Still Having Issues?

If you've tried everything in this guide and TTS still isn't working:

1. **Use Fallback Mode** temporarily to continue reading
2. **Report the Issue** on GitHub with detailed information
3. **Check for Known Issues** in the project's issue tracker
4. **Wait for Updates** - fixes may be in development

Remember: Fallback mode ensures you can always continue reading, even if audio synthesis isn't working.
