#ifndef PIPER_JNI_VOICE_MODEL_CACHE_H
#define PIPER_JNI_VOICE_MODEL_CACHE_H

#include <string>
#include <memory>
#include <mutex>
#include <list>
#include <unordered_map>
#include <cstdint>
#include <cstddef>

namespace piper_jni {

class VoiceInstance;

/**
 * Metadata about a cached voice model.
 */
struct CachedVoiceInfo {
    std::string modelPath;
    std::string configPath;
    size_t estimatedMemoryUsage;  // Bytes
    int64_t lastAccessTime;       // Timestamp
    size_t accessCount;           // Number of times accessed
};

/**
 * LRU (Least Recently Used) cache for voice model instances.
 * Automatically evicts least recently used models when cache is full.
 * Thread-safe for concurrent access.
 */
class VoiceModelCache {
public:
    /**
     * Get the singleton instance of the cache.
     */
    static VoiceModelCache& getInstance();
    
    /**
     * Get or load a voice model instance.
     * If the model is already cached, returns the cached instance.
     * Otherwise, creates a new instance and adds it to the cache.
     * 
     * @param modelPath Path to the ONNX model file
     * @param configPath Path to the JSON configuration file
     * @return Pointer to voice instance, or nullptr on error
     */
    VoiceInstance* getOrLoad(const std::string& modelPath, const std::string& configPath);
    
    /**
     * Check if a model is currently cached.
     * @param modelPath Path to the model file
     * @return true if model is in cache
     */
    bool isCached(const std::string& modelPath) const;
    
    /**
     * Remove a specific model from the cache.
     * @param modelPath Path to the model file
     * @return true if model was removed, false if not found
     */
    bool evict(const std::string& modelPath);
    
    /**
     * Clear all cached models.
     */
    void clear();
    
    /**
     * Get the current number of cached models.
     * @return Number of models in cache
     */
    size_t getCacheSize() const;
    
    /**
     * Get the maximum number of models that can be cached.
     * @return Maximum cache size
     */
    size_t getMaxCacheSize() const;
    
    /**
     * Set the maximum number of models to cache.
     * If the new size is smaller than current cache size, evicts LRU models.
     * 
     * @param maxSize Maximum number of models to cache
     */
    void setMaxCacheSize(size_t maxSize);
    
    /**
     * Get the maximum memory usage limit in bytes.
     * @return Maximum memory in bytes (0 = no limit)
     */
    size_t getMaxMemoryUsage() const;
    
    /**
     * Set the maximum memory usage limit.
     * When exceeded, evicts LRU models until under limit.
     * 
     * @param maxMemory Maximum memory in bytes (0 = no limit)
     */
    void setMaxMemoryUsage(size_t maxMemory);
    
    /**
     * Get the current estimated memory usage of cached models.
     * @return Estimated memory usage in bytes
     */
    size_t getCurrentMemoryUsage() const;
    
    /**
     * Get information about all cached models.
     * @return Vector of cached voice information
     */
    std::vector<CachedVoiceInfo> getCachedVoices() const;
    
    /**
     * Get cache statistics.
     */
    struct Statistics {
        size_t totalRequests;      // Total getOrLoad calls
        size_t cacheHits;          // Requests served from cache
        size_t cacheMisses;        // Requests requiring new load
        size_t evictions;          // Number of evictions performed
        size_t currentCacheSize;   // Current number of cached models
        size_t currentMemoryUsage; // Current memory usage in bytes
    };
    
    Statistics getStatistics() const;
    
    /**
     * Reset statistics counters.
     */
    void resetStatistics();
    
private:
    VoiceModelCache();
    ~VoiceModelCache();
    
    // Disable copy and move
    VoiceModelCache(const VoiceModelCache&) = delete;
    VoiceModelCache& operator=(const VoiceModelCache&) = delete;
    
    /**
     * Evict least recently used model if cache is full.
     */
    void evictLRU();
    
    /**
     * Evict models until memory usage is under limit.
     */
    void evictToMemoryLimit();
    
    /**
     * Update access time for a model (move to front of LRU list).
     */
    void updateAccessTime(const std::string& modelPath);
    
    /**
     * Estimate memory usage of a voice model.
     */
    size_t estimateMemoryUsage(const std::string& modelPath);
    
    // Cache entry structure
    struct CacheEntry {
        std::unique_ptr<VoiceInstance> instance;
        std::string modelPath;
        std::string configPath;
        size_t estimatedMemoryUsage;
        int64_t lastAccessTime;
        size_t accessCount;
    };
    
    mutable std::mutex mutex_;
    
    // LRU list (most recently used at front)
    std::list<std::string> lruList_;
    
    // Map from model path to cache entry
    std::unordered_map<std::string, CacheEntry> cache_;
    
    // Configuration
    size_t maxCacheSize_;
    size_t maxMemoryUsage_;
    
    // Statistics
    size_t totalRequests_;
    size_t cacheHits_;
    size_t cacheMisses_;
    size_t evictions_;
};

} // namespace piper_jni

#endif // PIPER_JNI_VOICE_MODEL_CACHE_H
