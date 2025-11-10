# Piper TTS Test Suite

This directory contains comprehensive tests for the Piper TTS JNI integration.

## Test Categories

### 1. Unit Tests (`PiperNativeTest.kt`)
Tests core JNI wrapper functionality:
- Voice initialization and shutdown
- Basic synthesis functionality
- Parameter adjustment (speech rate, noise scale, etc.)
- Error handling and validation
- Memory management

### 2. Integration Tests (`PiperIntegrationTest.kt`)
Tests end-to-end functionality:
- Voice catalog and metadata
- Voice download simulation
- Multi-language synthesis
- Voice switching
- Long-running synthesis
- Error recovery

### 3. Performance Tests (`PiperPerformanceTest.kt`)
Measures and validates performance metrics:
- Synthesis latency (target: < 200ms for short texts)
- Memory usage (target: < 500MB per model)
- Throughput (target: > 1000 chars/sec)
- Performance consistency
- Scalability

### 4. Cross-Platform Tests (`PiperCrossPlatformTest.kt`)
Verifies consistent behavior across platforms:
- Platform detection
- Library loading
- Consistent synthesis output
- Platform-specific features
- Resource management

## Running the Tests

### Prerequisites

1. **Native Libraries**: The tests require Piper JNI native libraries to be built and available:
   - Windows: `piper_jni.dll`
   - macOS: `libpiper_jni.dylib`
   - Linux: `libpiper_jni.so`

2. **Voice Models** (optional): For full integration testing, provide test voice models:
   ```
   -Dtest.model.path=/path/to/model.onnx
   -Dtest.config.path=/path/to/model.onnx.json
   ```

3. **Multiple Models** (optional): For multi-language testing:
   ```
   -Dtest.models.dir=/path/to/models/directory
   ```

### Running All Tests

```bash
# Run all desktop tests
./gradlew :domain:desktopTest

# Run with test models
./gradlew :domain:desktopTest -Dtest.model.path=/path/to/model.onnx -Dtest.config.path=/path/to/config.json

# Run with verbose output
./gradlew :domain:desktopTest --info
```

### Running Specific Test Classes

```bash
# Run only unit tests
./gradlew :domain:desktopTest --tests "PiperNativeTest"

# Run only integration tests
./gradlew :domain:desktopTest --tests "PiperIntegrationTest"

# Run only performance tests
./gradlew :domain:desktopTest --tests "PiperPerformanceTest"

# Run only cross-platform tests
./gradlew :domain:desktopTest --tests "PiperCrossPlatformTest"
```

### Running Specific Test Methods

```bash
# Run a specific test method
./gradlew :domain:desktopTest --tests "PiperNativeTest.test initialize with valid model returns positive instance"

# Run all tests matching a pattern
./gradlew :domain:desktopTest --tests "*synthesis*"
```

## Test Behavior

### Graceful Degradation

All tests are designed to gracefully handle missing native libraries or test models:

- If native libraries are not available, tests will be skipped with informative messages
- If test models are not configured, model-dependent tests will be skipped
- Tests will never fail due to missing optional resources

### Test Output

Tests provide detailed output including:
- Performance metrics (latency, throughput, memory usage)
- Platform information
- Error messages and diagnostics
- Progress indicators

### Performance Targets

The performance tests validate against these targets (from Requirements 5.1, 5.2, 5.3):

| Metric | Target | Test |
|--------|--------|------|
| Short text latency | < 200ms | `test short text synthesis latency meets target` |
| Memory per model | < 500MB | `test memory usage per voice model` |
| Throughput | > 1000 chars/sec | `test synthesis throughput meets target` |

## Continuous Integration

These tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run Piper TTS Tests
  run: ./gradlew :domain:desktopTest
  env:
    TEST_MODEL_PATH: ${{ secrets.TEST_MODEL_PATH }}
    TEST_CONFIG_PATH: ${{ secrets.TEST_CONFIG_PATH }}
```

## Troubleshooting

### Tests are Skipped

If tests are being skipped, check:

1. **Native libraries**: Ensure libraries are built and in the correct location
2. **Test models**: Provide test model paths via system properties
3. **Platform support**: Verify your platform is supported (Windows x64, macOS x64/ARM64, Linux x64)

### Tests Fail

If tests fail:

1. **Check diagnostics**: Review test output for detailed error messages
2. **Verify libraries**: Ensure native libraries are compatible with your platform
3. **Check dependencies**: Verify all system dependencies are installed
4. **Review logs**: Check for initialization errors in the output

### Performance Tests Fail

If performance tests fail to meet targets:

1. **Warm up**: Ensure sufficient warm-up iterations
2. **System load**: Run tests on a system with minimal background load
3. **Hardware**: Verify hardware meets minimum requirements
4. **Configuration**: Check JVM settings and memory allocation

## Test Coverage

Current test coverage includes:

- ✅ Voice initialization and shutdown
- ✅ Text synthesis (short, medium, long)
- ✅ Parameter adjustment (speech rate, noise scale, etc.)
- ✅ Error handling and validation
- ✅ Memory management and leak detection
- ✅ Multi-language support
- ✅ Voice switching
- ✅ Performance benchmarking
- ✅ Cross-platform compatibility
- ✅ Resource cleanup

## Contributing

When adding new tests:

1. Follow the existing test structure and naming conventions
2. Include graceful degradation for missing resources
3. Provide detailed output and diagnostics
4. Document performance expectations
5. Ensure tests are deterministic and repeatable

## References

- [Piper TTS Requirements](../../../../../../../../../.kiro/specs/piper-jni-production/requirements.md)
- [Piper TTS Design](../../../../../../../../../.kiro/specs/piper-jni-production/design.md)
- [Piper TTS Tasks](../../../../../../../../../.kiro/specs/piper-jni-production/tasks.md)
