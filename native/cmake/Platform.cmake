# Platform.cmake - Platform-specific build configurations

# Detect platform
if(WIN32)
    set(PLATFORM_WINDOWS TRUE)
    message(STATUS "Platform: Windows")
elseif(APPLE)
    set(PLATFORM_MACOS TRUE)
    message(STATUS "Platform: macOS")
elseif(UNIX)
    set(PLATFORM_LINUX TRUE)
    message(STATUS "Platform: Linux")
endif()

# Windows-specific configuration
if(PLATFORM_WINDOWS)
    # Use static runtime for better portability
    set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
    
    # Windows-specific compiler flags
    add_compile_definitions(
        _WIN32_WINNT=0x0601  # Windows 7+
        NOMINMAX
        WIN32_LEAN_AND_MEAN
    )
    
    # Export symbols
    set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS ON)
endif()

# macOS-specific configuration
if(PLATFORM_MACOS)
    # Set minimum macOS version
    set(CMAKE_OSX_DEPLOYMENT_TARGET "10.14" CACHE STRING "Minimum macOS version")
    
    # Universal binary support (x64 + ARM64)
    if(BUILD_UNIVERSAL)
        set(CMAKE_OSX_ARCHITECTURES "x86_64;arm64" CACHE STRING "Build architectures")
        message(STATUS "Building universal binary for x86_64 and arm64")
    else()
        # Detect current architecture
        execute_process(
            COMMAND uname -m
            OUTPUT_VARIABLE CURRENT_ARCH
            OUTPUT_STRIP_TRAILING_WHITESPACE
        )
        set(CMAKE_OSX_ARCHITECTURES "${CURRENT_ARCH}" CACHE STRING "Build architecture")
        message(STATUS "Building for architecture: ${CURRENT_ARCH}")
    endif()
    
    # macOS-specific compiler flags
    add_compile_options(
        -fvisibility=hidden
        -fvisibility-inlines-hidden
    )
endif()

# Linux-specific configuration
if(PLATFORM_LINUX)
    # Use older glibc for better compatibility
    add_compile_options(-D_GLIBCXX_USE_CXX11_ABI=0)
    
    # Linux-specific compiler flags
    add_compile_options(
        -fvisibility=hidden
        -fvisibility-inlines-hidden
        -fPIC
    )
    
    # Link with pthread
    set(CMAKE_THREAD_PREFER_PTHREAD TRUE)
    set(THREADS_PREFER_PTHREAD_FLAG TRUE)
    find_package(Threads REQUIRED)
endif()

# Common compiler flags for all platforms
if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
    add_compile_options(
        -Wall
        -Wextra
        -Wpedantic
        -Wno-unused-parameter
    )
    
    # Release optimizations
    if(CMAKE_BUILD_TYPE STREQUAL "Release")
        add_compile_options(
            -O3
            -DNDEBUG
        )
    endif()
elseif(MSVC)
    add_compile_options(
        /W4
        /permissive-
        /Zc:__cplusplus
    )
    
    # Release optimizations
    if(CMAKE_BUILD_TYPE STREQUAL "Release")
        add_compile_options(
            /O2
            /GL  # Whole program optimization
        )
        add_link_options(
            /LTCG  # Link-time code generation
        )
    endif()
endif()

# Set RPATH for better library loading
if(PLATFORM_MACOS)
    set(CMAKE_INSTALL_RPATH "@loader_path")
    set(CMAKE_BUILD_WITH_INSTALL_RPATH ON)
elseif(PLATFORM_LINUX)
    set(CMAKE_INSTALL_RPATH "$ORIGIN")
    set(CMAKE_BUILD_WITH_INSTALL_RPATH ON)
endif()
