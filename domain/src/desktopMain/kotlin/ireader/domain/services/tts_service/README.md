# Desktop TTS Implementation

## Overview

The Desktop TTS (Text-to-Speech) implementation provides reading functionality for the desktop version of IReader. It follows the same architecture as the Android TTS service but uses a simulated reading approach.

## Architecture

### Components

1. **TTSState (Common)** - `domain/src/commonMain/kotlin/ireader/domain/services/tts_service/TTSState.kt`
   - Common interface for TTS state across platforms
   - Defines core properties like reading position, book/chapter info, settings

2. **DesktopTTSState** - `domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/DesktopTTSState.kt`
   - Desktop-specific implementation of TTSState
   - Uses Compose state management for reactive UI updates

3. **DesktopTTSService** - `domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/DesktopTTSService.kt`
   - Main service that handles TTS operations
   - Simulates reading by calculating time based on word count and speech rate
   - Manages chapter navigation and paragraph progression

4. **DesktopTTSControls** - `presentation/src/desktopMain/kotlin/ireader/presentation/ui/reader/components/DesktopTTSControls.kt`
   - UI component for TTS controls
   - Provides play/pause, skip, and settings controls

## How It Works

### Reading Simulation

Since desktop doesn't have native TTS like Android, the service simulates reading:

1. **Word Count Calculation**: Counts words in each paragraph
2. **Time Estimation**: Calculates reading time based on:
   - Base reading speed: 150 words per minute
   - Speech rate multiplier (0.5x to 2.0x)
   - Formula: `readingTimeMs = (wordCount / wordsPerMinute) * 60 * 1000`

3. **Paragraph Progression**: After the calculated time, moves to next paragraph

### Features

- ✅ **Play/Pause/Stop**: Control reading state
- ✅ **Chapter Navigation**: Skip to next/previous chapter
- ✅ **Paragraph Navigation**: Skip to next/previous paragraph
- ✅ **Auto-Next Chapter**: Automatically continue to next chapter
- ✅ **Speech Rate Control**: Adjust reading speed (0.5x - 2.0x)
- ✅ **Sleep Timer**: Stop reading after specified time
- ✅ **Progress Tracking**: Shows current paragraph and progress
- ✅ **Preference Sync**: Reads settings from ReaderPreferences

## Usage

### Initialization

```kotlin
val ttsService = DesktopTTSService()
ttsService.initialize()
```

### Start Reading

```kotlin
// Start reading a specific chapter
ttsService.startReading(bookId = 123, chapterId = 456)
```

### Control Playback

```kotlin
// Play
ttsService.startService(DesktopTTSService.ACTION_PLAY)

// Pause
ttsService.startService(DesktopTTSService.ACTION_PAUSE)

// Stop
ttsService.startService(DesktopTTSService.ACTION_STOP)

// Next paragraph
ttsService.startService(DesktopTTSService.ACTION_NEXT_PAR)

// Previous paragraph
ttsService.startService(DesktopTTSService.ACTION_PREV_PAR)

// Next chapter
ttsService.startService(DesktopTTSService.ACTION_SKIP_NEXT)

// Previous chapter
ttsService.startService(DesktopTTSService.ACTION_SKIP_PREV)
```

### UI Integration

```kotlin
@Composable
fun ReaderScreen(ttsService: DesktopTTSService) {
    Column {
        // Reader content
        ReaderText(...)
        
        // TTS Controls
        DesktopTTSControls(ttsService = ttsService)
        
        // Or use compact indicator
        DesktopTTSIndicator(ttsService = ttsService)
    }
}
```

### Cleanup

```kotlin
// When done
ttsService.shutdown()
```

## State Management

The service uses Compose state for reactive updates:

```kotlin
val state = ttsService.state

// Access state properties
val isPlaying = state.isPlaying
val currentParagraph = state.currentReadingParagraph
val currentChapter = state.ttsChapter
val content = state.ttsContent?.value
```

## Configuration

Settings are automatically synced from preferences:

- **Speech Rate**: `readerPreferences.speechRate()`
- **Speech Pitch**: `readerPreferences.speechPitch()` (not used in simulation)
- **Auto-Next Chapter**: `readerPreferences.readerAutoNext()`
- **Sleep Time**: `readerPreferences.sleepTime()`
- **Sleep Mode**: `readerPreferences.sleepMode()`

## Future Enhancements

### Real TTS Integration

For production, consider integrating with actual TTS engines:

1. **FreeTTS** - Java-based TTS engine
   ```kotlin
   // Example integration
   val voice = VoiceManager.getInstance().getVoice("kevin16")
   voice.allocate()
   voice.speak(text)
   ```

2. **MaryTTS** - Open-source TTS platform
   ```kotlin
   val marytts = LocalMaryInterface()
   val audio = marytts.generateAudio(text)
   ```

3. **System TTS** - Use OS-level TTS
   - Windows: SAPI (Speech API)
   - macOS: NSSpeechSynthesizer
   - Linux: espeak/festival

### Word-by-Word Highlighting

To add word highlighting like Android:

```kotlin
private suspend fun readTextWithHighlight() {
    val words = text.split("\\s+".toRegex())
    
    words.forEachIndexed { index, word ->
        // Emit word boundary event
        _wordBoundaryFlow.emit(
            WordBoundary(
                wordIndex = index,
                startOffset = calculateOffset(index),
                endOffset = calculateOffset(index) + word.length,
                word = word
            )
        )
        
        // Calculate delay per word
        val delayMs = calculateWordDelay(word, speechRate)
        delay(delayMs)
    }
}
```

### Audio Playback

For actual audio output:

```kotlin
import javax.sound.sampled.*

class AudioTTSService {
    private var clip: Clip? = null
    
    fun playAudio(audioData: ByteArray) {
        val audioInputStream = AudioInputStream(
            ByteArrayInputStream(audioData),
            audioFormat,
            audioData.size.toLong()
        )
        
        clip = AudioSystem.getClip()
        clip?.open(audioInputStream)
        clip?.start()
    }
}
```

## Comparison with Android TTS

| Feature | Android | Desktop |
|---------|---------|---------|
| TTS Engine | Native TextToSpeech | Simulated |
| Word Boundaries | Real-time callbacks | Estimated |
| Voice Selection | Multiple voices | N/A |
| Audio Output | System TTS | Silent simulation |
| Media Controls | MediaSession | UI controls only |
| Notifications | Yes | No |
| Background Play | Yes | No |

## Troubleshooting

### Service Not Starting

**Problem**: TTS doesn't start when play is pressed

**Solution**:
- Verify service is initialized: `ttsService.initialize()`
- Check if chapter is loaded: `state.ttsChapter != null`
- Verify content exists: `state.ttsContent?.value?.isNotEmpty()`

### Reading Too Fast/Slow

**Problem**: Reading speed doesn't match expectations

**Solution**:
- Adjust base words per minute in `readText()` function
- Current: 150 WPM * speechRate
- Modify: `val wordsPerMinute = YOUR_VALUE * state.speechSpeed`

### Chapter Not Loading

**Problem**: Chapter content is empty

**Solution**:
- Check network connection for remote chapters
- Verify chapter is downloaded for local chapters
- Check logs for loading errors

## Testing

### Manual Testing

```kotlin
fun main() {
    val ttsService = DesktopTTSService()
    ttsService.initialize()
    
    // Test with sample data
    runBlocking {
        ttsService.startReading(bookId = 1, chapterId = 1)
        delay(5000) // Let it read for 5 seconds
        ttsService.startService(DesktopTTSService.ACTION_PAUSE)
        delay(2000)
        ttsService.startService(DesktopTTSService.ACTION_PLAY)
    }
}
```

### Unit Testing

```kotlin
@Test
fun testReadingProgression() = runTest {
    val service = DesktopTTSService()
    service.initialize()
    
    // Setup test data
    service.state.ttsChapter = testChapter
    service.state.currentReadingParagraph = 0
    
    // Start reading
    service.startService(DesktopTTSService.ACTION_PLAY)
    
    // Wait for progression
    delay(1000)
    
    // Verify paragraph advanced
    assertTrue(service.state.currentReadingParagraph > 0)
}
```

## Performance

- **Memory**: Minimal, only holds current chapter content
- **CPU**: Low, uses coroutine delays instead of active processing
- **Battery**: Negligible impact on laptops

## Accessibility

The TTS feature improves accessibility for:
- Users with visual impairments
- Users with reading difficulties
- Users learning a new language
- Users who prefer audio content

## License

Same as IReader project license.
