#include "piper_jni/piper_jni.h"
#include "piper_jni/voice_manager.h"
#include "piper_jni/error_handler.h"
#include <string>
#include <cstring>
#include <iostream>

using namespace piper_jni;

JNIEXPORT jlong JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_initialize(
    JNIEnv* env, jobject /* obj */, jstring modelPath, jstring configPath) {
    
    try {
        // Convert Java strings to C++ strings
        std::string modelPathStr = jstringToString(env, modelPath);
        std::string configPathStr = jstringToString(env, configPath);
        
        if (modelPathStr.empty()) {
            throwInitializationException(env, "Model path cannot be empty");
            return 0;
        }
        
        if (configPathStr.empty()) {
            throwInitializationException(env, "Config path cannot be empty");
            return 0;
        }
        
        // Create a new voice instance
        InstanceManager& manager = InstanceManager::getInstance();
        int64_t instanceId = manager.createInstance();
        
        VoiceInstance* instance = manager.getVoiceInstance(instanceId);
        if (instance == nullptr) {
            throwInitializationException(env, "Failed to create voice instance");
            return 0;
        }
        
        // Initialize the voice instance
        bool success = instance->initialize(modelPathStr, configPathStr);
        if (!success) {
            manager.destroyInstance(instanceId);
            throwInitializationException(env, 
                "Failed to initialize Piper voice model. Check that model and config files exist and are valid.");
            return 0;
        }
        
        return static_cast<jlong>(instanceId);
        
    } catch (const std::exception& e) {
        throwInitializationException(env, std::string("Initialization error: ") + e.what());
        return 0;
    } catch (...) {
        throwInitializationException(env, "Unknown initialization error");
        return 0;
    }
}

JNIEXPORT jbyteArray JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_synthesize(
    JNIEnv* env, jobject /* obj */, jlong instance, jstring text) {
    
    try {
        // Validate instance
        if (instance == 0) {
            throwSynthesisException(env, "Invalid instance ID (0)");
            return nullptr;
        }
        
        // Get the voice instance
        InstanceManager& manager = InstanceManager::getInstance();
        VoiceInstance* voiceInstance = manager.getVoiceInstance(instance);
        
        if (voiceInstance == nullptr) {
            throwSynthesisException(env, "Voice instance not found. It may have been shut down.");
            return nullptr;
        }
        
        if (!voiceInstance->isInitialized()) {
            throwSynthesisException(env, "Voice instance is not initialized");
            return nullptr;
        }
        
        // Convert Java string to C++ string
        std::string textStr = jstringToString(env, text);
        
        if (textStr.empty()) {
            // Return empty audio for empty text
            jbyteArray result = env->NewByteArray(0);
            return result;
        }
        
        // Synthesize audio
        std::vector<int16_t> audioSamples = voiceInstance->synthesize(textStr);
        
        // Convert int16_t samples to byte array (little-endian)
        size_t numBytes = audioSamples.size() * sizeof(int16_t);
        jbyteArray result = env->NewByteArray(static_cast<jsize>(numBytes));
        
        if (result == nullptr) {
            throwSynthesisException(env, "Failed to allocate byte array for audio data");
            return nullptr;
        }
        
        // Copy audio data to Java byte array
        env->SetByteArrayRegion(result, 0, static_cast<jsize>(numBytes),
                                reinterpret_cast<const jbyte*>(audioSamples.data()));
        
        if (env->ExceptionCheck()) {
            throwSynthesisException(env, "Failed to copy audio data to Java array");
            return nullptr;
        }
        
        return result;
        
    } catch (const std::exception& e) {
        throwSynthesisException(env, std::string("Synthesis error: ") + e.what());
        return nullptr;
    } catch (...) {
        throwSynthesisException(env, "Unknown synthesis error");
        return nullptr;
    }
}

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_setSpeechRate(
    JNIEnv* env, jobject /* obj */, jlong instance, jfloat rate) {
    
    try {
        if (instance == 0) {
            throwInvalidParameterException(env, "Invalid instance ID (0)");
            return;
        }
        
        InstanceManager& manager = InstanceManager::getInstance();
        VoiceInstance* voiceInstance = manager.getVoiceInstance(instance);
        
        if (voiceInstance == nullptr) {
            throwPiperException(env, "Voice instance not found");
            return;
        }
        
        voiceInstance->setSpeechRate(rate);
        
    } catch (const std::invalid_argument& e) {
        throwInvalidParameterException(env, e.what());
    } catch (const std::exception& e) {
        throwPiperException(env, std::string("Error setting speech rate: ") + e.what());
    } catch (...) {
        throwPiperException(env, "Unknown error setting speech rate");
    }
}

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_setNoiseScale(
    JNIEnv* env, jobject /* obj */, jlong instance, jfloat noiseScale) {
    
    try {
        if (instance == 0) {
            throwInvalidParameterException(env, "Invalid instance ID (0)");
            return;
        }
        
        InstanceManager& manager = InstanceManager::getInstance();
        VoiceInstance* voiceInstance = manager.getVoiceInstance(instance);
        
        if (voiceInstance == nullptr) {
            throwPiperException(env, "Voice instance not found");
            return;
        }
        
        voiceInstance->setNoiseScale(noiseScale);
        
    } catch (const std::invalid_argument& e) {
        throwInvalidParameterException(env, e.what());
    } catch (const std::exception& e) {
        throwPiperException(env, std::string("Error setting noise scale: ") + e.what());
    } catch (...) {
        throwPiperException(env, "Unknown error setting noise scale");
    }
}

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_setLengthScale(
    JNIEnv* env, jobject /* obj */, jlong instance, jfloat lengthScale) {
    
    try {
        if (instance == 0) {
            throwInvalidParameterException(env, "Invalid instance ID (0)");
            return;
        }
        
        InstanceManager& manager = InstanceManager::getInstance();
        VoiceInstance* voiceInstance = manager.getVoiceInstance(instance);
        
        if (voiceInstance == nullptr) {
            throwPiperException(env, "Voice instance not found");
            return;
        }
        
        voiceInstance->setLengthScale(lengthScale);
        
    } catch (const std::invalid_argument& e) {
        throwInvalidParameterException(env, e.what());
    } catch (const std::exception& e) {
        throwPiperException(env, std::string("Error setting length scale: ") + e.what());
    } catch (...) {
        throwPiperException(env, "Unknown error setting length scale");
    }
}

JNIEXPORT jint JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_getSampleRate(
    JNIEnv* env, jobject /* obj */, jlong instance) {
    
    try {
        if (instance == 0) {
            throwInvalidParameterException(env, "Invalid instance ID (0)");
            return 0;
        }
        
        InstanceManager& manager = InstanceManager::getInstance();
        VoiceInstance* voiceInstance = manager.getVoiceInstance(instance);
        
        if (voiceInstance == nullptr) {
            throwPiperException(env, "Voice instance not found");
            return 0;
        }
        
        return static_cast<jint>(voiceInstance->getSampleRate());
        
    } catch (const std::exception& e) {
        throwPiperException(env, std::string("Error getting sample rate: ") + e.what());
        return 0;
    } catch (...) {
        throwPiperException(env, "Unknown error getting sample rate");
        return 0;
    }
}

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_shutdown(
    JNIEnv* /* env */, jobject /* obj */, jlong instance) {
    
    try {
        if (instance == 0) {
            // Instance ID 0 is invalid, but not an error for shutdown
            return;
        }
        
        InstanceManager& manager = InstanceManager::getInstance();
        VoiceInstance* voiceInstance = manager.getVoiceInstance(instance);
        
        if (voiceInstance != nullptr) {
            // Shutdown the instance and release resources
            voiceInstance->shutdown();
        }
        
        // Remove the instance from the manager
        manager.destroyInstance(instance);
        
    } catch (const std::exception& e) {
        // Log error but don't throw - shutdown should be best-effort
        std::cerr << "Error during shutdown: " << e.what() << std::endl;
    } catch (...) {
        std::cerr << "Unknown error during shutdown" << std::endl;
    }
}

JNIEXPORT jboolean JNICALL 
Java_ireader_domain_services_tts_1service_piper_NativeLibraryLoader_isLibraryLoaded(
    JNIEnv* /* env */, jobject /* obj */) {
    
    // If this function is called, the library is loaded
    return JNI_TRUE;
}
