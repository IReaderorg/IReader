# Building piper_jni.dll - Quick Guide

## Prerequisites

1. **Visual Studio 2022** (or 2019)
   - Install "Desktop development with C++" workload
   - Download: https://visualstudio.microsoft.com/downloads/

2. **CMake 3.15+**
   - Download: https://cmake.org/download/
   - Add to PATH during installation

3. **JDK 11+**
   - Set `JAVA_HOME` environment variable
   - Example: `C:\Program Files\Java\jdk-17`

## Quick Build

### Method 1: PowerShell Script (Easiest)

```powershell
cd native/scripts
.\build_windows.ps1 -Config Release
```

The DLL will be automatically copied to:
`domain/src/desktopMain/resources/native/windows/piper_jni.dll`

### Method 2: Manual CMake

```powershell
# From project root
cd native

# Create and enter build directory
mkdir build\windows -Force
cd build\windows

# Configure
cmake ../.. -G "Visual Studio 17 2022" -A x64

# Build
cmake --build . --config Release

# Copy DLL manually
copy Release\piper_jni.dll ..\..\domain\src\desktopMain\resources\native\windows\
```

## Verify Build

Check if the DLL was created:

```powershell
# Check build output
dir native\build\windows\Release\piper_jni.dll

# Check resources directory
dir domain\src\desktopMain\resources\native\windows\piper_jni.dll
```

## Troubleshooting

### Error: "CMake not found"
- Install CMake from https://cmake.org/download/
- Restart PowerShell after installation

### Error: "JAVA_HOME not set"
```powershell
# Find Java installation
where java

# Set JAVA_HOME (adjust path)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Or set permanently in System Environment Variables
```

### Error: "Visual Studio not found"
- Install Visual Studio 2022 Community Edition
- Select "Desktop development with C++" workload
- Restart PowerShell after installation

### Error: "JNI headers not found"
- Verify JAVA_HOME points to JDK (not JRE)
- JDK should have `include/jni.h` file

### Build succeeds but DLL not copied
```powershell
# Manually copy the DLL
$source = "native\build\windows\Release\piper_jni.dll"
$dest = "domain\src\desktopMain\resources\native\windows\"

# Create destination directory
mkdir $dest -Force

# Copy DLL
copy $source $dest
```

## What Gets Built

The build process creates:

1. **piper_jni.dll** - Main JNI bridge library
   - Location: `native/build/windows/Release/piper_jni.dll`
   - Auto-copied to: `domain/src/desktopMain/resources/native/windows/`

2. **Test executables** (if tests enabled)
   - Location: `native/build/windows/test/Release/`

## Dependencies

The `piper_jni.dll` depends on these libraries (already in your resources):
- `onnxruntime.dll` - ONNX Runtime
- `espeak-ng.dll` - Phonemization
- `piper_phonemize.dll` - Piper phonemizer

All dependencies should be in the same directory as `piper_jni.dll`.

## Build Configurations

### Debug Build
```powershell
.\build_windows.ps1 -Config Debug
```
- Includes debug symbols
- Larger file size
- Useful for debugging

### Release Build (Recommended)
```powershell
.\build_windows.ps1 -Config Release
```
- Optimized for performance
- Smaller file size
- Use for production

## Clean Build

To start fresh:

```powershell
# Using script
.\build_windows.ps1 -Clean -Config Release

# Or manually
Remove-Item -Recurse -Force native\build\windows
```

## Next Steps

After building:

1. **Verify the DLL exists**:
   ```powershell
   dir domain\src\desktopMain\resources\native\windows\piper_jni.dll
   ```

2. **Run the Kotlin application**:
   ```powershell
   .\gradlew desktop:run
   ```

3. **Test TTS functionality**:
   - Open a book
   - Click the TTS button
   - Select a voice
   - Start playback

## Common Issues

### "Library not found" at runtime
- Ensure all DLLs are in the same directory
- Check that the path in `PiperNative.kt` is correct

### "UnsatisfiedLinkError"
- DLL architecture mismatch (ensure x64)
- Missing dependencies (check all DLLs present)
- Rebuild with matching JDK version

### Build is slow
- Use `-parallel` flag: `cmake --build . --parallel`
- Close other applications
- Use SSD for build directory

## Support

If you encounter issues:

1. Check the build log in `native/build/windows/`
2. Verify all prerequisites are installed
3. Try a clean build
4. Check the troubleshooting section above

## Quick Reference

```powershell
# Full build with tests
cd native/scripts
.\build_windows.ps1 -Config Release -Test

# Clean and rebuild
.\build_windows.ps1 -Clean -Config Release

# Debug build
.\build_windows.ps1 -Config Debug

# Show help
.\build_windows.ps1 -Help
```
