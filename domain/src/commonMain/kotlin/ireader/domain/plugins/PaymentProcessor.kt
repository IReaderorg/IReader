package ireader.domain.plugins

import ireader.domain.data.repository.Purchase

/**
 * Platform-specific payment processor interface
 * Implementations handle Google Play, App Store, and desktop payment processing
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5
 */
expect interface PaymentProcessor {
    /**
     * Process a payment for a plugin or feature
     * @param itemId The plugin ID or feature ID to purchase
     * @param price The price in the local currency
     * @param currency The currency code (e.g., "USD", "EUR")
     * @return Result containing the Purchase on success or PaymentError on failure
     */
    suspend fun processPayment(
        itemId: String,
        price: Double,
        currency: String
    ): Result<Purchase>
    
    /**
     * Verify a purchase receipt
     * @param receiptData Platform-specific receipt data
     * @return Result indicating if the receipt is valid
     */
    suspend fun verifyReceipt(receiptData: String): Result<Boolean>
    
    /**
     * Restore previous purchases for the current user
     * @return Result containing list of restored purchases
     */
    suspend fun restorePurchases(): Result<List<Purchase>>
}
