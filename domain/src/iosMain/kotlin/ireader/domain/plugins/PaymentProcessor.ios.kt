package ireader.domain.plugins

/**
 * iOS implementation of PaymentProcessor
 * 
 * TODO: Implement using StoreKit for in-app purchases
 */
actual interface PaymentProcessor {
    actual suspend fun processPayment(
        productId: String,
        amount: Double,
        currency: String
    ): Result<PaymentResult>
    
    actual suspend fun verifyPurchase(purchaseToken: String): Result<Boolean>
    
    actual suspend fun getProducts(): Result<List<Product>>
    
    actual suspend fun restorePurchases(): Result<List<String>>
}

data class PaymentResult(
    val transactionId: String,
    val status: PaymentStatus
)

enum class PaymentStatus {
    SUCCESS, PENDING, FAILED, CANCELLED
}

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String
)
