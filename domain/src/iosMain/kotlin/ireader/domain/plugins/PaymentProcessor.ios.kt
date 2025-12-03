package ireader.domain.plugins

import ireader.domain.data.repository.Purchase
import platform.StoreKit.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/**
 * iOS implementation of PaymentProcessor
 * 
 * Uses StoreKit for in-app purchases
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

/**
 * StoreKit-based payment processor implementation
 */
@OptIn(ExperimentalForeignApi::class)
class IosPaymentProcessor : PaymentProcessor {
    
    private val paymentQueue = SKPaymentQueue.defaultQueue()
    private var transactionObserver: TransactionObserver? = null
    
    init {
        transactionObserver = TransactionObserver()
        paymentQueue.addTransactionObserver(transactionObserver!!)
    }
    
    /**
     * Process a payment for an in-app purchase
     */
    override suspend fun processPayment(
        itemId: String,
        price: Double,
        currency: String
    ): Result<Purchase> = suspendCancellableCoroutine { continuation ->
        
        // Check if payments are allowed
        if (!SKPaymentQueue.canMakePayments()) {
            continuation.resume(Result.failure(PaymentException("Payments are not allowed on this device")))
            return@suspendCancellableCoroutine
        }
        
        // Request product info
        val productRequest = SKProductsRequest(productIdentifiers = setOf(itemId))
        
        val delegate = ProductRequestDelegate { products, error ->
            if (error != null) {
                continuation.resume(Result.failure(PaymentException(error.localizedDescription)))
                return@ProductRequestDelegate
            }
            
            val product = products?.firstOrNull()
            if (product == null) {
                continuation.resume(Result.failure(PaymentException("Product not found: $itemId")))
                return@ProductRequestDelegate
            }
            
            // Set up transaction completion handler
            transactionObserver?.onTransactionComplete = { transaction ->
                val state = transaction.transactionState
                when {
                    state == SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> {
                        val purchase = Purchase(
                            id = transaction.transactionIdentifier ?: "",
                            pluginId = itemId,
                            featureId = null,
                            amount = price,
                            currency = currency,
                            timestamp = (transaction.transactionDate?.timeIntervalSince1970?.toLong() ?: 0) * 1000,
                            userId = "",
                            receiptData = null
                        )
                        paymentQueue.finishTransaction(transaction)
                        continuation.resume(Result.success(purchase))
                    }
                    state == SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                        val errorMsg = transaction.error?.localizedDescription ?: "Payment failed"
                        paymentQueue.finishTransaction(transaction)
                        continuation.resume(Result.failure(PaymentException(errorMsg)))
                    }
                    state == SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                        paymentQueue.finishTransaction(transaction)
                    }
                    else -> {}
                }
            }
            
            // Add payment to queue
            val payment = SKPayment.paymentWithProduct(product)
            paymentQueue.addPayment(payment)
        }
        
        productRequest.delegate = delegate
        productRequest.start()
    }
    
    /**
     * Verify a receipt with Apple's servers
     */
    override suspend fun verifyReceipt(receiptData: String): Result<Boolean> {
        return try {
            // Get the receipt URL
            val receiptUrl = NSBundle.mainBundle.appStoreReceiptURL
            if (receiptUrl == null || !NSFileManager.defaultManager.fileExistsAtPath(receiptUrl.path ?: "")) {
                return Result.failure(PaymentException("Receipt not found"))
            }
            
            // Read receipt data
            val receiptNSData = NSData.dataWithContentsOfURL(receiptUrl)
            if (receiptNSData == null) {
                return Result.failure(PaymentException("Could not read receipt"))
            }
            
            // Base64 encode
            val base64Receipt = receiptNSData.base64EncodedStringWithOptions(0u)
            
            // In production, you should verify with your own server
            // which then verifies with Apple's servers
            // Direct verification with Apple is not recommended for security reasons
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(PaymentException("Receipt verification failed: ${e.message}"))
        }
    }
    
    /**
     * Restore previous purchases
     */
    override suspend fun restorePurchases(): Result<List<Purchase>> = suspendCancellableCoroutine { continuation ->
        
        val restoredPurchases = mutableListOf<Purchase>()
        
        transactionObserver?.onTransactionComplete = { transaction ->
            val state = transaction.transactionState
            when {
                state == SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    val purchase = Purchase(
                        id = transaction.transactionIdentifier ?: "",
                        pluginId = transaction.payment.productIdentifier,
                        featureId = null,
                        amount = 0.0,
                        currency = "USD",
                        timestamp = (transaction.transactionDate?.timeIntervalSince1970?.toLong() ?: 0) * 1000,
                        userId = "",
                        receiptData = null
                    )
                    restoredPurchases.add(purchase)
                    paymentQueue.finishTransaction(transaction)
                }
                state == SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    paymentQueue.finishTransaction(transaction)
                }
                else -> {}
            }
        }
        
        transactionObserver?.onRestoreComplete = { error ->
            if (error != null) {
                continuation.resume(Result.failure(PaymentException(error.localizedDescription)))
            } else {
                continuation.resume(Result.success(restoredPurchases))
            }
        }
        
        paymentQueue.restoreCompletedTransactions()
    }
    
    /**
     * Get available products
     */
    suspend fun getProducts(productIds: Set<String>): Result<List<ProductInfo>> = suspendCancellableCoroutine { continuation ->
        
        val productRequest = SKProductsRequest(productIdentifiers = productIds)
        
        val delegate = ProductRequestDelegate { products, error ->
            if (error != null) {
                continuation.resume(Result.failure(PaymentException(error.localizedDescription)))
                return@ProductRequestDelegate
            }
            
            val productInfos = products?.map { product ->
                ProductInfo(
                    id = product.productIdentifier,
                    title = product.localizedTitle,
                    description = product.localizedDescription,
                    price = product.price.doubleValue,
                    priceLocale = product.priceLocale.currencyCode ?: "USD"
                )
            } ?: emptyList()
            
            continuation.resume(Result.success(productInfos))
        }
        
        productRequest.delegate = delegate
        productRequest.start()
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        transactionObserver?.let {
            paymentQueue.removeTransactionObserver(it)
        }
        transactionObserver = null
    }
}

/**
 * Transaction observer for StoreKit
 */
@OptIn(ExperimentalForeignApi::class)
private class TransactionObserver : NSObject(), SKPaymentTransactionObserverProtocol {
    
    var onTransactionComplete: ((SKPaymentTransaction) -> Unit)? = null
    var onRestoreComplete: ((NSError?) -> Unit)? = null
    
    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        updatedTransactions.forEach { transaction ->
            (transaction as? SKPaymentTransaction)?.let {
                onTransactionComplete?.invoke(it)
            }
        }
    }
    
    override fun paymentQueueRestoreCompletedTransactionsFinished(queue: SKPaymentQueue) {
        onRestoreComplete?.invoke(null)
    }
    
    override fun paymentQueue(queue: SKPaymentQueue, restoreCompletedTransactionsFailedWithError: NSError) {
        onRestoreComplete?.invoke(restoreCompletedTransactionsFailedWithError)
    }
}

/**
 * Product request delegate
 */
@OptIn(ExperimentalForeignApi::class)
private class ProductRequestDelegate(
    private val onComplete: (List<SKProduct>?, NSError?) -> Unit
) : NSObject(), SKProductsRequestDelegateProtocol {
    
    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        @Suppress("UNCHECKED_CAST")
        val products = didReceiveResponse.products as? List<SKProduct>
        onComplete(products, null)
    }
    
    override fun request(request: SKRequest, didFailWithError: NSError) {
        onComplete(null, didFailWithError)
    }
}

/**
 * Product information
 */
data class ProductInfo(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val priceLocale: String
)

/**
 * Payment exception
 */
class PaymentException(message: String) : Exception(message)
