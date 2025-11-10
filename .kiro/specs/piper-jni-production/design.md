# Design Document: Production-Ready Piper JNI Integration

## Overview

This design document outlines the architecture and implementation strategy for building production-ready JNI wrapper libraries for Piper TTS. The system will provide native integration across Windows, macOS, and Linux platforms, enabling high-quality offline text-to-speech for users worldwide.

### Goals

1. **Cross-Platform Excellence**: Seamless operation on Windows, macOS (Intel/ARM), and Linux
2. **World-Class Performance**: Sub-200ms synthesis latency for short texts
3. **Global Language Support**: 20+ languages with natural-sounding voices
4. **Developer-Friendly**: Automated builds requiring minimal C++ expertise
5. **Production-Ready**: Comprehensive testing, error handling, and monitoring
6. **User Delight**: Intuitive interface with accessibility features

### Non-Goals

- Real-time voice cloning or training
- Cloud-based TTS services
- Speech recognition or voice input
- Video/animation lip-sync

## Architecture

### High-Level Architecture

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
│  ┌────────────────────────────────────────────────────────┐ │
│  │  JNI Function Implementations                          │ │
│  │  - Java_*_initialize()                                 │ │
│  │  - Java_*_synthesize()                                 │ │
│  │  - Java_*_setSpeechRate()                              │ │
│  │  - Java_*_shutdown()                                   │ │
│  └────────────────────────────────────────────────────────┘ │
│                            │                                 │
│                            ▼                                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         Piper C++ Wrapper Layer                        │ │
│  │  - Voice model lifecycle management                    │ │
│  │  - Thread-safe synthesis queue                         │ │
│  │  - Memory pool for audio buffers                       │ │
│  │  - Error handling and logging                          │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Piper Core Library                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Phonemizer  │  │ ONNX Runtime │  │ Audio Output │      │
│  │  (espeak-ng) │  │   Inference  │  │   Generator  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```


### Component Breakdown

#### 1. Build System Architecture

**Technology Stack:**
- CMake 3.15+ for cross-platform builds
- Docker for reproducible cross-compilation
- PowerShell/Bash scripts for automation
- GitHub Actions for CI/CD

**Directory Structure:**
```
piper-jni/
├── CMakeLists.txt              # Root build configuration
├── cmake/
│   ├── FindPiper.cmake         # Locate Piper library
│   ├── FindONNXRuntime.cmake   # Locate ONNX Runtime
│   └── Platform.cmake          # Platform-specific settings
├── src/
│   ├── jni/
│   │   ├── piper_jni.cpp       # Main JNI implementations
│   │   ├── piper_jni.h         # JNI header declarations
│   │   ├── voice_manager.cpp   # Voice model lifecycle
│   │   ├── audio_buffer.cpp    # Audio buffer management
│   │   └── error_handler.cpp   # Error handling utilities
│   └── wrapper/
│       ├── piper_wrapper.cpp   # C++ wrapper for Piper
│       └── piper_wrapper.h     # Wrapper interface
├── include/
│   └── piper_jni/              # Public headers
├── test/
│   ├── unit/                   # Unit tests
│   └── integration/            # Integration tests
├── scripts/
│   ├── build_windows.ps1       # Windows build script
│   ├── build_macos.sh          # macOS build script
│   ├── build_linux.sh          # Linux build script
│   └── setup_deps.sh           # Dependency installation
└── docker/
    ├── Dockerfile.windows      # Windows cross-compile
    ├── Dockerfile.linux        # Linux build environment
    └── Dockerfile.macos        # macOS build environment
```

#### 2. JNI Wrapper Implementation

**Core Classes:**

```cpp
// Voice instance management
class VoiceInstance {
    std::unique_ptr<piper::PiperVoice> voice;
    std::string modelPath;
    std::string configPath;
    std::mutex synthesisMutex;
    std::queue<SynthesisRequest> requestQueue;
    
public:
    VoiceInstance(const std::string& model, const std::string& config);
    std::vector<int16_t> synthesize(const std::string& text);
    void setSpeechRate(float rate);
    void setNoiseScale(float scale);
    int getSampleRate() const;
};

// Global instance manager
class InstanceManager {
    std::unordered_map<jlong, std::unique_ptr<VoiceInstance>> instances;
    std::mutex instanceMutex;
    std::atomic<jlong> nextInstanceId{1};
    
public:
    jlong createInstance(const std::string& model, const std::string& config);
    VoiceInstance* getInstance(jlong id);
    void destroyInstance(jlong id);
    void destroyAllInstances();
};

// Memory pool for audio buffers
class AudioBufferPool {
    std::vector<std::vector<int16_t>> buffers;
    std::mutex poolMutex;
    size_t maxPoolSize = 10;
    
public:
    std::vector<int16_t> acquire(size_t minSize);
    void release(std::vector<int16_t>&& buffer);
    void clear();
};
```

**JNI Function Signatures:**

```cpp
extern "C" {
    // Initialize voice model
    JNIEXPORT jlong JNICALL 
    Java_ireader_domain_services_tts_1service_piper_PiperNative_initialize(
        JNIEnv* env, jobject obj, jstring modelPath, jstring configPath);
    
    // Synthesize text to audio
    JNIEXPORT jbyteArray JNICALL 
    Java_ireader_domain_services_tts_1service_piper_PiperNative_synthesize(
        JNIEnv* env, jobject obj, jlong instance, jstring text);
    
    // Set speech rate (0.5 - 2.0)
    JNIEXPORT void JNICALL 
    Java_ireader_domain_services_tts_1service_piper_PiperNative_setSpeechRate(
        JNIEnv* env, jobject obj, jlong instance, jfloat rate);
    
    // Set noise scale (quality vs speed)
    JNIEXPORT void JNICALL 
    Java_ireader_domain_services_tts_1service_piper_PiperNative_setNoiseScale(
        JNIEnv* env, jobject obj, jlong instance, jfloat noiseScale);
    
    // Get audio sample rate
    JNIEXPORT jint JNICALL 
    Java_ireader_domain_services_tts_1service_piper_PiperNative_getSampleRate(
        JNIEnv* env, jobject obj, jlong instance);
    
    // Shutdown and cleanup
    JNIEXPORT void JNICALL 
    Java_ireader_domain_services_tts_1service_piper_PiperNative_shutdown(
        JNIEnv* env, jobject obj, jlong instance);
    
    // Get version info
    JNIEXPORT jstring JNICALL 
    Java_ireader_domain_services_tts_1service_piper_PiperNative_getVersion(
        JNIEnv* env, jobject obj);
}
```


#### 3. Voice Model Management System

**Voice Model Repository Structure:**

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

enum class VoiceGender { MALE, FEMALE, NEUTRAL }
enum class VoiceQuality { LOW, MEDIUM, HIGH, PREMIUM }

interface VoiceModelRepository {
    suspend fun getAvailableVoices(): List<VoiceModel>
    suspend fun getVoicesByLanguage(language: String): List<VoiceModel>
    suspend fun downloadVoice(voiceId: String, onProgress: (Float) -> Unit): Result<File>
    suspend fun deleteVoice(voiceId: String): Result<Unit>
    suspend fun getInstalledVoices(): List<VoiceModel>
    suspend fun verifyVoiceIntegrity(voiceId: String): Boolean
    suspend fun getStorageUsage(): Long
}
```

**Voice Catalog (Initial 20+ Languages):**

```yaml
voices:
  # English
  - id: en-us-amy-low
    name: Amy (US English)
    language: en
    locale: en-US
    gender: female
    quality: medium
    
  - id: en-us-ryan-medium
    name: Ryan (US English)
    language: en
    locale: en-US
    gender: male
    quality: high
    
  - id: en-gb-alan-medium
    name: Alan (British English)
    language: en
    locale: en-GB
    gender: male
    quality: high
    
  # Spanish
  - id: es-es-carla-medium
    name: Carla (European Spanish)
    language: es
    locale: es-ES
    gender: female
    quality: high
    
  - id: es-mx-diego-medium
    name: Diego (Mexican Spanish)
    language: es
    locale: es-MX
    gender: male
    quality: high
    
  # French
  - id: fr-fr-siwis-medium
    name: Siwis (French)
    language: fr
    locale: fr-FR
    gender: female
    quality: high
    
  # German
  - id: de-de-thorsten-medium
    name: Thorsten (German)
    language: de
    locale: de-DE
    gender: male
    quality: high
    
  # Chinese
  - id: zh-cn-huayan-medium
    name: Huayan (Mandarin)
    language: zh
    locale: zh-CN
    gender: female
    quality: high
    
  # Japanese
  - id: ja-jp-hikari-medium
    name: Hikari (Japanese)
    language: ja
    locale: ja-JP
    gender: female
    quality: high
    
  # Arabic
  - id: ar-eg-amira-medium
    name: Amira (Egyptian Arabic)
    language: ar
    locale: ar-EG
    gender: female
    quality: medium
    
  # Hindi
  - id: hi-in-aarav-medium
    name: Aarav (Hindi)
    language: hi
    locale: hi-IN
    gender: male
    quality: medium
    
  # Additional languages: Portuguese, Russian, Italian, Korean, 
  # Dutch, Polish, Turkish, Swedish, Norwegian, Danish, etc.
```


#### 4. Build Automation System

**Build Script Architecture:**

```powershell
# build_all.ps1 - Master build script
param(
    [string]$Platform = "all",      # all, windows, macos, linux
    [string]$Config = "Release",    # Debug, Release
    [switch]$Clean,                 # Clean before build
    [switch]$Test,                  # Run tests after build
    [switch]$Package                # Create distribution package
)

# Workflow:
# 1. Validate environment (CMake, compilers, JDK)
# 2. Clone and build Piper core library
# 3. Build JNI wrapper for target platform(s)
# 4. Run tests if requested
# 5. Copy libraries to resources directory
# 6. Create distribution package if requested
```

**Docker-Based Cross-Compilation:**

```dockerfile
# Dockerfile.linux - Linux build environment
FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    git \
    openjdk-17-jdk \
    libasound2-dev \
    libpulse-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build
COPY . .

CMD ["./scripts/build_linux.sh"]
```

**CI/CD Pipeline (GitHub Actions):**

```yaml
name: Build JNI Libraries

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup MSVC
        uses: microsoft/setup-msbuild@v1
      - name: Setup CMake
        uses: lukka/get-cmake@latest
      - name: Build
        run: .\scripts\build_windows.ps1
      - name: Test
        run: .\scripts\test_windows.ps1
      - name: Upload Artifacts
        uses: actions/upload-artifact@v3
        with:
          name: windows-x64-libs
          path: build/Release/*.dll
  
  build-macos:
    runs-on: macos-latest
    strategy:
      matrix:
        arch: [x64, arm64]
    steps:
      - uses: actions/checkout@v3
      - name: Setup CMake
        uses: lukka/get-cmake@latest
      - name: Build
        run: ./scripts/build_macos.sh ${{ matrix.arch }}
      - name: Test
        run: ./scripts/test_macos.sh
      - name: Upload Artifacts
        uses: actions/upload-artifact@v3
        with:
          name: macos-${{ matrix.arch }}-libs
          path: build/lib*.dylib
  
  build-linux:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Dependencies
        run: sudo apt-get install -y build-essential cmake libasound2-dev
      - name: Build
        run: ./scripts/build_linux.sh
      - name: Test
        run: ./scripts/test_linux.sh
      - name: Upload Artifacts
        uses: actions/upload-artifact@v3
        with:
          name: linux-x64-libs
          path: build/lib*.so
  
  package:
    needs: [build-windows, build-macos, build-linux]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Download All Artifacts
        uses: actions/download-artifact@v3
      - name: Create Release Package
        run: ./scripts/package_release.sh
      - name: Upload Release
        uses: actions/upload-artifact@v3
        with:
          name: piper-jni-release
          path: release/*.zip
```


#### 5. Performance Optimization Strategy

**Memory Management:**

```cpp
// Smart pointer usage for automatic cleanup
class VoiceInstance {
    std::unique_ptr<piper::PiperVoice> voice;
    std::shared_ptr<AudioBufferPool> bufferPool;
    
    // Pre-allocate buffers to avoid runtime allocation
    static constexpr size_t BUFFER_SIZE = 1024 * 1024; // 1MB
    std::vector<int16_t> workBuffer;
    
public:
    VoiceInstance() : workBuffer(BUFFER_SIZE) {
        // Reserve capacity upfront
    }
};

// Memory pool to reduce allocations
class AudioBufferPool {
    std::vector<std::unique_ptr<std::vector<int16_t>>> pool;
    
    std::unique_ptr<std::vector<int16_t>> acquire() {
        if (!pool.empty()) {
            auto buffer = std::move(pool.back());
            pool.pop_back();
            return buffer;
        }
        return std::make_unique<std::vector<int16_t>>();
    }
    
    void release(std::unique_ptr<std::vector<int16_t>> buffer) {
        if (pool.size() < MAX_POOL_SIZE) {
            buffer->clear();
            pool.push_back(std::move(buffer));
        }
    }
};
```

**Streaming Synthesis for Long Texts:**

```kotlin
class StreamingSynthesizer(private val piperNative: PiperNative) {
    
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
            val audio = piperNative.synthesize(instance, chunkText)
            onChunk(audio)
            
            // Allow cancellation
            yield()
        }
    }
}
```

**Caching Strategy:**

```kotlin
class VoiceModelCache(private val maxCacheSize: Int = 3) {
    private val cache = LruCache<String, VoiceInstance>(maxCacheSize)
    
    fun getOrLoad(modelPath: String, configPath: String): VoiceInstance {
        return cache.get(modelPath) ?: run {
            val instance = loadVoiceModel(modelPath, configPath)
            cache.put(modelPath, instance)
            instance
        }
    }
    
    fun evictLeastUsed() {
        // LruCache automatically evicts least recently used
    }
}
```

**Performance Targets:**

| Metric | Target | Measurement |
|--------|--------|-------------|
| Initialization | < 2 seconds | Time to load voice model |
| Short text synthesis | < 200ms | 100 characters or less |
| Long text synthesis | < 2s per 1000 chars | Streaming mode |
| Memory usage | < 500 MB | Per loaded voice model |
| CPU usage | < 30% | During active synthesis |
| Latency | < 100ms | From request to first audio |


#### 6. Error Handling and Logging

**Error Hierarchy:**

```kotlin
sealed class PiperException(message: String, cause: Throwable? = null) : 
    Exception(message, cause) {
    
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
}
```

**C++ Error Handling:**

```cpp
// Exception-safe JNI wrapper
jbyteArray synthesizeWithErrorHandling(
    JNIEnv* env, 
    jlong instance, 
    jstring text
) {
    try {
        // Get instance
        auto* voiceInstance = InstanceManager::getInstance(instance);
        if (!voiceInstance) {
            throwJavaException(env, "java/lang/IllegalArgumentException",
                             "Invalid voice instance");
            return nullptr;
        }
        
        // Convert string
        const char* textStr = env->GetStringUTFChars(text, nullptr);
        if (!textStr) {
            throwJavaException(env, "java/lang/OutOfMemoryError",
                             "Failed to allocate string");
            return nullptr;
        }
        
        // RAII wrapper for automatic cleanup
        auto cleanup = [&]() {
            env->ReleaseStringUTFChars(text, textStr);
        };
        std::unique_ptr<void, decltype(cleanup)> guard(nullptr, cleanup);
        
        // Synthesize
        auto audioData = voiceInstance->synthesize(textStr);
        
        // Convert to Java byte array
        jbyteArray result = env->NewByteArray(audioData.size() * 2);
        if (!result) {
            throwJavaException(env, "java/lang/OutOfMemoryError",
                             "Failed to allocate audio buffer");
            return nullptr;
        }
        
        env->SetByteArrayRegion(result, 0, audioData.size() * 2,
                               reinterpret_cast<jbyte*>(audioData.data()));
        
        return result;
        
    } catch (const std::bad_alloc& e) {
        throwJavaException(env, "java/lang/OutOfMemoryError", e.what());
    } catch (const std::exception& e) {
        throwJavaException(env, 
            "ireader/domain/services/tts_service/piper/PiperException$SynthesisException",
            e.what());
    } catch (...) {
        throwJavaException(env, "java/lang/RuntimeException",
                         "Unknown error during synthesis");
    }
    
    return nullptr;
}

void throwJavaException(JNIEnv* env, const char* className, const char* message) {
    jclass exClass = env->FindClass(className);
    if (exClass) {
        env->ThrowNew(exClass, message);
    }
}
```

**Logging Strategy:**

```kotlin
// Structured logging with context
class PiperLogger {
    private val logger = LoggerFactory.getLogger("PiperTTS")
    
    fun logInitialization(modelPath: String, success: Boolean, duration: Long) {
        if (success) {
            logger.info("Voice model loaded successfully",
                "model" to modelPath,
                "duration_ms" to duration)
        } else {
            logger.error("Voice model load failed",
                "model" to modelPath,
                "duration_ms" to duration)
        }
    }
    
    fun logSynthesis(textLength: Int, audioSize: Int, duration: Long) {
        logger.debug("Synthesis completed",
            "text_length" to textLength,
            "audio_bytes" to audioSize,
            "duration_ms" to duration,
            "chars_per_second" to (textLength * 1000 / duration))
    }
    
    fun logError(operation: String, error: Throwable) {
        logger.error("Operation failed: $operation",
            "error_type" to error::class.simpleName,
            "error_message" to error.message,
            "stack_trace" to error.stackTraceToString())
    }
}
```


## Data Models

### Voice Model Metadata

```kotlin
@Serializable
data class VoiceModelMetadata(
    val id: String,
    val version: String,
    val name: String,
    val language: String,
    val locale: String,
    val gender: VoiceGender,
    val quality: VoiceQuality,
    val sampleRate: Int,
    val modelFile: String,
    val configFile: String,
    val modelSize: Long,
    val checksum: String,
    val license: String,
    val author: String,
    val description: String,
    val tags: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Synthesis Configuration

```kotlin
data class SynthesisConfig(
    val speechRate: Float = 1.0f,        // 0.5 - 2.0
    val noiseScale: Float = 0.667f,      // Quality vs speed
    val noiseW: Float = 0.8f,            // Variation in speech
    val lengthScale: Float = 1.0f,       // Phoneme duration
    val sentenceSilence: Float = 0.2f    // Pause between sentences
) {
    init {
        require(speechRate in 0.5f..2.0f) { "Speech rate must be between 0.5 and 2.0" }
        require(noiseScale in 0.0f..1.0f) { "Noise scale must be between 0.0 and 1.0" }
    }
}
```

### Audio Output Format

```kotlin
data class AudioData(
    val samples: ByteArray,              // Raw PCM audio data
    val sampleRate: Int,                 // Samples per second
    val channels: Int = 1,               // Mono audio
    val bitsPerSample: Int = 16,         // 16-bit PCM
    val duration: Duration               // Total duration
) {
    val format: AudioFormat
        get() = AudioFormat(
            sampleRate.toFloat(),
            bitsPerSample,
            channels,
            true,  // signed
            false  // little-endian
        )
}
```

## Testing Strategy

### Unit Tests

```kotlin
class PiperJNITest {
    
    @Test
    fun `test voice model initialization`() {
        val modelPath = "test-models/en-us-test.onnx"
        val configPath = "test-models/en-us-test.json"
        
        val instance = PiperNative.initialize(modelPath, configPath)
        assertTrue(instance > 0, "Instance ID should be positive")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test basic synthesis`() {
        val instance = initializeTestVoice()
        
        val text = "Hello, world!"
        val audio = PiperNative.synthesize(instance, text)
        
        assertNotNull(audio)
        assertTrue(audio.isNotEmpty(), "Audio data should not be empty")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test speech rate adjustment`() {
        val instance = initializeTestVoice()
        
        // Test different speech rates
        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { rate ->
            PiperNative.setSpeechRate(instance, rate)
            val audio = PiperNative.synthesize(instance, "Test")
            assertNotNull(audio)
        }
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test error handling for invalid model`() {
        assertThrows<PiperException.ModelLoadException> {
            PiperNative.initialize("nonexistent.onnx", "nonexistent.json")
        }
    }
    
    @Test
    fun `test memory cleanup`() {
        val instances = mutableListOf<Long>()
        
        // Create multiple instances
        repeat(10) {
            instances.add(initializeTestVoice())
        }
        
        // Cleanup all
        instances.forEach { PiperNative.shutdown(it) }
        
        // Verify no memory leaks (would require profiling tools)
    }
}
```

### Integration Tests

```kotlin
class VoiceModelIntegrationTest {
    
    @Test
    fun `test voice model download and usage`() = runBlocking {
        val repository = VoiceModelRepositoryImpl()
        
        // Download a test voice
        val voice = repository.getAvailableVoices()
            .first { it.language == "en" }
        
        val result = repository.downloadVoice(voice.id) { progress ->
            println("Download progress: ${progress * 100}%")
        }
        
        assertTrue(result.isSuccess)
        
        // Use the downloaded voice
        val modelFile = result.getOrThrow()
        val instance = PiperNative.initialize(
            modelFile.absolutePath,
            modelFile.absolutePath.replace(".onnx", ".json")
        )
        
        val audio = PiperNative.synthesize(instance, "Integration test")
        assertNotNull(audio)
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test multi-language synthesis`() = runBlocking {
        val languages = listOf("en", "es", "fr", "de")
        
        languages.forEach { lang ->
            val voice = getVoiceForLanguage(lang)
            val instance = initializeVoice(voice)
            
            val text = getTestTextForLanguage(lang)
            val audio = PiperNative.synthesize(instance, text)
            
            assertNotNull(audio)
            assertTrue(audio.isNotEmpty())
            
            PiperNative.shutdown(instance)
        }
    }
}
```

### Performance Tests

```kotlin
class PerformanceTest {
    
    @Test
    fun `test synthesis latency`() {
        val instance = initializeTestVoice()
        val text = "This is a performance test."
        
        val durations = mutableListOf<Long>()
        
        repeat(100) {
            val start = System.currentTimeMillis()
            PiperNative.synthesize(instance, text)
            val duration = System.currentTimeMillis() - start
            durations.add(duration)
        }
        
        val avgDuration = durations.average()
        val p95Duration = durations.sorted()[95]
        
        println("Average latency: ${avgDuration}ms")
        println("P95 latency: ${p95Duration}ms")
        
        assertTrue(avgDuration < 200, "Average latency should be under 200ms")
        assertTrue(p95Duration < 500, "P95 latency should be under 500ms")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test memory usage`() {
        val runtime = Runtime.getRuntime()
        val instances = mutableListOf<Long>()
        
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        // Load multiple voice models
        repeat(5) {
            instances.add(initializeTestVoice())
        }
        
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = (memoryAfter - memoryBefore) / (1024 * 1024) // MB
        
        println("Memory used: ${memoryUsed}MB for 5 models")
        
        assertTrue(memoryUsed < 2500, "Should use less than 500MB per model")
        
        instances.forEach { PiperNative.shutdown(it) }
    }
}
```


## User Experience Design

### Voice Selection Interface

```kotlin
@Composable
fun VoiceSelectionScreen(
    viewModel: VoiceSelectionViewModel
) {
    val voices by viewModel.availableVoices.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Language filter
        LanguageFilterChips(
            languages = voices.map { it.language }.distinct(),
            selectedLanguage = viewModel.selectedLanguage,
            onLanguageSelected = { viewModel.filterByLanguage(it) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Voice list
        LazyColumn {
            items(voices) { voice ->
                VoiceCard(
                    voice = voice,
                    isSelected = voice.id == selectedVoice?.id,
                    isDownloaded = viewModel.isVoiceDownloaded(voice.id),
                    downloadProgress = if (downloadProgress?.voiceId == voice.id) 
                        downloadProgress?.progress else null,
                    onSelect = { viewModel.selectVoice(voice) },
                    onDownload = { viewModel.downloadVoice(voice) },
                    onPreview = { viewModel.previewVoice(voice) },
                    onDelete = { viewModel.deleteVoice(voice) }
                )
            }
        }
    }
}

@Composable
fun VoiceCard(
    voice: VoiceModel,
    isSelected: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onPreview: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onSelect),
        elevation = if (isSelected) 8.dp else 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = voice.name,
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "${voice.locale} • ${voice.gender} • ${voice.quality}",
                        style = MaterialTheme.typography.caption
                    )
                }
                
                QualityBadge(quality = voice.quality)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = voice.description,
                style = MaterialTheme.typography.body2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Size info
                Text(
                    text = formatFileSize(voice.modelSize),
                    style = MaterialTheme.typography.caption
                )
                
                // Action buttons
                Row {
                    IconButton(onClick = onPreview) {
                        Icon(Icons.Default.PlayArrow, "Preview")
                    }
                    
                    when {
                        downloadProgress != null -> {
                            CircularProgressIndicator(
                                progress = downloadProgress,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        isDownloaded -> {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                        else -> {
                            IconButton(onClick = onDownload) {
                                Icon(Icons.Default.Download, "Download")
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### TTS Control Interface

```kotlin
@Composable
fun TTSControlPanel(
    viewModel: TTSViewModel
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Time display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDuration(currentPosition))
            Text(formatDuration(duration))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.skipBackward() }) {
                Icon(Icons.Default.SkipPrevious, "Skip Backward")
            }
            
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(48.dp)
                )
            }
            
            IconButton(onClick = { viewModel.skipForward() }) {
                Icon(Icons.Default.SkipNext, "Skip Forward")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Speed control
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Speed, "Speed")
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = speechRate,
                onValueChange = { viewModel.setSpeechRate(it) },
                valueRange = 0.5f..2.0f,
                steps = 14,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(speechRate * 100).toInt()}%",
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
        }
    }
}
```


## Security Considerations

### Library Verification

```kotlin
class LibraryVerifier {
    
    fun verifyLibraryIntegrity(libraryPath: Path): Boolean {
        // Verify file signature (Windows)
        if (isWindows()) {
            return verifyWindowsSignature(libraryPath)
        }
        
        // Verify code signature (macOS)
        if (isMacOS()) {
            return verifyMacOSSignature(libraryPath)
        }
        
        // Verify checksum (Linux)
        return verifyChecksum(libraryPath)
    }
    
    private fun verifyChecksum(libraryPath: Path): Boolean {
        val expectedChecksum = getExpectedChecksum(libraryPath.fileName.toString())
        val actualChecksum = calculateSHA256(libraryPath)
        return expectedChecksum == actualChecksum
    }
    
    private fun calculateSHA256(file: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
```

### Sandboxing and Permissions

```kotlin
class SecurityManager {
    
    fun validateModelFile(modelPath: Path): Boolean {
        // Ensure model is in approved directory
        val approvedDir = getApprovedModelsDirectory()
        if (!modelPath.startsWith(approvedDir)) {
            logger.warn("Model file outside approved directory: $modelPath")
            return false
        }
        
        // Verify file extension
        if (modelPath.extension != "onnx") {
            logger.warn("Invalid model file extension: ${modelPath.extension}")
            return false
        }
        
        // Check file size (prevent DoS)
        val maxSize = 500 * 1024 * 1024 // 500 MB
        if (Files.size(modelPath) > maxSize) {
            logger.warn("Model file too large: ${Files.size(modelPath)} bytes")
            return false
        }
        
        return true
    }
    
    fun sanitizeUserInput(text: String): String {
        // Remove potentially dangerous characters
        return text
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Control characters
            .take(10000) // Limit length
    }
}
```

## Deployment Strategy

### Platform-Specific Packaging

**Windows (MSI Installer):**
```xml
<!-- WiX configuration -->
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
  <Product Id="*" Name="IReader" Version="1.0.0">
    <Package InstallerVersion="200" Compressed="yes" />
    
    <Directory Id="TARGETDIR" Name="SourceDir">
      <Directory Id="ProgramFilesFolder">
        <Directory Id="INSTALLFOLDER" Name="IReader">
          <Component Id="NativeLibs">
            <File Source="piper_jni.dll" />
            <File Source="onnxruntime.dll" />
            <File Source="espeak-ng.dll" />
            <File Source="piper_phonemize.dll" />
          </Component>
        </Directory>
      </Directory>
    </Directory>
    
    <Feature Id="Complete" Level="1">
      <ComponentRef Id="NativeLibs" />
    </Feature>
  </Product>
</Wix>
```

**macOS (DMG with Code Signing):**
```bash
#!/bin/bash
# sign_and_package.sh

# Sign libraries
codesign --force --sign "Developer ID Application: Your Name" \
  --options runtime \
  --entitlements entitlements.plist \
  libpiper_jni.dylib

codesign --force --sign "Developer ID Application: Your Name" \
  --options runtime \
  --entitlements entitlements.plist \
  libonnxruntime.dylib

# Create DMG
hdiutil create -volname "IReader" \
  -srcfolder IReader.app \
  -ov -format UDZO \
  IReader.dmg

# Notarize
xcrun notarytool submit IReader.dmg \
  --apple-id "your@email.com" \
  --password "@keychain:AC_PASSWORD" \
  --team-id "TEAM_ID" \
  --wait

# Staple notarization
xcrun stapler staple IReader.dmg
```

**Linux (DEB/RPM):**
```spec
# ireader.spec for RPM
Name:           ireader
Version:        1.0.0
Release:        1%{?dist}
Summary:        IReader - eBook reader with TTS

License:        MPL-2.0
URL:            https://github.com/yourusername/ireader

%description
IReader is a modern eBook reader with offline text-to-speech support.

%files
%{_bindir}/ireader
%{_libdir}/ireader/libpiper_jni.so
%{_libdir}/ireader/libonnxruntime.so
%{_datadir}/applications/ireader.desktop
%{_datadir}/icons/hicolor/*/apps/ireader.png

%post
ldconfig

%postun
ldconfig
```

### Update Mechanism

```kotlin
class LibraryUpdateManager {
    
    suspend fun checkForUpdates(): UpdateInfo? {
        val currentVersion = getCurrentLibraryVersion()
        val latestVersion = fetchLatestVersion()
        
        if (latestVersion > currentVersion) {
            return UpdateInfo(
                currentVersion = currentVersion,
                latestVersion = latestVersion,
                downloadUrl = getDownloadUrl(latestVersion),
                releaseNotes = getReleaseNotes(latestVersion),
                isCritical = isCriticalUpdate(latestVersion)
            )
        }
        
        return null
    }
    
    suspend fun downloadAndInstallUpdate(updateInfo: UpdateInfo) {
        // Download new libraries
        val tempDir = Files.createTempDirectory("ireader-update")
        downloadLibraries(updateInfo.downloadUrl, tempDir)
        
        // Verify integrity
        if (!verifyLibraries(tempDir)) {
            throw SecurityException("Library verification failed")
        }
        
        // Backup current libraries
        backupCurrentLibraries()
        
        // Install new libraries
        installLibraries(tempDir)
        
        // Cleanup
        Files.delete(tempDir)
    }
}
```

## Monitoring and Analytics

### Performance Metrics

```kotlin
class PerformanceMonitor {
    
    fun recordSynthesis(
        textLength: Int,
        audioSize: Int,
        duration: Long,
        voiceId: String
    ) {
        metrics.record("synthesis.duration", duration, mapOf(
            "voice_id" to voiceId,
            "text_length" to textLength.toString()
        ))
        
        metrics.record("synthesis.throughput", 
            textLength * 1000 / duration, 
            mapOf("voice_id" to voiceId))
    }
    
    fun recordError(
        operation: String,
        errorType: String,
        voiceId: String?
    ) {
        metrics.increment("errors.total", mapOf(
            "operation" to operation,
            "error_type" to errorType,
            "voice_id" to (voiceId ?: "unknown")
        ))
    }
}
```

### Usage Analytics (Privacy-Preserving)

```kotlin
class UsageAnalytics {
    
    fun recordVoiceUsage(voiceId: String, duration: Long) {
        // Anonymized usage tracking
        analytics.track("voice_used", mapOf(
            "voice_language" to getLanguageFromVoiceId(voiceId),
            "duration_bucket" to getDurationBucket(duration),
            "timestamp" to Instant.now().toString()
        ))
    }
    
    private fun getDurationBucket(duration: Long): String {
        return when {
            duration < 60_000 -> "0-1min"
            duration < 300_000 -> "1-5min"
            duration < 900_000 -> "5-15min"
            else -> "15min+"
        }
    }
}
```

## Documentation

### Developer Documentation

- **API Reference**: Complete Javadoc/KDoc for all public APIs
- **Build Guide**: Step-by-step instructions for building JNI libraries
- **Integration Guide**: How to integrate Piper TTS into applications
- **Troubleshooting**: Common issues and solutions
- **Performance Tuning**: Optimization tips and best practices

### User Documentation

- **Getting Started**: Quick start guide for users
- **Voice Selection**: How to choose and download voices
- **Keyboard Shortcuts**: TTS control shortcuts
- **Accessibility**: Features for users with disabilities
- **FAQ**: Frequently asked questions

## Future Enhancements

1. **Voice Cloning**: Allow users to create custom voices
2. **Emotion Control**: Adjust emotional tone of synthesis
3. **SSML Support**: Speech Synthesis Markup Language for fine control
4. **Streaming Synthesis**: Real-time synthesis for live content
5. **GPU Acceleration**: Use GPU for faster synthesis
6. **Cloud Sync**: Sync voice preferences across devices
7. **Offline Training**: Fine-tune voices on user's device
8. **Multi-Speaker**: Support for dialogue with multiple voices

