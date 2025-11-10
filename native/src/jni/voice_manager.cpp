#include "piper_jni/voice_manager.h"
#include "piper_jni/audio_buffer_pool.h"
#include "piper_jni/streaming_synthesizer.h"
#include "piper_jni/voice_model_cache.h"
#include <unordered_map>
#include <mutex>
#include <atomic>
#include <stdexcept>
#include <fstream>
#include <sstream>

namespace piper_jni {

// VoiceInstance implementation using PImpl idiom
class VoiceInstance::Impl {
public:
    bool initialized = false;
    std::string modelPath;
    std::string configPath;
    
    // Synthesis parameters
    float speechRate = 1.0f;
    float noiseScale = 0.667f;
    float lengthScale = 1.0f;
    int sampleRate = 22050;
    
    // TODO: Add Piper voice object when integrating Piper library
    // piper::Voice* piperVoice = nullptr;
    
    bool initialize(const std::string& model, const std::string& config) {
        if (initialized) {
            return false; // Already initialized
        }
        
        // Validate file paths
        std::ifstream modelFile(model, std::ios::binary);
        if (!modelFile.good()) {
            return false;
        }
        
        std::ifstream configFile(config);
        if (!configFile.good()) {
            return false;
        }
        
        modelPath = model;
        configPath = config;
        
        // TODO: Initialize Piper voice model
        // For now, just mark as initialized
        initialized = true;
        
        return true;
    }
    
    std::vector<int16_t> synthesize(const std::string& text) {
        if (!initialized) {
            throw std::runtime_error("Voice instance not initialized");
        }
        
        if (text.empty()) {
            return std::vector<int16_t>();
        }
        
        // Estimate buffer size (rough estimate: 100 samples per character)
        size_t estimatedSize = text.length() * 100;
        
        // Acquire buffer from pool
        auto buffer = AudioBufferPool::getInstance().acquire(estimatedSize);
        
        // TODO: Call Piper synthesis engine
        // For now, generate silence as placeholder
        size_t numSamples = sampleRate;
        buffer->resize(numSamples, 0);
        
        // Copy data and release buffer back to pool
        std::vector<int16_t> audioData = *buffer;
        AudioBufferPool::getInstance().release(std::move(buffer));
        
        return audioData;
    }
    
    void shutdown() {
        if (!initialized) {
            return;
        }
        
        // TODO: Release Piper resources
        
        initialized = false;
    }
};

VoiceInstance::VoiceInstance() : pImpl(std::make_unique<Impl>()) {}

VoiceInstance::~VoiceInstance() {
    if (pImpl && pImpl->initialized) {
        pImpl->shutdown();
    }
}

bool VoiceInstance::initialize(const std::string& modelPath, const std::string& configPath) {
    return pImpl->initialize(modelPath, configPath);
}

std::vector<int16_t> VoiceInstance::synthesize(const std::string& text) {
    return pImpl->synthesize(text);
}

void VoiceInstance::setSpeechRate(float rate) {
    if (rate < 0.25f || rate > 4.0f) {
        throw std::invalid_argument("Speech rate must be between 0.25 and 4.0");
    }
    pImpl->speechRate = rate;
}

void VoiceInstance::setNoiseScale(float scale) {
    if (scale < 0.0f || scale > 1.0f) {
        throw std::invalid_argument("Noise scale must be between 0.0 and 1.0");
    }
    pImpl->noiseScale = scale;
}

void VoiceInstance::setLengthScale(float scale) {
    if (scale <= 0.0f) {
        throw std::invalid_argument("Length scale must be positive");
    }
    pImpl->lengthScale = scale;
}

int VoiceInstance::getSampleRate() const {
    return pImpl->sampleRate;
}

bool VoiceInstance::isInitialized() const {
    return pImpl->initialized;
}

void VoiceInstance::shutdown() {
    pImpl->shutdown();
}

bool VoiceInstance::synthesizeStreaming(
    const std::string& text,
    std::function<bool(const int16_t*, size_t, void*)> callback,
    void* userData,
    size_t maxChunkSize
) {
    StreamingSynthesizer synthesizer;
    StreamingConfig config;
    config.maxChunkSize = maxChunkSize;
    
    return synthesizer.synthesizeStreaming(this, text, callback, userData, config);
}

// InstanceManager implementation
class InstanceManager::Impl {
public:
    std::unordered_map<int64_t, std::unique_ptr<VoiceInstance>> instances;
    std::unordered_map<int64_t, VoiceInstance*> cachedInstances; // Track cached instances
    mutable std::mutex mutex;
    std::atomic<int64_t> nextId{1};
    
    int64_t createInstance() {
        std::lock_guard<std::mutex> lock(mutex);
        int64_t id = nextId.fetch_add(1);
        instances[id] = std::make_unique<VoiceInstance>();
        return id;
    }
    
    int64_t createInstanceWithCache(const std::string& modelPath, const std::string& configPath) {
        std::lock_guard<std::mutex> lock(mutex);
        
        // Try to get from cache
        VoiceInstance* cachedInstance = VoiceModelCache::getInstance().getOrLoad(modelPath, configPath);
        if (!cachedInstance) {
            return -1; // Failed to load
        }
        
        // Create ID mapping to cached instance
        int64_t id = nextId.fetch_add(1);
        cachedInstances[id] = cachedInstance;
        
        return id;
    }
    
    VoiceInstance* getVoiceInstance(int64_t id) {
        std::lock_guard<std::mutex> lock(mutex);
        
        // Check cached instances first
        auto cachedIt = cachedInstances.find(id);
        if (cachedIt != cachedInstances.end()) {
            return cachedIt->second;
        }
        
        // Check owned instances
        auto it = instances.find(id);
        return (it != instances.end()) ? it->second.get() : nullptr;
    }
    
    void destroyInstance(int64_t id) {
        std::lock_guard<std::mutex> lock(mutex);
        
        // Check if it's a cached instance
        auto cachedIt = cachedInstances.find(id);
        if (cachedIt != cachedInstances.end()) {
            // Don't shutdown cached instances, just remove the mapping
            cachedInstances.erase(cachedIt);
            return;
        }
        
        // Handle owned instances
        auto it = instances.find(id);
        if (it != instances.end()) {
            it->second->shutdown();
            instances.erase(it);
        }
    }
    
    size_t getInstanceCount() const {
        std::lock_guard<std::mutex> lock(mutex);
        return instances.size() + cachedInstances.size();
    }
    
    void destroyAllInstances() {
        std::lock_guard<std::mutex> lock(mutex);
        
        // Shutdown owned instances
        for (auto& pair : instances) {
            pair.second->shutdown();
        }
        instances.clear();
        
        // Clear cached instance mappings (don't shutdown, cache manages them)
        cachedInstances.clear();
    }
};

InstanceManager::InstanceManager() : pImpl(std::make_unique<Impl>()) {}

InstanceManager::~InstanceManager() {
    if (pImpl) {
        pImpl->destroyAllInstances();
    }
}

InstanceManager& InstanceManager::getInstance() {
    static InstanceManager instance;
    return instance;
}

int64_t InstanceManager::createInstance() {
    return pImpl->createInstance();
}

int64_t InstanceManager::createInstanceWithCache(const std::string& modelPath, const std::string& configPath) {
    return pImpl->createInstanceWithCache(modelPath, configPath);
}

VoiceInstance* InstanceManager::getVoiceInstance(int64_t id) {
    return pImpl->getVoiceInstance(id);
}

void InstanceManager::destroyInstance(int64_t id) {
    pImpl->destroyInstance(id);
}

size_t InstanceManager::getInstanceCount() const {
    return pImpl->getInstanceCount();
}

void InstanceManager::destroyAllInstances() {
    pImpl->destroyAllInstances();
}

} // namespace piper_jni
