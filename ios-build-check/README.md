# iOS Build Check Module

A utility module to verify iOS Kotlin Multiplatform configuration without requiring macOS.

## Purpose

This module helps developers on Windows/Linux verify that:
- iOS targets are configured correctly in Gradle
- Dependencies resolve properly for iOS platforms
- Common code compiles without errors

## Usage

### Quick Check (Works on Windows/Linux)

```bash
# Verify iOS configuration
./gradlew :ios-build-check:checkIosDependencies

# Compile common code (catches most errors)
./gradlew :ios-build-check:compileCommonMainKotlinMetadata
```

### Check Other Modules

```bash
# Data module
./gradlew :data:compileCommonMainKotlinMetadata

# Domain module  
./gradlew :domain:compileCommonMainKotlinMetadata

# Core module
./gradlew :core:compileCommonMainKotlinMetadata

# Source API module
./gradlew :source-api:compileCommonMainKotlinMetadata
```

### Full iOS Compilation (Requires macOS with Xcode)

```bash
# Compile iOS targets
./gradlew :ios-build-check:compileKotlinIosArm64
./gradlew :data:compileKotlinIosArm64
./gradlew :domain:compileKotlinIosArm64
./gradlew :core:compileKotlinIosArm64
./gradlew :source-api:compileKotlinIosArm64
```

## What Each Command Checks

| Command | Platform | What it verifies |
|---------|----------|------------------|
| `checkIosDependencies` | Any | Gradle configuration syntax |
| `compileCommonMainKotlinMetadata` | Any | Common code compilation, expect/actual matching |
| `compileKotlinIosArm64` | macOS only | Full iOS native compilation |

## Limitations

On Windows/Linux:
- ✅ Gradle configuration validation
- ✅ Dependency resolution for iOS
- ✅ Common code compilation
- ✅ Expect/actual declaration matching (partial)
- ❌ iOS-specific code compilation (requires Kotlin/Native on macOS)
- ❌ iOS framework generation

## CI/CD Integration

For full iOS verification, use GitHub Actions with `macos-latest`:

```yaml
ios-build-check:
  runs-on: macos-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Build iOS targets
      run: |
        ./gradlew :data:compileKotlinIosArm64
        ./gradlew :domain:compileKotlinIosArm64
        ./gradlew :core:compileKotlinIosArm64
        ./gradlew :source-api:compileKotlinIosArm64
```

## Suppressing Warnings

Add to `gradle.properties` to suppress common warnings:

```properties
# Suppress hierarchy template warning (using manual dependsOn)
kotlin.mpp.applyDefaultHierarchyTemplate=false

# Suppress compileOnly warning for jsoup (JVM-only, iOS uses Ksoup)
kotlin.suppressGradlePluginWarnings=IncorrectCompileOnlyDependencyWarning
```
