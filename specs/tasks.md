# Tasks: Local WiFi Book Sync

## Phase 1: Foundation and Core Infrastructure

### 1.1 Project Setup and Module Structure
- [ ] 1.1.1 Create new KMP module `sync` with Android and Desktop targets
- [ ] 1.1.2 Add required dependencies (Ktor, mDNS libraries, kotlinx-serialization)
- [ ] 1.1.3 Set up module structure with common, Android, and Desktop source sets
- [ ] 1.1.4 Configure Koin dependency injection for sync module
- [ ] 1.1.5 Create base package structure (discovery, transfer, conflict, repository)

### 1.2 Data Models (Common)
- [ ] 1.2.1 Create DeviceInfo and DiscoveredDevice data classes
- [ ] 1.2.2 Create SyncData, SyncMetadata, and SyncManifest data classes
- [ ] 1.2.3 Create BookSyncData, ReadingProgressData, and BookmarkData data classes
- [ ] 1.2.4 Create DataConflict and ConflictResolutionStrategy enums
- [ ] 1.2.5 Create SyncStatus sealed class hierarchy
- [ ] 1.2.6 Create SyncError sealed class hierarchy
- [ ] 1.2.7 Add kotlinx-serialization annotations to all data models
- [ ] 1.2.8 Write unit tests for data model validation

### 1.3 Core Interfaces (Common)
- [ ] 1.3.1 Define SyncManager interface
- [ ] 1.3.2 Define DiscoveryService interface
- [ ] 1.3.3 Define TransferService interface
- [ ] 1.3.4 Define ConflictResolver interface
- [ ] 1.3.5 Define SyncRepository interface
- [ ] 1.3.6 Document all interfaces with KDoc comments

## Phase 2: Device Discovery Implementation

### 2.1 Discovery Service - Android Implementation
- [ ] 2.1.1 Implement DiscoveryService using Android NsdManager
- [ ] 2.1.2 Implement mDNS service registration with device info
- [ ] 2.1.3 Implement mDNS service discovery listener
- [ ] 2.1.4 Implement device reachability verification (ping)
- [ ] 2.1.5 Handle network change events (WiFi connect/disconnect)
- [ ] 2.1.6 Implement device list cleanup (remove stale devices)
- [ ] 2.1.7 Write unit tests for Android discovery service

### 2.2 Discovery Service - Desktop Implementation
- [ ] 2.2.1 Add JmDNS library dependency for Desktop
- [ ] 2.2.2 Implement DiscoveryService using JmDNS
- [ ] 2.2.3 Implement mDNS service registration with device info
- [ ] 2.2.4 Implement mDNS service discovery listener
- [ ] 2.2.5 Implement device reachability verification
- [ ] 2.2.6 Handle network change events
- [ ] 2.2.7 Write unit tests for Desktop discovery service

### 2.3 Discovery Integration Tests
- [ ] 2.3.1 Test device discovery between Android and Desktop
- [ ] 2.3.2 Test device removal when offline
- [ ] 2.3.3 Test network change handling
- [ ] 2.3.4 Test concurrent device discovery
- [ ] 2.3.5 Property test: device discovery symmetry


## Phase 3: Data Transfer Implementation

### 3.1 Transfer Service - Common
- [ ] 3.1.1 Create WebSocket message protocol (handshake, data, ack, error)
- [ ] 3.1.2 Implement data serialization/deserialization
- [ ] 3.1.3 Implement chunked file transfer logic
- [ ] 3.1.4 Implement progress tracking for transfers
- [ ] 3.1.5 Implement checksum calculation (SHA-256)
- [ ] 3.1.6 Write unit tests for transfer protocol

### 3.2 Transfer Service - Platform Implementation
- [ ] 3.2.1 Implement WebSocket server using Ktor (common)
- [ ] 3.2.2 Implement WebSocket client using Ktor (common)
- [ ] 3.2.3 Implement connection lifecycle management
- [ ] 3.2.4 Implement file streaming for large files
- [ ] 3.2.5 Implement retry logic for failed transfers
- [ ] 3.2.6 Implement connection timeout handling
- [ ] 3.2.7 Write unit tests for transfer service

### 3.3 Transfer Integration Tests
- [ ] 3.3.1 Test small file transfer (< 1 MB)
- [ ] 3.3.2 Test large file transfer (> 100 MB)
- [ ] 3.3.3 Test multiple file transfers
- [ ] 3.3.4 Test transfer interruption and retry
- [ ] 3.3.5 Test checksum verification
- [ ] 3.3.6 Property test: file integrity after transfer
- [ ] 3.3.7 Property test: progress monotonicity

## Phase 4: Conflict Resolution Implementation

### 4.1 Conflict Detection
- [ ] 4.1.1 Implement conflict detection for reading progress
- [ ] 4.1.2 Implement conflict detection for bookmarks
- [ ] 4.1.3 Implement conflict detection for book metadata
- [ ] 4.1.4 Implement timestamp comparison logic
- [ ] 4.1.5 Write unit tests for conflict detection
- [ ] 4.1.6 Property test: conflict detection completeness

### 4.2 Conflict Resolution Strategies
- [ ] 4.2.1 Implement Latest Timestamp strategy
- [ ] 4.2.2 Implement Local Wins strategy
- [ ] 4.2.3 Implement Remote Wins strategy
- [ ] 4.2.4 Implement Merge strategy for reading progress
- [ ] 4.2.5 Implement Merge strategy for book metadata
- [ ] 4.2.6 Implement Manual resolution interface
- [ ] 4.2.7 Write unit tests for each strategy
- [ ] 4.2.8 Property test: resolution determinism

### 4.3 Conflict Resolution Integration
- [ ] 4.3.1 Integrate conflict resolver with sync flow
- [ ] 4.3.2 Implement user prompt for manual resolution
- [ ] 4.3.3 Test conflict resolution in end-to-end sync
- [ ] 4.3.4 Test all resolution strategies with real data

## Phase 5: Sync Repository Implementation

### 5.1 Database Schema
- [ ] 5.1.1 Create sync_metadata table (device_id, last_sync_time)
- [ ] 5.1.2 Create trusted_devices table (device_id, device_name, paired_at, expires_at)
- [ ] 5.1.3 Create sync_log table (sync_id, device_id, status, timestamp)
- [ ] 5.1.4 Add migration scripts for new tables
- [ ] 5.1.5 Write tests for database migrations

### 5.2 Repository Implementation
- [ ] 5.2.1 Implement getBooksToSync() query
- [ ] 5.2.2 Implement getReadingProgress() query
- [ ] 5.2.3 Implement getBookmarks() query
- [ ] 5.2.4 Implement getSyncManifest() logic
- [ ] 5.2.5 Implement applySync() transaction
- [ ] 5.2.6 Implement sync timestamp management
- [ ] 5.2.7 Implement trusted devices management
- [ ] 5.2.8 Write unit tests for repository


## Phase 6: Security Implementation

### 6.1 Device Pairing
- [ ] 6.1.1 Implement PIN generation (6-digit random)
- [ ] 6.1.2 Implement PIN verification protocol
- [ ] 6.1.3 Implement device trust storage (secure storage)
- [ ] 6.1.4 Implement trust expiration logic (30 days)
- [ ] 6.1.5 Implement re-authentication flow
- [ ] 6.1.6 Write unit tests for pairing logic

### 6.2 Encryption
- [ ] 6.2.1 Implement TLS/SSL for WebSocket connections
- [ ] 6.2.2 Generate self-signed certificates for local network
- [ ] 6.2.3 Implement certificate pinning for paired devices
- [ ] 6.2.4 Implement AES-256 payload encryption
- [ ] 6.2.5 Implement secure key storage (Android Keystore / Java Keystore)
- [ ] 6.2.6 Write unit tests for encryption

### 6.3 Security Validation
- [ ] 6.3.1 Test PIN-based pairing flow
- [ ] 6.3.2 Test certificate pinning
- [ ] 6.3.3 Test encrypted data transfer
- [ ] 6.3.4 Test trust expiration and re-authentication
- [ ] 6.3.5 Verify no data leaks to external networks

## Phase 7: Sync Manager Implementation

### 7.1 Core Sync Logic
- [ ] 7.1.1 Implement startSync() lifecycle
- [ ] 7.1.2 Implement stopSync() cleanup
- [ ] 7.1.3 Implement syncWithDevice() orchestration
- [ ] 7.1.4 Implement sync manifest exchange
- [ ] 7.1.5 Implement sync plan calculation (what to send/receive)
- [ ] 7.1.6 Implement incremental sync logic
- [ ] 7.1.7 Write unit tests for sync manager

### 7.2 Status Management
- [ ] 7.2.1 Implement status flow (Idle, Discovering, Connecting, etc.)
- [ ] 7.2.2 Implement progress tracking and updates
- [ ] 7.2.3 Implement error handling and status updates
- [ ] 7.2.4 Write unit tests for status management

### 7.3 Sync Integration Tests
- [ ] 7.3.1 Test complete sync flow (discovery to completion)
- [ ] 7.3.2 Test incremental sync
- [ ] 7.3.3 Test sync with conflicts
- [ ] 7.3.4 Test sync cancellation
- [ ] 7.3.5 Test network interruption during sync
- [ ] 7.3.6 Property test: sync idempotency
- [ ] 7.3.7 Property test: connection cleanup

## Phase 8: UI Implementation

### 8.1 Sync Screen - Common UI
- [ ] 8.1.1 Create SyncViewModel with state management
- [ ] 8.1.2 Implement discovered devices list UI
- [ ] 8.1.3 Implement sync status display
- [ ] 8.1.4 Implement progress indicator
- [ ] 8.1.5 Implement error message display
- [ ] 8.1.6 Implement sync controls (start, stop, cancel)

### 8.2 Device Pairing UI
- [ ] 8.2.1 Create pairing dialog/screen
- [ ] 8.2.2 Display PIN code prominently
- [ ] 8.2.3 Implement PIN verification UI
- [ ] 8.2.4 Show pairing success/failure feedback

### 8.3 Settings UI
- [ ] 8.3.1 Add sync settings section
- [ ] 8.3.2 Implement conflict resolution strategy selector
- [ ] 8.3.3 Implement trusted devices list
- [ ] 8.3.4 Implement device removal (revoke trust)
- [ ] 8.3.5 Implement selective sync options
- [ ] 8.3.6 Add sync on charger only toggle

### 8.4 Notifications (Android)
- [ ] 8.4.1 Create foreground service for sync
- [ ] 8.4.2 Implement persistent notification with progress
- [ ] 8.4.3 Implement completion notification
- [ ] 8.4.4 Implement error notification
- [ ] 8.4.5 Add notification actions (cancel sync)


## Phase 9: Performance Optimization

### 9.1 Memory Optimization
- [ ] 9.1.1 Implement streaming for large files (avoid loading in memory)
- [ ] 9.1.2 Implement database batching for bulk operations
- [ ] 9.1.3 Optimize Flow collectors to prevent memory leaks
- [ ] 9.1.4 Profile memory usage during large syncs
- [ ] 9.1.5 Ensure memory usage stays under 200 MB

### 9.2 Network Optimization
- [ ] 9.2.1 Implement data compression for metadata
- [ ] 9.2.2 Optimize chunk size for file transfers
- [ ] 9.2.3 Implement connection pooling
- [ ] 9.2.4 Profile network throughput
- [ ] 9.2.5 Ensure transfer speed meets 10 MB/s target

### 9.3 Battery Optimization
- [ ] 9.3.1 Implement wake lock management
- [ ] 9.3.2 Respect battery saver mode
- [ ] 9.3.3 Implement adaptive sync based on battery level
- [ ] 9.3.4 Profile battery usage during sync
- [ ] 9.3.5 Ensure CPU usage stays under 30%

### 9.4 Concurrency Optimization
- [ ] 9.4.1 Use appropriate coroutine dispatchers
- [ ] 9.4.2 Implement parallel processing where beneficial
- [ ] 9.4.3 Add proper synchronization for shared state
- [ ] 9.4.4 Profile coroutine performance

## Phase 10: Testing and Quality Assurance

### 10.1 Unit Tests (TDD - Write Tests First)
- [ ] 10.1.1 Write and pass tests for all data models
- [ ] 10.1.2 Write and pass tests for discovery service
- [ ] 10.1.3 Write and pass tests for transfer service
- [ ] 10.1.4 Write and pass tests for conflict resolver
- [ ] 10.1.5 Write and pass tests for sync repository
- [ ] 10.1.6 Write and pass tests for sync manager
- [ ] 10.1.7 Achieve 80%+ code coverage

### 10.2 Property-Based Tests
- [ ] 10.2.1 Write property test: file integrity after transfer
- [ ] 10.2.2 Write property test: conflict detection completeness
- [ ] 10.2.3 Write property test: sync idempotency
- [ ] 10.2.4 Write property test: progress monotonicity
- [ ] 10.2.5 Write property test: device discovery symmetry
- [ ] 10.2.6 Write property test: conflict resolution determinism
- [ ] 10.2.7 Write property test: connection cleanup
- [ ] 10.2.8 Write property test: timestamp consistency

### 10.3 Integration Tests
- [ ] 10.3.1 Test Android-to-Android sync
- [ ] 10.3.2 Test Desktop-to-Desktop sync
- [ ] 10.3.3 Test Android-to-Desktop sync
- [ ] 10.3.4 Test Desktop-to-Android sync
- [ ] 10.3.5 Test sync with 1000+ books
- [ ] 10.3.6 Test sync with large files (500 MB)
- [ ] 10.3.7 Test network interruption scenarios
- [ ] 10.3.8 Test concurrent operations

### 10.4 UI Tests
- [ ] 10.4.1 Test device discovery UI updates
- [ ] 10.4.2 Test sync progress display
- [ ] 10.4.3 Test pairing flow UI
- [ ] 10.4.4 Test error message display
- [ ] 10.4.5 Test settings UI interactions
- [ ] 10.4.6 Test notification display (Android)

### 10.5 Security Tests
- [ ] 10.5.1 Verify TLS/SSL encryption
- [ ] 10.5.2 Verify certificate pinning
- [ ] 10.5.3 Verify no external network connections
- [ ] 10.5.4 Test PIN-based authentication
- [ ] 10.5.5 Test trust expiration
- [ ] 10.5.6 Penetration testing (if resources available)


## Phase 11: Documentation and Polish

### 11.1 Code Documentation
- [ ] 11.1.1 Add KDoc comments to all public APIs
- [ ] 11.1.2 Document platform-specific implementations
- [ ] 11.1.3 Create architecture documentation
- [ ] 11.1.4 Document sync protocol specification
- [ ] 11.1.5 Create troubleshooting guide

### 11.2 User Documentation
- [ ] 11.2.1 Write user guide for WiFi sync feature
- [ ] 11.2.2 Create FAQ for common issues
- [ ] 11.2.3 Document privacy policy for sync
- [ ] 11.2.4 Create setup instructions
- [ ] 11.2.5 Add in-app help text and tooltips

### 11.3 Error Messages and UX Polish
- [ ] 11.3.1 Review and improve all error messages
- [ ] 11.3.2 Add helpful suggestions to error messages
- [ ] 11.3.3 Implement loading states and animations
- [ ] 11.3.4 Add haptic feedback for key actions (Android)
- [ ] 11.3.5 Polish UI transitions and animations

### 11.4 Accessibility
- [ ] 11.4.1 Add content descriptions for all UI elements
- [ ] 11.4.2 Test with screen readers (TalkBack, VoiceOver)
- [ ] 11.4.3 Ensure proper focus order
- [ ] 11.4.4 Test with large text sizes
- [ ] 11.4.5 Ensure sufficient color contrast

## Phase 12: Release Preparation

### 12.1 Performance Validation
- [ ] 12.1.1 Validate device discovery completes within 10 seconds
- [ ] 12.1.2 Validate manifest exchange completes within 5 seconds
- [ ] 12.1.3 Validate transfer speed meets 10 MB/s target
- [ ] 12.1.4 Validate memory usage stays under 200 MB
- [ ] 12.1.5 Validate conflict detection for 1000 items under 2 seconds
- [ ] 12.1.6 Validate UI responsiveness (no ANR)

### 12.2 Compatibility Testing
- [ ] 12.2.1 Test on Android 7.0 (API 24)
- [ ] 12.2.2 Test on Android 14 (latest)
- [ ] 12.2.3 Test on Windows Desktop
- [ ] 12.2.4 Test on macOS Desktop
- [ ] 12.2.5 Test on Linux Desktop
- [ ] 12.2.6 Test on IPv4 networks
- [ ] 12.2.7 Test on IPv6 networks

### 12.3 Edge Case Testing
- [ ] 12.3.1 Test with empty library
- [ ] 12.3.2 Test with 10,000 books
- [ ] 12.3.3 Test with 50 GB total library size
- [ ] 12.3.4 Test with very slow WiFi (1 Mbps)
- [ ] 12.3.5 Test with unstable network (packet loss)
- [ ] 12.3.6 Test with low battery (< 20%)
- [ ] 12.3.7 Test with low storage (< 100 MB)
- [ ] 12.3.8 Test with airplane mode toggle during sync

### 12.4 Final Review
- [ ] 12.4.1 Code review by team
- [ ] 12.4.2 Security review
- [ ] 12.4.3 Performance review
- [ ] 12.4.4 UX review
- [ ] 12.4.5 Documentation review
- [ ] 12.4.6 Test coverage review (ensure 80%+)

### 12.5 Release
- [ ] 12.5.1 Create release notes
- [ ] 12.5.2 Update changelog
- [ ] 12.5.3 Tag release version
- [ ] 12.5.4 Build release artifacts
- [ ] 12.5.5 Submit to app stores (if applicable)
- [ ] 12.5.6 Monitor for issues post-release

## Notes

### TDD Approach
- All tasks in Phase 10.1 (Unit Tests) should follow strict TDD methodology
- Write test first (RED), implement minimal code (GREEN), refactor (REFACTOR)
- Never write production code without a failing test first
- Ensure tests fail for the right reason before implementing

### Dependencies Between Phases
- Phase 2 (Discovery) depends on Phase 1 (Foundation)
- Phase 3 (Transfer) depends on Phase 1 (Foundation)
- Phase 4 (Conflict) depends on Phase 1 (Foundation)
- Phase 5 (Repository) depends on Phase 1 (Foundation)
- Phase 6 (Security) can be done in parallel with Phases 2-5
- Phase 7 (Sync Manager) depends on Phases 2-6
- Phase 8 (UI) depends on Phase 7
- Phase 9 (Optimization) depends on Phases 7-8
- Phase 10 (Testing) runs throughout all phases (TDD)
- Phase 11 (Documentation) can start after Phase 7
- Phase 12 (Release) depends on all previous phases

### Estimated Timeline
- Phase 1: 1 week
- Phase 2: 2 weeks
- Phase 3: 2 weeks
- Phase 4: 1 week
- Phase 5: 1 week
- Phase 6: 2 weeks
- Phase 7: 2 weeks
- Phase 8: 2 weeks
- Phase 9: 1 week
- Phase 10: Ongoing (throughout development)
- Phase 11: 1 week
- Phase 12: 1 week

**Total Estimated Time: 16-18 weeks**

