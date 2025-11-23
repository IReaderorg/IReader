package ireader.data.backend

import kotlinx.serialization.json.JsonElement

/**
 * Backend service abstraction layer.
 * 
 * This interface abstracts away the specific backend implementation (Supabase, Firebase, etc.)
 * allowing repositories to work with any backend without knowing the implementation details.
 * 
 * Benefits:
 * - No reflection issues (manual JSON parsing)
 * - Database agnostic (easy to switch backends)
 * - Better testability (easy to mock)
 * - Cleaner separation of concerns
 */
interface BackendService {
    
    /**
     * Query records from a table
     * 
     * @param table Table name
     * @param filters Key-value pairs for filtering (e.g., "user_id" to "123")
     * @param columns Columns to select (empty = all columns)
     * @param orderBy Column to order by (optional)
     * @param ascending Sort order (default true)
     * @param limit Maximum number of records (optional)
     * @param offset Number of records to skip (optional)
     * @return List of JSON objects representing the records
     */
    suspend fun query(
        table: String,
        filters: Map<String, Any> = emptyMap(),
        columns: String = "*",
        orderBy: String? = null,
        ascending: Boolean = true,
        limit: Int? = null,
        offset: Int? = null
    ): Result<List<JsonElement>>
    
    /**
     * Insert a record into a table
     * 
     * @param table Table name
     * @param data JSON object representing the record to insert
     * @param returning Whether to return the inserted record
     * @return The inserted record (if returning = true)
     */
    suspend fun insert(
        table: String,
        data: JsonElement,
        returning: Boolean = true
    ): Result<JsonElement?>
    
    /**
     * Update records in a table
     * 
     * @param table Table name
     * @param filters Key-value pairs for filtering which records to update
     * @param data JSON object with fields to update
     * @param returning Whether to return the updated record
     * @return The updated record (if returning = true)
     */
    suspend fun update(
        table: String,
        filters: Map<String, Any>,
        data: JsonElement,
        returning: Boolean = true
    ): Result<JsonElement?>
    
    /**
     * Delete records from a table
     * 
     * @param table Table name
     * @param filters Key-value pairs for filtering which records to delete
     * @return Success/failure
     */
    suspend fun delete(
        table: String,
        filters: Map<String, Any>
    ): Result<Unit>
    
    /**
     * Call a remote procedure/function
     * 
     * @param function Function name
     * @param parameters Function parameters
     * @return Function result as JSON
     */
    suspend fun rpc(
        function: String,
        parameters: Map<String, Any> = emptyMap()
    ): Result<JsonElement>
    
    /**
     * Upsert (insert or update) a record
     * 
     * @param table Table name
     * @param data JSON object representing the record
     * @param onConflict Column(s) to check for conflicts
     * @param returning Whether to return the upserted record
     * @return The upserted record (if returning = true)
     */
    suspend fun upsert(
        table: String,
        data: JsonElement,
        onConflict: String? = null,
        returning: Boolean = true
    ): Result<JsonElement?>
}
