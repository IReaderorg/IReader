# Plugin Monetization System

This document describes the plugin monetization system implementation for IReader.

## Overview

The monetization system enables plugin developers to offer:
- **Premium Plugins**: One-time purchase to unlock full plugin functionality
- **Freemium Plugins**: Free base functionality with purchasable premium features
- **Trial Periods**: Time-limited trials for premium plugins

## Architecture

### Core Components

1. **MonetizationService**: Main service handling all purchase operations
2. **PaymentProcessor**: Platform-specific payment processing (Google Play, App Store, Desktop)
3. **PurchaseRepository**: Database operations for purchase records
4. **TrialRepository**: Database operations for trial period tracking

### Data Models

#### Purchase
```kotlin
data class Purchase(
    val id: String,
    val pluginId: String,
    val featureId: String?,  // null for plugin purchases, set for feature purchases
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val userId: String,
    val receiptData: String?  // Platform-specific receipt for verification
)
```

#### TrialInfo
```kotlin
data class TrialInfo(
    val pluginId: String,
    val startDate: Long,
    val expirationDate: Long,
    val isActive: Boolean
)
```

#### PaymentError
```kotlin
sealed class PaymentError : Exception() {
    object NetworkError
    object PaymentCancelled
    object PaymentFailed
    object AlreadyPurchased
    data class ServerError(val code: Int)
}
```

## Usage

### Purchasing a Plugin

```kotlin
val monetizationService: MonetizationService = get()

// Purchase a premium plugin
val result = monetizationService.purchasePlugin(
    pluginId = "com.example.premium-theme",
    price = 4.99,
    currency = "USD"
)

result.onSuccess { purchase ->
    println("Purchase successful: ${purchase.id}")
}.onFailure { error ->
    when (error) {
        is PaymentError.AlreadyPurchased -> println("Already purchased")
        is PaymentError.PaymentCancelled -> println("User cancelled")
        else -> println("Purchase failed: ${error.toUserMessage()}")
    }
}
```

### Purchasing a Feature

```kotlin
// Purchase a feature within a freemium plugin
val result = monetizationService.purchaseFeature(
    pluginId = "com.example.freemium-plugin",
    featureId = "premium-voices",
    price = 1.99,
    currency = "USD"
)
```

### Checking Purchase Status

```kotlin
// Check if plugin is purchased or has active trial
val isPurchased = monetizationService.isPurchased("com.example.premium-theme")

// Check if specific feature is purchased
val hasFeature = monetizationService.isFeaturePurchased(
    pluginId = "com.example.freemium-plugin",
    featureId = "premium-voices"
)
```

### Starting a Trial

```kotlin
// Start a 7-day trial
val result = monetizationService.startTrial(
    pluginId = "com.example.premium-theme",
    durationDays = 7
)

result.onSuccess { trialInfo ->
    println("Trial started, expires in ${trialInfo.getRemainingDays()} days")
}
```

### Syncing Purchases

```kotlin
// Sync purchases across devices
val result = monetizationService.syncPurchases(userId = "user123")

result.onSuccess {
    println("Purchases synced successfully")
}
```

### Restoring Purchases

```kotlin
// Restore purchases from platform store
val result = monetizationService.restorePurchases()

result.onSuccess { purchases ->
    println("Restored ${purchases.size} purchases")
}
```

## Platform-Specific Implementation

### Android (Google Play Billing)

The Android implementation uses Google Play Billing Library. To complete the implementation:

1. Add Google Play Billing dependency to `build.gradle.kts`
2. Implement `AndroidPaymentProcessor.processPayment()` using BillingClient
3. Handle purchase acknowledgment and consumption
4. Implement receipt verification with Google's servers

### Desktop

The Desktop implementation can integrate with various payment providers:
- Stripe
- PayPal
- Paddle
- Custom backend payment system

Purchases are stored on the backend and synced across devices.

### iOS (App Store)

When iOS support is added, implement using StoreKit:
1. Create iOS source set
2. Implement `PaymentProcessor.ios.kt`
3. Use StoreKit for in-app purchases
4. Handle receipt validation with Apple's servers

## Database Schema

### plugin_purchases Table
```sql
CREATE TABLE plugin_purchases (
    id TEXT PRIMARY KEY,
    plugin_id TEXT NOT NULL,
    feature_id TEXT,
    amount REAL NOT NULL,
    currency TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    user_id TEXT NOT NULL,
    receipt_data TEXT
);
```

### plugin_trials Table
```sql
CREATE TABLE plugin_trials (
    id TEXT PRIMARY KEY,
    plugin_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    start_date INTEGER NOT NULL,
    expiration_date INTEGER NOT NULL,
    is_active INTEGER NOT NULL
);
```

## Security Considerations

1. **Receipt Verification**: Always verify purchases with platform servers
2. **User Authentication**: Tie purchases to authenticated user accounts
3. **Secure Storage**: Store receipt data securely
4. **Fraud Prevention**: Implement server-side validation
5. **Refund Handling**: Support refund processing and purchase revocation

## Error Handling

All monetization operations return `Result<T>` types. Handle errors appropriately:

```kotlin
result.onFailure { error ->
    when (error) {
        is PaymentError.NetworkError -> {
            // Show retry option
        }
        is PaymentError.PaymentCancelled -> {
            // User cancelled, no action needed
        }
        is PaymentError.PaymentFailed -> {
            // Show error message, suggest checking payment method
        }
        is PaymentError.AlreadyPurchased -> {
            // Inform user they already own this
        }
        is PaymentError.ServerError -> {
            // Log error, show generic message
        }
    }
}
```

## Testing

### Unit Tests
- Test purchase flow with mock PaymentProcessor
- Test trial period expiration logic
- Test purchase verification
- Test error handling

### Integration Tests
- Test with platform billing sandbox
- Test purchase restoration
- Test cross-device sync
- Test refund handling

## Future Enhancements

1. **Subscription Support**: Add recurring subscription model
2. **Promotional Codes**: Support discount codes and promotions
3. **Family Sharing**: Allow purchase sharing within family groups
4. **Gift Purchases**: Enable gifting plugins to other users
5. **Analytics**: Track conversion rates and revenue metrics

## Requirements Coverage

This implementation satisfies the following requirements:
- 8.1: Premium plugin purchase processing
- 8.2: Secure payment handling
- 8.3: Purchase verification
- 8.4: Trial period support
- 8.5: Cross-device purchase sync
- 9.1: In-plugin feature purchases
- 9.2: Freemium model support
- 9.3: Feature unlock verification
- 9.4: Purchase history tracking
- 9.5: Refund support
