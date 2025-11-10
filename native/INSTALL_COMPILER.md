# Installing C++ Compiler for Windows

Quick guide to install a C++ compiler to complete your build environment.

## Current Status

✅ CMake 4.2.0 - Installed  
✅ Java 24 - Installed  
❌ C++ Compiler - **Not Installed** (Required)

## Option 1: Visual Studio 2022 (Recommended)

### Why Visual Studio?
- Official Microsoft compiler
- Best Windows compatibility
- Excellent debugging tools
- Free Community edition

### Installation Steps

1. **Download**
   - Go to: https://visualstudio.microsoft.com/downloads/
   - Click "Free download" under "Community 2022"
   - File size: ~3-4 MB (installer), ~7 GB (full installation)

2. **Run Installer**
   - Run `VisualStudioSetup.exe`
   - Wait for installer to load

3. **Select Workload**
   - ✅ Check "Desktop development with C++"
   - This includes:
     - MSVC compiler
     - Windows SDK
     - CMake tools
     - C++ core features

4. **Optional Components** (Recommended)
   - ✅ C++ CMake tools for Windows
   - ✅ Windows 10 SDK (latest version)
   - ✅ C++ Clang tools for Windows

5. **Install**
   - Click "Install" button
   - Wait 30-60 minutes (depending on internet speed)
   - Restart computer when prompted

6. **Verify Installation**
   ```powershell
   # Open new PowerShell window
   # Check compiler
   cl.exe
   # Should show: Microsoft (R) C/C++ Optimizing Compiler Version...
   ```

7. **Build Piper JNI**
   ```powershell
   cd native
   .\scripts\build_windows.ps1
   ```

### Disk Space Required
- Installer: ~4 MB
- Installation: ~7 GB
- Total: ~7 GB

## Option 2: MinGW-w64 (Lightweight)

### Why MinGW?
- Smaller download (~500 MB)
- Faster installation
- GCC compiler (Linux-like)
- Good for cross-platform development

### Installation via MSYS2 (Recommended)

1. **Download MSYS2**
   - Go to: https://www.msys2.org/
   - Download installer: `msys2-x86_64-<date>.exe`
   - File size: ~100 MB

2. **Install MSYS2**
   - Run installer
   - Install to: `C:\msys64` (default)
   - Click through installation

3. **Update MSYS2**
   ```bash
   # In MSYS2 terminal (opens automatically)
   pacman -Syu
   # Close terminal when prompted
   # Reopen MSYS2 terminal
   pacman -Su
   ```

4. **Install MinGW Toolchain**
   ```bash
   # Install GCC compiler
   pacman -S mingw-w64-x86_64-gcc
   
   # Install CMake
   pacman -S mingw-w64-x86_64-cmake
   
   # Install Make
   pacman -S mingw-w64-x86_64-make
   ```

5. **Add to PATH**
   ```powershell
   # In PowerShell (as Administrator)
   $env:Path += ";C:\msys64\mingw64\bin"
   [Environment]::SetEnvironmentVariable("Path", $env:Path, "Machine")
   ```

6. **Verify Installation**
   ```powershell
   # Open new PowerShell window
   g++ --version
   # Should show: g++ (Rev...) ...
   ```

7. **Build Piper JNI**
   ```powershell
   cd native
   cmake -S . -B build/windows -G "MinGW Makefiles"
   cmake --build build/windows --parallel
   ```

### Disk Space Required
- Installer: ~100 MB
- Installation: ~500 MB
- Total: ~600 MB

## Option 3: Docker (No Local Compiler)

### Why Docker?
- No local compiler installation
- Reproducible builds
- Same environment as CI/CD
- Cross-platform builds

### Installation Steps

1. **Install Docker Desktop**
   - Go to: https://www.docker.com/products/docker-desktop/
   - Download for Windows
   - Install and restart

2. **Build Using Docker**
   ```powershell
   cd native
   
   # Build Linux version (works on Windows via Docker)
   docker build -f docker/Dockerfile.linux -t piper-jni-linux .
   docker run --rm -v ${PWD}:/workspace piper-jni-linux
   ```

3. **Output**
   - Library will be in: `build/linux/libpiper_jni.so`
   - Note: This builds Linux version, not Windows

### Disk Space Required
- Docker Desktop: ~500 MB
- Docker images: ~1-2 GB
- Total: ~2-3 GB

## Comparison

| Feature | Visual Studio | MinGW | Docker |
|---------|--------------|-------|--------|
| Download Size | ~4 GB | ~500 MB | ~500 MB |
| Install Time | 30-60 min | 10-15 min | 10-15 min |
| Disk Space | ~7 GB | ~600 MB | ~2-3 GB |
| Windows Native | ✅ Yes | ✅ Yes | ❌ No (Linux) |
| Debugging | ✅ Excellent | ⚠️ Basic | ❌ Limited |
| IDE Included | ✅ Yes | ❌ No | ❌ No |
| Recommended | ✅ Best | ⚠️ Alternative | ⚠️ CI/CD |

## Recommendation

**For this project, install Visual Studio 2022 Community:**

✅ Best Windows compatibility  
✅ Official Microsoft toolchain  
✅ Matches CI/CD environment  
✅ Excellent debugging support  
✅ Free for open source projects  

## After Installation

Once you have a C++ compiler installed:

1. **Verify Setup**
   ```powershell
   cd native
   cmake --version  # Should show 4.2.0
   java -version    # Should show Java 24
   cl.exe           # (VS) or g++ --version (MinGW)
   ```

2. **Test Build**
   ```powershell
   # Visual Studio
   .\scripts\build_windows.ps1
   
   # MinGW
   cmake -S . -B build/windows -G "MinGW Makefiles"
   cmake --build build/windows
   ```

3. **Check Output**
   ```powershell
   # Library should be at:
   ls build/windows/Release/piper_jni.dll  # Visual Studio
   # or
   ls build/windows/libpiper_jni.dll       # MinGW
   ```

## Troubleshooting

### "cl.exe not found" after VS installation
- Restart PowerShell/Terminal
- Or run: `"C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"`

### "g++ not found" after MinGW installation
- Check PATH includes: `C:\msys64\mingw64\bin`
- Restart PowerShell/Terminal

### CMake can't find compiler
```powershell
# Specify compiler explicitly
cmake -S . -B build -DCMAKE_C_COMPILER=cl -DCMAKE_CXX_COMPILER=cl
# or for MinGW
cmake -S . -B build -DCMAKE_C_COMPILER=gcc -DCMAKE_CXX_COMPILER=g++
```

## Need Help?

See detailed troubleshooting in:
- [BUILD_GUIDE.md](BUILD_GUIDE.md) - Comprehensive build instructions
- [BUILD_STATUS.md](BUILD_STATUS.md) - Current environment status
- [README.md](README.md) - Project overview

## Quick Links

- Visual Studio: https://visualstudio.microsoft.com/downloads/
- MSYS2/MinGW: https://www.msys2.org/
- Docker Desktop: https://www.docker.com/products/docker-desktop/
- CMake: https://cmake.org/download/

---

**Next**: After installing compiler, run `.\scripts\build_windows.ps1` to build the library!
