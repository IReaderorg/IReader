# Optional Engine Plugins Design

This document covers the design for moving heavy dependencies to optional plugins:
1. **Piper TTS** - Neural text-to-speech engine (Desktop)
2. **JS Engines** - JavaScript execution for LNReader sources (Android/Desktop)

---

# Piper TTS Plugin Design

## Overview

Move Piper TTS from a bundled dependency to an optional plugin that users can install from the Feature Store. This reduces the base app size and allows independent updates.

## Current Architecture

```
IReader Desktop App (~100MB+)
├── Core App (~30MB)
├── Piper JNI Library (~20MB per platform)
│   ├── Windows: piper.dll, onnxruntime.dll
│   ├── macOS: libpiper.dylib, libonnxruntime.dylib
│   └── Linux: libpiper.so, libonnxruntime.so
└── Voice Models (downloaded separately, ~15-50MB each)
```

## Proposed Architecture

```
IReader Desktop App (~30MB)
├── Core App
└── Plugin System

Piper TTS Plugin (~20-25MB per platform)
├── plugin.json (manifest)
├── classes.jar (plugin code)
├── native/
│   ├── windows-x64/
│   │   ├── piper.dll
│   │   └── onnxruntime.dll
│   ├── macos-x64/
│   │   ├── libpiper.dylib
│   │   └── libonnxruntime.dylib
│   ├── macos-arm64/
│   │   ├── libpiper.dylib
│   │   └── libonnxruntime.dylib
│   └── linux-x64/
│       ├── libpiper.so
│       └── libonnxruntime.so
└── Voice Models (downloaded on-demand)
```

## Plugin Implementation

### 1. Plugin Manifest (plugin.json)

```json
{
  "id": "io.github.ireaderorg.plugins.piper-tts",
  "name": "Piper TTS",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "High-performance neural text-to-speech with 30+ voices",
  "author": {
    "name": "IReader Team"
  },
  "type": "TTS",
  "permissions": ["STORAGE", "NETWORK"],
  "minIReaderVersion": "1.0.0",
  "platforms": ["DESKTOP"],
  "mainClass": "io.github.ireaderorg.plugins.pipertts.PiperTTSPlugin",
  "nativeLibraries": {
    "windows-x64": ["native/windows-x64/piper.dll", "native/windows-x64/onnxruntime.dll"],
    "macos-x64": ["native/macos-x64/libpiper.dylib", "native/macos-x64/libonnxruntime.dylib"],
    "macos-arm64": ["native/macos-arm64/libpiper.dylib", "native/macos-arm64/libonnxruntime.dylib"],
    "linux-x64": ["native/linux-x64/libpiper.so", "native/linux-x64/libonnxruntime.so"]
  }
}
```

### 2. Plugin Class

```kotlin
package io.github.ireaderorg.plugins.pipertts

import ireader.plugin.api.*

class PiperTTSPlugin : TTSPlugin {
    
    private var synthesizer: PiperSynthesizer? = null
    private var nativeLibraryPath: String? = null
    
    override val manifest = PluginManifest(
        id = "io.github.ireaderorg.plugins.piper-tts",
        name = "Piper TTS",
        version = "1.0.0",
        versionCode = 1,
        description = "High-performance neural TTS with 30+ voices in 20+ languages",
        author = PluginAuthor(name = "IReader Team"),
        type = PluginType.TTS,
        permissions = listOf(PluginPermission.STORAGE, PluginPermission.NETWORK),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.DESKTOP)
    )
    
    override fun initialize(context: PluginContext) {
        // Extract and load native libraries
        nativeLibraryPath = context.extractNativeLibraries()
        
        // Initialize Piper with native library path
        synthesizer = PiperSynthesizer(nativeLibraryPath!!)
    }
    
    override fun cleanup() {
        synthesizer?.shutdown()
        synthesizer = null
    }
    
    override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
        val synth = synthesizer ?: return Result.failure(
            IllegalStateException("Plugin not initialized")
        )
        
        return synth.synthesize(text, voice)
    }
    
    override fun getAvailableVoices(): List<VoiceModel> {
        return PiperVoiceRegistry.getAvailableVoices()
    }
    
    override fun supportsStreaming(): Boolean = true
    
    override fun getAudioFormat(): AudioFormat = AudioFormat(
        encoding = AudioEncoding.PCM,
        sampleRate = 22050,
        channels = 1,
        bitDepth = 16
    )
}
```

## Implementation Steps

### Phase 1: Plugin Infrastructure (Required First)

1. **Extend PluginManifest** to support native libraries:
   ```kotlin
   data class PluginManifest(
       // ... existing fields ...
       val nativeLibraries: Map<String, List<String>>? = null
   )
   ```

2. **Extend PluginContext** to support native library extraction:
   ```kotlin
   interface PluginContext {
       // ... existing methods ...
       
       /**
        * Extract native libraries for the current platform.
        * @return Path to the directory containing extracted native libraries
        */
       fun extractNativeLibraries(): String
   }
   ```

3. **Update PluginClassLoader** to handle native libraries:
   - Extract native libraries from plugin package
   - Set `java.library.path` or use `System.load()` with absolute paths

### Phase 2: Piper Plugin Development

1. **Create plugin project** in `IReader-plugins/plugins/tts/piper-tts/`

2. **Package native libraries** per platform:
   - Download Piper JNI native libraries
   - Package into platform-specific directories

3. **Implement PiperTTSPlugin** class

4. **Voice model management**:
   - Download voices on-demand
   - Cache in plugin data directory

### Phase 3: App Integration

1. **Remove bundled Piper dependency** from `domain/build.gradle.kts`

2. **Update TTS service** to use plugin-based TTS:
   ```kotlin
   class TTSService(
       private val pluginManager: PluginManager
   ) {
       fun getAvailableTTSEngines(): List<TTSPlugin> {
           return pluginManager.getPluginsByType(PluginType.TTS)
               .filterIsInstance<TTSPlugin>()
       }
   }
   ```

3. **Update UI** to show TTS plugins in Feature Store

## Piper TTS Implementation Status

### Done ✅

1. ✅ Created Piper TTS plugin (`IReader-plugins/plugins/tts/piper-tts/`)
   - `PiperTTSPlugin.kt` - Main plugin implementing `TTSPlugin` interface
   - `PiperAudioStream.kt` - AudioStream implementation for PCM output
   - `PiperVoiceDownloader.kt` - Voice model download manager
   - `build.gradle.kts` - Plugin configuration
2. ✅ Defined 14 voice models across 10+ languages
3. ✅ Voice download from HuggingFace Piper voices repository
4. ✅ TTS service already supports plugin-based TTS (`PluginTTSManager`)

### Pending ⏳

1. ⏳ Package native libraries per platform (requires build pipeline)
2. ⏳ Remove bundled Piper from `domain/build.gradle.kts`
3. ⏳ Add Piper TTS plugin to Feature Store repository
4. ⏳ Test voice download and synthesis

**Files Created:**
- `IReader-plugins/plugins/tts/piper-tts/build.gradle.kts`
- `IReader-plugins/plugins/tts/piper-tts/src/main/kotlin/PiperTTSPlugin.kt`
- `IReader-plugins/plugins/tts/piper-tts/src/main/kotlin/PiperAudioStream.kt`
- `IReader-plugins/plugins/tts/piper-tts/src/main/kotlin/PiperVoiceDownloader.kt`

## Size Comparison

| Component | Current | With Plugin |
|-----------|---------|-------------|
| Base App (Desktop) | ~100MB | ~30MB |
| Piper Plugin (Windows) | - | ~25MB |
| Piper Plugin (macOS) | - | ~25MB |
| Piper Plugin (Linux) | - | ~20MB |
| Voice Models | ~15-50MB each | ~15-50MB each |

**Savings for users who don't use TTS: ~70MB per update**

## Platform-Specific Considerations

### Windows
- Native libraries: `piper.dll`, `onnxruntime.dll`
- May need Visual C++ Redistributable

### macOS
- Native libraries: `libpiper.dylib`, `libonnxruntime.dylib`
- Need both x64 and arm64 variants
- Code signing considerations

### Linux
- Native libraries: `libpiper.so`, `libonnxruntime.so`
- May need to set `LD_LIBRARY_PATH`

## Migration Path

1. **v1.x**: Keep Piper bundled, add plugin infrastructure
2. **v2.0**: Piper available as both bundled and plugin
3. **v2.1**: Remove bundled Piper, plugin-only

## Alternative: Lazy Loading

If full plugin extraction is complex, consider lazy loading:
- Keep Piper JNI as optional dependency
- Download native libraries on first use
- Store in app data directory

This is simpler but doesn't provide the same modularity benefits.


---

# JavaScript Engine Plugin Design

## Overview

Move JavaScript engines from bundled dependencies to optional plugins. This significantly reduces the base app size since JS engines include large native libraries.

## Current Architecture

```
IReader App
├── Android (~80MB with J2V8)
│   └── J2V8 (V8 JavaScript engine) - ~15-20MB per ABI
│       ├── arm64-v8a
│       ├── armeabi-v7a
│       ├── x86_64
│       └── x86
│
└── Desktop (~100MB with GraalVM)
    └── GraalVM Polyglot (~40-60MB per platform)
        ├── Windows x64
        ├── macOS x64
        ├── macOS arm64
        └── Linux x64
```

## Proposed Architecture

```
IReader App (Base: ~30MB Android, ~30MB Desktop)
├── Core App (no JS engine)
└── Plugin System

JS Engine Plugins (installed on-demand):
├── J2V8 Plugin (Android) - ~15-20MB per ABI
│   ├── plugin.json
│   ├── classes.dex
│   └── native/
│       ├── android-arm64/libj2v8.so
│       ├── android-arm32/libj2v8.so
│       └── android-x64/libj2v8.so
│
└── GraalVM Plugin (Desktop) - ~40-60MB per platform
    ├── plugin.json
    ├── classes.jar
    └── native/
        ├── windows-x64/polyglot.dll, js.dll
        ├── macos-arm64/libpolyglot.dylib, libjs.dylib
        └── linux-x64/libpolyglot.so, libjs.so
```

## Plugin API

### JSEnginePlugin Interface

```kotlin
interface JSEnginePlugin : Plugin {
    fun createEngine(): JSEngineInstance
    fun getCapabilities(): JSEngineCapabilities
    fun isAvailable(): Boolean
}

interface JSEngineInstance {
    suspend fun initialize()
    suspend fun evaluate(code: String): JSValue
    suspend fun callFunction(name: String, vararg args: Any?): JSValue
    fun setGlobal(name: String, value: Any?)
    fun registerFunction(name: String, function: JSNativeFunction)
    fun dispose()
}

data class JSEngineCapabilities(
    val engineName: String,           // "GraalVM", "J2V8", "QuickJS"
    val engineVersion: String,
    val ecmaScriptVersion: String,    // "ES2022"
    val supportsAsync: Boolean,
    val supportsPromises: Boolean,
    val supportsModules: Boolean
)
```

### Plugin Manifest Example

```json
{
  "id": "io.github.ireaderorg.plugins.j2v8-engine",
  "name": "J2V8 JavaScript Engine",
  "version": "6.3.4",
  "versionCode": 1,
  "description": "V8 JavaScript engine for Android - enables LNReader sources",
  "author": { "name": "IReader Team" },
  "type": "JS_ENGINE",
  "permissions": ["STORAGE"],
  "minIReaderVersion": "2.0.0",
  "platforms": ["ANDROID"],
  "mainClass": "io.github.ireaderorg.plugins.j2v8.J2V8EnginePlugin",
  "nativeLibraries": {
    "android-arm64": ["native/android-arm64/libj2v8.so"],
    "android-arm32": ["native/android-arm32/libj2v8.so"],
    "android-x64": ["native/android-x64/libj2v8.so"],
    "android-x86": ["native/android-x86/libj2v8.so"]
  }
}
```

## Implementation Steps

### Phase 1: Plugin API (Done ✅)

1. ✅ Add `JS_ENGINE` to `PluginType` enum
2. ✅ Create `JSEnginePlugin` interface
3. ✅ Create `JSEngineInstance` interface  
4. ✅ Create `JSValue` interface
5. ✅ Add native library support to `PluginManifest`
6. ✅ Add native library methods to `PluginContext`
7. ✅ Publish plugin-api 1.0.3 to Maven Central

### Phase 2: Engine Abstraction (Done ✅)

1. ✅ Create `JSEngineProvider` service in domain module
2. ✅ Create `NativeLibrarySupport` expect/actual for platform-specific native loading
3. ✅ Update `SandboxedPluginContext` with native library support
4. ✅ Register `JSEngineProvider` in `PluginModule`
5. ⏳ Modify `JSPluginLoader` to use `JSEngineProvider` (optional - bundled engines work as fallback)

### Phase 3: Plugin Development (Done ✅)

1. ✅ Create J2V8 plugin for Android (`IReader-plugins/plugins/engines/j2v8-engine/`)
   - Full implementation using `com.eclipsesource.v8.V8`
   - Implements `JSEnginePlugin`, `JSEngineInstance`, `JSValue`
2. ✅ Create GraalVM plugin for Desktop (`IReader-plugins/plugins/engines/graalvm-engine/`)
   - Full implementation using `org.graalvm.polyglot.Context`
   - Implements `JSEnginePlugin`, `JSEngineInstance`, `JSValue`
3. ⏳ Package native libraries per platform (requires build pipeline setup)

### Phase 4: App Integration (Pending)

1. ⏳ Remove bundled JS engine dependencies from `domain/build.gradle.kts`:
   - `libs.j2v8` (Android)
   - `libs.polyglot` and `libs.js` (Desktop)
2. ⏳ Show prompt to install JS engine when needed
3. ⏳ Add JS engine plugins to Feature Store repository

### Current Status

The plugin infrastructure is complete. The bundled JS engines (J2V8 for Android, GraalVM for Desktop) 
continue to work as before. The plugin-based engines are ready but optional - users can install them
from the Feature Store when the bundled engines are removed in a future version.

**Files Created/Modified:**
- `plugin-api/src/commonMain/kotlin/ireader/plugin/api/JSEnginePlugin.kt` - Plugin interface
- `plugin-api/src/commonMain/kotlin/ireader/plugin/api/PluginType.kt` - Added JS_ENGINE
- `plugin-api/src/commonMain/kotlin/ireader/plugin/api/PluginManifest.kt` - Added nativeLibraries
- `plugin-api/src/commonMain/kotlin/ireader/plugin/api/PluginContext.kt` - Added native library methods
- `domain/src/commonMain/kotlin/ireader/domain/js/engine/JSEngineProvider.kt` - Engine provider service
- `domain/src/commonMain/kotlin/ireader/domain/plugins/NativeLibrarySupport.kt` - Native library support
- `domain/src/commonMain/kotlin/ireader/domain/plugins/SandboxedPluginContext.kt` - Updated context
- `IReader-plugins/plugins/engines/j2v8-engine/` - J2V8 plugin implementation
- `IReader-plugins/plugins/engines/graalvm-engine/` - GraalVM plugin implementation

## Size Comparison

| Component | Current | With Plugin |
|-----------|---------|-------------|
| Android APK (arm64) | ~80MB | ~30MB |
| Android APK (universal) | ~120MB | ~30MB |
| Desktop (Windows) | ~100MB | ~30MB |
| J2V8 Plugin (arm64) | - | ~20MB |
| GraalVM Plugin (Windows) | - | ~50MB |

**Savings:**
- Android users who don't use JS sources: ~50-90MB
- Desktop users who don't use JS sources: ~70MB

## User Experience

### First-Time JS Source Usage

1. User tries to add a JS-based source
2. App detects no JS engine installed
3. Shows dialog: "JavaScript Engine Required"
4. User clicks "Install" → Opens Feature Store
5. User installs appropriate JS engine plugin
6. Returns to source → Works!

### Graceful Degradation

- App works without JS engine
- Built-in sources (non-JS) work normally
- JS sources show "Requires JavaScript Engine" badge
- Clear messaging about what's needed

## Alternative: QuickJS

For a lighter-weight option, consider QuickJS:
- Much smaller (~2MB vs ~20MB for J2V8)
- Pure C, easy to embed
- ES2020 support
- No JIT (slower but smaller)

Could offer both:
- QuickJS Plugin: Small, basic JS support
- J2V8/GraalVM Plugin: Full-featured, faster
