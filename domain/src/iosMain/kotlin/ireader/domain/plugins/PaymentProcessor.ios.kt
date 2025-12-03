package ireader.domain.plugins

import ireader.domain.data.repository.Purchase

/**
 * iOS implementation of PaymentProcessor
 * 
 * TODO: Implement using StoreKit for in-app purchases
 */
actual interface PaymentProcessor {
    actual suspend fun processPayment(
        itemId: String,
        price: Double,
        currency: String
    ): Result<Purchase>
    
    actual suspend fun verifyReceipt(receiptData: String): Result<Boolean>
    
    actual suspend fun restorePurchases(): Result<List<Purchase>>
}
