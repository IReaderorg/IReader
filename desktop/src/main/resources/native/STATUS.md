# Native Libraries Status

## Current Status (Updated: 2025-11-10)

### ✅ ONNX Runtime Libraries (Complete)

All ONNX Runtime libraries have been downloaded and placed:

- ✅ `windows-x64/onnxruntime.dll` - Windows 64-bit
- ✅ `macos-x64/libonnxruntime.dylib` - macOS Intel
- ✅ `macos-arm64/libonnxruntime.dylib` - macOS Apple Silicon
- ✅ `linux-x64/libonnxruntime.so` - Linux 64-bit

### ✅ Piper Support Libraries (Windows - Partial)

Additional Piper dependencies for Windows:

- ✅ `windows-x64/piper_phonemize.dll` - Phonemization library
- ✅ `windows-x64/espeak-ng.dll` - eSpeak NG for text processing
- ✅ `windows-x64/onnxruntime_providers_shared.dll` - ONNX Runtime providers

### ⚠️ Piper JNI Wrapper Libraries (Missing)

The JNI wrapper libraries that bridge Kotlin/Java to Piper C++ are not yet available:

- ❌ `windows-x64/piper_jni.dll` - **MISSING**
- ❌ `macos-x64/libpiper_jni.dylib` - **MISSING**
- ❌ `macos-arm64/libpiper_jni.dylib` - **MISSING**
- ❌ `linux-x64/libpiper_jni.so` - **MISSING**

## Impact

### Current Behavior:
- Application will compile successfully
- TTS service will detect missing JNI libraries
- System will automatically fall back to **simulation mode**
- Users will see: "TTS fallback to simulation mode: No voice model selected"

### With JNI Libraries:
- Full Piper TTS functionality
- High-quality neural voice synthesis
- Offline text-to-speech
- Multiple voice model support

## Next Steps

You have **three options** to enable full TTS functionality:

### Option 1: Build JNI Libraries (Recommended for Production)

Build the JNI wrapper libraries from source. This provides the best integration and performance.

**See:** `BUILD_NATIVE_LIBS.md` in the project root for detailed instructions.

**Time:** 2-4 hours (includes setup and compilation)

### Option 2: Use Process-Based Piper (Quick Alternative)

Instead of JNI, invoke the Piper CLI binary as a subprocess. This is simpler but has higher overhead.

**Advantages:**
- No C++ compilation required
- Works with downloaded binaries immediately
- Easier to debug

**Implementation:**
```kotlin
// Extract piper executable from resources
val piperExe = extractPiperExecutable()

// Run Piper as subprocess
val process = ProcessBuilder(
    piperExe.absolutePath,
    "--model", modelPath,
    "--output_file", "-"
).start()

// Read audio from stdout
val audioData = process.inputStream.readBytes()
```

**See:** `docs/PROCESS_BASED_TTS.md` (to be created)

### Option 3: Use Simulation Mode (Development)

Continue using simulation mode for development and testing. This is useful when:
- Focusing on UI/UX development
- Testing TTS controls and state management
- Working on non-TTS features

**No action required** - this is the current default behavior.

## Verification

To check the status of native libraries:

```bash
# Run verification task
./gradlew :desktop:verifyNativeLibraries

# Or check manually
ls -la desktop/src/main/resources/native/*/
```

## Resources

- **Piper TTS:** https://github.com/rhasspy/piper
- **ONNX Runtime:** https://github.com/microsoft/onnxruntime
- **Build Instructions:** See `BUILD_NATIVE_LIBS.md`
- **JNI Documentation:** https://docs.oracle.com/javase/8/docs/technotes/guides/jni/

## Support

If you encounter issues:

1. Check application logs for detailed error messages
2. Verify library files exist and have correct permissions
3. Ensure platform matches (x64 vs ARM, etc.)
4. Review `BUILD_NATIVE_LIBS.md` for troubleshooting

## Development Notes

The application is designed to gracefully handle missing native libraries:

```kotlin
val result = PiperInitializer.initialize()

if (result.isSuccess) {
    // Use Piper TTS
    logger.info("Piper TTS initialized successfully")
} else {
    // Fall back to simulation
    logger.warn("Piper TTS unavailable: ${result.exceptionOrNull()?.message}")
    logger.warn("Using simulation mode")
}
```

This allows development to continue without native libraries while providing a clear path to full functionality.

