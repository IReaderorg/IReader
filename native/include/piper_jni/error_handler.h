#ifndef PIPER_JNI_ERROR_HANDLER_H
#define PIPER_JNI_ERROR_HANDLER_H

#include <jni.h>
#include <string>

namespace piper_jni {

/**
 * Throw a Java exception from native code.
 * @param env JNI environment
 * @param className Fully qualified Java exception class name
 * @param message Error message
 */
void throwJavaException(JNIEnv* env, const char* className, const char* message);

/**
 * Throw a generic Piper exception.
 * @param env JNI environment
 * @param message Error message
 */
void throwPiperException(JNIEnv* env, const std::string& message);

/**
 * Throw an initialization exception.
 * @param env JNI environment
 * @param message Error message
 */
void throwInitializationException(JNIEnv* env, const std::string& message);

/**
 * Throw a synthesis exception.
 * @param env JNI environment
 * @param message Error message
 */
void throwSynthesisException(JNIEnv* env, const std::string& message);

/**
 * Throw an invalid parameter exception.
 * @param env JNI environment
 * @param message Error message
 */
void throwInvalidParameterException(JNIEnv* env, const std::string& message);

/**
 * Convert a Java string to C++ string.
 * @param env JNI environment
 * @param jstr Java string
 * @return C++ string
 */
std::string jstringToString(JNIEnv* env, jstring jstr);

/**
 * Check if a JNI exception is pending and log it.
 * @param env JNI environment
 * @return true if an exception is pending
 */
bool checkAndLogException(JNIEnv* env);

} // namespace piper_jni

#endif // PIPER_JNI_ERROR_HANDLER_H
