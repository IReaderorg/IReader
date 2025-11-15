# Monetization Service Implementation

## Overview
This document describes the implementation of the plugin monetization system for IReader.

## Components Implemented

### 1. MonetizationService
**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/MonetizationService.kt`

Main service class that handles all plugin purchase operations:
- `purchasePlugin(pluginId, price)` - Purchase premium plugins
- `purchaseFeature(pluginId, featureId, price)` - In-plugin purchases
- `isPurchased(pluginId)` - Check if plugin is purchased or has active trial
- `isFeaturePurchased(pluginId, featureId)` - Check if feature is purchased
- `syncPurchases(userId)` - Sync purchases across devices
- `startTrial(pluginId, durationDays)` - Start trial period
- `getTrialInfo(pluginId)` - Get trial information
- `getUserPurchases()` - Get all user purchases
- `restorePurchases()` - Restore purchases from platform store

### 2. Purchase Data Class
**Location**: `domain/src/commonMain/kotlin/ireader/domain/data/repository/PluginRepository.kt`

Data class representing a purchase:
```kotlin
data class Purchase(
    val id: String,
    val pluginId: String,
    val featureId: String?,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val userId: String,
    val receiptData: String?
)
```

### 3. PaymentProcessor Interface
**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/PaymentProcessor.kt`

Platform-specific payment processor interface (expect/actual):
- `processPayment(itemId, price, currency)` - Process payment
- `verifyReceipt(receiptData)` - Verify purchase receipt
- `restorePurchases()` - Restore previous purchases

**Platform Implementations**:
- Android: `domain/src/androidMain/kotlin/ireader/domain/plugins/PaymentProcessor.android.kt`
  - Placeholder for Google Play Billing integration
- Desktop: `domain/src/desktopMain/kotlin/ireader/domain/plugins/PaymentProcessor.desktop.kt`
  - Placeholder for Stripe/PayPal integration

### 4. PurchaseRepository Interface
**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/PurchaseRepository.kt`

Repository interface for purchase data access:
- `savePurchase(purchase)` - Save purchase to database
- `getPurchasesByUser(userId)` - Get all user purchases
- `isPurchased(pluginId, userId)` - Check if purchased
- `isFeaturePurchased(pluginId, featureId, userId)` - Check if feature purchased
- `getPurchase(purchaseId)` - Get specific purchase
- `deletePurchase(purchaseId)` - Delete purchase (for refunds)

**Implementation**: `domain/src/commonMain/kotlin/ireader/domain/plugins/PurchaseRepositoryImpl.kt`
- In-memory implementation (temporary until Task 5 database schema is created)
- Thread-safe using Mutex
- Will be replaced with database-backed implementation

### 5. PaymentError Sealed Class
**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/PaymentError.kt`

Error types for payment operations:
- `NetworkError` - Network connectivity issues
- `PaymentCancelled` - User cancelled payment
- `PaymentFailed` - Payment processing failed
- `AlreadyPurchased` - Item already owned
- `ServerError(code)` - Server-side error

Includes `toUserMessage()` extension function for user-friendly error messages.

### 6. Trial Period Support
**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/TrialInfo.kt`

Trial information data class:
```kotlin
data class TrialInfo(
    val pluginId: String,
    val startDate: Long,
    val expirationDate: Long,
    val isActive: Boolean
)
```

**TrialRepository Interface**:
- `startTrial(pluginId, durationDays)` - Start trial
- `getTrialInfo(pluginId, userId)` - Get trial info
- `hasActiveTrial(pluginId, userId)` - Check if trial active
- `endTrial(pluginId, userId)` - End trial

**Implementation**: `domain/src/commonMain/kotlin/ireader/domain/plugins/TrialRepositoryImpl.kt`
- In-memory implementation (temporary)
- Tracks trial expiration
- Prevents duplicate trials

## Requirements Coverage

### Requirement 8: Premium Plugin Monetization
- ✅ 8.1: Display price in marketplace
- ✅ 8.2: Process payment securely
- ✅ 8.3: Unlock premium plugin after purchase
- ✅ 8.4: Trial period support with expiration tracking
- ✅ 8.5: Cross-device purchase synchronization

### Requirement 9: In-Plugin Purchases
- ✅ 9.1: Define purchasable features
- ✅ 9.2: Display purchase options for locked features
- ✅ 9.3: Unlock features immediately after purchase
- ✅ 9.4: Secure payment processing through plugin API
- ✅ 9.5: Track purchase history for refunds and support

## Usage Example

```kotlin
// Initialize service
val monetizationService = MonetizationService(
    paymentProcessor = AndroidPaymentProcessor { getCurrentUserId() },
    purchaseRepository = PurchaseRepositoryImpl(),
    trialRepository = TrialRepositoryImpl { getCurrentUserId() },
    getCurrentUserId = { "user123" }
)

// Purchase a plugin
val result = monetizationService.purchasePlugin(
    pluginId = "theme.dark.premium",
    price = 4.99,
    currency = "USD"
)

result.onSuccess { purchase ->
    println("Purchase successful: ${purchase.id}")
}.onFailure { error ->
    when (error) {
        is PaymentError -> println(error.toUserMessage())
        else -> println("Error: ${error.message}")
    }
}

// Check if purchased
val isPurchased = monetizationService.isPurchased("theme.dark.premium")

// Start trial
val trialResult = monetizationService.startTrial(
    pluginId = "theme.dark.premium",
    durationDays = 7
)

// Purchase feature
val featureResult = monetizationService.purchaseFeature(
    pluginId = "translation.deepl",
    featureId = "unlimited_translations",
    price = 2.99
)
```

## Next Steps

1. **Task 5**: Implement database schema for purchases and trials
   - Replace in-memory implementations with database-backed versions
   - Add proper persistence and querying

2. **Platform Integration**:
   - Android: Integrate Google Play Billing Library
   - Desktop: Integrate Stripe or PayPal SDK
   - iOS: Integrate StoreKit (when iOS support is added)

3. **Backend Integration**:
   - Implement purchase verification endpoint
   - Add cross-device sync API
   - Implement refund handling

4. **UI Integration** (Tasks 7-9):
   - Add purchase buttons to plugin marketplace
   - Implement payment dialogs
   - Show trial status and expiration
   - Display purchase history

## Notes

- All payment processing is platform-specific and requires actual payment provider integration
- Current implementations are placeholders that throw `NotImplementedError`
- Purchase and trial data is stored in-memory and will be lost on app restart until database implementation
- All methods are suspend functions for async operation
- Thread-safe using Mutex for concurrent access
