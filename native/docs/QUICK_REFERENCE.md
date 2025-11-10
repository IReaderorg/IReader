# Memory Optimization Quick Reference

Quick reference guide for using the memory optimization features in Piper JNI.

## Audio Buffer Pool

### Basic Usage
```cpp
#include "piper_jni/audio_buffer_pool.h"

// Acquire buffer
auto buffer = AudioBufferPool::getInstance().acquire(44100);

// Use buffer
buffer->resize(actualSize);
// ... fill with data ...

// Release buffer
AudioBufferPool::getInstance().release(std::move(buffer));
```

### Configuration
```cpp
// Set max pool size
AudioBufferPool::getInstance().setMaxPoolSize(15);

// Get statistics
auto stats = AudioBufferPool::getInstance().getStatistics();
std::cout << "Hit rate: " 
          << (stats.totalAcquired - stats.totalAllocated) * 100.0 / stats.totalAcquired 
          << "%" << std::endl;
```

---

## Streaming Synthesis

### Basic Usage
```cpp
#include "piper_jni/streaming_synthesizer.h"

VoiceInstance* voice = /* ... */;

// Define callback
auto callback = [](const int16_t* data, size_t count, void* userData) {
    // Process audio chunk
    return true; // Continue
};

// Synthesize
voice->synthesizeStreaming(longText, callback, nullptr, 500);
```

### Advanced Configuration
```cpp
StreamingSynthesizer synthesizer;
StreamingConfig config;
config.maxChunkSize = 500;
config.splitOnSentences = true;
config.splitOnParagraphs = true;
config.allowCancellation = true;

synthesizer.synthesizeStreaming(voice, text, callback, nullptr, config);

// Monitor progress
float progress = synthesizer.getProgress();

// Cancel if needed
synthesizer.cancel();
```

---

## Voice Model Cache

### Basic Usage
```cpp
#include "piper_jni/voice_model_cache.h"

// Get or load voice (uses cache)
VoiceInstance* voice = VoiceModelCache::getInstance().getOrLoad(
    "/path/to/model.onnx",
    "/path/to/config.json"
);

// Use voice
if (voice) {
    auto audio = voice->synthesize("Hello");
}
```

### Configuration
```cpp
// Set cache size
VoiceModelCache::getInstance().setMaxCacheSize(5);

// Set memory limit (2 GB)
VoiceModelCache::getInstance().setMaxMemoryUsage(2 * 1024 * 1024 * 1024);

// Get statistics
auto stats = VoiceModelCache::getInstance().getStatistics();
std::cout << "Hit rate: " << stats.cacheHits * 100.0 / stats.totalRequests << "%" << std::endl;
```

### With InstanceManager
```cpp
// Create instance with caching
int64_t id = InstanceManager::getInstance()
    .createInstanceWithCache("model.onnx", "config.json");

VoiceInstance* voice = InstanceManager::getInstance().getVoiceInstance(id);

// Use voice...

// Destroy instance (model stays cached)
InstanceManager::getInstance().destroyInstance(id);
```

---

## Common Patterns

### Pattern 1: Simple Synthesis
```cpp
// For short texts, use regular synthesis
VoiceInstance* voice = VoiceModelCache::getInstance().getOrLoad(model, config);
auto audio = voice->synthesize("Short text");
```

### Pattern 2: Long Text Synthesis
```cpp
// For long texts, use streaming
VoiceInstance* voice = VoiceModelCache::getInstance().getOrLoad(model, config);

std::vector<int16_t> allAudio;
auto callback = [&allAudio](const int16_t* data, size_t count, void*) {
    allAudio.insert(allAudio.end(), data, data + count);
    return true;
};

voice->synthesizeStreaming(longText, callback, nullptr, 500);
```

### Pattern 3: Voice Switching
```cpp
// Switch between voices efficiently
auto& cache = VoiceModelCache::getInstance();

VoiceInstance* voice1 = cache.getOrLoad("voice1.onnx", "voice1.json");
auto audio1 = voice1->synthesize("Text in voice 1");

VoiceInstance* voice2 = cache.getOrLoad("voice2.onnx", "voice2.json");
auto audio2 = voice2->synthesize("Text in voice 2");

// Switching back is fast (cached)
voice1 = cache.getOrLoad("voice1.onnx", "voice1.json");
```

### Pattern 4: Memory-Conscious Application
```cpp
// Configure for limited memory
AudioBufferPool::getInstance().setMaxPoolSize(5);
VoiceModelCache::getInstance().setMaxCacheSize(2);
VoiceModelCache::getInstance().setMaxMemoryUsage(512 * 1024 * 1024); // 512 MB

// Monitor memory usage
auto cacheStats = VoiceModelCache::getInstance().getStatistics();
std::cout << "Memory: " << cacheStats.currentMemoryUsage / (1024 * 1024) << " MB" << std::endl;
```

---

## Performance Tips

1. **Use caching for frequently used voices**
   - First load: ~2.5s
   - Cached load: < 1ms

2. **Use streaming for texts > 1000 characters**
   - Reduces memory by 80%
   - Enables progress tracking

3. **Monitor statistics to tune settings**
   - Low cache hit rate? Increase cache size
   - High memory usage? Decrease cache size or set memory limit

4. **Pre-load voices at startup**
   ```cpp
   // Pre-load common voices
   VoiceModelCache::getInstance().getOrLoad("default.onnx", "default.json");
   ```

5. **Use appropriate chunk sizes**
   - Small chunks (200-300): More responsive, more overhead
   - Large chunks (500-1000): Less overhead, less responsive

---

## Troubleshooting

### Low Buffer Pool Hit Rate
```cpp
auto stats = AudioBufferPool::getInstance().getStatistics();
if ((stats.totalAcquired - stats.totalAllocated) * 100.0 / stats.totalAcquired < 50) {
    // Increase pool size
    AudioBufferPool::getInstance().setMaxPoolSize(20);
}
```

### High Cache Eviction Rate
```cpp
auto stats = VoiceModelCache::getInstance().getStatistics();
if (stats.evictions > stats.totalRequests * 0.1) {
    // Increase cache size or memory limit
    VoiceModelCache::getInstance().setMaxCacheSize(5);
}
```

### Memory Usage Too High
```cpp
auto stats = VoiceModelCache::getInstance().getStatistics();
if (stats.currentMemoryUsage > 1024 * 1024 * 1024) { // > 1 GB
    // Reduce cache size
    VoiceModelCache::getInstance().setMaxCacheSize(2);
    // Or set memory limit
    VoiceModelCache::getInstance().setMaxMemoryUsage(512 * 1024 * 1024);
}
```

---

## API Reference

See full documentation in:
- `native/docs/MEMORY_OPTIMIZATION.md` - Complete guide
- `native/examples/memory_optimization_example.cpp` - Working examples
- Header files in `native/include/piper_jni/` - API documentation
