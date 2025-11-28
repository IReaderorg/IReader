# IReader Benchmark Module

This module contains Macrobenchmark tests for measuring and optimizing app performance,
and generates baseline profiles for faster app startup.

## Features

- **Baseline Profile Generation**: Automatically generates baseline profiles based on real user journeys
- **Startup Benchmarks**: Measures cold, warm, and hot startup times
- **Scroll Performance**: Measures frame timing during library scrolling

## Prerequisites

- Physical Android device or emulator with API 28+ (Android 9.0+)
- USB debugging enabled
- Device connected via ADB

## Generating Baseline Profiles

### Using Gradle Task (Recommended)

```bash
# Generate baseline profile for standard flavor
./gradlew :android:generateStandardReleaseBaselineProfile

# Or for other flavors
./gradlew :android:generateDevReleaseBaselineProfile
./gradlew :android:generateFdroidReleaseBaselineProfile
```

The generated profile will be automatically saved to `android/src/standard/baselineProfiles/` 
(or the respective flavor directory).

### Manual Generation

```bash
# 1. Build and install the benchmark APK
./gradlew :benchmark:installStandardBenchmark

# 2. Run the baseline profile generator tests
./gradlew :benchmark:connectedStandardBenchmarkAndroidTest \
    -P android.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
```

## Running Benchmarks

### All Startup Benchmarks

```bash
./gradlew :benchmark:connectedStandardBenchmarkAndroidTest \
    -P android.testInstrumentationRunnerArguments.class=org.ireader.benchmark.StartupBenchmark
```

### Specific Benchmark

```bash
./gradlew :benchmark:connectedStandardBenchmarkAndroidTest \
    -P android.testInstrumentationRunnerArguments.class=org.ireader.benchmark.StartupBenchmark#startupWithBaselineProfile
```

## Understanding Results

Benchmark results are saved to:
```
benchmark/build/outputs/connected_android_test_additional_output/
```

### Metrics

- **timeToInitialDisplayMs**: Time from app launch to first frame
- **timeToFullDisplayMs**: Time from app launch to fully rendered content
- **frameDurationCpuMs**: CPU time per frame (lower is better)
- **frameOverrunMs**: How much frames exceeded 16.67ms budget (0 is ideal)

### Compilation Modes

- **None**: No AOT compilation (worst case, simulates first install)
- **Partial**: With baseline profile (typical case after install)
- **Full**: Fully AOT compiled (best case, after background optimization)

## Expected Improvements

With baseline profiles, you should see:
- **15-30% faster cold startup** time
- **Reduced jank** during first scroll
- **Smoother animations** on first launch

## Tips

1. **Use a physical device** for accurate benchmarks (emulators have different characteristics)
2. **Run multiple iterations** to get stable results
3. **Disable battery optimization** for the benchmark app
4. **Close other apps** to reduce interference
5. **Keep the device cool** - thermal throttling affects results

## Static Baseline Profile

A static baseline profile is included at `android/src/main/baseline-prof.txt`.
This covers common startup paths and will be used automatically.

For best results, generate a profile using the benchmark tests on your target device,
which will capture device-specific optimizations.

## Troubleshooting

### "EMULATOR" error
Already suppressed in the configuration. If you still see it, ensure you're using API 28+.

### Profile not being applied
1. Ensure `profileinstaller` dependency is in the app module
2. Check that the profile exists in `src/main/baseline-prof.txt`
3. Rebuild the app after adding/updating the profile
4. On first install, the profile is applied asynchronously

### Build variant mismatch
The benchmark module must have matching build types and flavors with the app module.
Both are configured with: standard, fdroid, dev flavors and benchmark, release build types.
