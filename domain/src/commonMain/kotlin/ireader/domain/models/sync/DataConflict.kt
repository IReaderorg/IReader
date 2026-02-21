package ireader.domain.models.sync

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Represents a data conflict between local and remote versions.
 *
 * @property conflictType Type of conflict
 * @property localData Local version of the data
 * @property remoteData Remote version of the data
 * @property conflictField Name of the field that has conflicting values
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class DataConflict(
    val conflictType: ConflictType,
    @Contextual val localData: Any,
    @Contextual val remoteData: Any,
    val conflictField: String
) {
    init {
        require(conflictField.isNotBlank()) { "Conflict field cannot be empty or blank" }
    }
}

/**
 * Types of data conflicts that can occur.
 */
@Serializable
enum class ConflictType {
    /** Conflict in reading progress (chapter, offset, etc.) */
    READING_PROGRESS,
    
    /** Conflict in bookmark data */
    BOOKMARK,
    
    /** Conflict in book metadata */
    BOOK_METADATA
}

/**
 * Strategies for resolving data conflicts.
 */
@Serializable
enum class ConflictResolutionStrategy {
    /**
     * Use the data with the most recent timestamp.
     * This is the default strategy.
     */
    LATEST_TIMESTAMP,
    
    /**
     * Always prefer local device data.
     */
    LOCAL_WINS,
    
    /**
     * Always prefer remote device data.
     */
    REMOTE_WINS,
    
    /**
     * Attempt to merge compatible changes.
     * Falls back to LATEST_TIMESTAMP if merge is not possible.
     */
    MERGE,
    
    /**
     * Prompt user to manually choose which version to keep.
     */
    MANUAL
}
