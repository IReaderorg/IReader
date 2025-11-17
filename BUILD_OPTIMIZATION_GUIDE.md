# Build Speed Optimization Guide

This document outlines the optimizations applied to improve build speed for the Infinity project.

## Applied Optimizations

### 1. Gradle Properties Optimizations

#### Memory Settings
- **Increased heap size**: `org.gradle.jvmargs=-Xmx6144m` (from 5120m)
  - Provides more memory for Gradle daemon to handle large multi-module builds
- **G1GC**: Switched from ParallelGC to G1GC for better memory management
- **MetaSpace**: Set `MaxMetaspaceSize=1024m` to prevent metaspace exhaustion
- **String Deduplication**: Enabled to reduce memory footprint

#### Parallel Execution
- **Parallel builds**: `org.gradle.parallel=true` (already enabled)
- **Worker threads**: `org.gradle.workers.max=4` - limits concurrent workers to prevent thrashing
- **Configuration on demand**: `org.gradle.configureondemand=true` - only configures relevant projects

#### Caching
- **Build cache**: `org.gradle.caching=true` (already enabled)
- **Kotlin caching**: `kotlin.caching.enabled=true`
- **Configuration cache**: `org.gradle.unsafe.configuration-cache=true` (already enabled)

#### Kotlin Incremental Compilation
- **Kotlin incremental**: `kotlin.incremental=true`
- **Kotlin JS incremental**: `kotlin.incremental.js=true`
- **Kotlin MPP incremental**: `kotlin.incremental.multiplatform=true`
- **In-process compilation**: `kotlin.compiler.execution.strategy=in-process`

#### Android Optimizations
- **Non-transitive R classes**: `android.nonTransitiveRClass=true`
- **Non-final resource IDs**: `android.nonFinalResIds=true`
- **Disabled unused features**: buildconfig, aidl, renderscript, resvalues, shaders set to false by default

### 2. Additional Recommendations

#### For Development Builds

1. **Use the dev flavor**:
   ```bash
   ./gradlew assembleDevDebug
   ```
   This flavor has limited resources (only English, xxhdpi) for faster builds.

2. **Skip tests during development**:
   ```bash
   ./gradlew assembleDebug -x test -x lint
   ```

3. **Build specific modules**:
   ```bash
   ./gradlew :android:assembleDebug
   ```

4. **Use build scans** to identify bottlenecks:
   ```bash
   ./gradlew assembleDebug --scan
   ```

#### Clean Build Cache (if issues occur)
```bash
./gradlew clean cleanBuildCache
rm -rf .gradle/
rm -rf build/
rm -rf */build/
```

#### IDE Settings (Android Studio / IntelliJ)

1. **Increase IDE memory**:
   - Help → Edit Custom VM Options
   - Add/modify: `-Xmx4096m`

2. **Enable offline mode** when not updating dependencies:
   - Settings → Build → Gradle → Offline work

3. **Disable unnecessary plugins** in IDE

4. **Use "Build" instead of "Rebuild"** when possible

### 3. Build Time Expectations

With these optimizations:
- **Clean build**: 3-5 minutes (depending on hardware)
- **Incremental build**: 30-60 seconds
- **No-op build**: 5-10 seconds

### 4. Hardware Recommendations

For optimal build performance:
- **RAM**: 16GB minimum, 32GB recommended
- **CPU**: 6+ cores recommended
- **Storage**: SSD required (NVMe preferred)

### 5. Monitoring Build Performance

Use Gradle build scans to identify slow tasks:
```bash
./gradlew assembleDebug --scan
```

Check which tasks take the longest and optimize accordingly.

### 6. Module-Specific Optimizations

The project has 8 modules:
- android
- data
- domain
- presentation
- core
- i18n
- desktop
- source-api

Each module builds in parallel when possible. Ensure dependencies are properly declared to maximize parallelization.

## Troubleshooting

### Out of Memory Errors
If you encounter OOM errors, increase heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx8192m -XX:+UseG1GC -XX:MaxMetaspaceSize=1536m
```

### Configuration Cache Issues
If configuration cache causes problems, disable it temporarily:
```properties
org.gradle.unsafe.configuration-cache=false
```

### Slow Kotlin Compilation
Ensure incremental compilation is working:
```bash
./gradlew assembleDebug --info | grep "Incremental compilation"
```

## Summary

These optimizations should significantly improve build times, especially for incremental builds. The key improvements are:
- Better memory management with G1GC
- Kotlin incremental compilation enabled
- Android resource optimization
- Parallel execution tuned
- Configuration cache enabled

Monitor your build times and adjust settings based on your hardware capabilities.
