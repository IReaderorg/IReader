/**
 * Example demonstrating memory optimization features in Piper JNI
 * 
 * This example shows how to use:
 * 1. Audio Buffer Pool for efficient memory management
 * 2. Streaming Synthesis for long texts
 * 3. Voice Model Cache for fast voice switching
 */

#include "piper_jni/voice_manager.h"
#include "piper_jni/audio_buffer_pool.h"
#include "piper_jni/streaming_synthesizer.h"
#include "piper_jni/voice_model_cache.h"
#include <iostream>
#include <fstream>
#include <vector>
#include <chrono>

using namespace piper_jni;

// Helper function to measure execution time
template<typename Func>
double measureTime(Func func) {
    auto start = std::chrono::high_resolution_clock::now();
    func();
    auto end = std::chrono::high_resolution_clock::now();
    return std::chrono::duration<double, std::milli>(end - start).count();
}

// Example 1: Using Audio Buffer Pool
void example1_buffer_pool() {
    std::cout << "\n=== Example 1: Audio Buffer Pool ===" << std::endl;
    
    // Get buffer pool instance
    auto& pool = AudioBufferPool::getInstance();
    
    // Configure pool
    pool.setMaxPoolSize(10);
    pool.resetStatistics();
    
    // Simulate multiple synthesis operations
    for (int i = 0; i < 20; i++) {
        // Acquire buffer
        auto buffer = pool.acquire(44100);
        
        // Simulate synthesis (fill with data)
        buffer->resize(44100);
        for (size_t j = 0; j < buffer->size(); j++) {
            (*buffer)[j] = static_cast<int16_t>(j % 1000);
        }
        
        // Release buffer back to pool
        pool.release(std::move(buffer));
    }
    
    // Print statistics
    auto stats = pool.getStatistics();
    std::cout << "Total acquired: " << stats.totalAcquired << std::endl;
    std::cout << "Total allocated: " << stats.totalAllocated << std::endl;
    std::cout << "Pool reuse rate: " 
              << (stats.totalAcquired - stats.totalAllocated) * 100.0 / stats.totalAcquired 
              << "%" << std::endl;
    std::cout << "Current pool size: " << stats.currentPoolSize << std::endl;
    std::cout << "Peak pool size: " << stats.peakPoolSize << std::endl;
}

// Example 2: Streaming Synthesis for Long Texts
void example2_streaming_synthesis() {
    std::cout << "\n=== Example 2: Streaming Synthesis ===" << std::endl;
    
    // Create a long text
    std::string longText = 
        "This is the first sentence. This is the second sentence. "
        "This is the third sentence.\n\n"
        "This is a new paragraph with more content. "
        "It contains multiple sentences that will be processed in chunks. "
        "The streaming synthesizer will split this text intelligently.\n\n"
        "Here is another paragraph. It demonstrates how the system handles "
        "long texts efficiently without consuming too much memory at once.";
    
    // Create voice instance (using stub for this example)
    VoiceInstance voice;
    voice.initialize("model.onnx", "config.json");
    
    // Create streaming synthesizer
    StreamingSynthesizer synthesizer;
    
    // Configure streaming
    StreamingConfig config;
    config.maxChunkSize = 100;  // Small chunks for demonstration
    config.splitOnSentences = true;
    config.splitOnParagraphs = true;
    
    // Audio output buffer
    std::vector<int16_t> allAudio;
    
    // Define callback
    auto callback = [&allAudio](const int16_t* audioData, size_t sampleCount, void* userData) {
        std::cout << "  Received chunk: " << sampleCount << " samples" << std::endl;
        
        // Append to output buffer
        allAudio.insert(allAudio.end(), audioData, audioData + sampleCount);
        
        // Continue processing
        return true;
    };
    
    // Synthesize with streaming
    std::cout << "Starting streaming synthesis..." << std::endl;
    bool success = synthesizer.synthesizeStreaming(&voice, longText, callback, nullptr, config);
    
    std::cout << "Synthesis " << (success ? "completed" : "failed") << std::endl;
    std::cout << "Total chunks processed: " << synthesizer.getProcessedChunks() << std::endl;
    std::cout << "Total audio samples: " << allAudio.size() << std::endl;
    
    voice.shutdown();
}

// Example 3: Voice Model Cache
void example3_voice_model_cache() {
    std::cout << "\n=== Example 3: Voice Model Cache ===" << std::endl;
    
    // Get cache instance
    auto& cache = VoiceModelCache::getInstance();
    
    // Configure cache
    cache.setMaxCacheSize(3);
    cache.setMaxMemoryUsage(1024 * 1024 * 1024); // 1 GB
    cache.resetStatistics();
    
    // Simulate loading multiple voices
    std::vector<std::string> voiceModels = {
        "voice1.onnx",
        "voice2.onnx",
        "voice3.onnx",
        "voice1.onnx",  // Repeat - should hit cache
        "voice2.onnx",  // Repeat - should hit cache
        "voice4.onnx",  // New - should evict voice3
    };
    
    for (const auto& model : voiceModels) {
        std::cout << "\nLoading: " << model << std::endl;
        
        double loadTime = measureTime([&]() {
            VoiceInstance* voice = cache.getOrLoad(model, model + ".json");
            if (voice) {
                std::cout << "  Loaded successfully" << std::endl;
            }
        });
        
        std::cout << "  Load time: " << loadTime << " ms" << std::endl;
        std::cout << "  Cached: " << (cache.isCached(model) ? "yes" : "no") << std::endl;
    }
    
    // Print cache statistics
    auto stats = cache.getStatistics();
    std::cout << "\nCache Statistics:" << std::endl;
    std::cout << "Total requests: " << stats.totalRequests << std::endl;
    std::cout << "Cache hits: " << stats.cacheHits << std::endl;
    std::cout << "Cache misses: " << stats.cacheMisses << std::endl;
    std::cout << "Hit rate: " << stats.cacheHits * 100.0 / stats.totalRequests << "%" << std::endl;
    std::cout << "Evictions: " << stats.evictions << std::endl;
    std::cout << "Current cache size: " << stats.currentCacheSize << std::endl;
    std::cout << "Memory usage: " << stats.currentMemoryUsage / (1024 * 1024) << " MB" << std::endl;
    
    // List cached voices
    std::cout << "\nCached voices:" << std::endl;
    auto cachedVoices = cache.getCachedVoices();
    for (const auto& info : cachedVoices) {
        std::cout << "  - " << info.modelPath << std::endl;
        std::cout << "    Access count: " << info.accessCount << std::endl;
        std::cout << "    Memory: " << info.estimatedMemoryUsage / (1024 * 1024) << " MB" << std::endl;
    }
}

// Example 4: Combined Usage with InstanceManager
void example4_combined_usage() {
    std::cout << "\n=== Example 4: Combined Usage ===" << std::endl;
    
    auto& manager = InstanceManager::getInstance();
    
    // Create instance using cache
    std::cout << "Creating voice instance with cache..." << std::endl;
    int64_t instanceId = manager.createInstanceWithCache("voice1.onnx", "voice1.json");
    
    if (instanceId > 0) {
        std::cout << "Instance created: " << instanceId << std::endl;
        
        // Get voice instance
        VoiceInstance* voice = manager.getVoiceInstance(instanceId);
        
        if (voice) {
            // Use streaming synthesis
            std::string text = "This is a test of the combined optimization features. "
                             "It demonstrates how all three optimizations work together.";
            
            auto callback = [](const int16_t* data, size_t count, void* userData) {
                std::cout << "  Audio chunk: " << count << " samples" << std::endl;
                return true;
            };
            
            std::cout << "Synthesizing with streaming..." << std::endl;
            voice->synthesizeStreaming(text, callback, nullptr, 50);
        }
        
        // Destroy instance (doesn't unload from cache)
        manager.destroyInstance(instanceId);
        std::cout << "Instance destroyed (model still cached)" << std::endl;
    }
    
    // Print final statistics
    std::cout << "\nFinal Statistics:" << std::endl;
    
    auto bufferStats = AudioBufferPool::getInstance().getStatistics();
    std::cout << "Buffer pool reuse rate: " 
              << (bufferStats.totalAcquired - bufferStats.totalAllocated) * 100.0 
                 / bufferStats.totalAcquired << "%" << std::endl;
    
    auto cacheStats = VoiceModelCache::getInstance().getStatistics();
    std::cout << "Cache hit rate: " 
              << cacheStats.cacheHits * 100.0 / cacheStats.totalRequests << "%" << std::endl;
}

int main() {
    std::cout << "Piper JNI Memory Optimization Examples" << std::endl;
    std::cout << "=======================================" << std::endl;
    
    try {
        // Run examples
        example1_buffer_pool();
        example2_streaming_synthesis();
        example3_voice_model_cache();
        example4_combined_usage();
        
        std::cout << "\n=== All examples completed successfully ===" << std::endl;
        
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        return 1;
    }
    
    return 0;
}
