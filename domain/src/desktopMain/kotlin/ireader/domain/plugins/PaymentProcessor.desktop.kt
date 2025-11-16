package ireader.domain.plugins

import ireader.domain.data.repository.Purchase
import java.util.UUID

/**
 * Desktop implementation of PaymentProcessor
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5
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
 * Default implementation for Desktop
 * This could integrate with Stripe, PayPal, or other payment providers
 */
class DesktopPaymentProcessor(
    private val getCurrentUserId: () -> String
) : PaymentProcessor {
    
    override suspend fun processPayment(
        itemId: String,
        price: Double,
        currency: String
    ): Result<Purchase> {
        return runCatching {
            // Traditional payments not supported. Use cryptocurrency donations instead.
            // 
            // IReader uses cryptocurrency donations for funding instead of traditional
            // payment processors like Stripe or PayPal. This approach:
            // - Avoids high transaction fees (2.9% + $0.30 for Stripe)
            // - Supports users in regions without access to traditional payment methods
            // - Provides transparency through blockchain verification
            // - Eliminates payment processor restrictions and account freezes
            // - No chargebacks or payment disputes
            //
            // Users can donate via the Donation Screen in Settings, which supports:
            // - Bitcoin (BTC)
            // - Ethereum (ETH)
            // - Litecoin (LTC)
            // - USDT and other major cryptocurrencies
            //
            // See: presentation/ui/settings/donation/DonationScreen.kt
            
            throw NotImplementedError(
                "Traditional payments not supported. " +
                "Please use cryptocurrency donations in Settings > Support Development"
            )
        }
    }
    
    override suspend fun verifyReceipt(receiptData: String): Result<Boolean> {
        return runCatching {
            // Traditional payments not supported. Use cryptocurrency donations instead.
            // Cryptocurrency donations are verified through blockchain explorers.
            // See: domain/models/donation/CryptoDonation.kt
            throw NotImplementedError(
                "Traditional payment verification not supported. " +
                "Cryptocurrency donations are verified via blockchain"
            )
        }
    }
    
    override suspend fun restorePurchases(): Result<List<Purchase>> {
        return runCatching {
            // Traditional payments not supported. Use cryptocurrency donations instead.
            // Cryptocurrency donations don't require "restoration" as they are
            // permanent blockchain transactions that can be verified at any time.
            throw NotImplementedError(
                "Traditional purchase restoration not supported. " +
                "Cryptocurrency donations are permanent blockchain transactions"
            )
        }
    }
}
