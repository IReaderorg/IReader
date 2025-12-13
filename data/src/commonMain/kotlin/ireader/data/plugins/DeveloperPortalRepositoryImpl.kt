package ireader.data.plugins

import ireader.domain.plugins.*
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory implementation of DeveloperPortalRepository.
 * For production, this would integrate with Supabase backend.
 */
class DeveloperPortalRepositoryImpl(
    private val checkDeveloperBadge: suspend (String) -> Boolean = { false },
    private val getCurrentUserId: () -> String?
) : DeveloperPortalRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // In-memory cache
    private val developerPluginsCache = MutableStateFlow<List<DeveloperPlugin>>(emptyList())
    private val pluginGrantsCache = MutableStateFlow<Map<String, List<PluginAccessGrant>>>(emptyMap())
    private val userPurchasesCache = MutableStateFlow<Map<String, List<PluginPurchase>>>(emptyMap())

    override suspend fun isDeveloper(userId: String): Boolean {
        return try {
            checkDeveloperBadge(userId)
        } catch (e: Exception) {
            false
        }
    }

    override fun getDeveloperPlugins(developerId: String): Flow<List<DeveloperPlugin>> {
        return developerPluginsCache
    }

    override fun getPluginGrants(pluginId: String): Flow<List<PluginAccessGrant>> {
        return pluginGrantsCache.map { it[pluginId] ?: emptyList() }
    }

    override suspend fun grantAccess(grant: PluginAccessGrant): Result<PluginAccessGrant> {
        return try {
            val currentGrants = pluginGrantsCache.value.toMutableMap()
            val pluginGrantList = currentGrants[grant.pluginId]?.toMutableList() ?: mutableListOf()

            // Check if already granted
            if (pluginGrantList.any { it.grantedToUsername == grant.grantedToUsername && it.isActive }) {
                return Result.failure(Exception("User already has access"))
            }

            // Check grant limit
            val activeGrants = pluginGrantList.count { it.isActive }
            if (activeGrants >= 10) {
                return Result.failure(Exception("Maximum grants reached"))
            }

            val newGrant = grant.copy(id = "grant_${currentTimeToLong()}")
            pluginGrantList.add(newGrant)
            currentGrants[grant.pluginId] = pluginGrantList
            pluginGrantsCache.value = currentGrants

            Result.success(newGrant)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeAccess(grantId: String): Result<Unit> {
        return try {
            val currentGrants = pluginGrantsCache.value.toMutableMap()
            for ((pluginId, grants) in currentGrants) {
                currentGrants[pluginId] = grants.map { g ->
                    if (g.id == grantId) g.copy(isActive = false) else g
                }
            }
            pluginGrantsCache.value = currentGrants
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRemainingGrants(pluginId: String): Int {
        val grants = pluginGrantsCache.value[pluginId] ?: emptyList()
        val activeGrants = grants.count { it.isActive }
        return 10 - activeGrants
    }

    override suspend fun verifyPurchase(request: PurchaseVerificationRequest): PurchaseVerificationResult {
        // In-memory implementation - always returns not verified
        return PurchaseVerificationResult(
            isValid = false,
            errorMessage = "Purchase verification not available in offline mode"
        )
    }

    override suspend fun hasAccess(userId: String, pluginId: String): Boolean {
        // Check purchases
        val purchases = userPurchasesCache.value[userId] ?: emptyList()
        if (purchases.any { it.pluginId == pluginId && it.status == PurchaseStatus.ACTIVE }) {
            return true
        }

        // Check grants
        val grants = pluginGrantsCache.value[pluginId] ?: emptyList()
        if (grants.any { it.grantedToUserId == userId && it.isActive }) {
            return true
        }

        return false
    }

    override fun getUserPurchases(userId: String): Flow<List<PluginPurchase>> {
        return userPurchasesCache.map { it[userId] ?: emptyList() }
    }

    override suspend fun getPluginStats(pluginId: String): PluginStats {
        val grants = pluginGrantsCache.value[pluginId] ?: emptyList()
        return PluginStats(
            totalDownloads = 0,
            totalPurchases = 0,
            totalRevenue = 0.0,
            activeUsers = 0,
            grantedUsers = grants.count { it.isActive },
            trialUsers = 0,
            averageRating = 0.0,
            totalReviews = 0
        )
    }
}
