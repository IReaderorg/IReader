# Desktop TTS Quick Start Guide

## 5-Minute Integration

### Step 1: Initialize the Service

In your desktop main or DI setup:

```kotlin
// In your Koin module or initialization code
val desktopTTSService = DesktopTTSService()
desktopTTSService.initialize()
```

### Step 2: Add to Reader Screen

In your desktop reader screen composable:

```kotlin
@Composable
fun DesktopReaderScreen(
    viewModel: ReaderScreenViewModel,
    ttsService: DesktopTTSService
) {
    val state = viewModel.state
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Your existing reader content
        ReaderContent(viewModel)
        
        // Add TTS controls at the bottom
        if (state.stateChapter != null) {
            DesktopTTSControls(
                ttsService = ttsService,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
    
    // Initialize TTS with current chapter when it changes
    LaunchedEffect(state.stateChapter) {
        state.stateChapter?.let { chapter ->
            state.stateBook?.let { book ->
                ttsService.startReading(
                    bookId = book.id,
                    chapterId = chapter.id
                )
            }
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            ttsService.shutdown()
        }
    }
}
```

### Step 3: Add TTS Button (Optional)

Add a TTS button to your reader toolbar:

```kotlin
IconButton(
    onClick = {
        if (ttsService.state.isPlaying) {
            ttsService.startService(DesktopTTSService.ACTION_PAUSE)
        } else {
            ttsService.startService(DesktopTTSService.ACTION_PLAY)
        }
    }
) {
    Icon(
        imageVector = if (ttsService.state.isPlaying) 
            Icons.Default.Pause 
        else 
            Icons.Default.PlayArrow,
        contentDescription = "Text-to-Speech"
    )
}
```

### Step 4: Test It

1. Open a chapter in the reader
2. Click the play button
3. Watch the progress indicator advance
4. Try pause, skip, and navigation controls

## That's It!

Your desktop TTS is now working. The service will:
- ✅ Automatically load chapters
- ✅ Progress through paragraphs
- ✅ Handle chapter navigation
- ✅ Respect user preferences
- ✅ Show progress and controls

## Customization

### Adjust Reading Speed

The default is 150 words per minute. To change:

```kotlin
// In DesktopTTSService.kt, modify the readText() function:
val wordsPerMinute = 200 * state.speechSpeed // Faster reading
```

### Change UI Position

```kotlin
// Place controls anywhere:
Box(modifier = Modifier.fillMaxSize()) {
    ReaderContent()
    
    // Top-right corner
    DesktopTTSIndicator(
        ttsService = ttsService,
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
    )
}
```

### Hide/Show Controls

```kotlin
var showTTSControls by remember { mutableStateOf(false) }

// Toggle button
IconButton(onClick = { showTTSControls = !showTTSControls }) {
    Icon(Icons.Default.Settings, "TTS Settings")
}

// Conditional display
if (showTTSControls) {
    DesktopTTSControls(ttsService)
}
```

## Troubleshooting

**TTS doesn't start?**
- Check if chapter is loaded: `ttsService.state.ttsChapter != null`
- Verify service is initialized: `ttsService.initialize()`

**Reading too fast/slow?**
- Adjust speech rate in preferences
- Or modify `wordsPerMinute` in the service

**Controls not responding?**
- Check if service is properly injected
- Verify state updates are being observed

## Next Steps

- Add keyboard shortcuts for TTS controls
- Integrate with system media keys
- Add voice selection (when real TTS is added)
- Implement word highlighting

For detailed documentation, see:
- `DESKTOP_TTS_IMPLEMENTATION.md` - Full implementation details
- `domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/README.md` - API reference
