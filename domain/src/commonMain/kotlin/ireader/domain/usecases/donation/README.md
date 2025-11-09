# Donation Trigger System

## Overview

The Donation Trigger System is designed to prompt users to donate at key moments of satisfaction, implementing a non-intrusive approach with a 30-day cooldown between prompts.

## Components

### 1. Domain Models

**DonationTrigger.kt**
- Sealed class representing different trigger events
- Three trigger types:
  - `BookCompleted`: Triggered when user completes a book with 500+ chapters
  - `FirstMigrationSuccess`: Triggered on first successful source migration
  - `ChapterMilestone`: Triggered every 1,000 chapters read
- Extension function `toPromptMessage()` provides contextual messages for each trigger

### 2. Use Cases

**DonationTriggerManager.kt**
- Core business logic for checking trigger conditions
- Implements 30-day cooldown between prompts
- Methods:
  - `checkBookCompletion()`: Check if book completion should trigger prompt
  - `checkSourceMigration()`: Check if migration should trigger prompt
  - `checkChapterMilestone()`: Check if milestone should trigger prompt
  - `shouldShowPrompt()`: Verify cooldown period has passed
  - `recordPromptShown()`: Record that a prompt was shown
  - `getDaysUntilNextPrompt()`: Get remaining cooldown days

**DonationUseCases.kt**
- Container class for all donation-related use cases
- Provides single point of access for donation functionality

**DonationTriggerIntegration.kt**
- Helper extension functions for easier integration
- Provides examples and documentation for integration points

### 3. Preferences

**AppPreferences.kt** (additions)
- `lastDonationPromptTime()`: Timestamp of last prompt (for cooldown)
- `hasCompletedMigration()`: Flag for first migration trigger
- `lastDonationMilestone()`: Last milestone shown (to avoid duplicates)

## Trigger Conditions

### Book Completion Trigger

**Conditions**:
- Book has 500+ chapters
- 30-day cooldown has passed

**Message**:
> "Congratulations! 🎉
> 
> You've finished "[Book Title]" with [X] chapters! If IReader made this journey better, please consider a small crypto donation to support development."

### First Migration Success Trigger

**Conditions**:
- User's first successful source migration
- 30-day cooldown has passed

**Message**:
> "Migration Complete! ✨
> 
> Saved you a headache, right? 😉 Found [X] more chapters on [Source Name]. If you find these power-features useful, please consider supporting the app."

### Chapter Milestone Trigger

**Conditions**:
- User reaches a 1,000 chapter milestone (1000, 2000, 3000, etc.)
- Milestone hasn't been shown before
- 30-day cooldown has passed

**Message**:
> "Amazing Progress! 📚
> 
> You've read [X,XXX] chapters! That's incredible. To help us build the app for the next [Y,000] chapters, please consider donating."

## Usage Examples

### Basic Usage

```kotlin
// Inject the use case
class MyViewModel(
    private val donationUseCases: DonationUseCases
) : ViewModel() {
    
    suspend fun onBookCompleted(book: Book, totalChapters: Int) {
        val trigger = donationUseCases.donationTriggerManager.checkBookCompletion(
            chapterCount = totalChapters,
            bookTitle = book.title
        )
        
        if (trigger != null) {
            // Show donation prompt
            showDonationPrompt(trigger.toPromptMessage())
        }
    }
}
```

### With ViewModel Integration

```kotlin
// Use the provided ViewModel
class MyScreen {
    val donationViewModel = getScreenModel<DonationTriggerViewModel>()
    
    fun onBookCompleted(book: Book, totalChapters: Int) {
        donationViewModel.checkBookCompletion(
            chapterCount = totalChapters,
            bookTitle = book.title
        )
    }
}
```

### UI Integration

```kotlin
@Composable
fun MyScreen() {
    val donationViewModel = getScreenModel<DonationTriggerViewModel>()
    
    // Your screen content
    MyScreenContent()
    
    // Donation prompt dialog
    donationViewModel.currentPrompt?.let { promptMessage ->
        DonationPromptDialog(
            promptMessage = promptMessage,
            onDonateNow = {
                donationViewModel.onDonateNow()
                navigator.push(DonationScreen())
            },
            onMaybeLater = {
                donationViewModel.onMaybeLater()
            }
        )
    }
}
```

## Integration Points

### 1. Book Detail Screen
When user marks a book as completed:
```kotlin
suspend fun markAsCompleted(book: Book, totalChapters: Int) {
    // Update book status
    updateBook(book.copy(status = Book.COMPLETED))
    
    // Check donation trigger
    donationViewModel.checkBookCompletion(totalChapters, book.title)
}
```

### 2. Source Migration
After successful migration:
```kotlin
suspend fun onMigrationSuccess(sourceName: String, chapterDiff: Int) {
    donationViewModel.checkSourceMigration(sourceName, chapterDiff)
}
```

### 3. Reader Screen
After reading a chapter:
```kotlin
suspend fun onChapterRead(progress: Float, wordCount: Int) {
    if (progress >= 0.8f) {
        statisticsUseCases.trackReadingProgress.onChapterProgressUpdate(progress, wordCount)
        donationViewModel.checkChapterMilestone()
    }
}
```

## Testing

### Manual Testing

1. **Test Cooldown**:
```kotlin
// Reset cooldown for testing
appPreferences.lastDonationPromptTime().set(0L)
```

2. **Test Book Completion**:
```kotlin
// Trigger with a 500+ chapter book
donationViewModel.checkBookCompletion(600, "Test Book")
```

3. **Test Migration**:
```kotlin
// Reset migration flag
appPreferences.hasCompletedMigration().set(false)
// Trigger
donationViewModel.checkSourceMigration("TestSource", 50)
```

4. **Test Milestone**:
```kotlin
// Reset milestone
appPreferences.lastDonationMilestone().set(0)
// Ensure statistics show 1000+ chapters read
```

### Unit Testing

```kotlin
@Test
fun `should trigger book completion for 500+ chapters`() = runTest {
    val manager = DonationTriggerManager(statisticsRepo, appPrefs)
    
    val trigger = manager.checkBookCompletion(600, "Test Book")
    
    assertNotNull(trigger)
    assertTrue(trigger is DonationTrigger.BookCompleted)
}

@Test
fun `should not trigger if cooldown active`() = runTest {
    // Set last prompt to 10 days ago
    appPrefs.lastDonationPromptTime().set(
        System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L)
    )
    
    val trigger = manager.checkBookCompletion(600, "Test Book")
    
    assertNull(trigger) // Should be null due to cooldown
}
```

## Configuration

### Cooldown Duration
Default: 30 days
Location: `DonationTriggerManager.COOLDOWN_DAYS`

### Chapter Thresholds
- Book completion: 500 chapters (`BOOK_COMPLETION_CHAPTER_THRESHOLD`)
- Milestone interval: 1,000 chapters (`CHAPTER_MILESTONE_INTERVAL`)

### Customization
To change these values, modify the constants in `DonationTriggerManager`:

```kotlin
companion object {
    private const val COOLDOWN_DAYS = 30
    private const val BOOK_COMPLETION_CHAPTER_THRESHOLD = 500
    private const val CHAPTER_MILESTONE_INTERVAL = 1000
}
```

## Dependencies

- `ReadingStatisticsRepository`: For tracking chapter milestones
- `AppPreferences`: For storing cooldown and trigger state
- Koin DI: For dependency injection

## Future Enhancements

Potential improvements:
- Remote configuration for thresholds and cooldown
- Analytics tracking for conversion rates
- A/B testing different messages
- Additional trigger types (app anniversary, feature usage)
- Localization of prompt messages
- User preference to disable prompts


---

## Wallet Integration

### Overview

The Wallet Integration system provides platform-agnostic cryptocurrency wallet integration for accepting donations. It supports deep linking to popular wallet apps on Android and provides fallback mechanisms for Desktop platforms.

### Supported Wallets

- **Trust Wallet** - Multi-chain mobile wallet
- **MetaMask** - Ethereum-focused wallet (mobile & browser extension)
- **Coinbase Wallet** - Multi-chain wallet from Coinbase

### Supported Cryptocurrencies

- **Bitcoin (BTC)** - bitcoin: URI scheme
- **Ethereum (ETH)** - ethereum: URI scheme
- **Litecoin (LTC)** - litecoin: URI scheme

### Components

**WalletIntegrationManager** (Interface)
- Platform-agnostic interface for wallet operations
- Methods:
  - `openWallet()`: Open a wallet app with pre-filled payment info
  - `isWalletInstalled()`: Check if a wallet app is installed
  - `generatePaymentUri()`: Generate standard payment URI
  - `copyToClipboard()`: Copy address to clipboard

**AndroidWalletIntegrationManager**
- Uses Android Intents and deep links
- Checks package manager for installed apps
- Handles ActivityNotFoundException gracefully

**DesktopWalletIntegrationManager**
- Opens payment URIs in default browser
- Provides clipboard operations
- Always returns false for wallet installation checks (desktop users should use QR codes)

### Use Cases

**OpenWalletUseCase**
```kotlin
val result = donationUseCases.openWallet(
    walletApp = WalletApp.TRUST_WALLET,
    cryptoType = CryptoType.ETHEREUM,
    address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
    amount = 0.01 // Optional
)

when (result) {
    is WalletIntegrationResult.Success -> {
        // Wallet opened successfully
    }
    is WalletIntegrationResult.WalletNotInstalled -> {
        // Show "Install ${result.walletApp.displayName}" message
    }
    is WalletIntegrationResult.Error -> {
        // Show error message
    }
}
```

**CheckWalletInstalledUseCase**
```kotlin
val isInstalled = donationUseCases.checkWalletInstalled(WalletApp.METAMASK)
if (isInstalled) {
    // Show "Pay with MetaMask" button
} else {
    // Show "Install MetaMask" or hide button
}
```

**CopyAddressUseCase**
```kotlin
donationUseCases.copyAddress("0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
// Show toast: "Address copied to clipboard"
```

**GeneratePaymentUriUseCase**
```kotlin
val uri = donationUseCases.generatePaymentUri(
    cryptoType = CryptoType.BITCOIN,
    address = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
    amount = 0.001
)
// Result: "bitcoin:1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa?amount=0.001"
```

### Deep Link Formats

**Trust Wallet**
```
trust://send?asset=<SYMBOL>&address=<ADDRESS>[&amount=<AMOUNT>]
```

**MetaMask**
```
metamask://send/<ADDRESS>[@<CHAIN_ID>][?value=<WEI_AMOUNT>]
```
Note: MetaMask amounts are in Wei (1 ETH = 10^18 Wei)

**Coinbase Wallet**
```
coinbase://send?address=<ADDRESS>&asset=<SYMBOL>[&amount=<AMOUNT>]
```

### Platform Differences

**Android**
- ✅ Deep linking to wallet apps
- ✅ Package manager checks for installed apps
- ✅ Clipboard operations
- ✅ ActivityNotFoundException handling

**Desktop**
- ❌ No native wallet apps (use browser extensions)
- ✅ Opens payment URIs in default browser
- ✅ Clipboard operations
- ✅ QR code display (handled in UI layer)
- ℹ️ Always returns false for wallet installation checks

### UI Integration Example

```kotlin
@Composable
fun DonationScreen() {
    val donationUseCases = get<DonationUseCases>()
    val scope = rememberCoroutineScope()
    
    Column {
        // Bitcoin section
        CryptoSection(
            cryptoType = CryptoType.BITCOIN,
            address = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
            onCopyAddress = {
                scope.launch {
                    donationUseCases.copyAddress(it)
                    // Show toast
                }
            },
            onOpenWallet = { wallet ->
                scope.launch {
                    val result = donationUseCases.openWallet(
                        walletApp = wallet,
                        cryptoType = CryptoType.BITCOIN,
                        address = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
                    )
                    // Handle result
                }
            }
        )
        
        // Ethereum section
        CryptoSection(
            cryptoType = CryptoType.ETHEREUM,
            address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            onCopyAddress = { /* ... */ },
            onOpenWallet = { /* ... */ }
        )
    }
}
```

### Error Handling

**WalletNotInstalled**
```kotlin
when (result) {
    is WalletIntegrationResult.WalletNotInstalled -> {
        val message = "${result.walletApp.displayName} is not installed. " +
                     "Please install it from the app store or copy the address manually."
        showError(message)
    }
}
```

**Generic Errors**
```kotlin
when (result) {
    is WalletIntegrationResult.Error -> {
        val message = "Failed to open wallet: ${result.error}"
        showError(message)
    }
}
```

### Security Considerations

1. **No Private Keys** - Never store or handle private keys
2. **Address Validation** - Validate addresses before display
3. **HTTPS Only** - All blockchain API calls use HTTPS
4. **Disclaimer** - Display clear disclaimer about crypto donations

```kotlin
const val CRYPTO_DISCLAIMER = """
    Cryptocurrency donations are non-refundable. 
    Please verify the wallet address before sending.
    IReader does not store or have access to your private keys.
"""
```

### Testing Wallet Integration

```kotlin
@Test
fun `should generate correct payment URI`() {
    val uri = walletIntegrationManager.generatePaymentUri(
        cryptoType = CryptoType.BITCOIN,
        address = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
        amount = 0.001
    )
    assertEquals("bitcoin:1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa?amount=0.001", uri)
}

@Test
fun `should handle wallet not installed gracefully`() = runTest {
    val result = openWalletUseCase(
        walletApp = WalletApp.TRUST_WALLET,
        cryptoType = CryptoType.ETHEREUM,
        address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
    )
    
    // On desktop or if wallet not installed
    assertTrue(
        result is WalletIntegrationResult.WalletNotInstalled ||
        result is WalletIntegrationResult.Success
    )
}
```

### Configuration

**Setting Wallet Addresses**
```kotlin
val donationConfig = DonationConfig(
    wallets = mapOf(
        CryptoType.BITCOIN to "YOUR_BTC_ADDRESS",
        CryptoType.ETHEREUM to "YOUR_ETH_ADDRESS",
        CryptoType.LITECOIN to "YOUR_LTC_ADDRESS"
    )
)
```

### Additional Documentation

For more detailed information, see:
- [WALLET_INTEGRATION_GUIDE.md](./WALLET_INTEGRATION_GUIDE.md) - Comprehensive wallet integration guide
- [ARCHITECTURE.md](./ARCHITECTURE.md) - System architecture
- [QUICK_START.md](./QUICK_START.md) - Quick start guide
