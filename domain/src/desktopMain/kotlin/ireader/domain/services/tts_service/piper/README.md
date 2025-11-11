# Piper TTS Integration

This package contains the Piper TTS integration using the **piper-jni** library for direct JNI access to Piper TTS engine.

## Architecture

We use the [GiviMAD/piper-jni](https://github.com/GiviMAD/piper-jni) library which provides:
- Pre-built native libraries for all platforms
- Direct JNI bindings to Piper TTS
- Automatic library loading
- Cross-platform support (Windows, macOS, Linux)

## Components

### 1. PiperJNISynthesizer
Wraps the piper-jni library with a clean Kotlin API.

**Key Methods:**
- `initialize(modelPath, configPath)` - Initialize Piper with a voice model
- `synthesize(text)` - Generate audio from text
- `setSpeechRate(rate)` - Adjust speech speed (0.5 - 2.0)
- `getSampleRate()` - Get audio sample rate
- `isInitialized()` - Check initialization status
- `shutdown()` - Release native resources

**Usage:**
```kotlin
val synthesizer = PiperJNISynthesizer()

// Initialize with voice model
val success = synthesizer.initialize(
    modelPath = "/path/to/model.onnx",
    configPath = "/path/to/config.json"
)

if (success) {
    // Synthesize text
    val audioData = synthesizer.synthesize("Hello, world!")
    
    // Adjust speech rate
    synthesizer.setSpeechRate(1.5f) // 1.5x speed
    
    // Cleanup when done
    synthesizer.shutdown()
}
```

### 2. PiperSpeechSynthesizer
High-level speech synthesizer implementing the `SpeechSynthesizer` interface.

**Usage:**
```kotlin
// Initialize Piper TTS
val result = PiperInitializer.initialize()

if (result.isSuccess) {
    // Piper is ready to use
    println("Piper TTS initialized successfully")
} else {
    // Fall back to simulation mode
    println("Piper TTS unavailable: ${result.exceptionOrNull()?.message}")
}

// Check availability
if (PiperInitializer.isAvailable()) {
    // Use Piper TTS
} else {
    // Use fallback
}

// Get detailed status for debugging
println(PiperInitializer.getStatusInfo())
```

## Dependencies

The piper-jni library is added as a Gradle dependency:

```kotlin
// domain/build.gradle.kts
desktopMain {
    dependencies {
        implementation("io.github.givimad:piper-jni:1.2.0-c0670df")
    }
}
```

**No manual library management needed!** The piper-jni library:
- Includes pre-built native libraries for all platforms
- Handles library extraction and loading automatically
- Supports Windows x64, macOS (Intel/ARM), and Linux x64

## Build Configuration

Simply sync Gradle and the piper-jni dependency will be downloaded:

```bash
./gradlew build
```

No additional build steps or native compilation required!

## Error Handling

The infrastructure is designed to fail gracefully:

1. **Missing Libraries**: If native libraries are not found, initialization returns a failure Result
2. **Unsupported Platform**: Throws `UnsupportedOperationException` with clear message
3. **Load Failures**: Throws `UnsatisfiedLinkError` with detailed error information

The application should catch these errors and fall back to simulation mode.

## Development

The piper-jni library makes development simple:

1. **Add dependency** - Already done in `domain/build.gradle.kts`
2. **Sync Gradle** - Libraries download automatically
3. **Use the API** - No manual setup required

```kotlin
// Just use it!
val synthesizer = PiperJNISynthesizer()
val success = synthesizer.initialize(modelPath, configPath)
```

## Testing

Test your integration:

```kotlin
@Test
fun testPiperSynthesis() {
    val synthesizer = PiperJNISynthesizer()
    
    // Initialize with a test model
    val success = synthesizer.initialize(
        modelPath = "path/to/test/model.onnx",
        configPath = "path/to/test/config.json"
    )
    
    assertTrue(success)
    assertTrue(synthesizer.isInitialized())
    
    // Test synthesis
    val audio = synthesizer.synthesize("Test text")
    assertTrue(audio.isNotEmpty())
    
    // Cleanup
    synthesizer.shutdown()
}
```

## Integration with TTS Service

The TTS service should initialize Piper during startup:

```kotlin
class DesktopTTSService {
    private var piperAvailable = false
    
    suspend fun initialize() {
        val result = PiperInitializer.initialize()
        piperAvailable = result.isSuccess
        
        if (piperAvailable) {
            // Initialize Piper components
            loadVoiceModel()
        } else {
            // Use simulation mode
            logger.warn("Piper TTS unavailable: ${result.exceptionOrNull()?.message}")
        }
    }
    
    private suspend fun readText() {
        if (piperAvailable) {
            // Use Piper synthesis
        } else {
            // Use simulation
        }
    }
}
```

## Troubleshooting

### Synthesis Fails
**Symptom**: `synthesize()` returns empty byte array

**Solutions**:
1. Verify voice model is downloaded and paths are correct
2. Check model file format (.onnx) and config (.json)
3. Ensure model is compatible with piper-jni version
4. Check logs for detailed error messages

### Initialization Fails
**Symptom**: `initialize()` returns false

**Solutions**:
1. Verify model and config files exist
2. Check file permissions
3. Ensure model is a valid Piper ONNX model
4. Try with a different voice model

### Performance Issues
**Symptom**: Slow synthesis or high memory usage

**Solutions**:
1. Use appropriate speech rate (0.5 - 2.0)
2. Synthesize shorter text chunks
3. Ensure proper cleanup with `shutdown()`
4. Monitor memory usage and adjust JVM heap if needed

### Platform-Specific Issues
**Symptom**: Works on one platform but not another

**Solutions**:
1. Verify piper-jni supports your platform
2. Check Java version compatibility (Java 11+)
3. Update to latest piper-jni version
4. Report issue to piper-jni repository

## Performance

Expected performance with piper-jni:

- **Initialization**: ~500ms - 2s (one-time per model)
- **Short text (100 chars)**: ~50-100ms
- **Long text (1000 chars)**: ~200-500ms
- **Memory usage**: ~200-500MB per loaded model

Much faster than subprocess approach (~100-200ms overhead eliminated)!

## Future Enhancements

Potential improvements:

1. **Streaming Synthesis**: Real-time audio generation for long texts
2. **Voice Caching**: Cache frequently used phrases
3. **Batch Processing**: Synthesize multiple texts efficiently
4. **Advanced Parameters**: Expose more Piper configuration options
5. **Model Preloading**: Load models in background on startup

## Migration from Custom JNI

If you're migrating from the old custom JNI implementation:

1. ✅ **Removed**: `/native` directory with C++ code
2. ✅ **Removed**: `PiperNative.kt`, `NativeLibraryLoader.kt`, `PiperSubprocessSynthesizer.kt`
3. ✅ **Added**: `PiperJNISynthesizer.kt` using piper-jni library
4. ✅ **Updated**: `PiperSpeechSynthesizer.kt` to use JNI synthesizer
5. ✅ **Simplified**: No manual library management needed

See `PIPER_JNI_MIGRATION.md` for detailed migration notes.

## License

This integration code is part of IReader and follows the project's license.

**Dependencies:**
- **piper-jni**: Apache License 2.0
- **Piper TTS** (via piper-jni): MIT License (pre-1.3.0 version)
- **ONNX Runtime**: MIT License

All dependencies use permissive licenses compatible with commercial use.

**Note**: Piper 1.3.0+ uses GPL-3.0 license, but piper-jni wraps the older MIT-licensed version, avoiding GPL concerns.
