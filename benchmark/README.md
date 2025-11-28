# IReader Benchmark Module

This module contains Macrobenchmark tests for measuring app performance and generating baseline profiles.

## Features

- **Baseline Profile Generation**: Generates baseline profiles based on real user journeys
- **Startup Benchmarks**: Measures cold, warm, and hot startup times
- **Scroll Performance**: Measures frame timing during library scrolling

## Prerequisites

- Physical Android device or emulator with API 28+ (Android 9.0+)
- USB debugging enabled
- Device connected via ADB

## Running Benchmarks

### Setup

First, install the app on your device:
```bash
./gradlew :android:installStandardDebug
```

### Run All Startup Benchmarks

```bash
./gradlew :benchmark:connectedStandardBenchmarkAndroidTest
```

### Run Specific Benchmark

```bash
# Startup benchmark only
./gradlew :benchmark:connectedStandardBenchmarkAndroidTest \
    -P android.testInstrumentationRunnerArguments.class=org.ireader.benchmark.StartupBenchmark

# Baseline profile generation only
./gradlew :benchmark:connectedStandardBenchmarkAndroidTest \
    -P android.testInstrumentationRunnerArguments.class=org.ireader.benchmark.BaselineProfileGenerator
```

## Generating Baseline Profiles

```bash
# Run the baseline profile generator
./gradlew :benchmark:connectedStandardBenchmarkAndroidTest \
    -P android.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile

# The profile will be output to:
# benchmark/build/outputs/connected_android_test_additional_output/
# Copy the generated baseline-prof.txt to android/src/main/baseline-prof.txt
```

## Understanding Results

Results are saved to: `benchmark/build/outputs/connected_android_test_additional_output/`

### Metrics

- **timeToInitialDisplayMs**: Time from app launch to first frame
- **timeToFullDisplayMs**: Time from app launch to fully rendered content
- **frameDurationCpuMs**: CPU time per frame (lower is better)
- **frameOverrunMs**: How much frames exceeded 16.67ms budget (0 is ideal)

### Compilation Modes

- **None**: No AOT compilation (worst case, simulates first install)
- **Partial**: With baseline profile (typical case after install)
- **Full**: Fully AOT compiled (best case, after background optimization)

## Static Baseline Profile

A static baseline profile is included at `android/src/main/baseline-prof.txt`.
This covers common startup paths and is automatically used via the `profileinstaller` library.

Expected improvements with baseline profiles:
- **15-30% faster cold startup**
- **Reduced jank** during first scroll
- **Smoother animations** on first launch

## Tips

1. **Use a physical device** for accurate benchmarks
2. **Run multiple iterations** for stable results
3. **Disable battery optimization** for the benchmark app
4. **Close other apps** to reduce interference
5. **Keep the device cool** - thermal throttling affects results
