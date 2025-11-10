#ifndef PIPER_JNI_H
#define PIPER_JNI_H

#include <jni.h>

// Platform-specific export macros
#ifdef _WIN32
    #ifdef PIPER_JNI_EXPORTS
        #define PIPER_JNI_API __declspec(dllexport)
    #else
        #define PIPER_JNI_API __declspec(dllimport)
    #endif
#else
    #define PIPER_JNI_API __attribute__((visibility("default")))
#endif

#ifdef __cplusplus
extern "C" {
#endif

// JNI function declarations
// These will be implemented in src/jni/piper_jni.cpp

JNIEXPORT jlong JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_initialize(
    JNIEnv* env, jobject obj, jstring modelPath, jstring configPath);

JNIEXPORT jbyteArray JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_synthesize(
    JNIEnv* env, jobject obj, jlong instance, jstring text);

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_setSpeechRate(
    JNIEnv* env, jobject obj, jlong instance, jfloat rate);

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_setNoiseScale(
    JNIEnv* env, jobject obj, jlong instance, jfloat noiseScale);

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_setLengthScale(
    JNIEnv* env, jobject obj, jlong instance, jfloat lengthScale);

JNIEXPORT jint JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_getSampleRate(
    JNIEnv* env, jobject obj, jlong instance);

JNIEXPORT void JNICALL 
Java_ireader_domain_services_tts_1service_piper_PiperNative_shutdown(
    JNIEnv* env, jobject obj, jlong instance);

JNIEXPORT jboolean JNICALL 
Java_ireader_domain_services_tts_1service_piper_NativeLibraryLoader_isLibraryLoaded(
    JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif

#endif // PIPER_JNI_H
