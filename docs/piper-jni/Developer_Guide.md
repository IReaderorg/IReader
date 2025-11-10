# Piper JNI Developer Guide

## Overview

This guide provides comprehensive information for developers working with the Piper JNI integration in IReader. The Piper JNI wrapper enables high-quality offline text-to-speech functionality across Windows, macOS, and Linux platforms.

## Architecture Overview

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Kotlin/Java Application                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │           DesktopTTSService (Kotlin)                   │ │
│  │  - Voice model management                              │ │
│  │  - Audio playback coordination                         │ │
│  │  - User preference handling                            │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                 │
│                            ▼                                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         PiperNative (JNI Interface - Kotlin)           │ │
│  │  - External native method declarations                 │ │
│  │  - Type conversion (String ↔ byte[])                   │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ JNI Boundary
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              piper_jni (C++ JNI Wrapper)                     │
│  - Voice instance management                                │
│  - Thread-safe synthesis                                    │
│  - Memory pool for audio buffers                            │
│  - Error handling and logging                               │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Piper Core Library                          │
│  - Phonemizer (espeak-ng)                                   │
│  - ONNX Runtime Inference                                   │
│  - Audio Output Generator                                   │
└─────────────────────────────────────────────────────────────┘
```

### Key Technologies

- **JNI (Java Native Interface)**: Bridge between Kotlin/Java and C++
- **CMake**: Cross-platform build system
- **Piper TTS**: Neural text-to-speech engine
- **ONNX Runtime**: Machine learning inference engine
- **espeak-ng**: Phonemization library


## Build Process

### Prerequisites

#### All Platforms

- **CMake** 3.15 or later
- **JDK** 17 or later (for JNI headers)
- **Git** for cloning dependencies

#### Windows

- **Visual Studio 2019 or later** with C++ development tools
- **Windows SDK** 10.0.19041.0 or later
- **PowerShell** 5.1 or later

#### macOS

- **Xcode** 12.0 or later with Command Line Tools
- **Homebrew** (recommended for dependencies)

#### Linux

- **GCC** 9.0 or later or **Clang** 10.0 or later
- **Build essentials**: `build-essential cmake git`
- **Audio libraries**: `libasound2-dev libpulse-dev`

### Building from Source

#### Step 1: Clone the Repository

```bash
git clone https://github.com/IReaderorg/IReader.git
cd IReader
```

#### Step 2: Install Dependencies

**Windows:**
```powershell
# Run the dependency setup script
.\scripts\setup_deps_windows.ps1
```

**macOS:**
```bash
# Install dependencies via Homebrew
brew install cmake openjdk@17

# Run the dependency setup script
./scripts/setup_deps_macos.sh
```

**Linux:**
```bash
# Install system dependencies
sudo apt-get update
sudo apt-get install -y build-essential cmake openjdk-17-jdk libasound2-dev libpulse-dev

# Run the dependency setup script
./scripts/setup_deps_linux.sh
```

#### Step 3: Build Native Libraries

**Windows:**
```powershell
# Build for Windows x64
.\native\scripts\build_windows.ps1 -Config Release

# Output: native\build\Release\piper_jni.dll
```

**macOS:**
```bash
# Build for current architecture (x64 or ARM64)
./native/scripts/build_macos.sh Release

# Build for specific architecture
./native/scripts/build_macos.sh Release x64
./native/scripts/build_macos.sh Release arm64

# Output: native/build/libpiper_jni.dylib
```

**Linux:**
```bash
# Build for Linux x64
./native/scripts/build_linux.sh Release

# Output: native/build/libpiper_jni.so
```

#### Step 4: Copy Libraries to Resources

```bash
# Libraries are automatically copied to:
# desktop/src/jvmMain/resources/native/
```

### Build Configuration

#### CMake Options

```cmake
# Enable debug symbols
cmake -DCMAKE_BUILD_TYPE=Debug ..

# Specify custom ONNX Runtime path
cmake -DONNXRUNTIME_DIR=/path/to/onnxruntime ..

# Enable verbose output
cmake -DCMAKE_VERBOSE_MAKEFILE=ON ..

# Specify target architecture (macOS)
cmake -DCMAKE_OSX_ARCHITECTURES=arm64 ..
```

#### Build Variants

- **Debug**: Includes debug symbols, no optimization
- **Release**: Optimized for performance, no debug symbols
- **RelWithDebInfo**: Optimized with debug symbols
- **MinSizeRel**: Optimized for size


## API Reference

### PiperNative (Kotlin)

The main JNI interface for interacting with Piper TTS.

#### Methods

##### initialize()

```kotlin
external fun initialize(modelPath: String, configPath: String): Long
```

Initializes a Piper voice instance.

**Parameters:**
- `modelPath`: Absolute path to the `.onnx` model file
- `configPath`: Absolute path to the `.json` config file

**Returns:** Instance handle (positive long value)

**Throws:**
- `PiperException.InitializationException`: If initialization fails
- `PiperException.ModelLoadException`: If model files cannot be loaded

**Example:**
```kotlin
val modelPath = "/path/to/en-us-amy-low.onnx"
val configPath = "/path/to/en-us-amy-low.onnx.json"
val instance = PiperNative.initialize(modelPath, configPath)
```

##### synthesize()

```kotlin
external fun synthesize(instance: Long, text: String): ByteArray
```

Synthesizes text to audio using the specified voice instance.

**Parameters:**
- `instance`: Voice instance handle from `initialize()`
- `text`: Text to synthesize (UTF-8 encoded)

**Returns:** Raw PCM audio data as byte array (16-bit signed, little-endian)

**Throws:**
- `PiperException.SynthesisException`: If synthesis fails
- `IllegalArgumentException`: If instance is invalid

**Example:**
```kotlin
val audio = PiperNative.synthesize(instance, "Hello, world!")
// audio contains raw PCM data at the voice's sample rate
```

##### setSpeechRate()

```kotlin
external fun setSpeechRate(instance: Long, rate: Float)
```

Adjusts the speech rate for the voice instance.

**Parameters:**
- `instance`: Voice instance handle
- `rate`: Speech rate multiplier (0.5 to 2.0, default 1.0)

**Throws:**
- `PiperException.InvalidParameterException`: If rate is out of range

**Example:**
```kotlin
PiperNative.setSpeechRate(instance, 1.5f) // 1.5x speed
```

##### setNoiseScale()

```kotlin
external fun setNoiseScale(instance: Long, noiseScale: Float)
```

Adjusts the noise scale (quality vs speed trade-off).

**Parameters:**
- `instance`: Voice instance handle
- `noiseScale`: Noise scale value (0.0 to 1.0, default 0.667)
  - Lower values: Faster, less variation
  - Higher values: Slower, more natural variation

**Example:**
```kotlin
PiperNative.setNoiseScale(instance, 0.8f) // More natural
```

##### getSampleRate()

```kotlin
external fun getSampleRate(instance: Long): Int
```

Gets the audio sample rate for the voice instance.

**Parameters:**
- `instance`: Voice instance handle

**Returns:** Sample rate in Hz (typically 22050 or 44100)

**Example:**
```kotlin
val sampleRate = PiperNative.getSampleRate(instance)
println("Sample rate: $sampleRate Hz")
```

##### shutdown()

```kotlin
external fun shutdown(instance: Long)
```

Releases resources for the voice instance.

**Parameters:**
- `instance`: Voice instance handle

**Note:** Always call this when done with a voice instance to prevent memory leaks.

**Example:**
```kotlin
try {
    val instance = PiperNative.initialize(modelPath, configPath)
    // Use the instance...
} finally {
    PiperNative.shutdown(instance)
}
```

##### getVersion()

```kotlin
external fun getVersion(): String
```

Gets the Piper JNI library version.

**Returns:** Version string (e.g., "1.0.0")

**Example:**
```kotlin
val version = PiperNative.getVersion()
println("Piper JNI version: $version")
```


### Exception Hierarchy

```kotlin
sealed class PiperException(message: String, cause: Throwable? = null) : Exception(message, cause)

class InitializationException(message: String, cause: Throwable? = null) : 
    PiperException("Failed to initialize Piper: $message", cause)

class ModelLoadException(modelPath: String, cause: Throwable? = null) : 
    PiperException("Failed to load voice model: $modelPath", cause)

class SynthesisException(text: String, cause: Throwable? = null) : 
    PiperException("Failed to synthesize text: ${text.take(50)}...", cause)

class InvalidParameterException(param: String, value: Any) : 
    PiperException("Invalid parameter $param: $value")

class ResourceException(message: String) : 
    PiperException("Resource error: $message")
```

### Voice Model Data Structures

#### VoiceModel

```kotlin
data class VoiceModel(
    val id: String,                    // Unique identifier
    val name: String,                  // Display name
    val language: String,              // ISO 639-1 code (e.g., "en", "es")
    val locale: String,                // Full locale (e.g., "en-US", "es-MX")
    val gender: VoiceGender,           // Male, Female, Neutral
    val quality: VoiceQuality,         // Low, Medium, High, Premium
    val sampleRate: Int,               // Audio sample rate (22050, 44100)
    val modelSize: Long,               // File size in bytes
    val downloadUrl: String,           // CDN URL for model file
    val configUrl: String,             // CDN URL for config file
    val checksum: String,              // SHA-256 checksum
    val license: String,               // License type
    val description: String,           // User-friendly description
    val tags: List<String>             // Searchable tags
)
```

#### SynthesisConfig

```kotlin
data class SynthesisConfig(
    val speechRate: Float = 1.0f,        // 0.5 - 2.0
    val noiseScale: Float = 0.667f,      // Quality vs speed
    val noiseW: Float = 0.8f,            // Variation in speech
    val lengthScale: Float = 1.0f,       // Phoneme duration
    val sentenceSilence: Float = 0.2f    // Pause between sentences
)
```

#### AudioData

```kotlin
data class AudioData(
    val samples: ByteArray,              // Raw PCM audio data
    val sampleRate: Int,                 // Samples per second
    val channels: Int = 1,               // Mono audio
    val bitsPerSample: Int = 16,         // 16-bit PCM
    val duration: Duration               // Total duration
)
```

## Integration Guide

### Basic Integration

#### Step 1: Initialize the Library

```kotlin
import ireader.domain.services.tts_service.piper.PiperNative
import ireader.domain.services.tts_service.piper.PiperInitializer

// Initialize the native library
val initialized = PiperInitializer.initialize()
if (!initialized) {
    throw RuntimeException("Failed to initialize Piper TTS")
}
```

#### Step 2: Load a Voice Model

```kotlin
val voiceModelPath = "/path/to/models/en-us-amy-low.onnx"
val voiceConfigPath = "/path/to/models/en-us-amy-low.onnx.json"

val instance = try {
    PiperNative.initialize(voiceModelPath, voiceConfigPath)
} catch (e: PiperException) {
    logger.error("Failed to load voice model", e)
    throw e
}
```

#### Step 3: Synthesize Text

```kotlin
val text = "Hello, this is a test of the Piper text-to-speech system."

val audioData = try {
    PiperNative.synthesize(instance, text)
} catch (e: PiperException.SynthesisException) {
    logger.error("Synthesis failed", e)
    throw e
}
```

#### Step 4: Play Audio

```kotlin
import javax.sound.sampled.*

val sampleRate = PiperNative.getSampleRate(instance)
val audioFormat = AudioFormat(
    sampleRate.toFloat(),
    16,  // bits per sample
    1,   // channels (mono)
    true,  // signed
    false  // little-endian
)

val dataLine = AudioSystem.getSourceDataLine(audioFormat)
dataLine.open(audioFormat)
dataLine.start()
dataLine.write(audioData, 0, audioData.size)
dataLine.drain()
dataLine.close()
```

#### Step 5: Cleanup

```kotlin
try {
    // Use the voice instance...
} finally {
    PiperNative.shutdown(instance)
}
```

### Advanced Integration

#### Managing Multiple Voice Instances

```kotlin
class VoiceManager {
    private val instances = mutableMapOf<String, Long>()
    
    fun loadVoice(voiceId: String, modelPath: String, configPath: String): Long {
        if (instances.containsKey(voiceId)) {
            return instances[voiceId]!!
        }
        
        val instance = PiperNative.initialize(modelPath, configPath)
        instances[voiceId] = instance
        return instance
    }
    
    fun getVoice(voiceId: String): Long? {
        return instances[voiceId]
    }
    
    fun unloadVoice(voiceId: String) {
        instances[voiceId]?.let { instance ->
            PiperNative.shutdown(instance)
            instances.remove(voiceId)
        }
    }
    
    fun unloadAll() {
        instances.values.forEach { instance ->
            PiperNative.shutdown(instance)
        }
        instances.clear()
    }
}
```

#### Streaming Synthesis for Long Texts

```kotlin
suspend fun synthesizeStreaming(
    instance: Long,
    text: String,
    chunkSize: Int = 500,
    onChunk: (ByteArray) -> Unit
) = withContext(Dispatchers.IO) {
    // Split text into sentences
    val sentences = text.split(Regex("[.!?]+"))
    
    // Process in chunks
    sentences.chunked(chunkSize).forEach { chunk ->
        val chunkText = chunk.joinToString(". ")
        val audio = PiperNative.synthesize(instance, chunkText)
        onChunk(audio)
        
        // Allow cancellation
        yield()
    }
}
```

#### Caching Voice Models

```kotlin
class VoiceModelCache(private val maxCacheSize: Int = 3) {
    private val cache = object : LinkedHashMap<String, Long>(maxCacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>): Boolean {
            if (size > maxCacheSize) {
                PiperNative.shutdown(eldest.value)
                return true
            }
            return false
        }
    }
    
    fun getOrLoad(voiceId: String, modelPath: String, configPath: String): Long {
        return cache.getOrPut(voiceId) {
            PiperNative.initialize(modelPath, configPath)
        }
    }
    
    fun clear() {
        cache.values.forEach { instance ->
            PiperNative.shutdown(instance)
        }
        cache.clear()
    }
}
```


## Troubleshooting Guide

### Common Build Issues

#### Issue: CMake Cannot Find JNI Headers

**Symptoms:**
```
CMake Error: Could not find JNI headers
```

**Solution:**
```bash
# Set JAVA_HOME environment variable
export JAVA_HOME=/path/to/jdk-17

# Or specify JNI include path explicitly
cmake -DJAVA_INCLUDE_PATH=$JAVA_HOME/include ..
```

#### Issue: ONNX Runtime Not Found

**Symptoms:**
```
CMake Error: Could not find ONNX Runtime
```

**Solution:**
```bash
# Download ONNX Runtime from official releases
# https://github.com/microsoft/onnxruntime/releases

# Extract and set ONNXRUNTIME_DIR
export ONNXRUNTIME_DIR=/path/to/onnxruntime
cmake -DONNXRUNTIME_DIR=$ONNXRUNTIME_DIR ..
```

#### Issue: Linker Errors on macOS

**Symptoms:**
```
ld: library not found for -lonnxruntime
```

**Solution:**
```bash
# Ensure ONNX Runtime dylib is in the correct location
cp /path/to/libonnxruntime.dylib native/build/

# Or add to library path
export DYLD_LIBRARY_PATH=/path/to/onnxruntime/lib:$DYLD_LIBRARY_PATH
```

#### Issue: Visual Studio Build Fails on Windows

**Symptoms:**
```
LINK : fatal error LNK1181: cannot open input file 'onnxruntime.lib'
```

**Solution:**
```powershell
# Ensure ONNX Runtime is in the correct location
# Copy onnxruntime.lib to native/build/Release/

# Or specify library path
cmake -DONNXRUNTIME_LIB_DIR="C:\path\to\onnxruntime\lib" ..
```

### Common Runtime Issues

#### Issue: UnsatisfiedLinkError

**Symptoms:**
```
java.lang.UnsatisfiedLinkError: no piper_jni in java.library.path
```

**Solution:**
1. Verify library is in resources directory:
   - Windows: `desktop/src/jvmMain/resources/native/windows-x64/piper_jni.dll`
   - macOS: `desktop/src/jvmMain/resources/native/macos-{arch}/libpiper_jni.dylib`
   - Linux: `desktop/src/jvmMain/resources/native/linux-x64/libpiper_jni.so`

2. Check library dependencies:
```bash
# Windows
dumpbin /dependents piper_jni.dll

# macOS
otool -L libpiper_jni.dylib

# Linux
ldd libpiper_jni.so
```

3. Ensure all dependencies are available

#### Issue: Model Load Failure

**Symptoms:**
```
PiperException$ModelLoadException: Failed to load voice model
```

**Solution:**
1. Verify model file exists and is readable
2. Check model file integrity (SHA-256 checksum)
3. Ensure config file (.json) is present alongside model
4. Verify sufficient memory is available

**Example:**
```kotlin
val modelFile = File(modelPath)
if (!modelFile.exists()) {
    throw FileNotFoundException("Model file not found: $modelPath")
}
if (!modelFile.canRead()) {
    throw IOException("Cannot read model file: $modelPath")
}
```

#### Issue: Synthesis Produces No Audio

**Symptoms:**
- `synthesize()` returns empty byte array
- No exceptions thrown

**Solution:**
1. Check if text is empty or contains only whitespace
2. Verify voice instance is valid
3. Check system resources (memory, CPU)
4. Enable debug logging to see detailed error messages

**Example:**
```kotlin
val text = inputText.trim()
if (text.isEmpty()) {
    logger.warn("Empty text provided for synthesis")
    return ByteArray(0)
}
```

#### Issue: Audio Playback Sounds Distorted

**Symptoms:**
- Audio plays but sounds garbled or distorted
- Incorrect pitch or speed

**Solution:**
1. Verify audio format matches voice sample rate:
```kotlin
val sampleRate = PiperNative.getSampleRate(instance)
val audioFormat = AudioFormat(
    sampleRate.toFloat(),
    16,  // Must be 16-bit
    1,   // Must be mono
    true,  // Must be signed
    false  // Must be little-endian
)
```

2. Check speech rate is within valid range (0.5 - 2.0)
3. Verify audio device supports the sample rate

#### Issue: Memory Leaks

**Symptoms:**
- Application memory usage grows over time
- OutOfMemoryError after extended use

**Solution:**
1. Always call `shutdown()` for each voice instance:
```kotlin
val instance = PiperNative.initialize(modelPath, configPath)
try {
    // Use instance...
} finally {
    PiperNative.shutdown(instance)
}
```

2. Implement proper resource management:
```kotlin
class VoiceInstance(modelPath: String, configPath: String) : AutoCloseable {
    private val handle = PiperNative.initialize(modelPath, configPath)
    
    fun synthesize(text: String) = PiperNative.synthesize(handle, text)
    
    override fun close() {
        PiperNative.shutdown(handle)
    }
}

// Usage
VoiceInstance(modelPath, configPath).use { voice ->
    val audio = voice.synthesize("Hello")
}
```

### Platform-Specific Issues

#### Windows

**Issue: DLL Load Failed**
```
Error loading native library: The specified module could not be found
```

**Solution:**
- Install Visual C++ Redistributable 2019 or later
- Ensure all dependent DLLs are in the same directory
- Check Windows Event Viewer for detailed error messages

#### macOS

**Issue: Library Cannot Be Opened**
```
"libpiper_jni.dylib" cannot be opened because the developer cannot be verified
```

**Solution:**
```bash
# Remove quarantine attribute
xattr -d com.apple.quarantine libpiper_jni.dylib

# Or allow in System Preferences > Security & Privacy
```

**Issue: Code Signature Invalid**
```
Code signature invalid
```

**Solution:**
```bash
# Re-sign the library
codesign --force --sign - libpiper_jni.dylib
```

#### Linux

**Issue: GLIBC Version Mismatch**
```
version `GLIBC_2.29' not found
```

**Solution:**
- Build on the oldest supported Linux distribution
- Or use Docker to build with specific glibc version
- Statically link glibc (not recommended)

**Issue: Audio Device Not Found**
```
ALSA lib pcm.c: Unknown PCM default
```

**Solution:**
```bash
# Install ALSA or PulseAudio
sudo apt-get install libasound2 pulseaudio

# Check audio devices
aplay -l
```

### Debugging Tips

#### Enable Verbose Logging

```kotlin
// Set system property before initializing
System.setProperty("piper.jni.debug", "true")

// Or use environment variable
// export PIPER_JNI_DEBUG=1
```

#### Capture Native Logs

```bash
# Redirect stderr to file
java -Djava.library.path=. YourApp 2> piper_debug.log
```

#### Profile Memory Usage

```kotlin
val runtime = Runtime.getRuntime()
val memBefore = runtime.totalMemory() - runtime.freeMemory()

// Perform operations...

val memAfter = runtime.totalMemory() - runtime.freeMemory()
val memUsed = (memAfter - memBefore) / (1024 * 1024)
println("Memory used: ${memUsed}MB")
```

#### Check Library Symbols

```bash
# Windows
dumpbin /exports piper_jni.dll

# macOS
nm -g libpiper_jni.dylib

# Linux
nm -D libpiper_jni.so | grep Java
```

### Getting Help

If you encounter issues not covered here:

1. Check the [GitHub Issues](https://github.com/IReaderorg/IReader/issues)
2. Search existing issues for similar problems
3. Create a new issue with:
   - Platform and version (OS, Java version)
   - Complete error message and stack trace
   - Steps to reproduce
   - Relevant code snippets
4. Join the community discussions

## Performance Optimization

### Best Practices

#### 1. Reuse Voice Instances

```kotlin
// Bad: Creating new instance for each synthesis
fun synthesize(text: String): ByteArray {
    val instance = PiperNative.initialize(modelPath, configPath)
    val audio = PiperNative.synthesize(instance, text)
    PiperNative.shutdown(instance)
    return audio
}

// Good: Reuse instance
class TTSService {
    private val instance = PiperNative.initialize(modelPath, configPath)
    
    fun synthesize(text: String): ByteArray {
        return PiperNative.synthesize(instance, text)
    }
    
    fun close() {
        PiperNative.shutdown(instance)
    }
}
```

#### 2. Use Appropriate Voice Quality

- **Low quality**: Faster synthesis, smaller models (~20-30MB)
- **Medium quality**: Balanced performance (~40-60MB)
- **High quality**: Best naturalness, slower (~80-120MB)

Choose based on your use case:
- Real-time applications: Low or Medium
- Audiobook production: High
- Background narration: Medium

#### 3. Chunk Long Texts

```kotlin
fun synthesizeLongText(instance: Long, text: String): List<ByteArray> {
    val chunks = text.chunked(500)  // 500 characters per chunk
    return chunks.map { chunk ->
        PiperNative.synthesize(instance, chunk)
    }
}
```

#### 4. Implement Caching

```kotlin
class AudioCache {
    private val cache = LruCache<String, ByteArray>(100)  // Cache 100 items
    
    fun getOrSynthesize(instance: Long, text: String): ByteArray {
        return cache.get(text) ?: run {
            val audio = PiperNative.synthesize(instance, text)
            cache.put(text, audio)
            audio
        }
    }
}
```

### Performance Targets

| Metric | Target | Notes |
|--------|--------|-------|
| Initialization | < 2 seconds | Per voice model |
| Short text (< 100 chars) | < 200ms | Synthesis time |
| Long text (1000 chars) | < 2 seconds | Streaming mode |
| Memory per model | < 500 MB | Loaded in memory |
| CPU usage | < 30% | During synthesis |

### Profiling

#### Measure Synthesis Performance

```kotlin
fun benchmarkSynthesis(instance: Long, text: String, iterations: Int = 100) {
    val durations = mutableListOf<Long>()
    
    repeat(iterations) {
        val start = System.nanoTime()
        PiperNative.synthesize(instance, text)
        val duration = (System.nanoTime() - start) / 1_000_000  // Convert to ms
        durations.add(duration)
    }
    
    val avg = durations.average()
    val min = durations.minOrNull() ?: 0
    val max = durations.maxOrNull() ?: 0
    val p95 = durations.sorted()[durations.size * 95 / 100]
    
    println("""
        Synthesis Performance:
        Average: ${avg}ms
        Min: ${min}ms
        Max: ${max}ms
        P95: ${p95}ms
    """.trimIndent())
}
```

## Contributing

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions focused and small

### Testing

- Write unit tests for new features
- Ensure all tests pass before submitting PR
- Add integration tests for complex scenarios
- Test on all supported platforms

### Pull Request Process

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Update documentation
6. Submit pull request
7. Address review feedback

## License

This project is licensed under the Mozilla Public License 2.0. See LICENSE file for details.

Third-party components:
- Piper TTS: MIT License
- ONNX Runtime: MIT License
- espeak-ng: GPL v3

## Additional Resources

- [Piper TTS Documentation](https://github.com/rhasspy/piper)
- [ONNX Runtime Documentation](https://onnxruntime.ai/)
- [JNI Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [CMake Documentation](https://cmake.org/documentation/)
