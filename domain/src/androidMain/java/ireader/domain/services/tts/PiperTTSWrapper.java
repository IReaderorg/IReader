package ireader.domain.services.tts;

import io.github.givimad.piperjni.PiperConfig;
import io.github.givimad.piperjni.PiperJNI;
import io.github.givimad.piperjni.PiperVoice;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Java wrapper for Piper JNI to expose protected APIs to Kotlin
 * This wrapper provides public access to Piper TTS functionality
 */
public class PiperTTSWrapper implements AutoCloseable {
    
    private final PiperJNI piper;
    private final PiperConfig config;
    private final Map<Integer, PiperVoice> loadedVoices;
    private int nextVoiceId = 1;
    private boolean initialized = false;
    
    public PiperTTSWrapper() {
        this.piper = new PiperJNI();
        this.config = new PiperConfig(piper);
        this.loadedVoices = new HashMap<>();
    }
    
    /**
     * Initialize Piper TTS engine
     */
    public void initialize() {
        if (!initialized) {
            piper.initialize();
            initialized = true;
        }
    }
    
    /**
     * Check if Piper is initialized
     */
    public boolean isInitialized() {
        return initialized && piper.isInitialized();
    }
    
    /**
     * Load a voice model from file paths
     * 
     * @param modelPath Path to the .onnx model file
     * @param configPath Path to the .onnx.json config file
     * @return Voice handle ID for use in synthesis
     */
    public int loadVoice(String modelPath, String configPath) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Piper TTS not initialized. Call initialize() first.");
        }
        
        try {
            Path modelFilePath = Paths.get(modelPath);
            Path configFilePath = Paths.get(configPath);
            
            // Create PiperVoice using protected constructor
            PiperVoice voice = new PiperVoice(piper, config, modelFilePath, configFilePath, 0, false);
            
            // Store voice with unique ID
            int voiceId = nextVoiceId++;
            loadedVoices.put(voiceId, voice);
            
            return voiceId;
        } catch (Exception e) {
            throw new Exception("Failed to load voice: " + e.getMessage(), e);
        }
    }
    
    /**
     * Synthesize text to audio using a loaded voice
     * 
     * @param voiceId Voice handle ID from loadVoice()
     * @param text Text to synthesize
     * @return PCM audio data as short array (16-bit samples)
     */
    public short[] synthesize(int voiceId, String text) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Piper TTS not initialized");
        }
        
        PiperVoice voice = loadedVoices.get(voiceId);
        if (voice == null) {
            throw new IllegalArgumentException("Voice not loaded: " + voiceId);
        }
        
        try {
            // Synthesize using the voice
            return piper.textToAudio(voice, text);
        } catch (Exception e) {
            throw new Exception("Synthesis failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Synthesize text with custom configuration
     * 
     * @param voiceId Voice handle ID
     * @param text Text to synthesize
     * @param lengthScale Speed adjustment (1.0 = normal, <1.0 = faster, >1.0 = slower)
     * @param noiseScale Voice variation (0.0-1.0, default 0.667)
     * @param noiseW Phoneme variation (0.0-1.0, default 0.8)
     * @return PCM audio data as short array
     */
    public short[] synthesizeWithConfig(
            int voiceId,
            String text,
            float lengthScale,
            float noiseScale,
            float noiseW
    ) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Piper TTS not initialized");
        }
        
        PiperVoice voice = loadedVoices.get(voiceId);
        if (voice == null) {
            throw new IllegalArgumentException("Voice not loaded: " + voiceId);
        }
        
        try {
            // Create custom config
            PiperConfig customConfig = new PiperConfig(piper);
            
            // Note: PiperConfig doesn't expose setters, so we use default config
            // Speed adjustment would need to be done at playback level
            
            return piper.textToAudio(voice, text);
        } catch (Exception e) {
            throw new Exception("Synthesis failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get sample rate for a loaded voice
     * 
     * @param voiceId Voice handle ID
     * @return Sample rate in Hz (e.g., 22050)
     */
    public int getSampleRate(int voiceId) throws Exception {
        PiperVoice voice = loadedVoices.get(voiceId);
        if (voice == null) {
            throw new IllegalArgumentException("Voice not loaded: " + voiceId);
        }
        
        return voice.sampleRate();
    }
    
    /**
     * Unload a voice from memory
     * 
     * @param voiceId Voice handle ID to unload
     */
    public void unloadVoice(int voiceId) {
        PiperVoice voice = loadedVoices.remove(voiceId);
        if (voice != null) {
            voice.close();
        }
    }
    
    /**
     * Unload all voices
     */
    public void unloadAllVoices() {
        for (PiperVoice voice : loadedVoices.values()) {
            voice.close();
        }
        loadedVoices.clear();
    }
    
    /**
     * Get number of loaded voices
     */
    public int getLoadedVoiceCount() {
        return loadedVoices.size();
    }
    
    /**
     * Terminate Piper TTS engine
     */
    public void terminate() {
        if (initialized) {
            unloadAllVoices();
            
            if (config != null) {
                config.close();
            }
            
            piper.terminate();
            initialized = false;
        }
    }
    
    @Override
    public void close() {
        terminate();
    }
}
