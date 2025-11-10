# Piper TTS Native Library Infrastructure

This package contains the JNI wrapper and native library loading infrastructure for Piper TTS integration.

## Components

### 1. PiperNative
The main JNI interface object that declares external native methods for interacting with the Piper C++ library.

**Key Methods:**
- `initialize(modelPath, configPath)` - Initialize Piper with a voice model
- `synthesize(instance, text)` - Generate audio from text
- `setSpeechRate(instance, rate)` - Adjust speech speed
- `setNoiseScale(instance, noiseScale)` - Control synthesis quality
- `getSampleRate(instance)` - Get audio sample rate
- `shutdown(instance)` - Release native resources

### 2. NativeLibraryLoader
Handles platform detection and loading of native libraries from resources.

**Features:**
- Automatic platform detection (Windows, macOS Intel/ARM, Linux)
- Extracts libraries from JAR resources to temporary directory
- Loads both Piper JNI and ONNX Runtime dependencies
- Thread-safe singleton pattern
- Detailed error reporting

**Platform Support:**
- Windows x64: `piper_jni.dll`, `onnxruntime.dll`
- macOS x64: `libpiper_jni.dylib`, `libonnxruntime.dylib`
- macOS ARM64: `libpiper_jni.dylib`, `libonnxruntime.dylib`
- Linux x64: `libpiper_jni.so`, `libonnxruntime.so`

### 3. PiperInitializer
High-level initialization helper that provides a simple API for loading and checking library status.

**Usage:**
```kotlin
// Initialize Piper TTS
val result = PiperInitializer.initialize()

if (result.isSuccess) {
    // Piper is ready to use
    println("Piper TTS initialized successfully")
} else {
    // Fall back to simulation mode
    println("Piper TTS unavailable: ${result.exceptionOrNull()?.message}")
}

// Check availability
if (PiperInitializer.isAvailable()) {
    // Use Piper TTS
} else {
    // Use fallback
}

// Get detailed status for debugging
println(PiperInitializer.getStatusInfo())
```

## Native Library Location

Native libraries must be placed in the desktop module's resources:

```
desktop/src/main/resources/native/
├── windows-x64/
│   ├── piper_jni.dll
│   └── onnxruntime.dll
├── macos-x64/
│   ├── libpiper_jni.dylib
│   └── libonnxruntime.dylib
├── macos-arm64/
│   ├── libpiper_jni.dylib
│   └── libonnxruntime.dylib
└── linux-x64/
    ├── libpiper_jni.so
    └── libonnxruntime.so
```

See `desktop/src/main/resources/native/README.md` for detailed instructions on obtaining these libraries.

## Build Configuration

The desktop `build.gradle.kts` is configured to:
1. Include native libraries in the application resources
2. Package them in native distributions (MSI, DMG, DEB, etc.)
3. Verify library presence before packaging

**Verification Task:**
```bash
./gradlew :desktop:verifyNativeLibraries
```

This task checks if all required native libraries are present and reports their status.

## Error Handling

The infrastructure is designed to fail gracefully:

1. **Missing Libraries**: If native libraries are not found, initialization returns a failure Result
2. **Unsupported Platform**: Throws `UnsupportedOperationException` with clear message
3. **Load Failures**: Throws `UnsatisfiedLinkError` with detailed error information

The application should catch these errors and fall back to simulation mode.

## Development Without Native Libraries

During development, you can work without native libraries:

```kotlin
val result = PiperInitializer.initialize()

if (result.isFailure) {
    // Expected during development without libraries
    logger.warn("Piper TTS not available, using simulation mode")
    // Continue with simulation mode
}
```

## Testing

Unit tests are provided in `PiperInitializerTest.kt`. These tests verify:
- Status information is available
- Initialization handles missing libraries gracefully
- Error reporting works correctly

**Note**: Tests will report failures when native libraries are not present, which is expected during development.

## Integration with TTS Service

The TTS service should initialize Piper during startup:

```kotlin
class DesktopTTSService {
    private var piperAvailable = false
    
    suspend fun initialize() {
        val result = PiperInitializer.initialize()
        piperAvailable = result.isSuccess
        
        if (piperAvailable) {
            // Initialize Piper components
            loadVoiceModel()
        } else {
            // Use simulation mode
            logger.warn("Piper TTS unavailable: ${result.exceptionOrNull()?.message}")
        }
    }
    
    private suspend fun readText() {
        if (piperAvailable) {
            // Use Piper synthesis
        } else {
            // Use simulation
        }
    }
}
```

## Troubleshooting

### Library Not Found
**Symptom**: `UnsatisfiedLinkError` or "Native library not found in resources"

**Solutions**:
1. Verify libraries are in `desktop/src/main/resources/native/[platform]/`
2. Run `./gradlew :desktop:verifyNativeLibraries` to check status
3. Ensure libraries are built for the correct platform
4. Check that resources are included in the JAR

### Wrong Platform
**Symptom**: `UnsupportedOperationException: Unsupported platform`

**Solutions**:
1. Check OS name and architecture: `System.getProperty("os.name")`, `System.getProperty("os.arch")`
2. Verify platform detection logic in `NativeLibraryLoader.detectPlatform()`
3. Add support for new platform if needed

### Library Dependencies Missing
**Symptom**: Library loads but crashes on use

**Solutions**:
1. Verify ONNX Runtime is loaded before Piper JNI
2. Check library dependencies with `ldd` (Linux), `otool -L` (macOS), or Dependency Walker (Windows)
3. Ensure all required system libraries are installed

### Permission Issues
**Symptom**: Cannot extract or load libraries

**Solutions**:
1. Check temp directory permissions
2. Verify libraries are marked executable (Unix-like systems)
3. Check antivirus/security software settings

## Future Enhancements

Potential improvements to the native library infrastructure:

1. **Lazy Loading**: Load libraries only when first needed
2. **Version Management**: Support multiple library versions
3. **Fallback Paths**: Check multiple locations for libraries
4. **Hot Reload**: Support reloading libraries without restart
5. **Diagnostics**: Enhanced debugging and logging tools

## License

This infrastructure code is part of IReader and follows the project's license (Mozilla Public License v2.0).

The native libraries (Piper and ONNX Runtime) have their own licenses:
- Piper TTS: MIT License
- ONNX Runtime: MIT License

Ensure compliance with all applicable licenses when distributing the application.
