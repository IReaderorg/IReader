#ifndef PIPER_JNI_VOICE_MANAGER_H
#define PIPER_JNI_VOICE_MANAGER_H

#include <memory>
#include <string>
#include <vector>
#include <cstdint>
#include <functional>

namespace piper_jni {

/**
 * Represents a single Piper voice instance with its configuration.
 * Manages the lifecycle of a voice model and synthesis parameters.
 */
class VoiceInstance {
public:
    VoiceInstance();
    ~VoiceInstance();
    
    // Disable copy, allow move
    VoiceInstance(const VoiceInstance&) = delete;
    VoiceInstance& operator=(const VoiceInstance&) = delete;
    VoiceInstance(VoiceInstance&&) = default;
    VoiceInstance& operator=(VoiceInstance&&) = default;
    
    /**
     * Initialize the voice instance with model and config files.
     * @param modelPath Path to the ONNX model file
     * @param configPath Path to the JSON configuration file
     * @return true if initialization succeeded, false otherwise
     */
    bool initialize(const std::string& modelPath, const std::string& configPath);
    
    /**
     * Synthesize text to audio samples.
     * @param text The text to synthesize
     * @return Vector of 16-bit PCM audio samples
     */
    std::vector<int16_t> synthesize(const std::string& text);
    
    /**
     * Set the speech rate multiplier.
     * @param rate Speech rate (0.25 to 4.0, default 1.0)
     */
    void setSpeechRate(float rate);
    
    /**
     * Set the noise scale for synthesis quality.
     * @param scale Noise scale (0.0 to 1.0, default 0.667)
     */
    void setNoiseScale(float scale);
    
    /**
     * Set the length scale for phoneme duration.
     * @param scale Length scale (default 1.0)
     */
    void setLengthScale(float scale);
    
    /**
     * Get the sample rate of the audio output.
     * @return Sample rate in Hz
     */
    int getSampleRate() const;
    
    /**
     * Check if the instance is initialized.
     * @return true if initialized, false otherwise
     */
    bool isInitialized() const;
    
    /**
     * Shutdown and release all resources.
     */
    void shutdown();
    
    /**
     * Synthesize text in streaming mode for long texts.
     * Splits text into chunks and calls callback for each chunk.
     * 
     * @param text The text to synthesize
     * @param callback Function called for each audio chunk
     * @param userData User context passed to callback
     * @param maxChunkSize Maximum characters per chunk
     * @return true if synthesis completed successfully, false if cancelled
     */
    bool synthesizeStreaming(
        const std::string& text,
        std::function<bool(const int16_t*, size_t, void*)> callback,
        void* userData = nullptr,
        size_t maxChunkSize = 500
    );
    
private:
    class Impl;
    std::unique_ptr<Impl> pImpl;
};

/**
 * Thread-safe manager for multiple voice instances.
 * Handles instance creation, lookup, and destruction.
 */
class InstanceManager {
public:
    /**
     * Get the singleton instance of the manager.
     */
    static InstanceManager& getInstance();
    
    /**
     * Create a new voice instance and return its ID.
     * @return Unique instance ID (positive integer)
     */
    int64_t createInstance();
    
    /**
     * Create or get a cached voice instance with model files.
     * Uses voice model cache for efficient memory management.
     * 
     * @param modelPath Path to the ONNX model file
     * @param configPath Path to the JSON configuration file
     * @return Instance ID, or -1 on error
     */
    int64_t createInstanceWithCache(const std::string& modelPath, const std::string& configPath);
    
    /**
     * Get a voice instance by ID.
     * @param id Instance ID
     * @return Pointer to the instance, or nullptr if not found
     */
    VoiceInstance* getVoiceInstance(int64_t id);
    
    /**
     * Destroy a voice instance and free its resources.
     * @param id Instance ID
     */
    void destroyInstance(int64_t id);
    
    /**
     * Get the number of active instances.
     * @return Number of instances
     */
    size_t getInstanceCount() const;
    
    /**
     * Destroy all instances and free all resources.
     */
    void destroyAllInstances();
    
private:
    InstanceManager();
    ~InstanceManager();
    
    // Disable copy and move
    InstanceManager(const InstanceManager&) = delete;
    InstanceManager& operator=(const InstanceManager&) = delete;
    
    class Impl;
    std::unique_ptr<Impl> pImpl;
};

} // namespace piper_jni

#endif // PIPER_JNI_VOICE_MANAGER_H
