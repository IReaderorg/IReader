package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated TrackRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential tracking operations for external services
 * like MyAnimeList, AniList, etc.
 */
interface TrackRepository {
    
    // Track retrieval
    suspend fun getTrackById(id: Long): Track?
    suspend fun getTracksByBookId(bookId: Long): List<Track>
    fun getTracksByBookIdAsFlow(bookId: Long): Flow<List<Track>>
    
    // Service-specific tracks
    suspend fun getTrackByBookIdAndService(bookId: Long, serviceId: Long): Track?
    suspend fun getTracksByService(serviceId: Long): List<Track>
    
    // Track management
    suspend fun insertTrack(track: Track): Boolean
    suspend fun updateTrack(update: TrackUpdate): Boolean
    suspend fun deleteTrack(trackId: Long): Boolean
    
    // Sync operations
    suspend fun syncTrack(trackId: Long): Boolean
    suspend fun syncAllTracks(): Boolean
    suspend fun syncTracksByService(serviceId: Long): Boolean
    
    // Bulk operations
    suspend fun insertTracks(tracks: List<Track>): Boolean
    suspend fun updateTracks(updates: List<TrackUpdate>): Boolean
    suspend fun deleteTracks(trackIds: List<Long>): Boolean
}