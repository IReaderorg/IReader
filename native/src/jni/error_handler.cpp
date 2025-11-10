#include "piper_jni/error_handler.h"
#include <iostream>
#include <sstream>

namespace piper_jni {

void throwJavaException(JNIEnv* env, const char* className, const char* message) {
    if (env->ExceptionCheck()) {
        // An exception is already pending, don't throw another
        return;
    }
    
    jclass exClass = env->FindClass(className);
    if (exClass != nullptr) {
        env->ThrowNew(exClass, message);
        env->DeleteLocalRef(exClass);
    } else {
        // Fallback to RuntimeException if the specified class is not found
        env->ExceptionClear();
        jclass runtimeExClass = env->FindClass("java/lang/RuntimeException");
        if (runtimeExClass != nullptr) {
            std::string fallbackMsg = std::string("Failed to throw ") + className + ": " + message;
            env->ThrowNew(runtimeExClass, fallbackMsg.c_str());
            env->DeleteLocalRef(runtimeExClass);
        }
    }
}

void throwPiperException(JNIEnv* env, const std::string& message) {
    throwJavaException(env, "java/lang/RuntimeException", message.c_str());
}

void throwInitializationException(JNIEnv* env, const std::string& message) {
    throwJavaException(env, "java/lang/IllegalStateException", message.c_str());
}

void throwSynthesisException(JNIEnv* env, const std::string& message) {
    throwJavaException(env, "java/lang/RuntimeException", message.c_str());
}

void throwInvalidParameterException(JNIEnv* env, const std::string& message) {
    throwJavaException(env, "java/lang/IllegalArgumentException", message.c_str());
}

std::string jstringToString(JNIEnv* env, jstring jstr) {
    if (jstr == nullptr) {
        return "";
    }
    
    const char* chars = env->GetStringUTFChars(jstr, nullptr);
    if (chars == nullptr) {
        return "";
    }
    
    std::string result(chars);
    env->ReleaseStringUTFChars(jstr, chars);
    
    return result;
}

bool checkAndLogException(JNIEnv* env) {
    if (env->ExceptionCheck()) {
        jthrowable exception = env->ExceptionOccurred();
        if (exception != nullptr) {
            env->ExceptionDescribe(); // Print to stderr
            env->ExceptionClear();
            env->DeleteLocalRef(exception);
        }
        return true;
    }
    return false;
}

} // namespace piper_jni
