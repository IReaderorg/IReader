package ireader.presentation.ui.settings.tracking

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.core.log.Log
import ireader.domain.data.repository.TrackingRepository
import ireader.domain.models.entities.TrackerService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Background worker for syncing tracking data with external services.
 * Runs periodically based on user preferences.
 */
class TrackingSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    
    private val trackingRepository: TrackingRepository by inject()
    
    override suspend fun doWork(): Result {
        return try {
            Log.info { "Starting tracking sync..." }
            
            val enabledServices = trackingRepository.getEnabledServices()
            var syncedCount = 0
            var failedCount = 0
            
            for (service in enabledServices) {
                try {
                    val tracks = trackingRepository.getTracksByService(service.id)
                    for (track in tracks) {
                        try {
                            val success = trackingRepository.syncTrack(track.mangaId, service.id)
                            if (success) syncedCount++ else failedCount++
                        } catch (e: Exception) {
                            Log.error(e, "Failed to sync track ${track.id}")
                            failedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.error(e, "Failed to get tracks for service ${service.name}")
                }
            }
            
            Log.info { "Tracking sync completed: $syncedCount synced, $failedCount failed" }
            Result.success()
        } catch (e: Exception) {
            Log.error(e, "Tracking sync failed")
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "tracking_sync_work"
    }
}
