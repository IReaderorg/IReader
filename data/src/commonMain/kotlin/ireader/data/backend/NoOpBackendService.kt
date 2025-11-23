package ireader.data.backend

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray

/**
 * No-op implementation of BackendService for when backend is not configured.
 * 
 * This allows the app to work without a backend, returning empty results
 * instead of crashing.
 */
class NoOpBackendService : BackendService {
    
    private val notConfiguredError = Exception("Backend service is not configured")
    
    override suspend fun query(
        table: String,
        filters: Map<String, Any>,
        columns: String,
        orderBy: String?,
        ascending: Boolean,
        limit: Int?,
        offset: Int?
    ): Result<List<JsonElement>> {
        // Return empty list instead of error - allows app to continue
        return Result.success(emptyList())
    }
    
    override suspend fun insert(
        table: String,
        data: JsonElement,
        returning: Boolean
    ): Result<JsonElement?> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun update(
        table: String,
        filters: Map<String, Any>,
        data: JsonElement,
        returning: Boolean
    ): Result<JsonElement?> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun delete(
        table: String,
        filters: Map<String, Any>
    ): Result<Unit> {
        return Result.failure(notConfiguredError)
    }
    
    override suspend fun rpc(
        function: String,
        parameters: Map<String, Any>
    ): Result<JsonElement> {
        return Result.success(buildJsonArray {})
    }
    
    override suspend fun upsert(
        table: String,
        data: JsonElement,
        onConflict: String?,
        returning: Boolean
    ): Result<JsonElement?> {
        return Result.failure(notConfiguredError)
    }
}
