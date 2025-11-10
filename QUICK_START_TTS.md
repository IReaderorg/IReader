# Quick Start: Piper TTS Integration

## Current Status: ✅ Ready for Development

Your application is now set up with native libraries and will run in **simulation mode** until JNI wrapper libraries are built.

## What's Working Right Now

✅ Application compiles and runs  
✅ TTS service initializes successfully  
✅ TTS controls and UI are functional  
✅ Simulation mode provides instant feedback  
✅ ONNX Runtime libraries in place (all platforms)  
✅ Piper support libraries in place (Windows)  

## What's in Simulation Mode

⚠️ No actual audio generation  
⚠️ Simulated playback timing  
⚠️ No voice model loading  

## File Summary

### Downloaded Libraries (70.98 MB total)

```
desktop/src/main/resources/native/
├── windows-x64/
│   ├── onnxruntime.dll                      (9.60 MB) ✅
│   ├── onnxruntime_providers_shared.dll     (0.02 MB) ✅
│   ├── piper_phonemize.dll                  (0.39 MB) ✅
│   ├── espeak-ng.dll                        (0.36 MB) ✅
│   └── piper_jni.dll                                  ❌ MISSING
│
├── macos-x64/
│   ├── libonnxruntime.dylib                (23.30 MB) ✅
│   └── libpiper_jni.dylib                             ❌ MISSING
│
├── macos-arm64/
│   ├── libonnxruntime.dylib                (20.68 MB) ✅
│   └── libpiper_jni.dylib                             ❌ MISSING
│
└── linux-x64/
    ├── libonnxruntime.so                   (16.63 MB) ✅
    └── libpiper_jni.so                                ❌ MISSING
```

## Running the Application

### 1. Build and Run

```bash
# Build the application
./gradlew :desktop:build

# Run the application
./gradlew :desktop:run
```

### 2. Expected Behavior

When you start the application:

```
[INFO] Initializing TTS service...
[WARN] Piper TTS unavailable: Native library not found in resources
[WARN] TTS fallback to simulation mode: No voice model selected
[INFO] TTS service initialized in simulation mode
```

This is **normal and expected** without JNI libraries.

### 3. Using TTS Features

All TTS features work in simulation mode:

- ✅ Play/Pause buttons
- ✅ Speed control
- ✅ Voice selection UI
- ✅ Progress tracking
- ✅ Text highlighting

The only difference: no actual audio is generated.

## Enabling Real TTS (3 Options)

### Option 1: Continue in Simulation Mode (Recommended for Now)

**Best for:**
- UI/UX development
- Testing application flow
- Working on non-TTS features

**Action:** None required - keep developing!

### Option 2: Build JNI Libraries (Production Solution)

**Best for:**
- Production deployment
- Best performance
- Full control

**Time:** 2-4 hours  
**Difficulty:** Medium (requires C++ toolchain)

**Steps:**
1. Read `BUILD_NATIVE_LIBS.md`
2. Install C++ compiler and CMake
3. Clone Piper repository
4. Build JNI wrapper
5. Copy libraries to project

### Option 3: Process-Based TTS (Quick Alternative)

**Best for:**
- Quick prototyping
- Testing real TTS without JNI
- Avoiding C++ compilation

**Time:** 1-2 hours  
**Difficulty:** Easy (Kotlin only)

**Steps:**
1. Extract Piper CLI binaries
2. Implement subprocess execution
3. Handle audio streaming

## Verification

### Check Library Status

```bash
./gradlew :desktop:verifyNativeLibraries
```

**Expected Output:**
```
Checking windows-x64:
  ✗ piper_jni.dll (missing)
  ✓ onnxruntime.dll (9.60 MB)
  
Checking macos-x64:
  ✗ libpiper_jni.dylib (missing)
  ✓ libonnxruntime.dylib (23.30 MB)
  
Checking macos-arm64:
  ✗ libpiper_jni.dylib (missing)
  ✓ libonnxruntime.dylib (20.68 MB)
  
Checking linux-x64:
  ✗ libpiper_jni.so (missing)
  ✓ libonnxruntime.so (16.63 MB)

WARNING: Some native libraries are missing.
Piper TTS will fall back to simulation mode.
```

### Check Application Logs

Look for these log messages when running:

```kotlin
// Successful initialization (simulation mode)
"TTS service initialized in simulation mode"

// Or with JNI libraries (future)
"Piper TTS initialized successfully"
"Loaded voice model: [model_name]"
```

## Testing TTS Features

### In Code

```kotlin
// Get TTS service
val ttsService = DesktopTTSService()

// Initialize
ttsService.initialize()

// Check mode
if (ttsService.isPiperAvailable()) {
    println("✅ Real TTS available")
} else {
    println("⚠️ Running in simulation mode")
}

// Use TTS (works in both modes)
ttsService.readText("Hello, this is a test.")

// Control playback
ttsService.pause()
ttsService.resume()
ttsService.stop()
```

### In UI

1. Open a book in the reader
2. Click the TTS button (🔊)
3. Click Play
4. Observe:
   - In simulation mode: Instant "playback" with no audio
   - With JNI: Real audio synthesis and playback

## Troubleshooting

### Issue: Application won't start

**Check:**
- Java version: `java -version` (need 11+)
- Gradle version: `./gradlew --version`
- Build errors: `./gradlew :desktop:build --stacktrace`

### Issue: TTS button doesn't appear

**Check:**
- Feature flag enabled
- Reader screen loaded correctly
- Check logs for initialization errors

### Issue: "Native library not found"

**This is expected!** The application is designed to work without JNI libraries.

**To enable real TTS:**
- Follow Option 2 or 3 above

### Issue: Performance problems

**In simulation mode:**
- Should be instant, no performance impact

**With real TTS (future):**
- First synthesis may be slow (model loading)
- Subsequent synthesis should be fast
- Check CPU/memory usage

## Next Steps

### Immediate (Now)

1. ✅ Run the application: `./gradlew :desktop:run`
2. ✅ Test TTS controls in simulation mode
3. ✅ Continue development on other features

### Short Term (This Week)

1. Decide on TTS approach (JNI vs Process-based)
2. If JNI: Start building libraries (see `BUILD_NATIVE_LIBS.md`)
3. If Process: Implement subprocess execution
4. Test with real voice models

### Long Term (Production)

1. Build JNI libraries for all platforms
2. Package voice models with application
3. Test on target platforms
4. Optimize performance
5. Add voice model download feature

## Documentation Reference

- **`NATIVE_LIBS_SUMMARY.md`** - Complete overview of library status
- **`BUILD_NATIVE_LIBS.md`** - Detailed JNI build instructions
- **`desktop/src/main/resources/native/STATUS.md`** - Current library status
- **`desktop/src/main/resources/native/README.md`** - Platform-specific notes
- **`domain/.../piper/README.md`** - Piper integration architecture

## Support

### Logs Location

Check application logs for detailed information:

```bash
# Application logs
tail -f logs/application.log

# Or check console output when running
./gradlew :desktop:run
```

### Debug Information

Get detailed platform and library information:

```kotlin
// In your code
println(NativeLibraryLoader.getPlatformInfo())
println(PiperInitializer.getStatusInfo())
```

### Common Log Messages

| Message | Meaning | Action |
|---------|---------|--------|
| "TTS service initialized in simulation mode" | Normal without JNI | Continue development |
| "Piper TTS initialized successfully" | JNI libraries loaded | Real TTS available |
| "Native library not found in resources" | Expected without JNI | Build JNI or use simulation |
| "Unsupported platform" | Platform not recognized | Check OS/architecture |

## Performance Expectations

### Simulation Mode (Current)

- Initialization: < 100ms
- "Playback": Instant
- Memory: < 10 MB
- CPU: < 1%

### Real TTS (With JNI)

- Initialization: 1-3 seconds (model loading)
- Synthesis: 100-500ms per sentence
- Memory: 100-500 MB (per loaded model)
- CPU: 10-30% during synthesis

## Conclusion

🎉 **You're all set!** The application is ready for development in simulation mode.

📋 **Current state:**
- ✅ ONNX Runtime libraries downloaded
- ✅ Application compiles and runs
- ✅ TTS features work in simulation mode
- ⚠️ JNI libraries pending (optional for now)

🚀 **Next action:** Run the app and start developing!

```bash
./gradlew :desktop:run
```

---

**Questions?** Check the documentation files listed above or review application logs for detailed error messages.

