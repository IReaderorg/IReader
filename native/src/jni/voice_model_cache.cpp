#include "piper_jni/voice_model_cache.h"
#include "piper_jni/voice_manager.h"
#include <algorithm>
#include <chrono>
#include <fstream>

namespace piper_jni {

// Default configuration
constexpr size_t DEFAULT_MAX_CACHE_SIZE = 3;
constexpr size_t DEFAULT_MAX_MEMORY_USAGE = 1500 * 1024 * 1024; // 1.5 GB

VoiceModelCache::VoiceModelCache()
    : maxCacheSize_(DEFAULT_MAX_CACHE_SIZE),
      maxMemoryUsage_(DEFAULT_MAX_MEMORY_USAGE),
      totalRequests_(0),
      cacheHits_(0),
      cacheMisses_(0),
      evictions_(0) {
}

VoiceModelCache::~VoiceModelCache() {
    clear();
}

VoiceModelCache& VoiceModelCache::getInstance() {
    static VoiceModelCache instance;
    return instance;
}

VoiceInstance* VoiceModelCache::getOrLoad(
    const std::string& modelPath,
    const std::string& configPath
) {
    std::lock_guard<std::mutex> lock(mutex_);
    
    totalRequests_++;
    
    // Check if model is already cached
    auto it = cache_.find(modelPath);
    if (it != cache_.end()) {
        // Cache hit
        cacheHits_++;
        updateAccessTime(modelPath);
        it->second.accessCount++;
        return it->second.instance.get();
    }
    
    // Cache miss - need to load model
    cacheMisses_++;
    
    // Check if we need to evict before adding new model
    if (cache_.size() >= maxCacheSize_) {
        evictLRU();
    }
    
    // Create and initialize new voice instance
    auto instance = std::make_unique<VoiceInstance>();
    if (!instance->initialize(modelPath, configPath)) {
        return nullptr;
    }
    
    // Estimate memory usage
    size_t memoryUsage = estimateMemoryUsage(modelPath);
    
    // Check memory limit
    if (maxMemoryUsage_ > 0) {
        while (!cache_.empty() && 
               getCurrentMemoryUsage() + memoryUsage > maxMemoryUsage_) {
            evictLRU();
        }
    }
    
    // Get current timestamp
    auto now = std::chrono::system_clock::now();
    int64_t timestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
        now.time_since_epoch()
    ).count();
    
    // Add to cache
    CacheEntry entry;
    entry.instance = std::move(instance);
    entry.modelPath = modelPath;
    entry.configPath = configPath;
    entry.estimatedMemoryUsage = memoryUsage;
    entry.lastAccessTime = timestamp;
    entry.accessCount = 1;
    
    VoiceInstance* instancePtr = entry.instance.get();
    cache_[modelPath] = std::move(entry);
    
    // Add to front of LRU list
    lruList_.push_front(modelPath);
    
    return instancePtr;
}

bool VoiceModelCache::isCached(const std::string& modelPath) const {
    std::lock_guard<std::mutex> lock(mutex_);
    return cache_.find(modelPath) != cache_.end();
}

bool VoiceModelCache::evict(const std::string& modelPath) {
    std::lock_guard<std::mutex> lock(mutex_);
    
    auto it = cache_.find(modelPath);
    if (it == cache_.end()) {
        return false;
    }
    
    // Remove from LRU list
    lruList_.remove(modelPath);
    
    // Shutdown and remove from cache
    it->second.instance->shutdown();
    cache_.erase(it);
    
    evictions_++;
    return true;
}

void VoiceModelCache::clear() {
    std::lock_guard<std::mutex> lock(mutex_);
    
    // Shutdown all instances
    for (auto& pair : cache_) {
        pair.second.instance->shutdown();
    }
    
    cache_.clear();
    lruList_.clear();
}

size_t VoiceModelCache::getCacheSize() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return cache_.size();
}

size_t VoiceModelCache::getMaxCacheSize() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return maxCacheSize_;
}

void VoiceModelCache::setMaxCacheSize(size_t maxSize) {
    std::lock_guard<std::mutex> lock(mutex_);
    maxCacheSize_ = maxSize;
    
    // Evict excess models
    while (cache_.size() > maxCacheSize_) {
        evictLRU();
    }
}

size_t VoiceModelCache::getMaxMemoryUsage() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return maxMemoryUsage_;
}

void VoiceModelCache::setMaxMemoryUsage(size_t maxMemory) {
    std::lock_guard<std::mutex> lock(mutex_);
    maxMemoryUsage_ = maxMemory;
    
    // Evict models if over limit
    if (maxMemoryUsage_ > 0) {
        evictToMemoryLimit();
    }
}

size_t VoiceModelCache::getCurrentMemoryUsage() const {
    // Note: mutex should already be locked by caller
    size_t total = 0;
    for (const auto& pair : cache_) {
        total += pair.second.estimatedMemoryUsage;
    }
    return total;
}

std::vector<CachedVoiceInfo> VoiceModelCache::getCachedVoices() const {
    std::lock_guard<std::mutex> lock(mutex_);
    
    std::vector<CachedVoiceInfo> voices;
    voices.reserve(cache_.size());
    
    for (const auto& pair : cache_) {
        const auto& entry = pair.second;
        CachedVoiceInfo info;
        info.modelPath = entry.modelPath;
        info.configPath = entry.configPath;
        info.estimatedMemoryUsage = entry.estimatedMemoryUsage;
        info.lastAccessTime = entry.lastAccessTime;
        info.accessCount = entry.accessCount;
        voices.push_back(info);
    }
    
    return voices;
}

VoiceModelCache::Statistics VoiceModelCache::getStatistics() const {
    std::lock_guard<std::mutex> lock(mutex_);
    
    return Statistics{
        totalRequests_,
        cacheHits_,
        cacheMisses_,
        evictions_,
        cache_.size(),
        getCurrentMemoryUsage()
    };
}

void VoiceModelCache::resetStatistics() {
    std::lock_guard<std::mutex> lock(mutex_);
    totalRequests_ = 0;
    cacheHits_ = 0;
    cacheMisses_ = 0;
    evictions_ = 0;
}

void VoiceModelCache::evictLRU() {
    // Note: mutex should already be locked by caller
    
    if (lruList_.empty()) {
        return;
    }
    
    // Get least recently used model (back of list)
    std::string modelPath = lruList_.back();
    lruList_.pop_back();
    
    // Remove from cache
    auto it = cache_.find(modelPath);
    if (it != cache_.end()) {
        it->second.instance->shutdown();
        cache_.erase(it);
        evictions_++;
    }
}

void VoiceModelCache::evictToMemoryLimit() {
    // Note: mutex should already be locked by caller
    
    while (!cache_.empty() && getCurrentMemoryUsage() > maxMemoryUsage_) {
        evictLRU();
    }
}

void VoiceModelCache::updateAccessTime(const std::string& modelPath) {
    // Note: mutex should already be locked by caller
    
    // Remove from current position in LRU list
    lruList_.remove(modelPath);
    
    // Add to front (most recently used)
    lruList_.push_front(modelPath);
    
    // Update timestamp
    auto now = std::chrono::system_clock::now();
    int64_t timestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
        now.time_since_epoch()
    ).count();
    
    auto it = cache_.find(modelPath);
    if (it != cache_.end()) {
        it->second.lastAccessTime = timestamp;
    }
}

size_t VoiceModelCache::estimateMemoryUsage(const std::string& modelPath) {
    // Estimate based on model file size
    // Typical overhead: model file size * 2 (for weights + runtime structures)
    
    std::ifstream file(modelPath, std::ios::binary | std::ios::ate);
    if (!file.good()) {
        // Default estimate: 200 MB
        return 200 * 1024 * 1024;
    }
    
    size_t fileSize = file.tellg();
    file.close();
    
    // Estimate: file size * 2 + 50 MB overhead
    return fileSize * 2 + 50 * 1024 * 1024;
}

} // namespace piper_jni
