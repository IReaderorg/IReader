# Piper JNI Code Examples

This document provides practical code examples for integrating and using Piper TTS in your applications.

## Table of Contents

1. [Basic Usage](#basic-usage)
2. [Advanced Features](#advanced-features)
3. [Error Handling](#error-handling)
4. [Performance Optimization](#performance-optimization)
5. [Complete Examples](#complete-examples)

## Basic Usage

### Example 1: Simple Text-to-Speech

The most basic example of synthesizing text to speech:

```kotlin
import ireader.domain.services.tts_service.piper.PiperNative
import ireader.domain.services.tts_service.piper.PiperInitializer

fun main() {
    // Initialize the native library
    if (!PiperInitializer.initialize()) {
        println("Failed to initialize Piper TTS")
        return
    }
    
    // Load a voice model
    val modelPath = "/path/to/models/en-us-amy-low.onnx"
    val configPath = "/path/to/models/en-us-amy-low.onnx.json"
    
    val instance = PiperNative.initialize(modelPath, configPath)
    
    try {
        // Synthesize text
        val text = "Hello, this is a test of Piper text-to-speech."
        val audioData = PiperNative.synthesize(instance, text)
        
        println("Generated ${audioData.size} bytes of audio")
        
        // Play the audio (see Example 2)
        playAudio(audioData, PiperNative.getSampleRate(instance))
        
    } finally {
        // Always cleanup
        PiperNative.shutdown(instance)
    }
}
```

### Example 2: Playing Audio

Playing the synthesized audio using Java Sound API:

```kotlin
import javax.sound.sampled.*

fun playAudio(audioData: ByteArray, sampleRate: Int) {
    // Create audio format
    val audioFormat = AudioFormat(
        sampleRate.toFloat(),  // Sample rate
        16,                     // Bits per sample
        1,                      // Channels (mono)
        true,                   // Signed
        false                   // Little-endian
    )
    
    // Get audio line
    val dataLineInfo = DataLine.Info(SourceDataLine::class.java, audioFormat)
    val sourceDataLine = AudioSystem.getLine(dataLineInfo) as SourceDataLine
    
    // Open and start playback
    sourceDataLine.open(audioFormat)
    sourceDataLine.start()
    
    // Write audio data
    sourceDataLine.write(audioData, 0, audioData.size)
    
    // Wait for playback to complete
    sourceDataLine.drain()
    sourceDataLine.close()
}
```

### Example 3: Adjusting Speech Parameters

Customizing speech rate and quality:

```kotlin
fun synthesizeWithCustomSettings(instance: Long, text: String): ByteArray {
    // Set speech rate (0.5 = half speed, 2.0 = double speed)
    PiperNative.setSpeechRate(instance, 1.2f)
    
    // Set noise scale (lower = faster but less variation)
    PiperNative.setNoiseScale(instance, 0.8f)
    
    // Synthesize with custom settings
    return PiperNative.synthesize(instance, text)
}

// Usage
val instance = PiperNative.initialize(modelPath, configPath)
try {
    val audio = synthesizeWithCustomSettings(instance, "This is faster speech.")
    playAudio(audio, PiperNative.getSampleRate(instance))
} finally {
    PiperNative.shutdown(instance)
}
```


## Advanced Features

### Example 4: Managing Multiple Voice Instances

Creating a voice manager to handle multiple voices:

```kotlin
class VoiceManager {
    private val instances = mutableMapOf<String, Long>()
    private val mutex = Any()
    
    fun loadVoice(voiceId: String, modelPath: String, configPath: String): Long {
        synchronized(mutex) {
            // Return existing instance if already loaded
            instances[voiceId]?.let { return it }
            
            // Load new voice
            val instance = PiperNative.initialize(modelPath, configPath)
            instances[voiceId] = instance
            
            println("Loaded voice: $voiceId (instance: $instance)")
            return instance
        }
    }
    
    fun getVoice(voiceId: String): Long? {
        synchronized(mutex) {
            return instances[voiceId]
        }
    }
    
    fun unloadVoice(voiceId: String) {
        synchronized(mutex) {
            instances[voiceId]?.let { instance ->
                PiperNative.shutdown(instance)
                instances.remove(voiceId)
                println("Unloaded voice: $voiceId")
            }
        }
    }
    
    fun unloadAll() {
        synchronized(mutex) {
            instances.forEach { (voiceId, instance) ->
                PiperNative.shutdown(instance)
                println("Unloaded voice: $voiceId")
            }
            instances.clear()
        }
    }
    
    fun getLoadedVoices(): List<String> {
        synchronized(mutex) {
            return instances.keys.toList()
        }
    }
}

// Usage
val voiceManager = VoiceManager()

// Load multiple voices
val enInstance = voiceManager.loadVoice(
    "en-us-amy",
    "/path/to/en-us-amy-low.onnx",
    "/path/to/en-us-amy-low.onnx.json"
)

val esInstance = voiceManager.loadVoice(
    "es-mx-diego",
    "/path/to/es-mx-diego-medium.onnx",
    "/path/to/es-mx-diego-medium.onnx.json"
)

// Use voices
val englishAudio = PiperNative.synthesize(enInstance, "Hello, world!")
val spanishAudio = PiperNative.synthesize(esInstance, "Hola, mundo!")

// Cleanup
voiceManager.unloadAll()
```

### Example 5: Streaming Synthesis for Long Texts

Processing long texts in chunks to avoid memory issues:

```kotlin
import kotlinx.coroutines.*

class StreamingSynthesizer(private val instance: Long) {
    
    suspend fun synthesizeStreaming(
        text: String,
        chunkSize: Int = 500,
        onChunk: suspend (ByteArray) -> Unit
    ) = withContext(Dispatchers.IO) {
        // Split text into sentences
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        
        // Group sentences into chunks
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()
        
        for (sentence in sentences) {
            if (currentChunk.length + sentence.length > chunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString())
                currentChunk = StringBuilder()
            }
            currentChunk.append(sentence).append(" ")
        }
        
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString())
        }
        
        // Synthesize each chunk
        for ((index, chunk) in chunks.withIndex()) {
            val audio = PiperNative.synthesize(instance, chunk.trim())
            onChunk(audio)
            
            println("Processed chunk ${index + 1}/${chunks.size}")
            
            // Allow cancellation
            yield()
        }
    }
}

// Usage
suspend fun main() = coroutineScope {
    val instance = PiperNative.initialize(modelPath, configPath)
    
    try {
        val synthesizer = StreamingSynthesizer(instance)
        val longText = """
            This is a very long text that needs to be processed in chunks.
            It contains multiple sentences and paragraphs.
            The streaming synthesizer will process it efficiently.
        """.trimIndent()
        
        synthesizer.synthesizeStreaming(longText) { audioChunk ->
            // Play each chunk as it's generated
            playAudio(audioChunk, PiperNative.getSampleRate(instance))
        }
    } finally {
        PiperNative.shutdown(instance)
    }
}
```

### Example 6: Voice Model Caching with LRU

Implementing an LRU cache for voice models:

```kotlin
class VoiceModelCache(private val maxCacheSize: Int = 3) {
    private val cache = object : LinkedHashMap<String, Long>(
        maxCacheSize,
        0.75f,
        true  // Access order (LRU)
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>): Boolean {
            if (size > maxCacheSize) {
                println("Evicting voice from cache: ${eldest.key}")
                PiperNative.shutdown(eldest.value)
                return true
            }
            return false
        }
    }
    
    private val mutex = Any()
    
    fun getOrLoad(
        voiceId: String,
        modelPath: String,
        configPath: String
    ): Long {
        synchronized(mutex) {
            return cache.getOrPut(voiceId) {
                println("Loading voice into cache: $voiceId")
                PiperNative.initialize(modelPath, configPath)
            }
        }
    }
    
    fun clear() {
        synchronized(mutex) {
            cache.values.forEach { instance ->
                PiperNative.shutdown(instance)
            }
            cache.clear()
        }
    }
    
    fun getCacheSize(): Int {
        synchronized(mutex) {
            return cache.size
        }
    }
}

// Usage
val cache = VoiceModelCache(maxCacheSize = 3)

// Load voices (only 3 will be kept in memory)
val voice1 = cache.getOrLoad("voice1", "/path/to/voice1.onnx", "/path/to/voice1.json")
val voice2 = cache.getOrLoad("voice2", "/path/to/voice2.onnx", "/path/to/voice2.json")
val voice3 = cache.getOrLoad("voice3", "/path/to/voice3.onnx", "/path/to/voice3.json")

// This will evict voice1 (least recently used)
val voice4 = cache.getOrLoad("voice4", "/path/to/voice4.onnx", "/path/to/voice4.json")

// Cleanup
cache.clear()
```

### Example 7: Audio Buffer Pool

Reusing audio buffers to reduce allocations:

```kotlin
class AudioBufferPool(private val maxPoolSize: Int = 10) {
    private val pool = mutableListOf<ByteArray>()
    private val mutex = Any()
    
    fun acquire(minSize: Int): ByteArray {
        synchronized(mutex) {
            // Find a buffer that's large enough
            val buffer = pool.firstOrNull { it.size >= minSize }
            
            if (buffer != null) {
                pool.remove(buffer)
                return buffer
            }
            
            // Allocate new buffer
            return ByteArray(minSize)
        }
    }
    
    fun release(buffer: ByteArray) {
        synchronized(mutex) {
            if (pool.size < maxPoolSize) {
                pool.add(buffer)
            }
            // Otherwise, let it be garbage collected
        }
    }
    
    fun clear() {
        synchronized(mutex) {
            pool.clear()
        }
    }
}

// Usage
val bufferPool = AudioBufferPool()

fun synthesizeWithPooling(instance: Long, text: String): ByteArray {
    // Estimate buffer size (rough estimate: 2 bytes per character)
    val estimatedSize = text.length * 2
    val buffer = bufferPool.acquire(estimatedSize)
    
    try {
        val audio = PiperNative.synthesize(instance, text)
        
        // Copy to pooled buffer if it fits
        if (audio.size <= buffer.size) {
            audio.copyInto(buffer)
            return buffer.copyOf(audio.size)
        }
        
        return audio
    } finally {
        bufferPool.release(buffer)
    }
}
```


## Error Handling

### Example 8: Comprehensive Error Handling

Properly handling all possible exceptions:

```kotlin
import ireader.domain.services.tts_service.piper.*

fun safeSynthesize(
    modelPath: String,
    configPath: String,
    text: String
): Result<ByteArray> {
    return try {
        // Initialize library
        if (!PiperInitializer.initialize()) {
            return Result.failure(
                PiperException.InitializationException("Failed to load native library")
            )
        }
        
        // Load voice model
        val instance = try {
            PiperNative.initialize(modelPath, configPath)
        } catch (e: PiperException.ModelLoadException) {
            return Result.failure(e)
        } catch (e: Exception) {
            return Result.failure(
                PiperException.InitializationException("Unexpected error loading model", e)
            )
        }
        
        // Synthesize text
        try {
            val audio = PiperNative.synthesize(instance, text)
            Result.success(audio)
        } catch (e: PiperException.SynthesisException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(
                PiperException.SynthesisException(text, e)
            )
        } finally {
            // Always cleanup
            try {
                PiperNative.shutdown(instance)
            } catch (e: Exception) {
                println("Warning: Failed to shutdown instance: ${e.message}")
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Usage
val result = safeSynthesize(modelPath, configPath, "Hello, world!")

result.onSuccess { audio ->
    println("Synthesis successful: ${audio.size} bytes")
    playAudio(audio, 22050)
}.onFailure { error ->
    when (error) {
        is PiperException.InitializationException -> {
            println("Failed to initialize: ${error.message}")
        }
        is PiperException.ModelLoadException -> {
            println("Failed to load model: ${error.message}")
        }
        is PiperException.SynthesisException -> {
            println("Failed to synthesize: ${error.message}")
        }
        else -> {
            println("Unexpected error: ${error.message}")
        }
    }
}
```

### Example 9: Retry Logic with Exponential Backoff

Implementing retry logic for transient failures:

```kotlin
import kotlinx.coroutines.*
import kotlin.math.pow

suspend fun synthesizeWithRetry(
    instance: Long,
    text: String,
    maxRetries: Int = 3,
    initialDelayMs: Long = 100
): ByteArray = withContext(Dispatchers.IO) {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return@withContext PiperNative.synthesize(instance, text)
        } catch (e: PiperException.SynthesisException) {
            lastException = e
            
            if (attempt < maxRetries - 1) {
                val delayMs = initialDelayMs * 2.0.pow(attempt).toLong()
                println("Synthesis failed, retrying in ${delayMs}ms (attempt ${attempt + 1}/$maxRetries)")
                delay(delayMs)
            }
        }
    }
    
    throw lastException ?: Exception("Synthesis failed after $maxRetries attempts")
}

// Usage
suspend fun main() = coroutineScope {
    val instance = PiperNative.initialize(modelPath, configPath)
    
    try {
        val audio = synthesizeWithRetry(instance, "Hello, world!")
        println("Synthesis successful after retries")
    } catch (e: Exception) {
        println("Synthesis failed after all retries: ${e.message}")
    } finally {
        PiperNative.shutdown(instance)
    }
}
```

### Example 10: Resource Management with AutoCloseable

Using Kotlin's `use` for automatic resource cleanup:

```kotlin
class VoiceInstance(
    modelPath: String,
    configPath: String
) : AutoCloseable {
    private val handle: Long = PiperNative.initialize(modelPath, configPath)
    private var closed = false
    
    fun synthesize(text: String): ByteArray {
        check(!closed) { "Voice instance is closed" }
        return PiperNative.synthesize(handle, text)
    }
    
    fun setSpeechRate(rate: Float) {
        check(!closed) { "Voice instance is closed" }
        PiperNative.setSpeechRate(handle, rate)
    }
    
    fun getSampleRate(): Int {
        check(!closed) { "Voice instance is closed" }
        return PiperNative.getSampleRate(handle)
    }
    
    override fun close() {
        if (!closed) {
            PiperNative.shutdown(handle)
            closed = true
        }
    }
}

// Usage with automatic cleanup
VoiceInstance(modelPath, configPath).use { voice ->
    voice.setSpeechRate(1.2f)
    val audio = voice.synthesize("Hello, world!")
    playAudio(audio, voice.getSampleRate())
}
// Voice is automatically closed here
```

## Performance Optimization

### Example 11: Benchmarking Synthesis Performance

Measuring and analyzing synthesis performance:

```kotlin
data class BenchmarkResult(
    val iterations: Int,
    val averageMs: Double,
    val minMs: Long,
    val maxMs: Long,
    val p50Ms: Long,
    val p95Ms: Long,
    val p99Ms: Long,
    val totalMs: Long
)

fun benchmarkSynthesis(
    instance: Long,
    text: String,
    iterations: Int = 100
): BenchmarkResult {
    val durations = mutableListOf<Long>()
    
    // Warmup
    repeat(10) {
        PiperNative.synthesize(instance, text)
    }
    
    // Actual benchmark
    repeat(iterations) {
        val start = System.nanoTime()
        PiperNative.synthesize(instance, text)
        val duration = (System.nanoTime() - start) / 1_000_000  // Convert to ms
        durations.add(duration)
    }
    
    val sorted = durations.sorted()
    
    return BenchmarkResult(
        iterations = iterations,
        averageMs = durations.average(),
        minMs = sorted.first(),
        maxMs = sorted.last(),
        p50Ms = sorted[sorted.size / 2],
        p95Ms = sorted[sorted.size * 95 / 100],
        p99Ms = sorted[sorted.size * 99 / 100],
        totalMs = durations.sum()
    )
}

// Usage
val instance = PiperNative.initialize(modelPath, configPath)

try {
    val shortText = "Hello, world!"
    val result = benchmarkSynthesis(instance, shortText)
    
    println("""
        Benchmark Results (${result.iterations} iterations):
        Average: ${result.averageMs}ms
        Min: ${result.minMs}ms
        Max: ${result.maxMs}ms
        P50: ${result.p50Ms}ms
        P95: ${result.p95Ms}ms
        P99: ${result.p99Ms}ms
        Total: ${result.totalMs}ms
    """.trimIndent())
} finally {
    PiperNative.shutdown(instance)
}
```

### Example 12: Memory Usage Monitoring

Tracking memory usage during synthesis:

```kotlin
data class MemoryStats(
    val usedMb: Long,
    val freeMb: Long,
    val totalMb: Long,
    val maxMb: Long
)

fun getMemoryStats(): MemoryStats {
    val runtime = Runtime.getRuntime()
    val totalMemory = runtime.totalMemory()
    val freeMemory = runtime.freeMemory()
    val maxMemory = runtime.maxMemory()
    val usedMemory = totalMemory - freeMemory
    
    return MemoryStats(
        usedMb = usedMemory / (1024 * 1024),
        freeMb = freeMemory / (1024 * 1024),
        totalMb = totalMemory / (1024 * 1024),
        maxMb = maxMemory / (1024 * 1024)
    )
}

fun monitorMemoryUsage(block: () -> Unit) {
    val beforeStats = getMemoryStats()
    println("Memory before: ${beforeStats.usedMb}MB used, ${beforeStats.freeMb}MB free")
    
    block()
    
    // Force garbage collection to get accurate measurement
    System.gc()
    Thread.sleep(100)
    
    val afterStats = getMemoryStats()
    println("Memory after: ${afterStats.usedMb}MB used, ${afterStats.freeMb}MB free")
    println("Memory delta: ${afterStats.usedMb - beforeStats.usedMb}MB")
}

// Usage
monitorMemoryUsage {
    val instance = PiperNative.initialize(modelPath, configPath)
    try {
        repeat(100) {
            PiperNative.synthesize(instance, "Test text for memory monitoring.")
        }
    } finally {
        PiperNative.shutdown(instance)
    }
}
```

### Example 13: Parallel Synthesis

Processing multiple texts in parallel:

```kotlin
import kotlinx.coroutines.*

suspend fun synthesizeParallel(
    instance: Long,
    texts: List<String>
): List<ByteArray> = coroutineScope {
    texts.map { text ->
        async(Dispatchers.IO) {
            PiperNative.synthesize(instance, text)
        }
    }.awaitAll()
}

// Usage
suspend fun main() = coroutineScope {
    val instance = PiperNative.initialize(modelPath, configPath)
    
    try {
        val texts = listOf(
            "First sentence to synthesize.",
            "Second sentence to synthesize.",
            "Third sentence to synthesize."
        )
        
        val start = System.currentTimeMillis()
        val audioChunks = synthesizeParallel(instance, texts)
        val duration = System.currentTimeMillis() - start
        
        println("Synthesized ${texts.size} texts in ${duration}ms")
        println("Total audio: ${audioChunks.sumOf { it.size }} bytes")
    } finally {
        PiperNative.shutdown(instance)
    }
}
```


## Complete Examples

### Example 14: Complete TTS Service Implementation

A production-ready TTS service with all features:

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.sound.sampled.*

class TTSService {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val voiceCache = VoiceModelCache(maxCacheSize = 3)
    private var currentPlayback: Job? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    data class VoiceConfig(
        val id: String,
        val modelPath: String,
        val configPath: String
    )
    
    fun loadVoice(config: VoiceConfig): Result<Long> {
        return try {
            val instance = voiceCache.getOrLoad(
                config.id,
                config.modelPath,
                config.configPath
            )
            Result.success(instance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun synthesize(
        voiceId: String,
        text: String,
        speechRate: Float = 1.0f
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val instance = voiceCache.getOrLoad(voiceId, "", "")
            
            // Set speech rate
            PiperNative.setSpeechRate(instance, speechRate)
            
            // Synthesize
            val audio = PiperNative.synthesize(instance, text)
            Result.success(audio)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun play(
        voiceId: String,
        text: String,
        speechRate: Float = 1.0f,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) {
        // Cancel any existing playback
        stop()
        
        currentPlayback = scope.launch {
            try {
                _isPlaying.value = true
                
                // Split text into sentences
                val sentences = text.split(Regex("(?<=[.!?])\\s+"))
                
                for ((index, sentence) in sentences.withIndex()) {
                    if (!isActive) break
                    
                    // Synthesize sentence
                    val result = synthesize(voiceId, sentence, speechRate)
                    
                    result.onSuccess { audio ->
                        // Play audio
                        playAudioBlocking(audio, voiceId)
                        
                        // Update progress
                        onProgress(index + 1, sentences.size)
                    }.onFailure { error ->
                        println("Synthesis failed: ${error.message}")
                    }
                }
            } finally {
                _isPlaying.value = false
                _currentPosition.value = 0
            }
        }
    }
    
    fun stop() {
        currentPlayback?.cancel()
        currentPlayback = null
        _isPlaying.value = false
        _currentPosition.value = 0
    }
    
    fun pause() {
        // Implementation depends on audio playback system
        _isPlaying.value = false
    }
    
    fun resume() {
        // Implementation depends on audio playback system
        _isPlaying.value = true
    }
    
    private suspend fun playAudioBlocking(audioData: ByteArray, voiceId: String) {
        withContext(Dispatchers.IO) {
            val instance = voiceCache.getOrLoad(voiceId, "", "")
            val sampleRate = PiperNative.getSampleRate(instance)
            
            val audioFormat = AudioFormat(
                sampleRate.toFloat(),
                16,
                1,
                true,
                false
            )
            
            val dataLineInfo = DataLine.Info(SourceDataLine::class.java, audioFormat)
            val sourceDataLine = AudioSystem.getLine(dataLineInfo) as SourceDataLine
            
            sourceDataLine.open(audioFormat)
            sourceDataLine.start()
            
            try {
                sourceDataLine.write(audioData, 0, audioData.size)
                sourceDataLine.drain()
            } finally {
                sourceDataLine.close()
            }
        }
    }
    
    fun shutdown() {
        stop()
        scope.cancel()
        voiceCache.clear()
    }
}

// Usage
fun main() = runBlocking {
    val ttsService = TTSService()
    
    try {
        // Load a voice
        val voiceConfig = TTSService.VoiceConfig(
            id = "en-us-amy",
            modelPath = "/path/to/en-us-amy-low.onnx",
            configPath = "/path/to/en-us-amy-low.onnx.json"
        )
        
        ttsService.loadVoice(voiceConfig).onSuccess {
            println("Voice loaded successfully")
        }.onFailure {
            println("Failed to load voice: ${it.message}")
            return@runBlocking
        }
        
        // Play text
        val text = """
            This is a complete example of a TTS service.
            It includes voice management, playback control, and error handling.
            The service can handle multiple voices and provides progress updates.
        """.trimIndent()
        
        ttsService.play(
            voiceId = "en-us-amy",
            text = text,
            speechRate = 1.2f
        ) { current, total ->
            println("Progress: $current/$total sentences")
        }
        
        // Wait for playback to complete
        while (ttsService.isPlaying.value) {
            delay(100)
        }
        
        println("Playback complete")
    } finally {
        ttsService.shutdown()
    }
}
```

### Example 15: Voice Model Downloader

Downloading and managing voice models:

```kotlin
import kotlinx.coroutines.*
import java.io.File
import java.net.URL
import java.security.MessageDigest

class VoiceModelDownloader {
    
    data class DownloadProgress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percentage: Float
    )
    
    suspend fun downloadVoiceModel(
        url: String,
        outputPath: String,
        expectedChecksum: String? = null,
        onProgress: (DownloadProgress) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()
            
            // Download file
            val connection = URL(url).openConnection()
            val totalBytes = connection.contentLengthLong
            
            connection.getInputStream().use { input ->
                outputFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesDownloaded = 0L
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead
                        
                        val progress = DownloadProgress(
                            bytesDownloaded = bytesDownloaded,
                            totalBytes = totalBytes,
                            percentage = if (totalBytes > 0) {
                                (bytesDownloaded.toFloat() / totalBytes) * 100
                            } else 0f
                        )
                        
                        onProgress(progress)
                        
                        // Allow cancellation
                        yield()
                    }
                }
            }
            
            // Verify checksum if provided
            if (expectedChecksum != null) {
                val actualChecksum = calculateChecksum(outputFile)
                if (actualChecksum != expectedChecksum) {
                    outputFile.delete()
                    return@withContext Result.failure(
                        Exception("Checksum mismatch: expected $expectedChecksum, got $actualChecksum")
                    )
                }
            }
            
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateChecksum(file: File): String {
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

// Usage
suspend fun main() = coroutineScope {
    val downloader = VoiceModelDownloader()
    
    val result = downloader.downloadVoiceModel(
        url = "https://example.com/voices/en-us-amy-low.onnx",
        outputPath = "/path/to/models/en-us-amy-low.onnx",
        expectedChecksum = "abc123..."
    ) { progress ->
        println("Download progress: ${progress.percentage.toInt()}%")
    }
    
    result.onSuccess { file ->
        println("Download complete: ${file.absolutePath}")
    }.onFailure { error ->
        println("Download failed: ${error.message}")
    }
}
```

### Example 16: Complete Application with UI Integration

A complete example with Compose UI:

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun TTSScreen(ttsService: TTSService) {
    var text by remember { mutableStateOf("") }
    var speechRate by remember { mutableStateOf(1.0f) }
    val isPlaying by ttsService.isPlaying.collectAsState()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Text input
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text to synthesize") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
        
        // Speech rate slider
        Column {
            Text("Speech Rate: ${(speechRate * 100).toInt()}%")
            Slider(
                value = speechRate,
                onValueChange = { speechRate = it },
                valueRange = 0.5f..2.0f,
                steps = 14
            )
        }
        
        // Playback controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (isPlaying) {
                        ttsService.stop()
                    } else {
                        scope.launch {
                            ttsService.play(
                                voiceId = "en-us-amy",
                                text = text,
                                speechRate = speechRate
                            )
                        }
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Text(if (isPlaying) "Stop" else "Play")
            }
        }
        
        // Status
        if (isPlaying) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Playing...",
                style = MaterialTheme.typography.caption
            )
        }
    }
}
```

## Performance Tips

### Tip 1: Preload Voice Models

```kotlin
// Preload commonly used voices at startup
class VoicePreloader {
    suspend fun preloadVoices(voiceIds: List<String>) = coroutineScope {
        voiceIds.map { voiceId ->
            async(Dispatchers.IO) {
                // Load voice in background
                loadVoice(voiceId)
            }
        }.awaitAll()
    }
}
```

### Tip 2: Use Appropriate Quality

```kotlin
// Choose quality based on use case
fun selectVoiceQuality(useCase: String): String {
    return when (useCase) {
        "real-time" -> "low"
        "general" -> "medium"
        "production" -> "high"
        else -> "medium"
    }
}
```

### Tip 3: Batch Processing

```kotlin
// Process multiple texts efficiently
suspend fun batchSynthesize(
    instance: Long,
    texts: List<String>
): List<ByteArray> {
    return texts.map { text ->
        PiperNative.synthesize(instance, text)
    }
}
```

## Additional Resources

- [Developer Guide](Developer_Guide.md) - Complete API reference and integration guide
- [User Guide](User_Guide.md) - End-user documentation
- [Piper TTS Documentation](https://github.com/rhasspy/piper) - Upstream project documentation
- [ONNX Runtime](https://onnxruntime.ai/) - ML inference engine documentation

## Contributing Examples

Have a useful example? Contributions are welcome!

1. Fork the repository
2. Add your example to this document
3. Ensure code is well-commented and tested
4. Submit a pull request

## License

All code examples in this document are provided under the Mozilla Public License 2.0, same as the IReader project.
