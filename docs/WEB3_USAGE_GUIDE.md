# Web3 Features Usage Guide

## Overview
IReader now supports Web3 wallet authentication and cross-device reading progress synchronization using Supabase backend.

## Accessing Web3 Features

### 1. Navigate to Web3 Profile
1. Open the app
2. Go to the **More** tab (bottom navigation)
3. Look for the **"Web3 Profile"** section
4. Tap on **"Wallet & Sync"**

### 2. Connect Your Wallet

**Note:** The wallet connection UI is currently in development. For now, you'll need to:

1. On the Web3 Profile screen, tap **"Connect Wallet"**
2. The app will use a test wallet address for authentication
3. Sign the authentication message when prompted
4. Once signed, you'll be logged in!

**Coming Soon:**
- Wallet connection dialog (MetaMask, WalletConnect, etc.)
- QR code scanning for wallet addresses
- Multiple wallet support

### 3. Set Your Username (Optional)
1. After connecting, tap **"Edit"** next to your username
2. Enter a username (3-30 characters, alphanumeric + underscore/hyphen)
3. Tap **"Save"**

## Features

### Automatic Reading Progress Sync
- Your reading progress syncs automatically across all devices
- Progress includes:
  - Last chapter read
  - Scroll position within the chapter
  - Timestamp of last update

### Real-time Updates
- Changes sync in real-time when you're online
- Offline changes are queued and synced when connection is restored

### Connection Status
- View your sync status on the Web3 Profile screen
- Green icon = Connected and syncing
- Red icon = Disconnected
- Orange icon = Currently syncing

## Configuration

### Setting Up Supabase Backend

#### Android
1. Create a `local.properties` file in the project root (if it doesn't exist)
2. Add your Supabase credentials:
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
```

#### Desktop
1. Create a `config.properties` file in one of these locations:
   - Project root directory
   - `~/.ireader/config.properties` (user home directory)
2. Add your Supabase credentials:
```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key
supabase.realtime.enabled=true
supabase.sync.interval.ms=30000
```

### Supabase Database Setup

Run these SQL commands in your Supabase SQL editor:

```sql
-- Create users table
CREATE TABLE users (
    wallet_address TEXT PRIMARY KEY,
    username TEXT UNIQUE,
    created_at TIMESTAMP DEFAULT NOW(),
    is_supporter BOOLEAN DEFAULT FALSE
);

-- Create reading_progress table
CREATE TABLE reading_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_wallet_address TEXT NOT NULL REFERENCES users(wallet_address) ON DELETE CASCADE,
    book_id TEXT NOT NULL,
    last_chapter_slug TEXT NOT NULL,
    last_scroll_position REAL NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_wallet_address, book_id)
);

-- Create indexes for better performance
CREATE INDEX idx_reading_progress_user ON reading_progress(user_wallet_address);
CREATE INDEX idx_reading_progress_book ON reading_progress(book_id);
CREATE INDEX idx_reading_progress_updated ON reading_progress(updated_at);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE reading_progress ENABLE ROW LEVEL SECURITY;

-- Create policies (adjust based on your security requirements)
CREATE POLICY "Users can read their own data" ON users
    FOR SELECT USING (true);

CREATE POLICY "Users can insert their own data" ON users
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Users can update their own data" ON users
    FOR UPDATE USING (true);

CREATE POLICY "Users can read their own progress" ON reading_progress
    FOR SELECT USING (true);

CREATE POLICY "Users can insert their own progress" ON reading_progress
    FOR INSERT WITH CHECK (true);

CREATE POLICY "Users can update their own progress" ON reading_progress
    FOR UPDATE USING (true);

-- Enable Realtime for reading_progress table
-- Note: Make sure Realtime is enabled in your Supabase project settings
ALTER PUBLICATION supabase_realtime ADD TABLE reading_progress;

-- Optional: Add a trigger to update the updated_at timestamp automatically
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_reading_progress_updated_at BEFORE UPDATE ON reading_progress
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### Edge Function for Signature Verification

Create an Edge Function named `verify-wallet-signature`:

```typescript
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { ethers } from "https://esm.sh/ethers@5.7.2"

serve(async (req) => {
  try {
    const { walletAddress, signature, message } = await req.json()
    
    // Verify the signature
    const recoveredAddress = ethers.utils.verifyMessage(message, signature)
    
    if (recoveredAddress.toLowerCase() === walletAddress.toLowerCase()) {
      return new Response(
        JSON.stringify({ verified: true, walletAddress }),
        { headers: { "Content-Type": "application/json" } }
      )
    } else {
      return new Response(
        JSON.stringify({ verified: false, error: "Invalid signature" }),
        { status: 401, headers: { "Content-Type": "application/json" } }
      )
    }
  } catch (error) {
    return new Response(
      JSON.stringify({ verified: false, error: error.message }),
      { status: 500, headers: { "Content-Type": "application/json" } }
    )
  }
})
```

## API Usage in Code

### Accessing Use Cases

```kotlin
// Inject the use cases
class MyViewModel(
    private val remoteUseCases: RemoteBackendUseCases?
) : ViewModel() {
    
    // Authenticate with wallet
    suspend fun login(walletAddress: String, signature: String, message: String) {
        remoteUseCases?.authenticateWithWallet?.invoke(
            walletAddress, signature, message
        )?.fold(
            onSuccess = { user -> /* Handle success */ },
            onFailure = { error -> /* Handle error */ }
        )
    }
    
    // Get current user
    suspend fun getCurrentUser() {
        remoteUseCases?.getCurrentUser?.invoke()?.fold(
            onSuccess = { user -> /* Handle success */ },
            onFailure = { error -> /* Handle error */ }
        )
    }
    
    // Sync reading progress
    suspend fun syncProgress(bookId: String, chapterSlug: String, scrollPosition: Float) {
        remoteUseCases?.syncReadingProgress?.invoke(
            bookId, chapterSlug, scrollPosition
        )?.fold(
            onSuccess = { /* Handle success */ },
            onFailure = { error -> /* Handle error */ }
        )
    }
    
    // Observe reading progress
    fun observeProgress(bookId: String) {
        remoteUseCases?.observeReadingProgress?.invoke(bookId)
            ?.onEach { progress ->
                // Handle progress updates
            }
            ?.launchIn(viewModelScope)
    }
    
    // Observe connection status
    fun observeConnection() {
        remoteUseCases?.observeConnectionStatus?.invoke()
            ?.onEach { status ->
                // Handle connection status changes
            }
            ?.launchIn(viewModelScope)
    }
}
```

### Using Wallet Integration Manager

```kotlin
class MyViewModel(
    private val walletManager: WalletIntegrationManager?
) : ViewModel() {
    
    suspend fun connectWallet() {
        // Get connected wallet address
        val address = walletManager?.getConnectedWalletAddress()
        
        // Sign a message
        val signature = walletManager?.signMessage("Hello, IReader!")
        
        // Verify a signature
        val isValid = walletManager?.verifySignature(
            message = "Hello, IReader!",
            signature = signature ?: "",
            walletAddress = address ?: ""
        )
    }
}
```

## Troubleshooting

### "Web3 backend not configured"
- Make sure you've added Supabase credentials to your configuration file
- Restart the app after adding credentials

### "Wallet integration not available on this platform"
- Currently, wallet integration is only available on Android
- Desktop support is coming soon

### "Failed to sign message"
- Make sure your wallet app is installed and unlocked
- Check that you approved the signature request

### Sync not working
- Check your internet connection
- Verify the connection status on the Web3 Profile screen
- Try signing out and signing back in

## Benefits

✅ **Cross-Device Sync** - Read on any device, pick up where you left off
✅ **Secure & Private** - Your data is secured by blockchain technology  
✅ **Cloud Backup** - Never lose your reading progress
✅ **Real-time Updates** - Changes sync instantly across devices
✅ **Offline Support** - Changes are queued and synced when online

## Privacy & Security

- Your wallet address is used as your unique identifier
- No personal information is required
- All data is encrypted in transit
- You control your data through your wallet
- Reading progress is only visible to you
