#ifndef PIPER_JNI_AUDIO_BUFFER_POOL_H
#define PIPER_JNI_AUDIO_BUFFER_POOL_H

#include <vector>
#include <memory>
#include <mutex>
#include <cstdint>
#include <cstddef>

namespace piper_jni {

/**
 * Thread-safe pool for reusing audio buffers to reduce memory allocations.
 * Pre-allocates buffers and provides acquire/release pattern for efficient memory management.
 */
class AudioBufferPool {
public:
    /**
     * Get the singleton instance of the audio buffer pool.
     */
    static AudioBufferPool& getInstance();
    
    /**
     * Acquire a buffer from the pool with at least the specified capacity.
     * If no buffer is available, creates a new one.
     * 
     * @param minSize Minimum required capacity in samples
     * @return Unique pointer to a buffer (ownership transferred to caller)
     */
    std::unique_ptr<std::vector<int16_t>> acquire(size_t minSize);
    
    /**
     * Release a buffer back to the pool for reuse.
     * If the pool is full, the buffer is destroyed.
     * 
     * @param buffer Buffer to return to the pool (ownership transferred to pool)
     */
    void release(std::unique_ptr<std::vector<int16_t>> buffer);
    
    /**
     * Clear all buffers from the pool and free memory.
     */
    void clear();
    
    /**
     * Get the current number of buffers in the pool.
     * @return Number of available buffers
     */
    size_t getPoolSize() const;
    
    /**
     * Get the maximum pool size.
     * @return Maximum number of buffers that can be pooled
     */
    size_t getMaxPoolSize() const;
    
    /**
     * Set the maximum pool size.
     * @param maxSize Maximum number of buffers to keep in pool
     */
    void setMaxPoolSize(size_t maxSize);
    
    /**
     * Get statistics about buffer pool usage.
     */
    struct Statistics {
        size_t totalAcquired;      // Total number of acquire calls
        size_t totalReleased;      // Total number of release calls
        size_t totalAllocated;     // Total number of new buffers created
        size_t currentPoolSize;    // Current number of buffers in pool
        size_t peakPoolSize;       // Maximum pool size reached
    };
    
    Statistics getStatistics() const;
    
    /**
     * Reset statistics counters.
     */
    void resetStatistics();
    
private:
    AudioBufferPool();
    ~AudioBufferPool() = default;
    
    // Disable copy and move
    AudioBufferPool(const AudioBufferPool&) = delete;
    AudioBufferPool& operator=(const AudioBufferPool&) = delete;
    
    mutable std::mutex mutex_;
    std::vector<std::unique_ptr<std::vector<int16_t>>> pool_;
    size_t maxPoolSize_;
    
    // Statistics
    size_t totalAcquired_;
    size_t totalReleased_;
    size_t totalAllocated_;
    size_t peakPoolSize_;
};

} // namespace piper_jni

#endif // PIPER_JNI_AUDIO_BUFFER_POOL_H
