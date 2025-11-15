# JavaScript Plugin Testing Guide

## Overview

This guide provides comprehensive instructions for testing the JavaScript plugin system in IReader. It covers unit tests, integration tests, end-to-end tests, performance tests, and manual testing procedures.

---

## Table of Contents

1. [Test Environment Setup](#test-environment-setup)
2. [Running Automated Tests](#running-automated-tests)
3. [Unit Tests](#unit-tests)
4. [Integration Tests](#integration-tests)
5. [End-to-End Tests](#end-to-end-tests)
6. [Performance Tests](#performance-tests)
7. [Manual Testing](#manual-testing)
8. [Platform-Specific Testing](#platform-specific-testing)
9. [Real Plugin Testing](#real-plugin-testing)
10. [Stress Testing](#stress-testing)
11. [Troubleshooting](#troubleshooting)

---

## Test Environment Setup

### Prerequisites

- JDK 11 or higher
- Android SDK (for Android tests)
- Gradle 7.0+
- Git

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/IReaderorg/IReader.git
   cd IReader
   ```

2. **Install dependencies**
   ```bash
   ./gradlew build
   ```

3. **Verify setup**
   ```bash
   ./gradlew tasks
   ```

---

## Running Automated Tests

### Quick Test Execution

**Windows:**
```cmd
scripts\run-js-plugin-tests.bat
```

**Unix/Linux/macOS:**
```bash
chmod +x scripts/run-js-plugin-tests.sh
./scripts/run-js-plugin-tests.sh
```

### Individual Test Suites

**All JS Plugin Tests:**
```bash
./gradlew domain:testDebugUnitTest --tests "ireader.domain.js.*"
```

**Specific Test Class:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginEndToEndTest"
```

**Specific Test Method:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginEndToEndTest.testCompleteUserFlow"
```

### Code Coverage

**Generate Coverage Report:**
```bash
./gradlew koverHtmlReport
```

**View Report:**
Open `build/reports/kover/html/index.html` in a browser

**Coverage Targets:**
- Overall: >80%
- Core modules: >85%
- Critical paths: >90%

---

## Unit Tests

### Test Categories

#### 1. JSEngine Tests
**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSEngineTest.kt`

**Coverage:**
- Script execution
- Function calling
- Global object management
- Error handling
- Memory limits
- Timeout handling

**Run:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSEngineTest"
```

#### 2. JSLibraryProvider Tests
**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSLibraryProviderTest.kt`

**Coverage:**
- require() function
- fetch() implementation
- Storage operations
- Library loading
- Error handling

**Run:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSLibraryProviderTest"
```

#### 3. JSPluginBridge Tests
**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSPluginBridgeTest.kt`

**Coverage:**
- Method invocation
- Data conversion (JS ↔ Kotlin)
- Async/Promise handling
- Error propagation
- Timeout handling

**Run:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginBridgeTest"
```

#### 4. JSPluginValidator Tests
**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSPluginValidatorTest.kt`

**Coverage:**
- Code validation
- Metadata validation
- Security checks
- Version validation

**Run:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginValidatorTest"
```

#### 5. JSFilterConverter Tests
**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSFilterConverterTest.kt`

**Coverage:**
- Filter definition parsing
- Filter value conversion
- All filter types
- Error handling

**Run:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSFilterConverterTest"
```

---

## Integration Tests

### JSPluginLoader Integration Tests

**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSPluginLoaderTest.kt`

**Coverage:**
- Plugin discovery
- Plugin loading
- Cache management
- Error handling
- Multiple plugins

**Run:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginLoaderTest"
```

### JSPluginSource Integration Tests

**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSPluginSourceTest.kt`

**Coverage:**
- Source interface implementation
- Data mapping
- Chapter loading
- Content retrieval

**Run:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginSourceTest"
```

---

## End-to-End Tests

### Complete User Flow Tests

**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSPluginEndToEndTest.kt`

**Test Scenarios:**

1. **Complete User Flow**
   - Load plugin → Browse → Search → Open novel → Read chapter
   - Verifies entire pipeline works correctly

2. **Multiple Plugins Simultaneously**
   - Load 5+ plugins
   - Execute operations concurrently
   - Verify no interference

3. **Plugin Enable/Disable**
   - Load plugin
   - Unload plugin
   - Reload plugin
   - Verify state management

4. **Error Scenarios**
   - Malformed plugin (syntax error)
   - Missing methods
   - Timeout scenarios
   - Network failures

5. **Backward Compatibility**
   - Verify existing sources still work
   - No breaking changes

**Run All E2E Tests:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginEndToEndTest"
```

**Run Specific E2E Test:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginEndToEndTest.testCompleteUserFlow"
```

---

## Performance Tests

### Performance Benchmarks

**Location:** `domain/src/commonTest/kotlin/ireader/domain/js/JSPluginPerformanceTest.kt`

**Metrics Tested:**

| Metric | Target | Test Method |
|--------|--------|-------------|
| Plugin Load Time | < 500ms | `testPluginLoadTime()` |
| Method Execution | < 2s | `testMethodExecutionTime()` |
| Memory Usage (10 plugins) | < 100MB | `testMemoryUsage()` |
| Startup Impact | < 200ms | `testStartupTimeImpact()` |
| Concurrent Execution | 5+ plugins | `testConcurrentExecution()` |
| Cache Effectiveness | 30%+ improvement | `testCacheEffectiveness()` |
| Large Files | < 1s for 500KB | `testLargePluginFile()` |
| Memory Leaks | < 10MB growth | `testMemoryLeaks()` |

**Run All Performance Tests:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginPerformanceTest"
```

**Run Specific Performance Test:**
```bash
./gradlew domain:testDebugUnitTest --tests "JSPluginPerformanceTest.testPluginLoadTime"
```

### Performance Profiling

**Android Profiler:**
1. Open Android Studio
2. Run app in debug mode
3. Open Profiler tab
4. Monitor CPU, Memory, Network
5. Load plugins and execute operations
6. Analyze bottlenecks

**IntelliJ Profiler (Desktop):**
1. Run with profiler: `Run → Profile`
2. Load plugins
3. Execute operations
4. Analyze flame graphs
5. Identify hot paths

---

## Manual Testing

### Test Checklist

#### 1. Plugin Installation

- [ ] Create plugins directory
- [ ] Copy plugin file
- [ ] Verify file permissions
- [ ] Restart app
- [ ] Check plugin appears in catalog list

#### 2. Plugin Loading

- [ ] Enable JS plugins in settings
- [ ] Load single plugin
- [ ] Load multiple plugins (5+)
- [ ] Verify icons display correctly
- [ ] Check metadata (name, version, site)

#### 3. Browsing

- [ ] Open plugin catalog
- [ ] Browse popular novels
- [ ] Verify novel list displays
- [ ] Check covers load
- [ ] Test pagination (next page)

#### 4. Searching

- [ ] Enter search query
- [ ] Verify results display
- [ ] Check relevance
- [ ] Test empty results
- [ ] Test special characters

#### 5. Filters

- [ ] Open filter panel
- [ ] Apply Picker filter
- [ ] Apply TextInput filter
- [ ] Apply CheckboxGroup filter
- [ ] Verify filtered results

#### 6. Novel Details

- [ ] Open novel from list
- [ ] Verify title, cover, summary
- [ ] Check author, genres, status
- [ ] Verify chapter list loads
- [ ] Check chapter count

#### 7. Reading

- [ ] Open chapter
- [ ] Verify content displays
- [ ] Check formatting
- [ ] Test navigation (next/previous)
- [ ] Verify bookmarks work

#### 8. Settings

- [ ] Open JS plugin settings
- [ ] Toggle enable/disable
- [ ] Adjust timeout
- [ ] Change concurrent limit
- [ ] Enable debug mode
- [ ] Check logs

#### 9. Updates

- [ ] Check for updates
- [ ] Download update
- [ ] Install update
- [ ] Verify new version
- [ ] Test rollback

#### 10. Error Handling

- [ ] Load malformed plugin
- [ ] Trigger timeout
- [ ] Simulate network error
- [ ] Test with missing methods
- [ ] Verify error messages

---

## Platform-Specific Testing

### Android Testing

#### Devices to Test

- **Android 8.0 (API 26)** - Minimum supported version
- **Android 10 (API 29)** - Common version
- **Android 13 (API 33)** - Latest stable
- **Phone** - Various screen sizes
- **Tablet** - Large screen layout

#### Android-Specific Tests

1. **APK Installation**
   ```bash
   ./gradlew assembleDebug
   adb install -r android/build/outputs/apk/debug/android-debug.apk
   ```

2. **Plugin Directory**
   - Verify: `/data/data/com.ireader/files/js-plugins/`
   - Check permissions
   - Test file access

3. **Memory Constraints**
   - Monitor with Android Profiler
   - Test on low-memory devices
   - Verify no OOM errors

4. **Battery Impact**
   - Use Battery Historian
   - Monitor power consumption
   - Verify acceptable drain

5. **ProGuard/R8**
   ```bash
   ./gradlew assembleRelease
   ```
   - Verify obfuscation
   - Test release build
   - Check mapping file

#### Run Android Tests

```bash
# Unit tests
./gradlew domain:testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew domain:connectedAndroidTest
```

### Desktop Testing

#### Platforms to Test

- **Windows 10/11** - x64
- **macOS Intel** - x64
- **macOS Apple Silicon** - ARM64
- **Linux Ubuntu** - x64

#### Desktop-Specific Tests

1. **Build Desktop App**
   ```bash
   ./gradlew desktop:packageDistributionForCurrentOS
   ```

2. **Plugin Directory**
   - Windows: `%USERPROFILE%\.ireader\js-plugins\`
   - macOS/Linux: `~/.ireader/js-plugins/`

3. **JavaScript Engine**
   - Verify GraalVM loads
   - Test ES6+ features
   - Check performance

4. **File System**
   - Test path handling
   - Verify permissions
   - Check cross-platform compatibility

#### Run Desktop Tests

```bash
./gradlew desktop:test
```

---

## Real Plugin Testing

### LNReader Plugin Testing

#### Popular Plugins to Test

1. **NovelBuddy**
   - Browse popular
   - Search novels
   - Read chapters

2. **LightNovelPub**
   - Test filters
   - Verify pagination
   - Check images

3. **ReadLightNovel**
   - Test search
   - Verify metadata
   - Check chapter loading

4. **BoxNovel**
   - Browse genres
   - Test filters
   - Read content

5. **NovelFull**
   - Search functionality
   - Chapter navigation
   - Content formatting

6. **WuxiaWorld**
   - Browse popular
   - Test authentication (if required)
   - Verify premium content handling

7. **ScribbleHub**
   - User-generated content
   - Tags and filters
   - Chapter updates

8. **RoyalRoad**
   - Fiction browsing
   - Advanced search
   - Chapter comments

9. **WebNovel**
   - Premium content
   - Coins/currency
   - Locked chapters

10. **Wattpad**
    - Story discovery
    - Reading lists
    - Social features

#### Testing Procedure

For each plugin:

1. **Installation**
   - Download plugin file
   - Place in plugins directory
   - Restart app

2. **Basic Functionality**
   - Browse popular novels
   - Search for specific novel
   - Open novel details
   - Read first chapter

3. **Advanced Features**
   - Apply filters (if available)
   - Test pagination
   - Check images/covers
   - Verify metadata accuracy

4. **Error Handling**
   - Test with invalid URLs
   - Simulate network errors
   - Check timeout handling

5. **Performance**
   - Measure load time
   - Check memory usage
   - Monitor responsiveness

#### Test Results Template

```markdown
## Plugin: [Plugin Name]

**Version:** [version]
**Test Date:** [date]
**Tester:** [name]

### Results

- [ ] Installation successful
- [ ] Browse popular works
- [ ] Search functionality
- [ ] Novel details load
- [ ] Chapters load
- [ ] Content displays correctly
- [ ] Filters work (if applicable)
- [ ] Performance acceptable
- [ ] No errors or crashes

### Issues Found

1. [Issue description]
2. [Issue description]

### Notes

[Additional observations]
```

---

## Stress Testing

### Load Testing

#### Test 1: Many Plugins (50+)

```bash
# Create 50 test plugins
for i in {1..50}; do
  cp test-plugin.js plugins/test-plugin-$i.js
  sed -i "s/test-plugin/test-plugin-$i/g" plugins/test-plugin-$i.js
done

# Load and test
./gradlew domain:testDebugUnitTest --tests "JSPluginPerformanceTest.testStressWithManyPlugins"
```

**Verify:**
- All plugins load
- No crashes
- Memory usage acceptable
- Performance degradation minimal

#### Test 2: Concurrent Requests (100+)

```kotlin
// Execute 100 concurrent browse operations
val results = (1..100).map {
    async {
        catalog.source.getPopularNovels(page = 1, filters = emptyMap())
    }
}.awaitAll()
```

**Verify:**
- All requests complete
- No deadlocks
- No race conditions
- Response times acceptable

#### Test 3: Extended Operation (1 hour+)

```bash
# Run continuous operations for 1 hour
./gradlew domain:testDebugUnitTest --tests "JSPluginStressTest.testExtendedOperation"
```

**Monitor:**
- Memory usage over time
- CPU usage
- Response times
- Error rate
- Resource leaks

### Stress Test Checklist

- [ ] 50+ plugins loaded successfully
- [ ] 100+ concurrent requests handled
- [ ] 1 hour+ continuous operation stable
- [ ] No memory leaks detected
- [ ] No performance degradation
- [ ] No crashes or errors
- [ ] Resource usage acceptable

---

## Troubleshooting

### Common Test Failures

#### 1. Plugin Load Failures

**Symptom:** Plugin fails to load

**Possible Causes:**
- Invalid JavaScript syntax
- Missing required methods
- Validation errors

**Solution:**
- Check plugin code syntax
- Verify all required methods present
- Enable debug mode
- Check logs

#### 2. Timeout Errors

**Symptom:** Tests fail with timeout

**Possible Causes:**
- Slow network
- Heavy operations
- Infinite loops

**Solution:**
- Increase timeout in settings
- Check network connection
- Review plugin code for loops
- Use mock data for tests

#### 3. Memory Issues

**Symptom:** OutOfMemoryError

**Possible Causes:**
- Too many plugins loaded
- Memory leaks
- Large data sets

**Solution:**
- Reduce plugin count
- Check for leaks with profiler
- Increase heap size: `-Xmx2g`
- Dispose engines properly

#### 4. Platform-Specific Failures

**Symptom:** Tests pass on one platform, fail on another

**Possible Causes:**
- Path separator differences
- File system permissions
- JavaScript engine differences

**Solution:**
- Use platform-agnostic paths
- Check file permissions
- Test on all platforms
- Use expect/actual pattern

### Debug Mode

**Enable Debug Logging:**

1. Open Settings → JavaScript Plugins
2. Enable "Debug Mode"
3. Reproduce issue
4. Check logs

**Log Locations:**
- Android: Logcat
- Desktop: Console output

**Log Levels:**
- DEBUG: Detailed execution info
- INFO: General information
- ERROR: Error messages

### Test Data

**Create Test Plugins:**

```javascript
// Minimal test plugin
const plugin = {
    id: 'test-plugin',
    name: 'Test Plugin',
    version: '1.0.0',
    site: 'https://example.com',
    icon: 'https://example.com/icon.png',
    lang: 'en',
    
    popularNovels: function(page, options) {
        return [{
            name: 'Test Novel',
            path: '/novel/test',
            cover: 'https://example.com/cover.jpg'
        }];
    },
    
    searchNovels: function(searchTerm, page) {
        return [{
            name: 'Search Result',
            path: '/novel/search',
            cover: 'https://example.com/search.jpg'
        }];
    },
    
    parseNovel: function(novelPath) {
        return {
            name: 'Test Novel',
            path: novelPath,
            summary: 'Test summary',
            chapters: [{
                name: 'Chapter 1',
                path: '/chapter/1'
            }]
        };
    },
    
    parseChapter: function(chapterPath) {
        return '<p>Test content</p>';
    }
};

plugin;
```

---

## Continuous Integration

### CI/CD Pipeline

**GitHub Actions Workflow:**

```yaml
name: JS Plugin Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 11
      uses: actions/setup-java@v2
      with:
        java-version: '11'
    
    - name: Run tests
      run: ./gradlew domain:testDebugUnitTest --tests "ireader.domain.js.*"
    
    - name: Generate coverage
      run: ./gradlew koverHtmlReport
    
    - name: Upload coverage
      uses: codecov/codecov-action@v2
```

### Pre-commit Hooks

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Running JS plugin tests..."
./gradlew domain:testDebugUnitTest --tests "ireader.domain.js.*"

if [ $? -ne 0 ]; then
    echo "Tests failed! Commit aborted."
    exit 1
fi

echo "Tests passed!"
```

---

## Test Reporting

### Generate Test Report

```bash
./gradlew domain:testDebugUnitTest
```

**Report Location:**
`domain/build/reports/tests/testDebugUnitTest/index.html`

### Coverage Report

```bash
./gradlew koverHtmlReport
```

**Report Location:**
`build/reports/kover/html/index.html`

### Test Summary

After running tests, check:
- Total tests run
- Tests passed
- Tests failed
- Tests skipped
- Execution time
- Code coverage percentage

---

## Best Practices

### Writing Tests

1. **Follow AAA Pattern**
   - Arrange: Set up test data
   - Act: Execute operation
   - Assert: Verify results

2. **Use Descriptive Names**
   ```kotlin
   @Test
   fun testPluginLoadTime_shouldBeUnder500ms()
   ```

3. **Test One Thing**
   - Each test should verify one behavior
   - Keep tests focused and simple

4. **Use Test Fixtures**
   - Create reusable test data
   - Use @BeforeTest and @AfterTest

5. **Mock External Dependencies**
   - Use MockK for mocking
   - Avoid real network calls in unit tests

### Running Tests

1. **Run Tests Frequently**
   - Before committing
   - After making changes
   - In CI/CD pipeline

2. **Fix Failing Tests Immediately**
   - Don't ignore failures
   - Don't disable tests
   - Investigate and fix

3. **Monitor Performance**
   - Track test execution time
   - Optimize slow tests
   - Use parallel execution

4. **Maintain Test Coverage**
   - Aim for >80% coverage
   - Focus on critical paths
   - Add tests for bug fixes

---

## Resources

### Documentation

- [Architecture Guide](ARCHITECTURE.md)
- [JS Plugin System](js-plugin-system.md)
- [Plugin Development Guide](plugin-development/)

### Tools

- [Gradle](https://gradle.org/)
- [JUnit](https://junit.org/)
- [MockK](https://mockk.io/)
- [Kover](https://github.com/Kotlin/kotlinx-kover)

### Community

- [GitHub Issues](https://github.com/IReaderorg/IReader/issues)
- [Discord Server](https://discord.gg/your-invite)
- [Discussions](https://github.com/IReaderorg/IReader/discussions)

---

**Last Updated:** [Date]
**Version:** 1.0
