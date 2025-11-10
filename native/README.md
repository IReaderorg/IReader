# Piper JNI Native Library Build System

This directory contains the native C++ code and build infrastructure for the Piper JNI wrapper library, enabling high-quality offline text-to-speech functionality across Windows, macOS, and Linux platforms.

## Overview

The Piper JNI library provides a Java Native Interface (JNI) bridge between Kotlin/Java code and the Piper C++ TTS engine. This enables production-ready, cross-platform text-to-speech with minimal latency and maximum reliability.

## Directory Structure

```
native/
├── CMakeLists.txt              # Root CMake configuration
├── cmake/                      # CMake modules
│   ├── FindPiper.cmake        # Locate Piper library
│   ├── FindONNXRuntime.cmake  # Locate ONNX Runtime
│   └── Platform.cmake         # Platform-specific settings
├── include/                    # Public headers
│   └── piper_jni/
│       └── piper_jni.h        # JNI function declarations
├── src/                        # Source code
│   ├── jni/                   # JNI wrapper implementation
│   │   ├── piper_jni.cpp      # Main JNI functions
│   │   ├── voice_manager.cpp  # Voice instance management
│   │   ├── audio_buffer.cpp   # Audio buffer pooling
│   │   └── error_handler.cpp  # Error handling utilities
│   └── wrapper/               # Piper C++ wrapper
│       └── piper_wrapper.cpp  # Wrapper for Piper library
├── test/                       # Tests
│   ├── CMakeLists.txt
│   └── basic_test.cpp
├── scripts/                    # Build scripts
│   ├── build_windows.ps1      # Windows build script
│   ├── build_macos.sh         # macOS build script
│   └── build_linux.sh         # Linux build script
├── docker/                     # Docker build environments
│   ├── Dockerfile.linux
│   ├── Dockerfile.windows
│   └── Dockerfile.macos
└── README.md                   # This file
```

## Prerequisites

### All Platforms

- **CMake 3.15+**: Build system generator
- **JDK 11+**: Java Development Kit (for JNI headers)
- **C++17 compiler**: GCC, Clang, or MSVC

### Windows

- **Visual Studio 2019+** with C++ development tools
- **CMake** (can be installed via Visual Studio Installer)
- **JDK 11+** with JAVA_HOME environment variable set

### macOS

- **Xcode Command Line Tools**: `xcode-select --install`
- **CMake**: `brew install cmake`
- **JDK 11+**: `brew install openjdk@17`

### Linux (Ubuntu/Debian)

```bash
sudo apt-get update
sudo apt-get install -y build-essential cmake openjdk-17-jdk libasound2-dev
```

### Linux (Fedora)

```bash
sudo dnf install -y gcc-c++ cmake java-17-openjdk-devel alsa-lib-devel
```

### Linux (Arch)

```bash
sudo pacman -S base-devel cmake jdk17-openjdk alsa-lib
```

## Building

### Quick Start

#### Windows

```powershell
cd native
.\scripts\build_windows.ps1
```

#### macOS

```bash
cd native
chmod +x scripts/build_macos.sh
./scripts/build_macos.sh
```

#### Linux

```bash
cd native
chmod +x scripts/build_linux.sh
./scripts/build_linux.sh
```

### Build Options

All build scripts support the following options:

- **Configuration**: `release` or `debug` (default: release)
- **Clean**: Clean build directory before building
- **Test**: Run tests after building

#### Windows Examples

```powershell
# Debug build
.\scripts\build_windows.ps1 -Config Debug

# Clean release build
.\scripts\build_windows.ps1 -Config Release -Clean

# Build and test
.\scripts\build_windows.ps1 -Test
```

#### macOS/Linux Examples

```bash
# Debug build
./scripts/build_macos.sh debug

# Clean release build
./scripts/build_macos.sh release clean

# Build and test
./scripts/build_macos.sh release test

# Universal binary (macOS only)
./scripts/build_macos.sh release universal
```

### Manual Build

If you prefer to build manually:

```bash
# Create build directory
mkdir -p build
cd build

# Configure
cmake .. -DCMAKE_BUILD_TYPE=Release

# Build
cmake --build . --config Release --parallel

# Test (optional)
ctest --build-config Release --output-on-failure
```

## Docker Builds

For reproducible builds, use Docker:

### Linux

```bash
cd native
docker build -f docker/Dockerfile.linux -t piper-jni-linux .
docker run --rm -v $(pwd):/workspace piper-jni-linux
```

### Windows (Cross-compilation)

```bash
cd native
docker build -f docker/Dockerfile.windows -t piper-jni-windows .
docker run --rm -v $(pwd):/workspace piper-jni-windows
```

## CI/CD

The project uses GitHub Actions for automated builds across all platforms. See `.github/workflows/build-native-libs.yml` for the complete CI/CD pipeline.

### Workflow Triggers

- Push to `main`, `develop`, or `feature/piper-jni-*` branches
- Pull requests to `main` or `develop`
- Manual workflow dispatch

### Artifacts

Build artifacts are automatically uploaded and available for download:

- `windows-x64-libs`: Windows DLL
- `macos-x64-libs`: macOS Intel dylib
- `macos-arm64-libs`: macOS Apple Silicon dylib
- `linux-x64-libs`: Linux shared object
- `piper-jni-release`: Combined release package

## Output

Built libraries are automatically copied to:

```
domain/src/desktopMain/resources/native/
├── windows/
│   └── piper_jni.dll
├── macos-x64/
│   └── libpiper_jni.dylib
├── macos-arm64/
│   └── libpiper_jni.dylib
└── linux/
    └── libpiper_jni.so
```

These are packaged with the Kotlin application and loaded at runtime.

## Troubleshooting

### CMake can't find JNI

Ensure JAVA_HOME is set:

```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# macOS/Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### Visual Studio not found (Windows)

Install Visual Studio 2019 or later with "Desktop development with C++" workload.

### Missing libraries (Linux)

Install development libraries:

```bash
sudo apt-get install -y libasound2-dev libpulse-dev
```

### Permission denied (macOS/Linux)

Make scripts executable:

```bash
chmod +x scripts/*.sh
```

### Build fails with "Piper not found"

This is expected for initial builds. The stub implementation will compile successfully. Piper library integration will be added in task 2.

## Development

### Adding New Source Files

1. Add the source file to `src/` directory
2. Update `CMakeLists.txt` to include the new file in `SOURCES`
3. Rebuild

### Modifying JNI Functions

1. Update function signatures in `include/piper_jni/piper_jni.h`
2. Implement in `src/jni/piper_jni.cpp`
3. Update corresponding Kotlin declarations in `PiperNative.kt`
4. Rebuild

### Platform-Specific Code

Use preprocessor directives:

```cpp
#ifdef _WIN32
    // Windows-specific code
#elif __APPLE__
    // macOS-specific code
#elif __linux__
    // Linux-specific code
#endif
```

## Testing

Run tests after building:

```bash
cd build/<platform>
ctest --output-on-failure
```

Comprehensive tests will be added in task 8.

## Performance

Target performance metrics:

- **Initialization**: < 2 seconds
- **Short text synthesis**: < 200ms (100 chars)
- **Memory usage**: < 500 MB per voice model
- **CPU usage**: < 30% during synthesis

## License

This project is part of the Infinity reader application. See the main LICENSE file for details.

## Support

For issues or questions:

1. Check this README and troubleshooting section
2. Review GitHub Actions build logs
3. Open an issue on the project repository

## Next Steps

After setting up the build infrastructure:

1. **Task 2**: Implement core JNI wrapper with Piper integration
2. **Task 3**: Add memory optimization and performance enhancements
3. **Task 4**: Build libraries for all platforms
4. **Task 5**: Implement voice model management system

See `.kiro/specs/piper-jni-production/tasks.md` for the complete implementation plan.
