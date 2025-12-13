package ireader.data.plugins

import ireader.domain.plugins.sync.ChangeTracker
import ireader.domain.plugins.sync.SyncChange
import ireader.domain.plugins.sync.ChangeType
import ireader.domain.plugins.sync.EntityType
import ir.kazemcodes.infinityreader.Database

/**
 * Implementation of ChangeTracker using SQLDelight.
 */
class ChangeTrackerImpl(
    private val database: Database
) : ChangeTracker {
    
    private val queries get() = database.pluginSyncQueries
    
    override suspend fun trackChange(change: SyncChange) {
        queries.insertChange(
            id = change.id,
            change_type = change.changeType.name,
            entity_type = change.entityType.name,
            entity_id = change.entityId,
            old_value = change.oldValue,
            new_value = change.newValue,
            timestamp = change.timestamp,
            synced = change.synced
        )
    }
    
    override suspend fun getPendingChanges(): List<SyncChange> {
        return queries.selectPendingChanges().executeAsList().map { it.toDomain() }
    }
    
    override suspend fun getChange(entityType: EntityType, entityId: String): SyncChange? {
        return queries.selectChangeByEntity(entityType.name, entityId)
            .executeAsOneOrNull()?.toDomain()
    }
    
    override suspend fun markSynced(changeId: String) {
        queries.markSynced(changeId)
    }
    
    override suspend fun clearSyncedChanges() {
        queries.deleteSyncedChanges()
    }
    
    private fun data.Plugin_sync_change.toDomain(): SyncChange {
        return SyncChange(
            id = id,
            changeType = ChangeType.valueOf(change_type),
            entityType = EntityType.valueOf(entity_type),
            entityId = entity_id,
            oldValue = old_value,
            newValue = new_value,
            timestamp = timestamp,
            synced = synced
        )
    }
}
