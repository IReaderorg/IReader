package ireader.data.plugin

import ireader.data.core.DatabaseHandler
import ireader.domain.plugins.TrialInfo
import ireader.domain.plugins.TrialRepository
import java.util.UUID

/**
 * Implementation of TrialRepository using SQLDelight
 * Requirements: 8.4, 8.5
 */
class TrialRepositoryImpl(
    private val handler: DatabaseHandler,
    private val getCurrentUserId: () -> String
) : TrialRepository {

    override suspend fun startTrial(pluginId: String, durationDays: Int): Result<TrialInfo> = runCatching {
        val userId = getCurrentUserId()
        val startDate = System.currentTimeMillis()
        val expirationDate = startDate + (durationDays * 24 * 60 * 60 * 1000L)
        
        handler.await {
            pluginTrialQueries.insert(
                id = UUID.randomUUID().toString(),
                plugin_id = pluginId,
                user_id = userId,
                start_date = startDate,
                expiration_date = expirationDate,
                is_active = true
            )
        }
        
        TrialInfo(
            pluginId = pluginId,
            startDate = startDate,
            expirationDate = expirationDate,
            isActive = true
        )
    }

    override suspend fun getTrialInfo(pluginId: String, userId: String): TrialInfo? {
        return handler.awaitOneOrNull {
            pluginTrialQueries.selectByPluginAndUser(pluginId, userId)
        }?.let { entity ->
            TrialInfo(
                pluginId = entity.plugin_id,
                startDate = entity.start_date,
                expirationDate = entity.expiration_date,
                isActive = entity.is_active
            )
        }
    }

    override suspend fun hasActiveTrial(pluginId: String, userId: String): Boolean {
        val trial = getTrialInfo(pluginId, userId) ?: return false
        return trial.isActive && !trial.isExpired()
    }

    override suspend fun endTrial(pluginId: String, userId: String): Result<Unit> = runCatching {
        handler.await {
            pluginTrialQueries.endTrial(pluginId, userId)
        }
    }
}
