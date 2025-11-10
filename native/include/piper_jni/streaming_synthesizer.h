#ifndef PIPER_JNI_STREAMING_SYNTHESIZER_H
#define PIPER_JNI_STREAMING_SYNTHESIZER_H

#include <string>
#include <vector>
#include <functional>
#include <memory>
#include <atomic>
#include <cstdint>

namespace piper_jni {

class VoiceInstance;

/**
 * Callback function type for receiving audio chunks during streaming synthesis.
 * @param audioData Pointer to audio samples
 * @param sampleCount Number of samples in the chunk
 * @param userData User-provided context pointer
 * @return true to continue synthesis, false to cancel
 */
using StreamingCallback = std::function<bool(const int16_t* audioData, size_t sampleCount, void* userData)>;

/**
 * Configuration for streaming synthesis behavior.
 */
struct StreamingConfig {
    size_t maxChunkSize = 500;           // Maximum characters per chunk
    bool splitOnSentences = true;        // Split at sentence boundaries
    bool splitOnParagraphs = true;       // Split at paragraph boundaries
    size_t minChunkSize = 50;            // Minimum characters per chunk
    bool allowCancellation = true;       // Allow cancellation during synthesis
    
    StreamingConfig() = default;
};

/**
 * Handles streaming synthesis of long texts by splitting into manageable chunks.
 * Supports cancellation and progress tracking.
 */
class StreamingSynthesizer {
public:
    StreamingSynthesizer();
    ~StreamingSynthesizer();
    
    /**
     * Synthesize text in streaming mode, calling the callback for each chunk.
     * 
     * @param voiceInstance Voice instance to use for synthesis
     * @param text Text to synthesize
     * @param callback Function called for each audio chunk
     * @param userData User context passed to callback
     * @param config Streaming configuration
     * @return true if synthesis completed successfully, false if cancelled or error
     */
    bool synthesizeStreaming(
        VoiceInstance* voiceInstance,
        const std::string& text,
        StreamingCallback callback,
        void* userData = nullptr,
        const StreamingConfig& config = StreamingConfig()
    );
    
    /**
     * Cancel ongoing streaming synthesis.
     * Thread-safe and can be called from any thread.
     */
    void cancel();
    
    /**
     * Check if synthesis is currently in progress.
     * @return true if synthesis is active
     */
    bool isActive() const;
    
    /**
     * Get the current progress of synthesis.
     * @return Progress as a value between 0.0 and 1.0
     */
    float getProgress() const;
    
    /**
     * Get the number of chunks processed so far.
     * @return Number of completed chunks
     */
    size_t getProcessedChunks() const;
    
    /**
     * Get the total number of chunks to process.
     * @return Total chunk count
     */
    size_t getTotalChunks() const;
    
private:
    /**
     * Split text into chunks based on configuration.
     */
    std::vector<std::string> splitTextIntoChunks(
        const std::string& text,
        const StreamingConfig& config
    );
    
    /**
     * Find sentence boundaries in text.
     */
    std::vector<size_t> findSentenceBoundaries(const std::string& text);
    
    /**
     * Find paragraph boundaries in text.
     */
    std::vector<size_t> findParagraphBoundaries(const std::string& text);
    
    std::atomic<bool> cancelled_;
    std::atomic<bool> active_;
    std::atomic<size_t> processedChunks_;
    std::atomic<size_t> totalChunks_;
};

} // namespace piper_jni

#endif // PIPER_JNI_STREAMING_SYNTHESIZER_H
