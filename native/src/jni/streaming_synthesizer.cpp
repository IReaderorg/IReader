#include "piper_jni/streaming_synthesizer.h"
#include "piper_jni/voice_manager.h"
#include <algorithm>
#include <sstream>
#include <regex>

namespace piper_jni {

StreamingSynthesizer::StreamingSynthesizer()
    : cancelled_(false),
      active_(false),
      processedChunks_(0),
      totalChunks_(0) {
}

StreamingSynthesizer::~StreamingSynthesizer() {
    cancel();
}

bool StreamingSynthesizer::synthesizeStreaming(
    VoiceInstance* voiceInstance,
    const std::string& text,
    StreamingCallback callback,
    void* userData,
    const StreamingConfig& config
) {
    if (!voiceInstance || !voiceInstance->isInitialized()) {
        return false;
    }
    
    if (text.empty() || !callback) {
        return false;
    }
    
    // Reset state
    cancelled_.store(false);
    active_.store(true);
    processedChunks_.store(0);
    
    // Split text into chunks
    auto chunks = splitTextIntoChunks(text, config);
    totalChunks_.store(chunks.size());
    
    bool success = true;
    
    // Process each chunk
    for (size_t i = 0; i < chunks.size(); ++i) {
        // Check for cancellation
        if (config.allowCancellation && cancelled_.load()) {
            success = false;
            break;
        }
        
        const auto& chunk = chunks[i];
        if (chunk.empty()) {
            continue;
        }
        
        try {
            // Synthesize chunk
            auto audioData = voiceInstance->synthesize(chunk);
            
            // Call callback with audio data
            if (!audioData.empty()) {
                bool continueProcessing = callback(
                    audioData.data(),
                    audioData.size(),
                    userData
                );
                
                if (!continueProcessing) {
                    success = false;
                    break;
                }
            }
            
            processedChunks_.fetch_add(1);
            
        } catch (const std::exception& e) {
            // Log error and continue or abort based on configuration
            success = false;
            break;
        }
    }
    
    active_.store(false);
    return success;
}

void StreamingSynthesizer::cancel() {
    cancelled_.store(true);
}

bool StreamingSynthesizer::isActive() const {
    return active_.load();
}

float StreamingSynthesizer::getProgress() const {
    size_t total = totalChunks_.load();
    if (total == 0) {
        return 0.0f;
    }
    
    size_t processed = processedChunks_.load();
    return static_cast<float>(processed) / static_cast<float>(total);
}

size_t StreamingSynthesizer::getProcessedChunks() const {
    return processedChunks_.load();
}

size_t StreamingSynthesizer::getTotalChunks() const {
    return totalChunks_.load();
}

std::vector<std::string> StreamingSynthesizer::splitTextIntoChunks(
    const std::string& text,
    const StreamingConfig& config
) {
    std::vector<std::string> chunks;
    
    if (text.length() <= config.maxChunkSize) {
        chunks.push_back(text);
        return chunks;
    }
    
    // Find boundaries
    std::vector<size_t> boundaries;
    
    if (config.splitOnParagraphs) {
        auto paragraphBoundaries = findParagraphBoundaries(text);
        boundaries.insert(boundaries.end(), paragraphBoundaries.begin(), paragraphBoundaries.end());
    }
    
    if (config.splitOnSentences) {
        auto sentenceBoundaries = findSentenceBoundaries(text);
        boundaries.insert(boundaries.end(), sentenceBoundaries.begin(), sentenceBoundaries.end());
    }
    
    // Sort and remove duplicates
    std::sort(boundaries.begin(), boundaries.end());
    boundaries.erase(std::unique(boundaries.begin(), boundaries.end()), boundaries.end());
    
    // Add start and end boundaries
    if (boundaries.empty() || boundaries.front() != 0) {
        boundaries.insert(boundaries.begin(), 0);
    }
    if (boundaries.back() != text.length()) {
        boundaries.push_back(text.length());
    }
    
    // Create chunks based on boundaries
    size_t currentStart = 0;
    std::string currentChunk;
    
    for (size_t i = 1; i < boundaries.size(); ++i) {
        size_t boundaryPos = boundaries[i];
        std::string segment = text.substr(currentStart, boundaryPos - currentStart);
        
        // Check if adding this segment would exceed max chunk size
        if (!currentChunk.empty() && 
            currentChunk.length() + segment.length() > config.maxChunkSize) {
            // Save current chunk if it meets minimum size
            if (currentChunk.length() >= config.minChunkSize) {
                chunks.push_back(currentChunk);
                currentChunk.clear();
            }
        }
        
        currentChunk += segment;
        currentStart = boundaryPos;
        
        // If chunk is large enough, save it
        if (currentChunk.length() >= config.maxChunkSize) {
            chunks.push_back(currentChunk);
            currentChunk.clear();
        }
    }
    
    // Add remaining text
    if (!currentChunk.empty()) {
        chunks.push_back(currentChunk);
    }
    
    // If no boundaries found, split by character count
    if (chunks.empty()) {
        for (size_t i = 0; i < text.length(); i += config.maxChunkSize) {
            size_t chunkSize = std::min(config.maxChunkSize, text.length() - i);
            chunks.push_back(text.substr(i, chunkSize));
        }
    }
    
    return chunks;
}

std::vector<size_t> StreamingSynthesizer::findSentenceBoundaries(const std::string& text) {
    std::vector<size_t> boundaries;
    
    // Common sentence-ending punctuation
    const std::string sentenceEnders = ".!?";
    
    for (size_t i = 0; i < text.length(); ++i) {
        char c = text[i];
        
        // Check if this is a sentence ender
        if (sentenceEnders.find(c) != std::string::npos) {
            // Look ahead to see if there's whitespace or end of text
            size_t nextPos = i + 1;
            
            // Skip multiple punctuation marks
            while (nextPos < text.length() && 
                   (sentenceEnders.find(text[nextPos]) != std::string::npos ||
                    text[nextPos] == '"' || text[nextPos] == '\'' || 
                    text[nextPos] == ')' || text[nextPos] == ']')) {
                nextPos++;
            }
            
            // Check if followed by whitespace or end of text
            if (nextPos >= text.length() || std::isspace(text[nextPos])) {
                boundaries.push_back(nextPos);
            }
        }
    }
    
    return boundaries;
}

std::vector<size_t> StreamingSynthesizer::findParagraphBoundaries(const std::string& text) {
    std::vector<size_t> boundaries;
    
    // Look for double newlines or multiple consecutive newlines
    for (size_t i = 0; i < text.length(); ++i) {
        if (text[i] == '\n') {
            size_t newlineCount = 1;
            size_t j = i + 1;
            
            // Count consecutive newlines (with possible whitespace)
            while (j < text.length()) {
                if (text[j] == '\n') {
                    newlineCount++;
                    j++;
                } else if (std::isspace(text[j])) {
                    j++;
                } else {
                    break;
                }
            }
            
            // If we found multiple newlines, this is a paragraph boundary
            if (newlineCount >= 2) {
                boundaries.push_back(j);
                i = j - 1; // Skip ahead
            }
        }
    }
    
    return boundaries;
}

} // namespace piper_jni
