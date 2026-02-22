# Sync Integration Tests

This directory contains comprehensive integration tests for the local sync functionality.

## Test Files

### 1. EndToEndSyncTest.kt
Tests complete sync flows from discovery to completion:
- Complete sync flow (discovery → pairing → sync → disconnect)
- Manifest exchange and comparison
- Conflict detection and resolution
- Data transfer with progress tracking
- Error recovery (transient and persistent errors)

### 2. SecurityIntegrationTest.kt
Tests security features:
- PIN-based pairing flow (success and failure cases)
- PIN attempt lockout after max attempts
- Certificate pinning validation
- Certificate mismatch detection (MITM prevention)
- Encrypted data transfer
- Trust expiration and re-authentication
- Certificate rotation handling

### 3. NetworkResilienceTest.kt
Tests network error handling:
- Network interruption handling and resume
- Connection timeout handling (discovery, pairing, sync)
- Retry logic with exponential backoff
- Graceful degradation (partial sync completion)
- Resumable sync after interruption

### 4. PerformanceIntegrationTest.kt
Tests performance and scalability:
- Sync with 1000+ books
- Batching for large datasets
- Large file transfer with streaming
- Memory usage monitoring
- Concurrent operations (multiple simultaneous syncs)
- Rapid start/stop cycles

## Fake Implementations

### FakeDiscoveryDataSource
Simulates device discovery without real network operations:
- Device discovery simulation
- Configurable timeouts
- Failure mode simulation
- Retry tracking

### FakeTransferDataSource
Simulates data transfer, pairing, and security:
- PIN-based pairing with attempt tracking
- Certificate generation and validation
- Trust management with expiration
- Data encryption simulation
- Transfer progress tracking
- Configurable delays and timeouts
- Failure injection (transient, persistent, interruptions)
- Memory usage tracking
- Batching and streaming simulation

### FakeSyncLocalDataSource
Simulates local database operations:
- In-memory book storage
- CRUD operations for books

## Domain Models

All domain models are located in `domain/src/commonMain/kotlin/ireader/domain/models/sync/`:

- **SyncDevice**: Represents a device available for sync
- **PairedDevice**: Represents a paired device with trust information
- **SyncSession**: Represents an active or completed sync session
- **SyncProgress**: Represents progress of an ongoing sync operation
- **SyncableBook**: Represents a book that can be synced
- **SyncableChapter**: Represents a chapter within a syncable book
- **SyncException**: Exception thrown during sync operations
- **SyncErrorType**: Enum of possible sync error types

## Running Tests

These tests follow TDD principles (RED → GREEN → REFACTOR):

1. **RED Phase**: All tests are currently written and will FAIL because the implementation doesn't exist yet
2. **GREEN Phase**: Implement minimal code to make tests pass
3. **REFACTOR Phase**: Improve code quality while keeping tests green

To run the tests:
```bash
.\gradlew.bat :data:testDebugUnitTest
```

## Test Coverage

The integration tests cover:
- ✅ Complete sync workflows
- ✅ Security features (PIN, certificates, encryption)
- ✅ Network resilience (timeouts, retries, interruptions)
- ✅ Performance with large datasets
- ✅ Concurrent operations
- ✅ Error handling and recovery
- ✅ Progress tracking
- ✅ Conflict detection

## Next Steps

1. Run tests to verify they fail (RED phase)
2. Implement SyncRepository with real data sources
3. Implement discovery, transfer, and local data sources
4. Run tests again to verify they pass (GREEN phase)
5. Refactor implementation for code quality (REFACTOR phase)
