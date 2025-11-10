# Build Status - Piper JNI Native Libraries

This document tracks the build status of native libraries for all supported platforms.

## Build Status

| Platform | Status | Library | Size | Last Built |
|----------|--------|---------|------|------------|
| Windows x64 | ✅ Built | `piper_jni.dll` | ~67 KB | 2025-11-10 |
| macOS x64 | ⏳ Pending | `libpiper_jni.dylib` | - | - |
| macOS ARM64 | ⏳ Pending | `libpiper_jni.dylib` | - | - |
| Linux x64 | ⏳ Pending | `libpiper_jni.so` | - | - |

## Build Instructions

### Windows x64 ✅

**Status:** Successfully built on local machine

**Prerequisites:**
- Visual Studio 2019+ with C++ development tools
- CMake 3.15+
- JDK 11+

**Build Command:**
```powershell
.\native\scripts\build_windows.ps1 -Config Release
```

**Output Location:**
- Build: `native/build/windows/Release/piper_jni.dll`
- Resources: `domain/src/desktopMain/resources/native/windows/piper_jni.dll`

**Verification:**
```powershell
Test-Path native\build\windows\Release\piper_jni.dll
# Should return: True
```

**Test Results:**
```
Test project C:/Users/PC/StudioProjects/IReader/native/build/windows
    Start 1: BasicTest
1/1 Test #1: BasicTest ........................   Passed    0.02 sec

100% tests passed, 0 tests failed out of 1
```

---

### macOS x64 (Intel) ⏳

**Status:** Requires macOS machine or CI/CD

**Prerequisites:**
- Xcode Command Line Tools
- CMake 3.15+
- JDK 11+

**Build Command:**
```bash
cd native
chmod +x scripts/build_macos.sh
./scripts/build_macos.sh release
```

**Output Location:**
- Build: `native/build/macos-x64/libpiper_jni.dylib`
- Resources: `domain/src/desktopMain/resources/native/macos-x64/libpiper_jni.dylib`

**Verification:**
```bash
file native/build/macos-x64/libpiper_jni.dylib
lipo -info native/build/macos-x64/libpiper_jni.dylib
# Should show: x86_64
```

**CI/CD:** GitHub Actions workflow configured for `macos-13` runner (Intel)

---

### macOS ARM64 (Apple Silicon) ⏳

**Status:** Requires Apple Silicon Mac or CI/CD

**Prerequisites:**
- Xcode Command Line Tools
- CMake 3.15+
- JDK 11+

**Build Command:**
```bash
cd native
chmod +x scripts/build_macos.sh
./scripts/build_macos.sh release
```

**Universal Binary (Optional):**
```bash
./scripts/build_macos.sh release universal
# Builds for both x86_64 and arm64
```

**Output Location:**
- Build: `native/build/macos-arm64/libpiper_jni.dylib`
- Resources: `domain/src/desktopMain/resources/native/macos-arm64/libpiper_jni.dylib`

**Verification:**
```bash
file native/build/macos-arm64/libpiper_jni.dylib
lipo -info native/build/macos-arm64/libpiper_jni.dylib
# Should show: arm64
```

**CI/CD:** GitHub Actions workflow configured for `macos-14` runner (Apple Silicon)

---

### Linux x64 ⏳

**Status:** Requires Linux machine or CI/CD

**Prerequisites:**
- GCC/G++ 7+
- CMake 3.15+
- JDK 11+
- Development libraries: `libasound2-dev`

**Install Dependencies:**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y build-essential cmake openjdk-17-jdk libasound2-dev

# Fedora
sudo dnf install -y gcc-c++ cmake java-17-openjdk-devel alsa-lib-devel

# Arch
sudo pacman -S base-devel cmake jdk17-openjdk alsa-lib
```

**Build Command:**
```bash
cd native
chmod +x scripts/build_linux.sh
./scripts/build_linux.sh release
```

**Output Location:**
- Build: `native/build/linux/libpiper_jni.so`
- Resources: `domain/src/desktopMain/resources/native/linux/libpiper_jni.so`

**Verification:**
```bash
file native/build/linux/libpiper_jni.so
ldd native/build/linux/libpiper_jni.so
# Should show shared library dependencies
```

**CI/CD:** GitHub Actions workflow configured for `ubuntu-latest` runner

---

## CI/CD Build Process

### GitHub Actions Workflow

The project includes a comprehensive GitHub Actions workflow that automatically builds libraries for all platforms:

**File:** `.github/workflows/build-native-libs.yml`

**Triggers:**
- Push to `main`, `develop`, or `feature/piper-jni-*` branches
- Pull requests to `main` or `develop`
- Manual workflow dispatch

**Jobs:**
1. `build-windows` - Builds Windows x64 library
2. `build-macos-x64` - Builds macOS Intel library
3. `build-macos-arm64` - Builds macOS Apple Silicon library
4. `build-linux` - Builds Linux x64 library
5. `package-release` - Packages all libraries into a release artifact

**Artifacts:**
- `windows-x64-libs` - Windows DLL
- `macos-x64-libs` - macOS Intel dylib
- `macos-arm64-libs` - macOS ARM64 dylib
- `linux-x64-libs` - Linux shared object
- `piper-jni-release` - Combined release package with checksums

**Retention:** 30 days for individual artifacts, 90 days for release package

---

## Verification

### Verify All Libraries

Run the verification script to check which libraries are present:

```powershell
.\native\scripts\verify_build.ps1
```

**Expected Output:**
```
=== Piper JNI Library Verification ===

Checking libraries in: domain/src/desktopMain/resources/native

Platform: windows
  Expected: domain/src/desktopMain/resources/native/windows/piper_jni.dll
  ✓ Found (67.5 KB)
  Last modified: 11/10/2025 7:46:16 PM

Platform: macos-x64
  Expected: domain/src/desktopMain/resources/native/macos-x64/libpiper_jni.dylib
  ✗ Not found

Platform: macos-arm64
  Expected: domain/src/desktopMain/resources/native/macos-arm64/libpiper_jni.dylib
  ✗ Not found

Platform: linux
  Expected: domain/src/desktopMain/resources/native/linux/libpiper_jni.so
  ✗ Not found

=== Verification Summary ===
Total platforms: 4
Present: 1
Missing: 3
```

---

## Next Steps

### For Local Development

1. **Windows:** ✅ Complete - Library built and tested
2. **macOS:** Use GitHub Actions or build on macOS machine
3. **Linux:** Use GitHub Actions or build on Linux machine

### For Production Release

1. **Trigger CI/CD:** Push to `main` branch or manually trigger workflow
2. **Download Artifacts:** Download all platform libraries from GitHub Actions
3. **Verify Checksums:** Verify integrity using `checksums.txt`
4. **Deploy:** Copy libraries to `domain/src/desktopMain/resources/native/`

### Manual Cross-Platform Build

If you need to build all platforms locally:

1. **Use Docker:** Build containers for each platform
2. **Use Virtual Machines:** Set up VMs for macOS and Linux
3. **Use Cloud CI/CD:** GitHub Actions, GitLab CI, or similar

---

## Troubleshooting

### Windows Build Issues

**Problem:** CMake can't find JNI
```powershell
# Solution: Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

**Problem:** Visual Studio not found
```
# Solution: Install Visual Studio 2019+ with C++ workload
# Or use Visual Studio Build Tools
```

### macOS Build Issues

**Problem:** Xcode Command Line Tools not found
```bash
# Solution: Install Xcode Command Line Tools
xcode-select --install
```

**Problem:** Wrong architecture
```bash
# Solution: Check current architecture
uname -m
# Build for specific architecture or use universal build
```

### Linux Build Issues

**Problem:** Missing development libraries
```bash
# Solution: Install required packages
sudo apt-get install -y build-essential cmake libasound2-dev
```

**Problem:** GLIBC compatibility
```bash
# Solution: Build with older GLIBC
cmake .. -DCMAKE_CXX_FLAGS="-D_GLIBCXX_USE_CXX11_ABI=0"
```

---

## Build Metrics

### Windows x64

- **Build Time:** ~30 seconds
- **Library Size:** 67 KB
- **Dependencies:** MSVC Runtime (statically linked)
- **Minimum OS:** Windows 7+

### macOS x64 (Estimated)

- **Build Time:** ~45 seconds
- **Library Size:** ~80 KB (estimated)
- **Dependencies:** System libraries
- **Minimum OS:** macOS 10.14+

### macOS ARM64 (Estimated)

- **Build Time:** ~30 seconds
- **Library Size:** ~75 KB (estimated)
- **Dependencies:** System libraries
- **Minimum OS:** macOS 11.0+

### Linux x64 (Estimated)

- **Build Time:** ~40 seconds
- **Library Size:** ~85 KB (estimated)
- **Dependencies:** libasound2, glibc 2.27+
- **Minimum OS:** Ubuntu 18.04+, Fedora 28+, Arch (current)

---

## Related Documentation

- [Build Guide](BUILD_GUIDE.md) - Detailed build instructions
- [Quick Start](QUICK_START.md) - Quick start guide
- [README](README.md) - Project overview
- [Tasks](../.kiro/specs/piper-jni-production/tasks.md) - Implementation tasks

---

**Last Updated:** 2025-11-10
**Task:** 4. Build JNI libraries for all platforms
**Status:** In Progress (1/4 platforms complete)
