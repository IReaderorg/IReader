package ireader.domain.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Base repository interface with common CRUD operations
 * 
 * @param T The entity type
 * @param ID The ID type
 */
interface BaseRepository<T, ID> {
    /**
     * Find entity by ID
     */
    suspend fun findById(id: ID): T?
    
    /**
     * Find all entities
     */
    suspend fun findAll(): List<T>
    
    /**
     * Insert a new entity
     */
    suspend fun insert(entity: T): ID
    
    /**
     * Update an existing entity
     */
    suspend fun update(entity: T)
    
    /**
     * Delete an entity by ID
     */
    suspend fun delete(id: ID)
}

/**
 * Extended base repository with reactive queries
 */
interface ReactiveRepository<T, ID> : BaseRepository<T, ID> {
    /**
     * Subscribe to entity changes by ID
     */
    fun subscribeById(id: ID): Flow<T?>
    
    /**
     * Subscribe to all entities
     */
    fun subscribeAll(): Flow<List<T>>
}

/**
 * Repository with batch operations support
 */
interface BatchRepository<T, ID> : BaseRepository<T, ID> {
    /**
     * Insert multiple entities
     */
    suspend fun insertBatch(entities: List<T>): List<ID>
    
    /**
     * Update multiple entities
     */
    suspend fun updateBatch(entities: List<T>)
    
    /**
     * Delete multiple entities by IDs
     */
    suspend fun deleteBatch(ids: List<ID>)
}

/**
 * Full-featured repository with all operations
 */
interface FullRepository<T, ID> : ReactiveRepository<T, ID>, BatchRepository<T, ID>
