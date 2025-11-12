# Design Document: Supabase Web3 Backend

## Overview

This design document outlines the implementation of a portable, Supabase-based backend for the IReader Kotlin Multiplatform application with Web3 wallet authentication. The solution follows Clean Architecture principles to ensure the backend remains swappable while providing real-time data synchronization across Android and Desktop platforms.

### Key Design Goals

1. **Portability**: Backend implementation can be replaced without modifying domain logic
2. **Web3 Authentication**: Wallet-based authentication without passwords or gas fees
3. **Real-time Sync**: Reading progress syncs across devices within seconds
4. **Universal Book IDs**: Books identified by normalized titles, not source-specific IDs
5. **Offline Support**: Queue updates locally and sync when connectivity returns

## Architecture

### Layer Structure

The implementation follows Clean Architecture with three distinct layers:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (Android/Desktop UI Components)      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Domain Layer                   │
│  - RemoteRepository Interface           │
│  - Use Cases (Auth, Sync, etc.)         │
│  - Domain Models                        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          Data Layer                     │
│  - SupabaseRemoteRepository (impl)      │
│  - Supabase Client Configuration        │
│  - Local Cache & Sync Queue             │
└─────────────────────────────────────────┘
```


### Dependency Flow

Dependencies point inward following Clean Architecture:
- Presentation depends on Domain
- Data depends on Domain
- Domain depends on nothing (pure Kotlin)

This ensures the domain layer remains framework-agnostic and testable.

## Components and Interfaces

### 1. Domain Layer Components

#### RemoteRepository Interface

Located in `domain/src/commonMain/kotlin/ireader/domain/data/repository/RemoteRepository.kt`

```kotlin
interface RemoteRepository {
    // Authentication
    suspend fun authenticateWithWallet(walletAddress: String, signature: String, message: String): Result<User>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun signOut()
    
    // User Management
    suspend fun updateUsername(walletAddress: String, username: String): Result<Unit>
    suspend fun getUserByWallet(walletAddress: String): Result<User?>
    
    // Reading Progress
    suspend fun syncReadingProgress(progress: ReadingProgress): Result<Unit>
    suspend fun getReadingProgress(walletAddress: String, bookId: String): Result<ReadingProgress?>
    fun observeReadingProgress(walletAddress: String, bookId: String): Flow<ReadingProgress?>
    
    // Connection Status
    fun observeConnectionStatus(): Flow<ConnectionStatus>
}
```

#### Domain Models

Located in `domain/src/commonMain/kotlin/ireader/domain/models/remote/`

```kotlin
data class User(
    val walletAddress: String,
    val username: String?,
    val createdAt: Long,
    val isSupporter: Boolean
)

data class ReadingProgress(
    val id: String? = null,
    val userWalletAddress: String,
    val bookId: String,
    val lastChapterSlug: String,
    val lastScrollPosition: Float,
    val updatedAt: Long
)

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}
```


#### Use Cases

Located in `domain/src/commonMain/kotlin/ireader/domain/usecases/remote/`

```kotlin
class AuthenticateWithWalletUseCase(
    private val remoteRepository: RemoteRepository,
    private val walletManager: WalletIntegrationManager
) {
    suspend operator fun invoke(walletAddress: String): Result<User> {
        // Generate challenge message
        val message = "Sign this message to authenticate with IReader: ${System.currentTimeMillis()}"
        
        // Request signature from wallet (platform-specific)
        val signature = walletManager.requestSignature(walletAddress, message)
            ?: return Result.failure(Exception("Signature cancelled"))
        
        // Authenticate with backend
        return remoteRepository.authenticateWithWallet(walletAddress, signature, message)
    }
}

class SyncReadingProgressUseCase(
    private val remoteRepository: RemoteRepository,
    private val localRepository: BookRepository
) {
    suspend operator fun invoke(bookId: Long, chapterSlug: String, scrollPosition: Float): Result<Unit> {
        val user = remoteRepository.getCurrentUser().getOrNull()
            ?: return Result.failure(Exception("User not authenticated"))
        
        val book = localRepository.findBookById(bookId)
            ?: return Result.failure(Exception("Book not found"))
        
        val normalizedBookId = normalizeBookId(book.title)
        
        val progress = ReadingProgress(
            userWalletAddress = user.walletAddress,
            bookId = normalizedBookId,
            lastChapterSlug = chapterSlug,
            lastScrollPosition = scrollPosition,
            updatedAt = System.currentTimeMillis()
        )
        
        return remoteRepository.syncReadingProgress(progress)
    }
    
    private fun normalizeBookId(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "-")
    }
}
```

### 2. Data Layer Components

#### SupabaseRemoteRepository Implementation

Located in `data/src/commonMain/kotlin/ireader/data/remote/SupabaseRemoteRepository.kt`

This implementation uses the Supabase Kotlin client to interact with the backend.


```kotlin
class SupabaseRemoteRepository(
    private val supabaseClient: SupabaseClient,
    private val syncQueue: SyncQueue
) : RemoteRepository {
    
    private val auth = supabaseClient.auth
    private val database = supabaseClient.database
    private val realtime = supabaseClient.realtime
    
    override suspend fun authenticateWithWallet(
        walletAddress: String,
        signature: String,
        message: String
    ): Result<User> = runCatching {
        // Verify signature on backend using Edge Function
        val response = supabaseClient.functions.invoke(
            "verify-wallet-signature",
            body = mapOf(
                "walletAddress" to walletAddress,
                "signature" to signature,
                "message" to message
            )
        )
        
        // Create or get user
        val user = database.from("users")
            .upsert(mapOf("wallet_address" to walletAddress))
            .select()
            .decodeSingle<User>()
        
        // Store session locally
        storeSession(user)
        
        user
    }
    
    override suspend fun syncReadingProgress(progress: ReadingProgress): Result<Unit> = runCatching {
        try {
            database.from("reading_progress")
                .upsert(progress)
                .execute()
        } catch (e: Exception) {
            // Queue for later if offline
            syncQueue.enqueue(progress)
            throw e
        }
    }
    
    override fun observeReadingProgress(
        walletAddress: String,
        bookId: String
    ): Flow<ReadingProgress?> {
        return realtime.channel("reading_progress:$walletAddress:$bookId")
            .postgresChangeFlow<ReadingProgress>(schema = "public") {
                table = "reading_progress"
                filter = "user_wallet_address=eq.$walletAddress,book_id=eq.$bookId"
            }
            .map { it.record }
    }
}
```

#### Supabase Client Configuration

Located in `data/src/commonMain/kotlin/ireader/data/remote/SupabaseConfig.kt`

```kotlin
object SupabaseConfig {
    fun createClient(url: String, apiKey: String): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = apiKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Functions)
            
            // Configure HTTP client
            httpEngine {
                requestTimeout = 30_000
            }
        }
    }
}
```


#### Sync Queue for Offline Support

Located in `data/src/commonMain/kotlin/ireader/data/remote/SyncQueue.kt`

```kotlin
class SyncQueue(
    private val database: DatabaseHandler
) {
    suspend fun enqueue(progress: ReadingProgress) {
        database.await {
            syncQueueQueries.insert(
                bookId = progress.bookId,
                data = Json.encodeToString(progress),
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    suspend fun processQueue(remoteRepository: RemoteRepository) {
        val items = database.awaitList {
            syncQueueQueries.selectAll()
        }
        
        items.forEach { item ->
            try {
                val progress = Json.decodeFromString<ReadingProgress>(item.data)
                remoteRepository.syncReadingProgress(progress).getOrThrow()
                
                // Remove from queue on success
                database.await {
                    syncQueueQueries.delete(item.id)
                }
            } catch (e: Exception) {
                // Keep in queue, will retry later
            }
        }
    }
}
```

### 3. Platform-Specific Components

#### Android Wallet Integration

Located in `domain/src/androidMain/kotlin/ireader/domain/services/AndroidWalletManager.kt`

```kotlin
class AndroidWalletManager(
    private val context: Context
) : WalletIntegrationManager {
    
    suspend fun requestSignature(walletAddress: String, message: String): String? {
        // Use WalletConnect or deep linking to request signature
        // This is a simplified example
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("wc://sign?address=$walletAddress&message=${Uri.encode(message)}")
        }
        
        return suspendCoroutine { continuation ->
            // Launch wallet app and wait for result
            // Implementation depends on wallet integration method
        }
    }
}
```

#### Desktop Wallet Integration

Located in `domain/src/desktopMain/kotlin/ireader/domain/services/DesktopWalletManager.kt`

```kotlin
class DesktopWalletManager : WalletIntegrationManager {
    
    suspend fun requestSignature(walletAddress: String, message: String): String? {
        // Desktop implementation could use:
        // 1. QR code for mobile wallet scanning
        // 2. Browser extension integration
        // 3. WalletConnect desktop protocol
        
        return showQRCodeDialog(walletAddress, message)
    }
}
```


## Data Models

### Database Schema

#### Supabase Tables

**users**
```sql
CREATE TABLE users (
    wallet_address TEXT PRIMARY KEY,
    username TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    is_supporter BOOLEAN DEFAULT FALSE
);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Users can read all, but only update their own
CREATE POLICY "Users can read all users"
    ON users FOR SELECT
    USING (true);

CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.uid() = wallet_address);
```

**reading_progress**
```sql
CREATE TABLE reading_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_wallet_address TEXT REFERENCES users(wallet_address) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position REAL NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_wallet_address, book_id)
);

-- Indexes for performance
CREATE INDEX idx_reading_progress_user ON reading_progress(user_wallet_address);
CREATE INDEX idx_reading_progress_book ON reading_progress(book_id);

-- Enable Row Level Security
ALTER TABLE reading_progress ENABLE ROW LEVEL SECURITY;

-- Users can only access their own progress
CREATE POLICY "Users can manage own progress"
    ON reading_progress
    USING (auth.uid() = user_wallet_address);
```

#### Local SQLDelight Schema

**sync_queue.sq**
```sql
CREATE TABLE sync_queue (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id TEXT NOT NULL,
    data TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    retry_count INTEGER DEFAULT 0
);

selectAll:
SELECT * FROM sync_queue ORDER BY timestamp ASC;

insert:
INSERT INTO sync_queue(book_id, data, timestamp)
VALUES (?, ?, ?);

delete:
DELETE FROM sync_queue WHERE id = ?;

incrementRetry:
UPDATE sync_queue SET retry_count = retry_count + 1 WHERE id = ?;
```


### Supabase Edge Functions

#### verify-wallet-signature

Located in Supabase project at `supabase/functions/verify-wallet-signature/index.ts`

```typescript
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { ethers } from "https://esm.sh/ethers@6.7.0"

serve(async (req) => {
  const { walletAddress, signature, message } = await req.json()
  
  try {
    // Verify the signature
    const recoveredAddress = ethers.verifyMessage(message, signature)
    
    if (recoveredAddress.toLowerCase() !== walletAddress.toLowerCase()) {
      return new Response(
        JSON.stringify({ error: "Invalid signature" }),
        { status: 401 }
      )
    }
    
    // Check message timestamp to prevent replay attacks
    const timestamp = parseInt(message.split(": ")[1])
    const now = Date.now()
    const fiveMinutes = 5 * 60 * 1000
    
    if (now - timestamp > fiveMinutes) {
      return new Response(
        JSON.stringify({ error: "Message expired" }),
        { status: 401 }
      )
    }
    
    return new Response(
      JSON.stringify({ verified: true, walletAddress: recoveredAddress }),
      { status: 200 }
    )
  } catch (error) {
    return new Response(
      JSON.stringify({ error: error.message }),
      { status: 500 }
    )
  }
})
```

## Error Handling

### Error Types

```kotlin
sealed class RemoteError : Exception() {
    data class NetworkError(override val message: String) : RemoteError()
    data class AuthenticationError(override val message: String) : RemoteError()
    data class ValidationError(override val message: String) : RemoteError()
    data class ServerError(override val message: String) : RemoteError()
    data class UnknownError(override val cause: Throwable?) : RemoteError()
}
```

### Error Handling Strategy

1. **Network Errors**: Queue operations for retry when connection returns
2. **Authentication Errors**: Clear session and prompt re-authentication
3. **Validation Errors**: Show user-friendly error messages
4. **Server Errors**: Log for debugging, show generic error to user
5. **Conflict Resolution**: Use timestamp-based last-write-wins strategy


### Retry Logic

```kotlin
class RetryPolicy {
    companion object {
        const val MAX_RETRIES = 3
        const val INITIAL_DELAY_MS = 1000L
        const val MAX_DELAY_MS = 10000L
        const val BACKOFF_MULTIPLIER = 2.0
    }
    
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T
    ): Result<T> {
        var currentDelay = INITIAL_DELAY_MS
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * BACKOFF_MULTIPLIER)
                        .toLong()
                        .coerceAtMost(MAX_DELAY_MS)
                }
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error"))
    }
}
```

## Testing Strategy

### Unit Tests

1. **Domain Layer Tests**
   - Test use cases with mocked repositories
   - Test book ID normalization logic
   - Test error handling in use cases

2. **Data Layer Tests**
   - Test SupabaseRemoteRepository with mocked Supabase client
   - Test SyncQueue operations
   - Test retry logic

3. **Model Tests**
   - Test serialization/deserialization
   - Test data validation

### Integration Tests

1. **Supabase Integration**
   - Test against local Supabase instance
   - Test authentication flow
   - Test CRUD operations
   - Test real-time subscriptions

2. **Sync Queue Tests**
   - Test offline queueing
   - Test sync on reconnection
   - Test conflict resolution

### End-to-End Tests

1. **Authentication Flow**
   - Complete wallet sign-in flow
   - Session persistence
   - Sign-out flow

2. **Reading Progress Sync**
   - Update progress on device A
   - Verify sync to device B
   - Test offline updates


## Configuration Management

### Environment Configuration

```kotlin
// domain/src/commonMain/kotlin/ireader/domain/models/remote/RemoteConfig.kt
data class RemoteConfig(
    val supabaseUrl: String,
    val supabaseAnonKey: String,
    val enableRealtime: Boolean = true,
    val syncIntervalMs: Long = 30_000
)

// Load from platform-specific sources
expect fun loadRemoteConfig(): RemoteConfig
```

### Android Configuration

```kotlin
// domain/src/androidMain/kotlin/ireader/domain/models/remote/RemoteConfig.kt
actual fun loadRemoteConfig(): RemoteConfig {
    return RemoteConfig(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
    )
}
```

### Desktop Configuration

```kotlin
// domain/src/desktopMain/kotlin/ireader/domain/models/remote/RemoteConfig.kt
actual fun loadRemoteConfig(): RemoteConfig {
    val properties = Properties()
    properties.load(FileInputStream("config.properties"))
    
    return RemoteConfig(
        supabaseUrl = properties.getProperty("supabase.url"),
        supabaseAnonKey = properties.getProperty("supabase.anon.key")
    )
}
```

## Dependency Injection

### Koin Module Configuration

```kotlin
// data/src/commonMain/kotlin/ireader/data/di/RemoteModule.kt
val remoteModule = module {
    
    // Configuration
    single { loadRemoteConfig() }
    
    // Supabase Client
    single {
        val config = get<RemoteConfig>()
        SupabaseConfig.createClient(config.supabaseUrl, config.supabaseAnonKey)
    }
    
    // Sync Queue
    single { SyncQueue(get()) }
    
    // Repository Implementation
    single<RemoteRepository> {
        SupabaseRemoteRepository(
            supabaseClient = get(),
            syncQueue = get()
        )
    }
    
    // Use Cases
    factory { AuthenticateWithWalletUseCase(get(), get()) }
    factory { SyncReadingProgressUseCase(get(), get()) }
    factory { ObserveReadingProgressUseCase(get()) }
}
```


## Performance Considerations

### Caching Strategy

1. **User Profile Cache**
   - Cache authenticated user in memory
   - Refresh on app start
   - Clear on sign-out

2. **Reading Progress Cache**
   - Cache last known progress locally
   - Update cache on sync
   - Use cached value if offline

3. **Connection Pooling**
   - Reuse Supabase client instance
   - Configure appropriate timeouts
   - Handle connection lifecycle

### Optimization Techniques

1. **Debouncing Progress Updates**
   ```kotlin
   class DebouncedProgressSync(
       private val syncUseCase: SyncReadingProgressUseCase,
       private val delayMs: Long = 2000
   ) {
       private var syncJob: Job? = null
       
       fun scheduleSync(bookId: Long, chapterSlug: String, position: Float) {
           syncJob?.cancel()
           syncJob = CoroutineScope(Dispatchers.IO).launch {
               delay(delayMs)
               syncUseCase(bookId, chapterSlug, position)
           }
       }
   }
   ```

2. **Batch Operations**
   - Batch multiple progress updates when syncing queue
   - Use Supabase bulk insert/update operations

3. **Selective Sync**
   - Only sync changed fields
   - Use delta updates where possible

### Monitoring and Logging

```kotlin
interface RemoteLogger {
    fun logAuthAttempt(walletAddress: String)
    fun logAuthSuccess(walletAddress: String)
    fun logAuthFailure(walletAddress: String, error: String)
    fun logSyncAttempt(bookId: String)
    fun logSyncSuccess(bookId: String, latencyMs: Long)
    fun logSyncFailure(bookId: String, error: String)
    fun logConnectionStatus(status: ConnectionStatus)
}
```

## Security Considerations

### Authentication Security

1. **Message Signing**
   - Include timestamp in signed message
   - Validate timestamp on server (5-minute window)
   - Prevent replay attacks

2. **Session Management**
   - Store session tokens securely (Android Keystore / Desktop secure storage)
   - Implement session expiration
   - Refresh tokens before expiration

3. **API Key Protection**
   - Use anon key for client (limited permissions)
   - Never expose service role key
   - Configure RLS policies properly

### Data Security

1. **Row Level Security**
   - Users can only access their own data
   - Enforce at database level
   - Test RLS policies thoroughly

2. **Input Validation**
   - Validate wallet addresses (checksum)
   - Sanitize user inputs
   - Validate data types and ranges

3. **Rate Limiting**
   - Implement on Supabase Edge Functions
   - Prevent abuse and DoS attacks
   - Configure appropriate limits


## Migration Strategy

### Phase 1: Infrastructure Setup

1. Create Supabase project
2. Set up database tables and RLS policies
3. Deploy Edge Functions
4. Configure environment variables

### Phase 2: Domain Layer Implementation

1. Define RemoteRepository interface
2. Create domain models
3. Implement use cases
4. Add to dependency injection

### Phase 3: Data Layer Implementation

1. Add Supabase Kotlin SDK dependency
2. Implement SupabaseRemoteRepository
3. Create SyncQueue with SQLDelight
4. Implement retry logic

### Phase 4: Platform Integration

1. Implement Android wallet manager
2. Implement Desktop wallet manager
3. Add configuration loading
4. Wire up dependency injection

### Phase 5: Testing and Validation

1. Unit test all components
2. Integration test with local Supabase
3. End-to-end testing
4. Performance testing

### Phase 6: Gradual Rollout

1. Deploy to beta users
2. Monitor error rates and performance
3. Gather feedback
4. Fix issues and optimize
5. Full production release

## Rollback Plan

If issues arise during rollout:

1. **Immediate**: Disable remote sync feature flag
2. **Short-term**: App continues working with local-only data
3. **Investigation**: Analyze logs and error reports
4. **Fix**: Deploy hotfix or roll back to previous version
5. **Re-enable**: Gradually re-enable after validation

## Future Enhancements

### Potential Improvements

1. **Multi-wallet Support**
   - Allow users to link multiple wallets
   - Primary wallet for authentication

2. **Encrypted Sync**
   - End-to-end encryption for sensitive data
   - User-controlled encryption keys

3. **Conflict Resolution UI**
   - Show conflicts to user
   - Allow manual resolution

4. **Advanced Analytics**
   - Reading patterns
   - Popular books
   - User engagement metrics

5. **Social Features Foundation**
   - This backend enables future reviews/comments
   - Shared reading lists
   - Friend system

## Dependencies

### Required Libraries

Add to `gradle/libs.versions.toml`:

```toml
[versions]
supabase = "2.0.0"

[libraries]
supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt", version.ref = "supabase" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt", version.ref = "supabase" }
supabase-realtime = { module = "io.github.jan-tennert.supabase:realtime-kt", version.ref = "supabase" }
supabase-functions = { module = "io.github.jan-tennert.supabase:functions-kt", version.ref = "supabase" }

[bundles]
supabase = ["supabase-postgrest", "supabase-auth", "supabase-realtime", "supabase-functions"]
```

Add to `data/build.gradle.kts`:

```kotlin
commonMain {
    dependencies {
        implementation(libs.bundles.supabase)
    }
}
```

## Conclusion

This design provides a solid foundation for integrating Supabase with Web3 authentication while maintaining Clean Architecture principles. The portable design ensures the backend can be swapped if needed, and the real-time sync capabilities provide a seamless multi-device experience. The implementation is structured to be testable, maintainable, and scalable for future enhancements.
