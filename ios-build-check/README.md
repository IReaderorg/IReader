# iOS Build Check Module

A utility module to verify iOS Kotlin Multiplatform configuration without requiring macOS.

## Purpose

This module helps developers on Windows/Linux verify that:
- iOS targets are configured correctly in Gradle
- Dependencies resolve properly for iOS platforms
- Common code compiles without errors

## Current Status: source-api Requires Refactoring

The `source-api` module currently uses JVM-only dependencies in `commonMain`:
- **Jsoup** - HTML parsing (JVM-only)
- **OkHttp types** - `Headers`, `Cookie`, `HttpUrl` (JVM-only)
- **Android annotations** - `@Keep` (Android-only)

### Required Migration (per iOS-Source-Architecture.md)

**Phase 1: Migrate from Jsoup to Ksoup**
```kotlin
// Before (Jsoup - JVM only)
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

// After (Ksoup - KMP)
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.Ksoup
```

**Phase 2: Replace OkHttp types with Ktor/common types**
- Replace `okhttp3.Headers` with `Map<String, String>` or Ktor headers
- Replace `okhttp3.Cookie` with a common `Cookie` data class
- Replace `okhttp3.HttpUrl` with `String` URLs

Until this refactoring is complete, iOS compilation of `source-api` will fail.

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


## Module iOS Compilation Status

| Module | Status | Notes |
|--------|--------|-------|
| `i18n` | ✅ Ready | No JVM dependencies |
| `ios-build-check` | ✅ Ready | Test module |
| `core` | ⚠️ Partial | Some expect/actual may need fixes |
| `data` | ⚠️ Partial | Depends on source-api |
| `domain` | ⚠️ Partial | Depends on source-api |
| `source-api` | ❌ Blocked | Requires Jsoup→Ksoup migration |

## Next Steps for Full iOS Support

1. **Migrate source-api to Ksoup** (biggest task)
   - Add Ksoup dependency: `com.fleeksoft.ksoup:ksoup:0.1.2`
   - Update all Jsoup imports to Ksoup
   - Replace OkHttp types with common types

2. **Fix remaining expect/actual mismatches**
   - Run `./gradlew :module:compileKotlinIosArm64` on macOS
   - Fix signature mismatches in iOS implementations

3. **Add iOS-specific implementations**
   - JavaScriptCore for JS plugin execution
   - WKWebView for browser engine
   - NSHTTPCookieStorage for cookies

## Testing Commands

```bash
# Test what works now (Windows/Linux)
./gradlew :i18n:compileCommonMainKotlinMetadata
./gradlew :ios-build-check:compileCommonMainKotlinMetadata

# Full iOS compilation (macOS only)
./gradlew :source-api:compileKotlinIosArm64  # Will fail until Ksoup migration
```
