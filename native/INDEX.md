# Piper JNI Native Library - File Index

Quick reference to all files in the native library build system.

## 📚 Documentation

| File | Purpose |
|------|---------|
| [README.md](README.md) | Main overview and architecture |
| [BUILD_GUIDE.md](BUILD_GUIDE.md) | Comprehensive build instructions |
| [QUICK_START.md](QUICK_START.md) | Quick start for developers |
| [INSTALL_COMPILER.md](INSTALL_COMPILER.md) | C++ compiler installation guide |
| [BUILD_STATUS.md](BUILD_STATUS.md) | Current build environment status |
| [TASK_1_SUMMARY.md](TASK_1_SUMMARY.md) | Task 1 implementation summary |
| [INDEX.md](INDEX.md) | This file - navigation guide |

## 🔧 Build Configuration

| File | Purpose |
|------|---------|
| [CMakeLists.txt](CMakeLists.txt) | Root CMake configuration |
| [cmake/FindPiper.cmake](cmake/FindPiper.cmake) | Locate Piper library |
| [cmake/FindONNXRuntime.cmake](cmake/FindONNXRuntime.cmake) | Locate ONNX Runtime |
| [cmake/Platform.cmake](cmake/Platform.cmake) | Platform-specific settings |

## 🚀 Build Scripts

| File | Platform | Purpose |
|------|----------|---------|
| [scripts/build_windows.ps1](scripts/build_windows.ps1) | Windows | PowerShell build script |
| [scripts/build_macos.sh](scripts/build_macos.sh) | macOS | Bash build script |
| [scripts/build_linux.sh](scripts/build_linux.sh) | Linux | Bash build script |
| [scripts/setup_deps.sh](scripts/setup_deps.sh) | All | Dependency installer |

## 🐳 Docker

| File | Purpose |
|------|---------|
| [docker/Dockerfile.linux](docker/Dockerfile.linux) | Linux build environment |
| [docker/Dockerfile.windows](docker/Dockerfile.windows) | Windows cross-compile |
| [docker/Dockerfile.macos](docker/Dockerfile.macos) | macOS documentation |

## 💻 Source Code

### Headers
| File | Purpose |
|------|---------|
| [include/piper_jni/piper_jni.h](include/piper_jni/piper_jni.h) | JNI function declarations |

### JNI Implementation
| File | Purpose |
|------|---------|
| [src/jni/piper_jni.cpp](src/jni/piper_jni.cpp) | Main JNI functions |
| [src/jni/voice_manager.cpp](src/jni/voice_manager.cpp) | Voice instance management |
| [src/jni/audio_buffer.cpp](src/jni/audio_buffer.cpp) | Audio buffer pooling |
| [src/jni/error_handler.cpp](src/jni/error_handler.cpp) | Error handling |

### Wrapper
| File | Purpose |
|------|---------|
| [src/wrapper/piper_wrapper.cpp](src/wrapper/piper_wrapper.cpp) | Piper C++ wrapper |

## 🧪 Tests

| File | Purpose |
|------|---------|
| [test/CMakeLists.txt](test/CMakeLists.txt) | Test configuration |
| [test/basic_test.cpp](test/basic_test.cpp) | Basic build test |

## ⚙️ CI/CD

| File | Purpose |
|------|---------|
| [../.github/workflows/build-native-libs.yml](../.github/workflows/build-native-libs.yml) | GitHub Actions pipeline |

## 📝 Other

| File | Purpose |
|------|---------|
| [.gitignore](.gitignore) | Git exclusions |

## 📂 Directory Structure

```
native/
├── 📚 Documentation (7 files)
│   ├── README.md
│   ├── BUILD_GUIDE.md
│   ├── QUICK_START.md
│   ├── INSTALL_COMPILER.md
│   ├── BUILD_STATUS.md
│   ├── TASK_1_SUMMARY.md
│   └── INDEX.md
│
├── 🔧 Build System (4 files)
│   ├── CMakeLists.txt
│   └── cmake/
│       ├── FindPiper.cmake
│       ├── FindONNXRuntime.cmake
│       └── Platform.cmake
│
├── 🚀 Scripts (4 files)
│   └── scripts/
│       ├── build_windows.ps1
│       ├── build_macos.sh
│       ├── build_linux.sh
│       └── setup_deps.sh
│
├── 🐳 Docker (3 files)
│   └── docker/
│       ├── Dockerfile.linux
│       ├── Dockerfile.windows
│       └── Dockerfile.macos
│
├── 💻 Source Code (6 files)
│   ├── include/piper_jni/
│   │   └── piper_jni.h
│   ├── src/jni/
│   │   ├── piper_jni.cpp
│   │   ├── voice_manager.cpp
│   │   ├── audio_buffer.cpp
│   │   └── error_handler.cpp
│   └── src/wrapper/
│       └── piper_wrapper.cpp
│
├── 🧪 Tests (2 files)
│   └── test/
│       ├── CMakeLists.txt
│       └── basic_test.cpp
│
└── .gitignore
```

## 🎯 Quick Navigation

### I want to...

**Build the library**
→ See [QUICK_START.md](QUICK_START.md)

**Install C++ compiler**
→ See [INSTALL_COMPILER.md](INSTALL_COMPILER.md)

**Check my build environment**
→ See [BUILD_STATUS.md](BUILD_STATUS.md)

**Understand the architecture**
→ See [README.md](README.md)

**Troubleshoot build issues**
→ See [BUILD_GUIDE.md](BUILD_GUIDE.md) → Troubleshooting section

**Set up my development environment**
→ Run `scripts/setup_deps.sh` or see [BUILD_GUIDE.md](BUILD_GUIDE.md) → Prerequisites

**Understand what was implemented**
→ See [TASK_1_SUMMARY.md](TASK_1_SUMMARY.md)

**Modify the build configuration**
→ Edit [CMakeLists.txt](CMakeLists.txt) or [cmake/Platform.cmake](cmake/Platform.cmake)

**Add new source files**
→ Add to `src/` and update [CMakeLists.txt](CMakeLists.txt) SOURCES variable

**Run CI/CD locally**
→ Use Docker: `docker build -f docker/Dockerfile.linux .`

## 📊 Statistics

- **Total Files**: 27
- **Documentation**: 7 files
- **Build Configuration**: 4 files
- **Build Scripts**: 4 files
- **Docker Files**: 3 files
- **Source Code**: 6 files
- **Tests**: 2 files
- **Lines of Code**: ~2,500+ (excluding comments)

## 🔄 Build Flow

```
Developer
    ↓
[Build Script] → [CMake Configure] → [Platform Detection]
    ↓                                        ↓
[Compile C++] ← [Find Dependencies] ← [CMake Modules]
    ↓
[Link Library]
    ↓
[Copy to Resources] → domain/src/desktopMain/resources/native/
```

## 🌐 CI/CD Flow

```
Git Push/PR
    ↓
[GitHub Actions]
    ↓
[Parallel Builds]
    ├── Windows x64
    ├── macOS x64
    ├── macOS ARM64
    └── Linux x64
    ↓
[Run Tests]
    ↓
[Upload Artifacts]
    ↓
[Package Release] (on main branch)
```

## 📖 Reading Order

For new developers, read in this order:

1. [QUICK_START.md](QUICK_START.md) - Get building fast
2. [README.md](README.md) - Understand the system
3. [BUILD_GUIDE.md](BUILD_GUIDE.md) - Deep dive into builds
4. [TASK_1_SUMMARY.md](TASK_1_SUMMARY.md) - Implementation details
5. Source code in `src/` - Understand the implementation

## 🔗 Related Files

Outside the `native/` directory:

- **Kotlin Integration**: `domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/PiperNative.kt`
- **Resources**: `domain/src/desktopMain/resources/native/`
- **Spec**: `.kiro/specs/piper-jni-production/`

---

**Last Updated**: Task 1 Completion
**Status**: ✅ Build infrastructure complete
**Next**: Task 2 - Implement core JNI wrapper
