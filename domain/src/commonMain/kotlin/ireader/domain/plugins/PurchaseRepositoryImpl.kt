package ireader.domain.plugins

import ireader.domain.data.repository.Purchase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory implementation of PurchaseRepository
 * This is a temporary implementation until the database schema is created (Task 5)
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5
 */
class PurchaseRepositoryImpl : PurchaseRepository {
    private val purchases = mutableListOf<Purchase>()
    private val mutex = Mutex()
    
    override suspend fun savePurchase(purchase: Purchase): Result<Unit> {
        return runCatching {
            mutex.withLock {
                // Remove existing purchase with same ID if exists
                purchases.removeIf { it.id == purchase.id }
                purchases.add(purchase)
            }
        }
    }
    
    override suspend fun getPurchasesByUser(userId: String): List<Purchase> {
        return mutex.withLock {
            purchases.filter { it.userId == userId }
        }
    }
    
    override suspend fun isPurchased(pluginId: String, userId: String): Boolean {
        return mutex.withLock {
            purchases.any { 
                it.pluginId == pluginId && 
                it.userId == userId && 
                it.featureId == null 
            }
        }
    }
    
    override suspend fun isFeaturePurchased(
        pluginId: String, 
        featureId: String, 
        userId: String
    ): Boolean {
        return mutex.withLock {
            purchases.any { 
                it.pluginId == pluginId && 
                it.featureId == featureId && 
                it.userId == userId 
            }
        }
    }
    
    override suspend fun getPurchase(purchaseId: String): Purchase? {
        return mutex.withLock {
            purchases.find { it.id == purchaseId }
        }
    }
    
    override suspend fun deletePurchase(purchaseId: String): Result<Unit> {
        return runCatching {
            mutex.withLock {
                purchases.removeIf { it.id == purchaseId }
            }
        }
    }
}
