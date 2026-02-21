# Requirements Document: Local WiFi Book Sync

## Feature Overview

Enable seamless synchronization of books, reading progress, bookmarks, and book-related metadata between Android and Desktop devices when connected to the same WiFi network, providing users with a fast, private, and reliable peer-to-peer sync solution.

## Functional Requirements

### FR1: Device Discovery

**FR1.1**: The system shall automatically discover other IReader devices on the same WiFi network using mDNS/DNS-SD protocol.

**FR1.2**: The system shall broadcast device presence including device name, type (Android/Desktop), IP address, and port.

**FR1.3**: The system shall display a list of discovered devices with their names, types, and connection status.

**FR1.4**: The system shall verify device reachability before allowing sync initiation.

**FR1.5**: The system shall update the discovered devices list in real-time as devices join or leave the network.

**FR1.6**: The system shall remove devices from the list if they haven't been seen for more than 5 minutes.

### FR2: Sync Initiation

**FR2.1**: The system shall allow users to manually start the sync service.

**FR2.2**: The system shall allow users to select a discovered device to sync with.

**FR2.3**: The system shall establish a WebSocket connection with the selected device.

**FR2.4**: The system shall exchange sync manifests with the remote device before transferring data.

**FR2.5**: The system shall verify app version compatibility before proceeding with sync.

**FR2.6**: The system shall allow users to cancel an ongoing sync operation.

### FR3: Data Synchronization

**FR3.1**: The system shall sync book files between devices.

**FR3.2**: The system shall sync reading progress (chapter index, offset, progress percentage) for all books.

**FR3.3**: The system shall sync bookmarks (position, note, creation time) for all books.

**FR3.4**: The system shall sync book metadata (title, author, cover URL, added date, updated date).

**FR3.5**: The system shall only transfer books that have changed since the last sync (incremental sync).

**FR3.6**: The system shall verify file integrity using SHA-256 hashes after each book transfer.

**FR3.7**: The system shall track and display sync progress (percentage, current item).

**FR3.8**: The system shall record the timestamp of the last successful sync for each device.


### FR4: Conflict Resolution

**FR4.1**: The system shall detect conflicts when the same data has been modified on both devices since the last sync.

**FR4.2**: The system shall support multiple conflict resolution strategies:
- Latest Timestamp: Use data with the most recent modification time
- Local Wins: Always prefer local device data
- Remote Wins: Always prefer remote device data
- Merge: Attempt to merge compatible changes
- Manual: Prompt user to choose

**FR4.3**: The system shall allow users to configure their preferred conflict resolution strategy.

**FR4.4**: The system shall prompt users for manual resolution when automatic resolution fails or when Manual strategy is selected.

**FR4.5**: The system shall preserve both local and remote data when conflicts cannot be automatically resolved.

**FR4.6**: The system shall apply conflict resolution deterministically (same inputs always produce same output).

### FR5: Status and Notifications

**FR5.1**: The system shall display the current sync status (Idle, Discovering, Connecting, Syncing, Completed, Failed).

**FR5.2**: The system shall show detailed progress during sync (percentage complete, current item being synced).

**FR5.3**: The system shall display the number of items synced and total duration upon completion.

**FR5.4**: The system shall show clear error messages when sync fails, including the reason for failure.

**FR5.5**: The system shall show a persistent notification during active sync operations (Android).

**FR5.6**: The system shall notify users when sync completes successfully or fails.

### FR6: Device Pairing and Trust

**FR6.1**: The system shall require device pairing before first sync using a 6-digit PIN.

**FR6.2**: The system shall display the pairing PIN on both devices for user verification.

**FR6.3**: The system shall maintain a list of trusted (paired) devices.

**FR6.4**: The system shall allow users to view and manage trusted devices.

**FR6.5**: The system shall allow users to revoke trust for specific devices.

**FR6.6**: The system shall auto-expire device trust after 30 days (configurable).

**FR6.7**: The system shall require re-authentication when trust expires.

### FR7: Selective Sync

**FR7.1**: The system shall allow users to choose which types of data to sync (books, progress, bookmarks, metadata).

**FR7.2**: The system shall allow users to select specific books to sync instead of syncing entire library.

**FR7.3**: The system shall display estimated sync size before starting transfer.

**FR7.4**: The system shall warn users if sync size exceeds available storage on receiving device.


## Non-Functional Requirements

### NFR1: Performance

**NFR1.1**: Device discovery shall complete within 10 seconds of starting the sync service.

**NFR1.2**: Sync manifest exchange shall complete within 5 seconds for libraries up to 1000 books.

**NFR1.3**: Book file transfer shall achieve at least 10 MB/s on typical WiFi networks (802.11n or better).

**NFR1.4**: The system shall support syncing libraries with up to 10,000 books.

**NFR1.5**: Memory usage during sync shall not exceed 200 MB on Android devices.

**NFR1.6**: The system shall process conflict detection for 1000 items in under 2 seconds.

**NFR1.7**: UI shall remain responsive during sync operations (no ANR on Android).

### NFR2: Reliability

**NFR2.1**: The system shall automatically retry failed transfers up to 3 times with exponential backoff.

**NFR2.2**: The system shall resume interrupted transfers from the last successful checkpoint when possible.

**NFR2.3**: The system shall ensure no data loss occurs during sync operations, even if sync fails.

**NFR2.4**: The system shall maintain data consistency across devices after successful sync.

**NFR2.5**: The system shall properly clean up all resources (connections, files, memory) after sync completion or failure.

**NFR2.6**: The system shall handle network interruptions gracefully without crashing.

### NFR3: Security

**NFR3.1**: All data transferred between devices shall be encrypted using TLS/SSL.

**NFR3.2**: The system shall use AES-256 encryption for additional payload encryption.

**NFR3.3**: Device pairing shall use secure PIN-based authentication.

**NFR3.4**: The system shall store device keys and paired device IDs in platform secure storage (Android Keystore, Java Keystore).

**NFR3.5**: The system shall verify device identity on each connection using certificate pinning.

**NFR3.6**: The system shall verify data integrity using SHA-256 checksums for all transferred files.

**NFR3.7**: The system shall only allow sync on private WiFi networks by default.

**NFR3.8**: The system shall implement rate limiting to prevent denial of service attacks (max 5 connection attempts per minute).

**NFR3.9**: The system shall not send any data to external servers or cloud services.

### NFR4: Usability

**NFR4.1**: Users shall be able to start sync with no more than 3 taps/clicks.

**NFR4.2**: The sync UI shall clearly indicate current status and progress at all times.

**NFR4.3**: Error messages shall be user-friendly and provide actionable guidance.

**NFR4.4**: The system shall provide estimated time remaining during sync operations.

**NFR4.5**: Users shall be able to cancel sync at any time with immediate response.

**NFR4.6**: The pairing process shall be completable in under 30 seconds.


### NFR5: Compatibility

**NFR5.1**: The system shall support Android 7.0 (API 24) and above.

**NFR5.2**: The system shall support Desktop platforms (Windows, macOS, Linux) with JVM 11+.

**NFR5.3**: The system shall maintain backward compatibility with previous sync protocol versions for at least 2 major versions.

**NFR5.4**: The system shall gracefully handle version mismatches with clear error messages.

**NFR5.5**: The system shall work on both IPv4 and IPv6 networks.

### NFR6: Maintainability

**NFR6.1**: The sync feature shall be implemented as a separate module with clear interfaces.

**NFR6.2**: Platform-specific code shall be isolated using Kotlin Multiplatform expect/actual mechanism.

**NFR6.3**: All public APIs shall be documented with KDoc comments.

**NFR6.4**: The system shall log all sync operations and errors for debugging purposes.

**NFR6.5**: The codebase shall maintain at least 80% test coverage for business logic.

### NFR7: Scalability

**NFR7.1**: The system shall support concurrent discovery of up to 10 devices.

**NFR7.2**: The system shall handle book files up to 500 MB in size.

**NFR7.3**: The system shall support syncing total library sizes up to 50 GB.

**NFR7.4**: The system shall efficiently handle incremental syncs with minimal data transfer.

### NFR8: Battery and Resource Efficiency

**NFR8.1**: The system shall acquire wake lock only during active transfer operations.

**NFR8.2**: The system shall release all wake locks immediately after sync completion.

**NFR8.3**: The system shall respect Android battery saver mode and Doze restrictions.

**NFR8.4**: The system shall reduce discovery frequency when battery level is below 20%.

**NFR8.5**: The system shall offer "sync on charger only" option for battery conservation.

**NFR8.6**: CPU usage during sync shall not exceed 30% on average.

### NFR9: Privacy

**NFR9.1**: The system shall not collect or transmit any telemetry or analytics data.

**NFR9.2**: All sync operations shall occur entirely on the local network.

**NFR9.3**: The system shall not sync personal notes unless explicitly enabled by user.

**NFR9.4**: Users shall have full control over what data is synced.

**NFR9.5**: The system shall provide clear privacy policy documentation for the sync feature.


## Acceptance Criteria

### AC1: Device Discovery

**Given** two IReader devices are on the same WiFi network  
**When** sync service is started on both devices  
**Then** each device shall appear in the other's discovered devices list within 10 seconds

**Given** a device leaves the network  
**When** it has been offline for more than 5 minutes  
**Then** it shall be removed from the discovered devices list

### AC2: First-Time Pairing

**Given** two devices that have never synced before  
**When** user initiates sync  
**Then** a 6-digit PIN shall be displayed on both devices  
**And** user must verify PIN matches before sync proceeds  
**And** devices shall be added to each other's trusted list

### AC3: Successful Sync

**Given** two paired devices with different book libraries  
**When** user initiates sync  
**Then** all books, reading progress, bookmarks, and metadata shall be transferred  
**And** both devices shall have identical data after sync completes  
**And** sync status shall show "Completed" with item count and duration

### AC4: Incremental Sync

**Given** two devices that have synced before  
**And** only 2 books have been added/modified since last sync  
**When** user initiates sync  
**Then** only the 2 changed books shall be transferred  
**And** unchanged books shall be skipped

### AC5: Conflict Resolution - Latest Timestamp

**Given** the same book has been read on both devices since last sync  
**And** conflict resolution strategy is set to "Latest Timestamp"  
**When** sync occurs  
**Then** the reading progress with the most recent timestamp shall be kept  
**And** no user intervention shall be required

### AC6: Conflict Resolution - Manual

**Given** the same book has been read on both devices since last sync  
**And** conflict resolution strategy is set to "Manual"  
**When** sync occurs  
**Then** user shall be prompted to choose which reading progress to keep  
**And** sync shall not complete until user makes a choice

### AC7: Network Interruption

**Given** sync is in progress  
**When** WiFi connection is lost  
**Then** sync shall pause and attempt to reconnect up to 3 times  
**And** if reconnection fails, sync status shall show "Failed" with "Network Unavailable" error  
**And** all resources shall be properly cleaned up

### AC8: File Integrity

**Given** a book file is being transferred  
**When** transfer completes  
**Then** file hash shall be calculated and compared with expected hash  
**And** if hashes don't match, transfer shall be retried up to 3 times  
**And** if all retries fail, that book shall be skipped and sync continues with remaining books


### AC9: Insufficient Storage

**Given** receiving device has only 100 MB free storage  
**And** sync requires 500 MB  
**When** user initiates sync  
**Then** sync shall be rejected before transfer starts  
**And** error message shall show required vs available space  
**And** user shall be offered option for selective sync

### AC10: Version Incompatibility

**Given** two devices with incompatible sync protocol versions  
**When** user attempts to sync  
**Then** connection shall be rejected during handshake  
**And** error message shall indicate version incompatibility  
**And** user shall be prompted to update the app on the older device

### AC11: Sync Cancellation

**Given** sync is in progress at 50% completion  
**When** user cancels the sync  
**Then** sync shall stop immediately  
**And** partial data shall be saved if possible  
**And** all connections and resources shall be cleaned up  
**And** sync status shall show "Cancelled"

### AC12: Trust Expiration

**Given** two devices that synced 31 days ago  
**When** user attempts to sync again  
**Then** re-authentication shall be required  
**And** new PIN shall be generated and displayed  
**And** after successful re-authentication, sync shall proceed normally

### AC13: Selective Sync

**Given** user has 100 books in library  
**When** user selects only 10 specific books to sync  
**Then** only those 10 books shall be transferred  
**And** reading progress and bookmarks for all books shall still sync  
**And** sync status shall reflect only 10 books transferred

### AC14: Progress Tracking

**Given** sync is transferring 50 books  
**When** 25 books have been transferred  
**Then** progress indicator shall show 50%  
**And** current book being transferred shall be displayed  
**And** estimated time remaining shall be shown

### AC15: Background Sync (Android)

**Given** sync is in progress on Android  
**When** user switches to another app or locks screen  
**Then** sync shall continue in background  
**And** persistent notification shall be shown  
**And** notification shall show current progress

### AC16: Security - Encrypted Transfer

**Given** sync is in progress  
**When** network traffic is captured  
**Then** all data shall be encrypted  
**And** book content shall not be readable in captured packets  
**And** TLS/SSL encryption shall be verified


## User Stories

### US1: As a reader, I want to discover my other devices automatically

**Story**: As a reader who owns both an Android phone and a desktop computer, I want the app to automatically find my other devices when I'm at home, so I don't have to manually enter IP addresses or configure network settings.

**Acceptance**: When I open the sync screen on both devices, they appear in each other's device list within 10 seconds.

### US2: As a reader, I want to sync my reading progress

**Story**: As a reader who switches between devices, I want my reading progress to sync automatically, so I can continue reading from where I left off on any device.

**Acceptance**: When I read to chapter 5 on my phone and then open the same book on my desktop after syncing, it opens to chapter 5.

### US3: As a reader, I want to sync my bookmarks

**Story**: As a reader who takes notes while reading, I want my bookmarks and notes to be available on all my devices, so I can reference them regardless of which device I'm using.

**Acceptance**: When I create a bookmark with a note on one device and sync, the bookmark appears on my other device with the note intact.

### US4: As a reader, I want to sync my entire library

**Story**: As a reader who adds books on different devices, I want all my books to be available on all my devices, so I have access to my complete library everywhere.

**Acceptance**: When I add 5 new books on my phone and sync with my desktop, those 5 books appear in my desktop library.

### US5: As a reader, I want fast incremental syncs

**Story**: As a reader who syncs frequently, I want the sync to be fast by only transferring what changed, so I don't have to wait for my entire library to transfer every time.

**Acceptance**: When I sync after reading one chapter, only the reading progress updates and sync completes in under 5 seconds.

### US6: As a reader, I want to resolve conflicts easily

**Story**: As a reader who sometimes reads the same book on multiple devices, I want the app to handle conflicts intelligently, so I don't lose my progress.

**Acceptance**: When I read to chapter 3 on my phone and chapter 5 on my desktop, the app keeps the furthest progress (chapter 5) after sync.

### US7: As a reader, I want secure device pairing

**Story**: As a privacy-conscious reader, I want to ensure only my devices can sync with each other, so strangers can't access my reading data.

**Acceptance**: When I try to sync with a new device, I must verify a PIN code displayed on both devices before sync proceeds.

### US8: As a reader, I want to see sync progress

**Story**: As a reader syncing a large library, I want to see how the sync is progressing, so I know how long to wait.

**Acceptance**: During sync, I see a progress bar, the current book being transferred, and estimated time remaining.


### US9: As a reader, I want to cancel sync if needed

**Story**: As a reader who might need to leave home during a sync, I want to be able to cancel the sync operation, so I'm not stuck waiting.

**Acceptance**: When I tap the cancel button during sync, the sync stops immediately and I can close the app.

### US10: As a reader, I want sync to work in the background

**Story**: As a reader on Android, I want sync to continue even if I switch to another app, so I can do other things while waiting.

**Acceptance**: When I start a sync and switch to another app, the sync continues and I see a notification showing progress.

### US11: As a reader, I want to manage trusted devices

**Story**: As a reader who might sell or lose a device, I want to remove it from my trusted devices list, so it can't sync with my other devices anymore.

**Acceptance**: When I go to trusted devices settings, I see a list of all paired devices and can remove any of them.

### US12: As a reader, I want selective sync

**Story**: As a reader with limited storage on my phone, I want to choose which books to sync, so I don't fill up my phone's storage.

**Acceptance**: When I initiate sync, I can select specific books to transfer instead of syncing everything.

### US13: As a reader, I want clear error messages

**Story**: As a reader who isn't tech-savvy, I want to understand what went wrong if sync fails, so I can fix the problem.

**Acceptance**: When sync fails due to network issues, I see a message like "WiFi connection lost. Please check your network and try again" instead of a technical error code.

### US14: As a reader, I want privacy

**Story**: As a privacy-conscious reader, I want to ensure my books and reading data never leave my local network, so my reading habits remain private.

**Acceptance**: The app clearly states that sync is peer-to-peer and no data is sent to cloud servers, and I can verify this in the privacy policy.

### US15: As a reader, I want battery-efficient sync

**Story**: As a mobile reader concerned about battery life, I want sync to be efficient and not drain my battery, so I can read longer.

**Acceptance**: When I sync on low battery, the app offers to skip large file transfers and only sync reading progress and bookmarks.

## Out of Scope

The following features are explicitly out of scope for this initial release:

1. **Cloud-based sync**: This feature is local WiFi only; cloud sync is a separate feature
2. **Bluetooth sync**: Only WiFi is supported in this version
3. **Internet-based remote sync**: Devices must be on the same local network
4. **Automatic scheduled sync**: User must manually initiate sync
5. **Sync of app settings**: Only book-related data is synced
6. **Sync of reading statistics**: Only progress, bookmarks, and books are synced
7. **Multi-device simultaneous sync**: Only one sync operation at a time
8. **Sync history/logs UI**: Basic logging only, no user-facing history
9. **Sync conflict preview**: Conflicts are resolved automatically or with simple prompts
10. **Bandwidth throttling controls**: Sync uses available bandwidth without limits

