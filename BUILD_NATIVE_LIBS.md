# Building Piper JNI Native Libraries

## Overview

The Piper TTS integration requires JNI wrapper libraries to bridge Kotlin/Java with the native Piper C++ library. This document explains how to build these libraries for all supported platforms.

## Current Status

✅ **ONNX Runtime libraries** - Downloaded and placed in `desktop/src/main/resources/native/`
⚠️ **Piper JNI libraries** - Need to be built (instructions below)

## Option 1: Use Process-Based Approach (Recommended for Quick Start)

Instead of JNI, you can use the pre-built Piper binaries via process execution. This is simpler and doesn't require building JNI libraries.

### Advantages:
- No C++ compilation required
- Works immediately with downloaded binaries
- Easier to maintain and debug
- Cross-platform without building

### Implementation:
The Piper binaries have been downloaded to the project root:
- `piper_windows.zip` - Windows executable
- `piper_macos.tar.gz` - macOS executable  
- `piper_linux.tar.gz` - Linux executable

You can extract and use these via `ProcessBuilder` in Kotlin.

## Option 2: Build JNI Libraries (For Native Integration)

If you need true JNI integration for better performance and control, follow these steps:

### Prerequisites

#### All Platforms:
- CMake 3.15 or later
- C++17 compatible compiler
- JDK 11 or later (for JNI headers)

#### Windows:
- Visual Studio 2019 or later with C++ tools
- Windows SDK

#### macOS:
- Xcode Command Line Tools
- Homebrew (optional, for dependencies)

#### Linux:
- GCC 7+ or Clang 6+
- Development packages: `build-essential`, `libasound2-dev`

### Step 1: Clone Piper Repository

```bash
git clone https://github.com/rhasspy/piper.git
cd piper
git submodule update --init --recursive
```

### Step 2: Create JNI Wrapper

Create a new directory `piper/jni` with the following structure:

```
piper/jni/
├── CMakeLists.txt
├── src/
│   └── piper_jni.cpp
└── include/
    └── piper_jni.h
```

#### piper_jni.cpp (Minimal Example):

```cpp
#include <jni.h>
#include <piper.hpp>
#include <string>
#include <memory>

extern "C" {

JNIEXPORT jlong JNICALL Java_ireader_domain_services_tts_1service_piper_PiperNative_initialize(
    JNIEnv* env, jobject obj, jstring modelPath, jstring configPath) {
    
    const char* modelPathStr = env->GetStringUTFChars(modelPath, nullptr);
    const char* configPathStr = env->GetStringUTFChars(configPath, nullptr);
    
    try {
        auto* piper = new piper::PiperVoice();
        piper::loadVoice(*piper, modelPathStr, configPathStr);
        
        env->ReleaseStringUTFChars(modelPath, modelPathStr);
        env->ReleaseStringUTFChars(configPath, configPathStr);
        
        return reinterpret_cast<jlong>(piper);
    } catch (const std::exception& e) {
        env->ReleaseStringUTFChars(modelPath, modelPathStr);
        env->ReleaseStringUTFChars(configPath, configPathStr);
        
        jclass exClass = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exClass, e.what());
        return 0;
    }
}

JNIEXPORT jbyteArray JNICALL Java_ireader_domain_services_tts_1service_piper_PiperNative_synthesize(
    JNIEnv* env, jobject obj, jlong instance, jstring text) {
    
    auto* piper = reinterpret_cast<piper::PiperVoice*>(instance);
    const char* textStr = env->GetStringUTFChars(text, nullptr);
    
    try {
        std::vector<int16_t> audioBuffer;
        piper::textToAudio(*piper, textStr, audioBuffer);
        
        env->ReleaseStringUTFChars(text, textStr);
        
        jbyteArray result = env->NewByteArray(audioBuffer.size() * 2);
        env->SetByteArrayRegion(result, 0, audioBuffer.size() * 2, 
                               reinterpret_cast<jbyte*>(audioBuffer.data()));
        return result;
    } catch (const std::exception& e) {
        env->ReleaseStringUTFChars(text, textStr);
        
        jclass exClass = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exClass, e.what());
        return nullptr;
    }
}

JNIEXPORT void JNICALL Java_ireader_domain_services_tts_1service_piper_PiperNative_shutdown(
    JNIEnv* env, jobject obj, jlong instance) {
    
    auto* piper = reinterpret_cast<piper::PiperVoice*>(instance);
    delete piper;
}

} // extern "C"
```

#### CMakeLists.txt:

```cmake
cmake_minimum_required(VERSION 3.15)
project(piper_jni)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find JNI
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Find Piper library
find_library(PIPER_LIB piper PATHS ../build/lib)
find_library(ONNXRUNTIME_LIB onnxruntime PATHS ../build/lib)

# Add JNI library
add_library(piper_jni SHARED
    src/piper_jni.cpp
)

target_include_directories(piper_jni PRIVATE
    ../src/cpp
    ${JNI_INCLUDE_DIRS}
)

target_link_libraries(piper_jni
    ${PIPER_LIB}
    ${ONNXRUNTIME_LIB}
)

# Platform-specific settings
if(WIN32)
    set_target_properties(piper_jni PROPERTIES
        OUTPUT_NAME "piper_jni"
        SUFFIX ".dll"
    )
elseif(APPLE)
    set_target_properties(piper_jni PROPERTIES
        OUTPUT_NAME "piper_jni"
        SUFFIX ".dylib"
    )
else()
    set_target_properties(piper_jni PROPERTIES
        OUTPUT_NAME "piper_jni"
        SUFFIX ".so"
    )
endif()
```

### Step 3: Build Piper Core Library

```bash
cd piper
mkdir build
cd build

# Configure
cmake ..

# Build
cmake --build . --config Release

# This creates libpiper.a or piper.lib
```

### Step 4: Build JNI Wrapper

```bash
cd ../jni
mkdir build
cd build

# Configure with JNI
cmake .. -DCMAKE_BUILD_TYPE=Release

# Build
cmake --build . --config Release
```

### Step 5: Copy Libraries to Project

After building, copy the libraries to the appropriate directories:

#### Windows:
```powershell
Copy-Item build/Release/piper_jni.dll desktop/src/main/resources/native/windows-x64/
```

#### macOS (Intel):
```bash
cp build/libpiper_jni.dylib desktop/src/main/resources/native/macos-x64/
```

#### macOS (ARM):
```bash
cp build/libpiper_jni.dylib desktop/src/main/resources/native/macos-arm64/
```

#### Linux:
```bash
cp build/libpiper_jni.so desktop/src/main/resources/native/linux-x64/
```

## Option 3: Use Pre-Built Libraries (If Available)

Check if the community has pre-built JNI libraries:
- https://github.com/rhasspy/piper/releases
- https://github.com/rhasspy/piper/discussions

## Verification

After placing the libraries, verify them:

```bash
./gradlew :desktop:verifyNativeLibraries
```

## Troubleshooting

### Library Not Found
- Ensure libraries are in the correct platform directory
- Check file permissions (must be readable)
- Verify library architecture matches system

### Link Errors During Build
- Ensure ONNX Runtime is built first
- Check CMake can find all dependencies
- Verify JNI headers are accessible

### Runtime Errors
- Check library dependencies with:
  - Windows: `dumpbin /dependents piper_jni.dll`
  - macOS: `otool -L libpiper_jni.dylib`
  - Linux: `ldd libpiper_jni.so`

## Alternative: Use Existing Piper Binaries

For development and testing, you can use the Piper CLI binaries via process execution:

```kotlin
val process = ProcessBuilder(
    "piper",
    "--model", modelPath,
    "--output_file", outputPath
).start()

process.inputStream.use { input ->
    input.copyTo(FileOutputStream(outputPath))
}
```

This approach doesn't require JNI libraries but has higher overhead.

## Next Steps

1. Choose your approach (Process-based or JNI)
2. If using JNI, follow the build instructions above
3. Place libraries in `desktop/src/main/resources/native/[platform]/`
4. Run verification: `./gradlew :desktop:verifyNativeLibraries`
5. Test TTS functionality in the application

## Resources

- Piper GitHub: https://github.com/rhasspy/piper
- ONNX Runtime: https://github.com/microsoft/onnxruntime
- JNI Documentation: https://docs.oracle.com/javase/8/docs/technotes/guides/jni/
- CMake Tutorial: https://cmake.org/cmake/help/latest/guide/tutorial/

