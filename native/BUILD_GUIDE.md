# Piper JNI Build Guide

Complete guide for building the Piper JNI native libraries across all supported platforms.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Platform-Specific Instructions](#platform-specific-instructions)
4. [Docker Builds](#docker-builds)
5. [Troubleshooting](#troubleshooting)
6. [Advanced Configuration](#advanced-configuration)

## Prerequisites

### Required Tools

| Tool | Minimum Version | Purpose |
|------|----------------|---------|
| CMake | 3.15+ | Build system generator |
| C++ Compiler | C++17 | Compile native code |
| JDK | 11+ | JNI headers and runtime |

### Platform-Specific Requirements

#### Windows
- Visual Studio 2019 or later with "Desktop development with C++" workload
- Windows 10 SDK
- CMake (can be installed via Visual Studio Installer)

#### macOS
- Xcode Command Line Tools
- macOS 10.14 (Mojave) or later
- Homebrew (recommended for package management)

#### Linux
- GCC 7+ or Clang 6+
- ALSA development libraries
- PulseAudio development libraries (optional)

## Quick Start

### 1. Install Dependencies

Run the automated setup script:

```bash
cd native
chmod +x scripts/setup_deps.sh
./scripts/setup_deps.sh
```

Or install manually (see [Platform-Specific Instructions](#platform-specific-instructions)).

### 2. Build

#### Windows
```powershell
cd native
.\scripts\build_windows.ps1
```

#### macOS
```bash
cd native
./scripts/build_macos.sh
```

#### Linux
```bash
cd native
./scripts/build_linux.sh
```

### 3. Verify

Check that the library was built:

- **Windows**: `build/windows/Release/piper_jni.dll`
- **macOS**: `build/macos-*/libpiper_jni.dylib`
- **Linux**: `build/linux/libpiper_jni.so`

## Platform-Specific Instructions

### Windows

#### Prerequisites Installation

1. **Install Visual Studio 2022**
   - Download from: https://visualstudio.microsoft.com/
   - Select "Desktop development with C++" workload
   - Include "C++ CMake tools for Windows"

2. **Install JDK**
   - Download from: https://adoptium.net/
   - Install to default location
   - Set JAVA_HOME environment variable:
     ```powershell
     [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot", "Machine")
     ```

3. **Verify Installation**
   ```powershell
   cmake --version
   cl.exe  # Should show MSVC compiler version
   java -version
   echo $env:JAVA_HOME
   ```

#### Building

```powershell
# Navigate to native directory
cd native

# Basic build
.\scripts\build_windows.ps1

# Debug build
.\scripts\build_windows.ps1 -Config Debug

# Clean build
.\scripts\build_windows.ps1 -Clean

# Build and test
.\scripts\build_windows.ps1 -Test

# Get help
.\scripts\build_windows.ps1 -Help
```

#### Manual Build

```powershell
mkdir build\windows
cd build\windows

cmake ..\.. -G "Visual Studio 17 2022" -A x64
cmake --build . --config Release --parallel

# Output: Release\piper_jni.dll
```

### macOS

#### Prerequisites Installation

1. **Install Xcode Command Line Tools**
   ```bash
   xcode-select --install
   ```

2. **Install Homebrew** (if not already installed)
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```

3. **Install Dependencies**
   ```bash
   brew install cmake
   brew install openjdk@17
   
   # Link Java
   sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk \
     /Library/Java/JavaVirtualMachines/openjdk-17.jdk
   ```

4. **Set JAVA_HOME**
   ```bash
   # Add to ~/.zshrc or ~/.bash_profile
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home
   export PATH=$JAVA_HOME/bin:$PATH
   ```

5. **Verify Installation**
   ```bash
   cmake --version
   clang++ --version
   java -version
   echo $JAVA_HOME
   ```

#### Building

```bash
# Navigate to native directory
cd native

# Make script executable
chmod +x scripts/build_macos.sh

# Basic build
./scripts/build_macos.sh

# Debug build
./scripts/build_macos.sh debug

# Clean build
./scripts/build_macos.sh clean

# Build and test
./scripts/build_macos.sh test

# Universal binary (x86_64 + arm64)
./scripts/build_macos.sh universal

# Get help
./scripts/build_macos.sh help
```

#### Manual Build

```bash
mkdir -p build/macos
cd build/macos

cmake ../.. \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_OSX_DEPLOYMENT_TARGET=10.14

cmake --build . --parallel $(sysctl -n hw.ncpu)

# Output: libpiper_jni.dylib
```

#### Building Universal Binary

```bash
cmake ../.. \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_OSX_ARCHITECTURES="x86_64;arm64" \
  -DCMAKE_OSX_DEPLOYMENT_TARGET=10.14

cmake --build . --parallel $(sysctl -n hw.ncpu)

# Verify architectures
lipo -info libpiper_jni.dylib
# Output: Architectures in the fat file: libpiper_jni.dylib are: x86_64 arm64
```

### Linux

#### Prerequisites Installation

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install -y \
  build-essential \
  cmake \
  git \
  openjdk-17-jdk \
  libasound2-dev \
  libpulse-dev \
  pkg-config
```

**Fedora/RHEL:**
```bash
sudo dnf install -y \
  gcc-c++ \
  cmake \
  git \
  java-17-openjdk-devel \
  alsa-lib-devel \
  pulseaudio-libs-devel \
  pkg-config
```

**Arch Linux:**
```bash
sudo pacman -S \
  base-devel \
  cmake \
  git \
  jdk17-openjdk \
  alsa-lib \
  libpulse \
  pkg-config
```

**Set JAVA_HOME:**
```bash
# Add to ~/.bashrc or ~/.zshrc
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64  # Ubuntu/Debian
# or
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk  # Fedora/Arch

export PATH=$JAVA_HOME/bin:$PATH
```

**Verify Installation:**
```bash
cmake --version
g++ --version
java -version
echo $JAVA_HOME
```

#### Building

```bash
# Navigate to native directory
cd native

# Make script executable
chmod +x scripts/build_linux.sh

# Basic build
./scripts/build_linux.sh

# Debug build
./scripts/build_linux.sh debug

# Clean build
./scripts/build_linux.sh clean

# Build and test
./scripts/build_linux.sh test

# Get help
./scripts/build_linux.sh help
```

#### Manual Build

```bash
mkdir -p build/linux
cd build/linux

cmake ../.. \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_CXX_FLAGS="-D_GLIBCXX_USE_CXX11_ABI=0"

cmake --build . --parallel $(nproc)

# Output: libpiper_jni.so
```

## Docker Builds

For reproducible builds across platforms, use Docker.

### Linux Build

```bash
cd native

# Build Docker image
docker build -f docker/Dockerfile.linux -t piper-jni-linux .

# Run build
docker run --rm -v $(pwd):/workspace piper-jni-linux

# Output in: build/linux/libpiper_jni.so
```

### Windows Cross-Compilation

```bash
cd native

# Build Docker image
docker build -f docker/Dockerfile.windows -t piper-jni-windows .

# Run build
docker run --rm -v $(pwd):/workspace piper-jni-windows

# Output in: build/windows/piper_jni.dll
```

### Custom Docker Build

```bash
# Build with custom configuration
docker run --rm -v $(pwd):/workspace piper-jni-linux \
  bash -c "cd /workspace && ./scripts/build_linux.sh debug"
```

## Troubleshooting

### Common Issues

#### CMake can't find JNI

**Problem:** `Could NOT find JNI`

**Solution:**
```bash
# Set JAVA_HOME
export JAVA_HOME=/path/to/jdk
# or on Windows:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

#### Visual Studio not found (Windows)

**Problem:** `Could not find Visual Studio`

**Solution:**
- Install Visual Studio 2019 or later
- Include "Desktop development with C++" workload
- Restart terminal after installation

#### Missing ALSA libraries (Linux)

**Problem:** `alsa/asoundlib.h: No such file or directory`

**Solution:**
```bash
# Ubuntu/Debian
sudo apt-get install libasound2-dev

# Fedora
sudo dnf install alsa-lib-devel

# Arch
sudo pacman -S alsa-lib
```

#### Permission denied (macOS/Linux)

**Problem:** `Permission denied` when running scripts

**Solution:**
```bash
chmod +x scripts/*.sh
```

#### Library not found at runtime

**Problem:** `java.lang.UnsatisfiedLinkError`

**Solution:**
- Verify library was copied to resources directory
- Check library architecture matches JVM architecture
- On macOS, check code signing: `codesign -v libpiper_jni.dylib`

### Build Warnings

#### Piper library not found

This is expected for initial builds. The stub implementation will compile successfully. Piper integration will be added in task 2.

#### ONNX Runtime not found

This is expected for initial builds. ONNX Runtime integration will be added in task 2.

## Advanced Configuration

### Custom CMake Options

```bash
cmake ../.. \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_INSTALL_PREFIX=/custom/path \
  -DPIPER_ROOT=/path/to/piper \
  -DONNXRUNTIME_ROOT=/path/to/onnxruntime
```

### Cross-Compilation

#### Linux to Windows (MinGW)

```bash
cmake ../.. \
  -DCMAKE_TOOLCHAIN_FILE=mingw-toolchain.cmake \
  -DCMAKE_BUILD_TYPE=Release
```

#### macOS Universal Binary

```bash
cmake ../.. \
  -DCMAKE_OSX_ARCHITECTURES="x86_64;arm64" \
  -DCMAKE_BUILD_TYPE=Release
```

### Optimization Flags

#### Maximum Performance

```bash
cmake ../.. \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_CXX_FLAGS="-O3 -march=native -flto"
```

#### Debug with Symbols

```bash
cmake ../.. \
  -DCMAKE_BUILD_TYPE=Debug \
  -DCMAKE_CXX_FLAGS="-g -O0"
```

## Verification

### Check Library

```bash
# Linux
ldd libpiper_jni.so
file libpiper_jni.so

# macOS
otool -L libpiper_jni.dylib
file libpiper_jni.dylib
lipo -info libpiper_jni.dylib

# Windows (PowerShell)
dumpbin /dependents piper_jni.dll
```

### Run Tests

```bash
cd build/<platform>
ctest --output-on-failure --verbose
```

## Next Steps

After successful build:

1. Library is automatically copied to `domain/src/desktopMain/resources/native/<platform>/`
2. Proceed to task 2: Implement core JNI wrapper
3. See `.kiro/specs/piper-jni-production/tasks.md` for full implementation plan

## Support

For issues:
1. Check this guide and troubleshooting section
2. Review build logs in `build/<platform>/`
3. Check GitHub Actions CI logs
4. Open an issue on the repository
