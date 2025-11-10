#include "piper_jni/audio_buffer_pool.h"
#include <algorithm>

namespace piper_jni {

// Default maximum pool size (10 buffers)
constexpr size_t DEFAULT_MAX_POOL_SIZE = 10;

AudioBufferPool::AudioBufferPool()
    : maxPoolSize_(DEFAULT_MAX_POOL_SIZE),
      totalAcquired_(0),
      totalReleased_(0),
      totalAllocated_(0),
      peakPoolSize_(0) {
    // Pre-allocate some buffers to reduce initial allocation overhead
    pool_.reserve(maxPoolSize_);
}

AudioBufferPool& AudioBufferPool::getInstance() {
    static AudioBufferPool instance;
    return instance;
}

std::unique_ptr<std::vector<int16_t>> AudioBufferPool::acquire(size_t minSize) {
    std::lock_guard<std::mutex> lock(mutex_);
    
    totalAcquired_++;
    
    // Try to find a buffer with sufficient capacity
    for (auto it = pool_.begin(); it != pool_.end(); ++it) {
        if ((*it)->capacity() >= minSize) {
            auto buffer = std::move(*it);
            pool_.erase(it);
            buffer->clear();
            return buffer;
        }
    }
    
    // If no suitable buffer found, try to reuse any buffer and resize it
    if (!pool_.empty()) {
        auto buffer = std::move(pool_.back());
        pool_.pop_back();
        buffer->clear();
        buffer->reserve(minSize);
        return buffer;
    }
    
    // No buffers available, create a new one
    totalAllocated_++;
    auto buffer = std::make_unique<std::vector<int16_t>>();
    buffer->reserve(minSize);
    return buffer;
}

void AudioBufferPool::release(std::unique_ptr<std::vector<int16_t>> buffer) {
    if (!buffer) {
        return;
    }
    
    std::lock_guard<std::mutex> lock(mutex_);
    
    totalReleased_++;
    
    // Only keep buffer if pool is not full
    if (pool_.size() < maxPoolSize_) {
        buffer->clear();
        // Shrink buffer if it's excessively large (> 10MB)
        if (buffer->capacity() > 5 * 1024 * 1024) {
            buffer->shrink_to_fit();
        }
        pool_.push_back(std::move(buffer));
        
        // Update peak pool size
        if (pool_.size() > peakPoolSize_) {
            peakPoolSize_ = pool_.size();
        }
    }
    // Otherwise, buffer is destroyed when unique_ptr goes out of scope
}

void AudioBufferPool::clear() {
    std::lock_guard<std::mutex> lock(mutex_);
    pool_.clear();
}

size_t AudioBufferPool::getPoolSize() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return pool_.size();
}

size_t AudioBufferPool::getMaxPoolSize() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return maxPoolSize_;
}

void AudioBufferPool::setMaxPoolSize(size_t maxSize) {
    std::lock_guard<std::mutex> lock(mutex_);
    maxPoolSize_ = maxSize;
    
    // Trim pool if it exceeds new max size
    while (pool_.size() > maxPoolSize_) {
        pool_.pop_back();
    }
}

AudioBufferPool::Statistics AudioBufferPool::getStatistics() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return Statistics{
        totalAcquired_,
        totalReleased_,
        totalAllocated_,
        pool_.size(),
        peakPoolSize_
    };
}

void AudioBufferPool::resetStatistics() {
    std::lock_guard<std::mutex> lock(mutex_);
    totalAcquired_ = 0;
    totalReleased_ = 0;
    totalAllocated_ = 0;
    peakPoolSize_ = pool_.size();
}

} // namespace piper_jni
