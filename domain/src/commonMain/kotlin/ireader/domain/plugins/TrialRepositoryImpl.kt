package ireader.domain.plugins

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * In-memory implementation of TrialRepository
 * This is a temporary implementation until the database schema is created (Task 5)
 * Requirements: 8.4, 8.5
 */
class TrialRepositoryImpl(
    private val getCurrentUserId: () -> String
) : TrialRepository {
    private val trials = mutableMapOf<String, TrialInfo>() // Key: "pluginId:userId"
    private val mutex = Mutex()
    
    override suspend fun startTrial(pluginId: String, durationDays: Int): Result<TrialInfo> {
        return runCatching {
            val userId = getCurrentUserId()
            val key = "$pluginId:$userId"
            
            mutex.withLock {
                // Check if trial already exists
                if (trials.containsKey(key)) {
                    throw IllegalStateException("Trial already exists for this plugin")
                }
                
                val startDate = currentTimeToLong()
                val expirationDate = startDate + (durationDays * 24 * 60 * 60 * 1000L)
                
                val trialInfo = TrialInfo(
                    pluginId = pluginId,
                    startDate = startDate,
                    expirationDate = expirationDate,
                    isActive = true
                )
                
                trials[key] = trialInfo
                trialInfo
            }
        }
    }
    
    override suspend fun getTrialInfo(pluginId: String, userId: String): TrialInfo? {
        return mutex.withLock {
            val key = "$pluginId:$userId"
            trials[key]
        }
    }
    
    override suspend fun hasActiveTrial(pluginId: String, userId: String): Boolean {
        return mutex.withLock {
            val key = "$pluginId:$userId"
            val trial = trials[key]
            trial != null && trial.isActive && !trial.isExpired()
        }
    }
    
    override suspend fun endTrial(pluginId: String, userId: String): Result<Unit> {
        return runCatching {
            mutex.withLock {
                val key = "$pluginId:$userId"
                val trial = trials[key]
                if (trial != null) {
                    trials[key] = trial.copy(isActive = false)
                }
            }
        }
    }
}
