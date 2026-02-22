# Integration Test Strategy

## Overview

This document outlines the testing strategy for the local sync functionality. All tests follow strict TDD methodology (RED → GREEN → REFACTOR).

## Test Organization

### Test Suites

1. **EndToEndSyncTest** - Complete workflow testing
2. **SecurityIntegrationTest** - Security feature testing
3. **NetworkResilienceTest** - Error handling and recovery
4. **PerformanceIntegrationTest** - Scalability and performance

### Test Isolation

Each test suite is independent and can run in parallel:
- Uses fake implementations (no real network)
- Each test has its own setup/teardown
- No shared mutable state between tests
- Tests can run in any order

## TDD Workflow

### Phase 1: RED (Current State)

All tests are written and will FAIL because:
- SyncRepository is a placeholder
- Real data sources don't exist yet
- Implementation logic is missing

**Expected failures:**
- Method not implemented
- Null pointer exceptions
- Missing functionality

### Phase 2: GREEN (Implementation)

Implement minimal code to make tests pass:

1. **Discovery Implementation**
   - Implement NSD/Bonjour discovery
   - Handle device announcement
   - Manage discovery lifecycle

2. **Pairing Implementation**
   - Implement PIN generation/validation
   - Certificate exchange
   - Trust establishment

3. **Transfer Implementation**
   - Implement manifest exchange
   - Conflict detection
   - Data transfer with progress

4. **Security Implementation**
   - TLS/SSL encryption
   - Certificate pinning
   - Trust expiration

5. **Resilience Implementation**
   - Retry logic with backoff
   - Interruption handling
   - Timeout management

### Phase 3: REFACTOR (Optimization)

Improve code quality while keeping tests green:
- Extract common patterns
- Optimize performance
- Improve readability
- Add documentation

## Test Coverage Matrix

| Feature | Unit Tests | Integration Tests | E2E Tests |
|---------|-----------|-------------------|-----------|
| Discovery | ✅ | ✅ | ✅ |
| Pairing | ✅ | ✅ | ✅ |
| Transfer | ✅ | ✅ | ✅ |
| Security | ✅ | ✅ | ✅ |
| Conflicts | ✅ | ✅ | ✅ |
| Progress | ✅ | ✅ | ✅ |
| Errors | ✅ | ✅ | ✅ |
| Performance | ❌ | ✅ | ❌ |

## Test Scenarios

### Happy Path Scenarios

1. **Simple Sync**
   - Discover device
   - Pair with PIN
   - Sync data
   - Disconnect

2. **Incremental Sync**
   - Initial full sync
   - Modify data
   - Sync only changes

3. **Bidirectional Sync**
   - Both devices have changes
   - Merge without conflicts
   - Verify consistency

### Error Scenarios

1. **Network Errors**
   - Connection lost during discovery
   - Timeout during pairing
   - Interruption during transfer
   - Recovery and resume

2. **Security Errors**
   - Wrong PIN
   - Certificate mismatch
   - Trust expired
   - MITM attack detected

3. **Data Errors**
   - Conflicts detected
   - Invalid data format
   - Corrupted transfer
   - Partial sync

### Edge Cases

1. **Large Datasets**
   - 1000+ books
   - Books with 1000+ chapters
   - 500MB+ total data

2. **Concurrent Operations**
   - Multiple devices syncing
   - Rapid connect/disconnect
   - Overlapping operations

3. **Resource Constraints**
   - Low memory
   - Slow network
   - High latency

## Fake Implementation Strategy

### FakeDiscoveryDataSource

**Purpose**: Simulate device discovery without real network

**Features**:
- Instant discovery (no network delay)
- Configurable device list
- Failure injection
- Timeout simulation

**Usage**:
```kotlin
val fake = FakeDiscoveryDataSource()
fake.addDiscoverableDevice(device)
fake.setDiscoveryTimeout(1000L)
fake.startDiscovery()
```

### FakeTransferDataSource

**Purpose**: Simulate data transfer and security

**Features**:
- PIN validation
- Certificate management
- Progress tracking
- Failure injection
- Delay simulation
- Memory tracking

**Usage**:
```kotlin
val fake = FakeTransferDataSource()
fake.setExpectedPin("123456")
fake.setTransferDelay(100L)
fake.enableEncryptionValidation()
```

### FakeSyncLocalDataSource

**Purpose**: Simulate local database

**Features**:
- In-memory storage
- CRUD operations
- Fast access

**Usage**:
```kotlin
val fake = FakeSyncLocalDataSource()
fake.setBooks(testBooks)
val books = fake.getBooks()
```

## Test Data Builders

### TestFixtures

Provides factory methods for creating test data:

```kotlin
val device = TestFixtures.createDevice(id = "device-1")
val book = TestFixtures.createBook(id = 1L, title = "Test")
```

### BookBuilder

Fluent API for building complex test books:

```kotlin
val book = book {
    withId(1L)
    withTitle("Complex Book")
    addChapters(100, contentSize = 50_000)
}
```

### Bulk Creation

Create multiple test objects easily:

```kotlin
val books = books(1000) { builder, index ->
    builder.withAuthor("Author $index")
}
```

## Assertions

### Standard Assertions

```kotlin
assertTrue(result.isSuccess)
assertEquals(expected, actual)
assertNotNull(value)
```

### Custom Assertions

```kotlin
// Verify sync completed
val session = result.getOrNull()
assertNotNull(session)
assertEquals(SyncStatus.COMPLETED, session.status)

// Verify progress
assertTrue(progressUpdates.any { it.status == SyncStatus.IN_PROGRESS })
assertTrue(progressUpdates.any { it.status == SyncStatus.COMPLETED })

// Verify error type
val error = result.exceptionOrNull()
assertTrue(error is SyncException)
assertEquals(SyncErrorType.TIMEOUT, (error as SyncException).errorType)
```

## Performance Benchmarks

### Target Metrics

- **Discovery**: < 2 seconds
- **Pairing**: < 1 second
- **Sync 100 books**: < 10 seconds
- **Sync 1000 books**: < 60 seconds
- **Memory usage**: < 100MB peak

### Measurement

```kotlin
val duration = measureTime {
    repository.syncWithDevice(deviceId)
}
assertTrue(duration.inWholeSeconds < 30)
```

## Continuous Integration

### Test Execution

```bash
# Run all tests
.\gradlew.bat :data:test

# Run specific suite
.\gradlew.bat :data:testDebugUnitTest --tests "EndToEndSyncTest"

# Run with coverage
.\gradlew.bat :data:testDebugUnitTestCoverage
```

### Coverage Goals

- **Line coverage**: > 80%
- **Branch coverage**: > 70%
- **Integration coverage**: 100% of critical paths

## Debugging Failed Tests

### Common Issues

1. **Test passes immediately**
   - Implementation already exists
   - Test is not testing the right thing
   - Fix: Verify test logic

2. **Test errors instead of fails**
   - Missing dependencies
   - Syntax errors
   - Fix: Check imports and syntax

3. **Test fails with wrong error**
   - Unexpected exception
   - Wrong assertion
   - Fix: Check error messages

### Debug Strategy

1. Read the failure message
2. Check the assertion that failed
3. Verify test setup (Arrange)
4. Verify test execution (Act)
5. Verify test expectations (Assert)

## Maintenance

### Adding New Tests

1. Follow AAA pattern (Arrange-Act-Assert)
2. Use descriptive test names
3. Test one thing per test
4. Keep tests independent
5. Use test fixtures

### Updating Tests

1. Run tests before changes
2. Update test expectations
3. Run tests after changes
4. Verify all tests still pass

### Removing Tests

1. Verify test is obsolete
2. Check for dependencies
3. Remove test
4. Verify coverage maintained

## References

- [TDD Methodology](.kiro/steering/tdd-methodology.md)
- [Sync Specification](.kiro/specs/local-sync/)
- [Domain Models](domain/src/commonMain/kotlin/ireader/domain/models/sync/)
