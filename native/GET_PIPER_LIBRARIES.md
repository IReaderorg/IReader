# Getting Piper Libraries

## The Problem

The current `piper_jni.dll` was built with **stub implementations** because we don't have the actual Piper C++ libraries. This causes crashes when trying to use TTS.

## Solution: Get Pre-built Piper Libraries

### Option 1: Download Pre-built Piper (Easiest)

1. **Download Piper for Windows:**
   ```
   https://github.com/rhasspy/piper/releases/latest
   ```
   
   Look for: `piper_windows_amd64.zip`

2. **Extract the archive** and you'll find:
   - `piper.exe` - Command-line tool
   - `piper_phonemize.dll` - Already have this ✅
   - `espeak-ng-data/` - Phoneme data
   - `onnxruntime.dll` - Already have this ✅

3. **The issue**: Piper doesn't distribute a C++ library, only the executable.

### Option 2: Use Piper as a Subprocess (Workaround)

Instead of JNI, call `piper.exe` as a subprocess:

**Advantages:**
- ✅ No need to build C++ code
- ✅ Works immediately
- ✅ Easier to maintain

**Disadvantages:**
- ❌ Slower (process startup overhead)
- ❌ Less control
- ❌ More complex error handling

### Option 3: Build Piper from Source (Advanced)

This requires building the entire Piper project:

1. **Clone Piper:**
   ```bash
   git clone https://github.com/rhasspy/piper.git
   cd piper
   ```

2. **Install dependencies:**
   - ONNX Runtime development libraries
   - espeak-ng development libraries
   - CMake, C++ compiler

3. **Build Piper as a library:**
   ```bash
   mkdir build
   cd build
   cmake .. -DBUILD_SHARED_LIBS=ON
   cmake --build . --config Release
   ```

4. **Link against the built library** in our `CMakeLists.txt`

## Recommended Approach: Subprocess Implementation

Since building Piper from source is complex, I recommend implementing a subprocess-based approach:

### Implementation Plan

1. **Download `piper.exe`** from releases
2. **Create a subprocess wrapper** in Kotlin
3. **Call Piper via command line:**
   ```bash
   piper.exe --model model.onnx --config config.json --output_file output.wav < input.txt
   ```
4. **Read the generated audio file**
5. **Play it in the application**

### Advantages of This Approach

- ✅ **Works immediately** - No C++ compilation needed
- ✅ **Officially supported** - Uses Piper's intended interface
- ✅ **Easier to debug** - Can test with command line
- ✅ **Simpler maintenance** - No native code to maintain
- ✅ **Cross-platform** - Same approach works on all platforms

### Performance Considerations

- **Startup overhead**: ~100-200ms per synthesis
- **For long texts**: Batch multiple sentences
- **Caching**: Cache frequently used phrases
- **Streaming**: Process text in chunks

## Next Steps

### Immediate Fix (Subprocess Approach)

1. Download `piper.exe` from GitHub releases
2. Place in: `domain/src/desktopMain/resources/native/windows-x64/`
3. Implement subprocess wrapper (I can help with this)
4. Update `DesktopTTSService` to use subprocess instead of JNI

### Long-term Solution (Build Piper Library)

1. Set up Piper build environment
2. Build Piper as a shared library
3. Update `piper_wrapper.cpp` with actual Piper API calls
4. Rebuild `piper_jni.dll` with Piper linked

## Which Should You Choose?

### Use Subprocess If:
- ✅ You want it working quickly
- ✅ You don't need ultra-low latency
- ✅ You want easier maintenance
- ✅ You're okay with ~100ms overhead

### Build from Source If:
- ✅ You need lowest possible latency
- ✅ You want fine-grained control
- ✅ You have C++ build experience
- ✅ You can maintain native code

## My Recommendation

**Start with the subprocess approach** because:
1. It works immediately
2. It's officially supported by Piper
3. It's much simpler to implement and maintain
4. Performance is still good for most use cases
5. You can always optimize later if needed

Would you like me to implement the subprocess-based approach? It would take about 30 minutes and would give you working TTS immediately.

## Alternative: Use Existing TTS Libraries

If you want JNI-based TTS, consider:

1. **MaryTTS** - Java-based TTS (no JNI needed)
2. **FreeTTS** - Pure Java TTS
3. **Google Cloud TTS** - Cloud-based (requires internet)
4. **Microsoft Speech Platform** - Windows-only, has JNI bindings

These are easier to integrate than building Piper from source.

## Resources

- Piper Releases: https://github.com/rhasspy/piper/releases
- Piper Documentation: https://github.com/rhasspy/piper
- ONNX Runtime: https://github.com/microsoft/onnxruntime
- espeak-ng: https://github.com/espeak-ng/espeak-ng
