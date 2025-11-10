# Native Libraries Setup Summary

## ✅ Completed Tasks

### 1. ONNX Runtime Libraries Downloaded
All ONNX Runtime libraries have been successfully downloaded and placed in the correct directories:

```
desktop/src/main/resources/native/
├── windows-x64/
│   └── onnxruntime.dll (10.1 MB) ✅
├── macos-x64/
│   └── libonnxruntime.dylib (24.4 MB) ✅
├── macos-arm64/
│   └── libonnxruntime.dylib (21.7 MB) ✅
└── linux-x64/
    └── libonnxruntime.so (17.4 MB) ✅
```

### 2. Additional Windows Libraries
Piper support libraries for Windows have been extracted and placed:

```
desktop/src/main/resources/native/windows-x64/
├── piper_phonemize.dll ✅
├── espeak-ng.dll ✅
└── onnxruntime_providers_shared.dll ✅
```

### 3. Verification Task
The Gradle verification task confirms the current status:

```bash
./gradlew :desktop:verifyNativeLibraries
```

**Output:**
- ✅ All ONNX Runtime libraries present
- ⚠️ Piper JNI wrapper libraries missing (expected)

## ⚠️ Remaining Tasks

### Piper JNI Wrapper Libraries

The following JNI wrapper libraries still need to be built or obtained:

- ❌ `windows-x64/piper_jni.dll`
- ❌ `macos-x64/libpiper_jni.dylib`
- ❌ `macos-arm64/libpiper_jni.dylib`
- ❌ `linux-x64/libpiper_jni.so`

## Current Application Behavior

### Without JNI Libraries:
1. Application compiles and runs successfully ✅
2. TTS service initializes in **simulation mode** ✅
3. User sees: "TTS fallback to simulation mode: No voice model selected" ⚠️
4. TTS controls work but no actual audio is generated 📢

### With JNI Libraries (Future):
1. Full Piper TTS functionality 🎯
2. High-quality neural voice synthesis 🔊
3. Offline text-to-speech 📱
4. Multiple voice model support 🗣️

## Next Steps - Choose Your Path

### Path 1: Continue Development (Recommended Now)

**Action:** None required

The application works in simulation mode, allowing you to:
- Develop and test UI/UX
- Implement TTS controls and state management
- Work on other features
- Test the application flow

**When ready for real TTS:** Follow Path 2 or 3 below.

### Path 2: Build JNI Libraries (Production Solution)

**Action:** Follow instructions in `BUILD_NATIVE_LIBS.md`

**Requirements:**
- C++ compiler (Visual Studio, GCC, or Clang)
- CMake 3.15+
- JDK 11+ (for JNI headers)
- 2-4 hours for setup and compilation

**Benefits:**
- Best performance
- Native integration
- Full control over TTS pipeline

**Steps:**
1. Clone Piper repository
2. Create JNI wrapper code
3. Build with CMake
4. Copy libraries to project
5. Verify with `./gradlew :desktop:verifyNativeLibraries`

### Path 3: Process-Based Approach (Quick Alternative)

**Action:** Implement subprocess-based TTS

**Requirements:**
- Extract Piper CLI binaries from downloaded archives
- Implement process execution in Kotlin
- 1-2 hours for implementation

**Benefits:**
- No C++ compilation
- Works immediately
- Easier to debug
- Good for prototyping

**Trade-offs:**
- Higher overhead
- Less control
- Subprocess management complexity

## Documentation

### Created Files:
1. **`BUILD_NATIVE_LIBS.md`** - Detailed instructions for building JNI libraries
2. **`desktop/src/main/resources/native/STATUS.md`** - Current status and options
3. **`desktop/src/main/resources/native/README.md`** - Already existed, updated with audio config
4. **`download_native_libs.ps1`** - Script used to download ONNX Runtime (completed)
5. **`NATIVE_LIBS_SUMMARY.md`** - This file

### Existing Documentation:
- `domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/README.md`
- `domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/README.md`

## Verification Commands

### Check Library Status:
```bash
./gradlew :desktop:verifyNativeLibraries
```

### List Libraries:
```bash
# Windows (PowerShell)
Get-ChildItem desktop/src/main/resources/native -Recurse -File

# Linux/macOS
find desktop/src/main/resources/native -type f -ls
```

### Check Library Dependencies:
```bash
# Windows
dumpbin /dependents desktop/src/main/resources/native/windows-x64/onnxruntime.dll

# macOS
otool -L desktop/src/main/resources/native/macos-x64/libonnxruntime.dylib

# Linux
ldd desktop/src/main/resources/native/linux-x64/libonnxruntime.so
```

## Testing

### Current Testing (Simulation Mode):
```kotlin
// In your test or main code
val ttsService = DesktopTTSService()
ttsService.initialize()

// Check if Piper is available
if (ttsService.isPiperAvailable()) {
    println("Piper TTS is ready!")
} else {
    println("Running in simulation mode")
}

// This will work in simulation mode
ttsService.readText("Hello, world!")
```

### Future Testing (With JNI Libraries):
```kotlin
// Same code, but will use real TTS
val ttsService = DesktopTTSService()
ttsService.initialize()

// Should return true once JNI libraries are in place
assert(ttsService.isPiperAvailable())

// Will generate actual audio
ttsService.readText("Hello, world!")
```

## Troubleshooting

### Issue: "Native library not found in resources"

**Solution:**
- Verify files exist in `desktop/src/main/resources/native/[platform]/`
- Run `./gradlew :desktop:verifyNativeLibraries`
- Check file permissions

### Issue: "UnsatisfiedLinkError"

**Solution:**
- This is expected without JNI libraries
- Application will fall back to simulation mode
- See `BUILD_NATIVE_LIBS.md` to build JNI libraries

### Issue: "Unsupported platform"

**Solution:**
- Check OS and architecture: `System.getProperty("os.name")`, `System.getProperty("os.arch")`
- Verify platform detection in `NativeLibraryLoader.kt`
- Ensure libraries exist for your platform

## Performance Notes

### Current (Simulation Mode):
- No actual audio generation
- Instant "playback" (simulated)
- No CPU/memory overhead
- Perfect for UI development

### With JNI Libraries:
- Real-time audio synthesis
- CPU: ~10-30% per voice stream
- Memory: ~100-500 MB per loaded model
- Latency: ~100-500ms for short texts

## Security Considerations

### Native Libraries:
- Downloaded from official Microsoft (ONNX Runtime) and Rhasspy (Piper) repositories
- Verify checksums if security is critical
- Libraries are loaded from application resources (sandboxed)

### JNI Libraries (When Built):
- Build from source for maximum security
- Review JNI wrapper code before compilation
- Sign libraries for distribution (especially macOS)

## License Compliance

### ONNX Runtime:
- License: MIT
- ✅ Compatible with project

### Piper TTS:
- License: MIT
- ✅ Compatible with project

### Your Application:
- Ensure license compatibility when distributing
- Include license notices for third-party libraries

## Support and Resources

### Official Documentation:
- **Piper TTS:** https://github.com/rhasspy/piper
- **ONNX Runtime:** https://github.com/microsoft/onnxruntime
- **JNI Guide:** https://docs.oracle.com/javase/8/docs/technotes/guides/jni/

### Community:
- Piper Discussions: https://github.com/rhasspy/piper/discussions
- ONNX Runtime Issues: https://github.com/microsoft/onnxruntime/issues

### Project-Specific:
- Check application logs for detailed error messages
- Review `PiperInitializer.getStatusInfo()` for debugging
- Use `NativeLibraryLoader.getPlatformInfo()` for platform details

## Conclusion

✅ **Phase 1 Complete:** ONNX Runtime libraries are in place for all platforms

⚠️ **Phase 2 Pending:** Piper JNI wrapper libraries need to be built

🎯 **Current Status:** Application works in simulation mode, ready for development

📋 **Next Action:** Choose your path (continue development, build JNI, or implement process-based approach)

---

**Last Updated:** November 10, 2025
**Status:** ONNX Runtime libraries downloaded, JNI wrappers pending
**Application State:** Functional in simulation mode

