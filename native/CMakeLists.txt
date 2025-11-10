cmake_minimum_required(VERSION 3.15)
project(piper_jni VERSION 1.0.0 LANGUAGES CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Platform detection
if(WIN32)
    set(PLATFORM_NAME "windows")
    set(LIBRARY_EXTENSION "dll")
elseif(APPLE)
    set(PLATFORM_NAME "macos")
    set(LIBRARY_EXTENSION "dylib")
    # Detect architecture
    execute_process(
        COMMAND uname -m
        OUTPUT_VARIABLE ARCH
        OUTPUT_STRIP_TRAILING_WHITESPACE
    )
    if(ARCH STREQUAL "arm64")
        set(PLATFORM_NAME "macos-arm64")
    else()
        set(PLATFORM_NAME "macos-x64")
    endif()
elseif(UNIX)
    set(PLATFORM_NAME "linux")
    set(LIBRARY_EXTENSION "so")
endif()

message(STATUS "Building for platform: ${PLATFORM_NAME}")

# Include custom CMake modules
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

# Find JNI
find_package(JNI REQUIRED)
if(JNI_FOUND)
    message(STATUS "JNI include directories: ${JNI_INCLUDE_DIRS}")
    include_directories(${JNI_INCLUDE_DIRS})
else()
    message(FATAL_ERROR "JNI not found. Please install JDK and set JAVA_HOME")
endif()

# Find Piper library (will be implemented in cmake/FindPiper.cmake)
find_package(Piper)
if(Piper_FOUND)
    message(STATUS "Piper library found: ${Piper_LIBRARIES}")
    include_directories(${Piper_INCLUDE_DIRS})
else()
    message(WARNING "Piper library not found. Will use stub implementation.")
    set(USE_STUB_IMPLEMENTATION ON)
endif()

# Find ONNX Runtime (will be implemented in cmake/FindONNXRuntime.cmake)
find_package(ONNXRuntime)
if(ONNXRuntime_FOUND)
    message(STATUS "ONNX Runtime found: ${ONNXRuntime_LIBRARIES}")
    include_directories(${ONNXRuntime_INCLUDE_DIRS})
else()
    message(WARNING "ONNX Runtime not found. Will use stub implementation.")
    set(USE_STUB_IMPLEMENTATION ON)
endif()

# Include directories
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/src
)

# Source files
set(SOURCES
    src/jni/piper_jni.cpp
    src/jni/voice_manager.cpp
    src/jni/audio_buffer.cpp
    src/jni/error_handler.cpp
    src/jni/streaming_synthesizer.cpp
    src/jni/voice_model_cache.cpp
    src/wrapper/piper_wrapper.cpp
)

# Create shared library
add_library(piper_jni SHARED ${SOURCES})

# Link libraries
if(Piper_FOUND AND ONNXRuntime_FOUND)
    target_link_libraries(piper_jni
        ${Piper_LIBRARIES}
        ${ONNXRuntime_LIBRARIES}
    )
endif()

# Platform-specific settings
if(WIN32)
    # Windows-specific settings
    target_compile_definitions(piper_jni PRIVATE
        _WIN32
        _WINDOWS
        PIPER_JNI_EXPORTS
    )
    # Set output name
    set_target_properties(piper_jni PROPERTIES
        OUTPUT_NAME "piper_jni"
        PREFIX ""
        SUFFIX ".dll"
    )
elseif(APPLE)
    # macOS-specific settings
    target_compile_definitions(piper_jni PRIVATE
        __APPLE__
        __MACH__
    )
    set_target_properties(piper_jni PROPERTIES
        OUTPUT_NAME "piper_jni"
        PREFIX "lib"
        SUFFIX ".dylib"
        MACOSX_RPATH ON
        BUILD_WITH_INSTALL_RPATH ON
        INSTALL_RPATH "@loader_path"
    )
elseif(UNIX)
    # Linux-specific settings
    target_compile_definitions(piper_jni PRIVATE
        __linux__
    )
    set_target_properties(piper_jni PROPERTIES
        OUTPUT_NAME "piper_jni"
        PREFIX "lib"
        SUFFIX ".so"
        BUILD_WITH_INSTALL_RPATH ON
        INSTALL_RPATH "$ORIGIN"
    )
endif()

# Compiler warnings
if(MSVC)
    target_compile_options(piper_jni PRIVATE /W4)
else()
    target_compile_options(piper_jni PRIVATE -Wall -Wextra -Wpedantic)
endif()

# Install rules
install(TARGETS piper_jni
    LIBRARY DESTINATION lib
    RUNTIME DESTINATION bin
)

# Copy to resources directory for Kotlin project
# Use platform-specific directory names that match the loader expectations
if(WIN32)
    set(RESOURCES_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../domain/src/desktopMain/resources/native/windows-x64")
elseif(APPLE)
    if(ARCH STREQUAL "arm64")
        set(RESOURCES_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../domain/src/desktopMain/resources/native/macos-arm64")
    else()
        set(RESOURCES_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../domain/src/desktopMain/resources/native/macos-x64")
    endif()
else()
    set(RESOURCES_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../domain/src/desktopMain/resources/native/linux-x64")
endif()

add_custom_command(TARGET piper_jni POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory ${RESOURCES_DIR}
    COMMAND ${CMAKE_COMMAND} -E copy $<TARGET_FILE:piper_jni> ${RESOURCES_DIR}/
    COMMENT "Copying library to resources directory: ${RESOURCES_DIR}"
)

# Testing
enable_testing()
add_subdirectory(test)

message(STATUS "Configuration complete. Build with: cmake --build .")
