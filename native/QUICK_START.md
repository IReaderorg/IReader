# Quick Start Guide

Get up and running with Piper JNI native library builds in minutes.

## Prerequisites

### Windows
```powershell
# Install Visual Studio 2022 with C++ tools
# Install JDK 17+
# Set JAVA_HOME environment variable
```

### macOS
```bash
xcode-select --install
brew install cmake openjdk@17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home
```

### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install -y build-essential cmake openjdk-17-jdk libasound2-dev
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

## Build Commands

### Windows
```powershell
cd native
.\scripts\build_windows.ps1
```

### macOS
```bash
cd native
chmod +x scripts/build_macos.sh
./scripts/build_macos.sh
```

### Linux
```bash
cd native
chmod +x scripts/build_linux.sh
./scripts/build_linux.sh
```

## Output

Built libraries are automatically copied to:
```
domain/src/desktopMain/resources/native/<platform>/
```

## Common Options

### Debug Build
```bash
# Windows
.\scripts\build_windows.ps1 -Config Debug

# macOS/Linux
./scripts/build_macos.sh debug
```

### Clean Build
```bash
# Windows
.\scripts\build_windows.ps1 -Clean

# macOS/Linux
./scripts/build_macos.sh clean
```

### Run Tests
```bash
# Windows
.\scripts\build_windows.ps1 -Test

# macOS/Linux
./scripts/build_macos.sh test
```

## Troubleshooting

### CMake not found
Install CMake from https://cmake.org/download/

### JAVA_HOME not set
```bash
# Find Java installation
# Windows: where java
# macOS/Linux: which java

# Set JAVA_HOME
export JAVA_HOME=/path/to/jdk
```

### Build fails
1. Check prerequisites are installed
2. Verify JAVA_HOME is set correctly
3. See BUILD_GUIDE.md for detailed troubleshooting

## Docker Alternative

```bash
cd native
docker build -f docker/Dockerfile.linux -t piper-jni-linux .
docker run --rm -v $(pwd):/workspace piper-jni-linux
```

## Help

For detailed instructions, see:
- **README.md** - Overview and architecture
- **BUILD_GUIDE.md** - Comprehensive build instructions
- **TASK_1_SUMMARY.md** - Implementation details

## Next Steps

After successful build:
1. Library is in `build/<platform>/`
2. Copy is in `domain/src/desktopMain/resources/native/<platform>/`
3. Ready for Task 2: Implement JNI wrapper
