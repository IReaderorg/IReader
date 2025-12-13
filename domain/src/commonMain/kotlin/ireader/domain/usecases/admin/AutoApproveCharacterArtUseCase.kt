package ireader.domain.usecases.admin

import ireader.domain.data.repository.CharacterArtRepository
import ireader.domain.models.characterart.CharacterArt

/**
 * Use case for auto-approving character art that has been pending for more than 7 days.
 * This is an admin-only operation that can be triggered manually or via scheduled task.
 */
class AutoApproveCharacterArtUseCase(
    private val repository: CharacterArtRepository
) {
    companion object {
        const val DEFAULT_DAYS_THRESHOLD = 7
    }
    
    /**
     * Get all pending art older than the specified number of days
     */
    suspend fun getPendingArtOlderThan(days: Int = DEFAULT_DAYS_THRESHOLD): Result<List<CharacterArt>> {
        return repository.getPendingArtOlderThan(days)
    }
    
    /**
     * Auto-approve a single art piece
     */
    suspend fun autoApprove(artId: String): Result<Unit> {
        return repository.autoApproveArt(artId)
    }
    
    /**
     * Auto-approve all pending art older than the specified number of days
     * @return Number of successfully approved items
     */
    suspend fun autoApproveAll(days: Int = DEFAULT_DAYS_THRESHOLD): Result<Int> {
        val pendingArt = getPendingArtOlderThan(days).getOrElse { 
            return Result.failure(it) 
        }
        
        if (pendingArt.isEmpty()) {
            return Result.success(0)
        }
        
        var successCount = 0
        for (art in pendingArt) {
            autoApprove(art.id).onSuccess { successCount++ }
        }
        
        return Result.success(successCount)
    }
}
