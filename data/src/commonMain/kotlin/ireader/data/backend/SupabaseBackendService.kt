package ireader.data.backend

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.net.URLEncoder

/**
 * Supabase implementation of BackendService.
 * 
 * This implementation uses Supabase Postgrest but parses responses manually
 * to avoid Kotlin reflection issues on Android.
 */
class SupabaseBackendService(
    private val client: SupabaseClient
) : BackendService {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    override suspend fun query(
        table: String,
        filters: Map<String, Any>,
        columns: String,
        orderBy: String?,
        ascending: Boolean,
        limit: Int?,
        offset: Int?
    ): Result<List<JsonElement>> {
        return try {
            // Build query parameters manually for better control
            val queryParams = mutableListOf<String>()
            
            // Add select columns
            queryParams.add("select=$columns")
            
            // Add filters
            filters.forEach { (key, value) ->
                val encodedValue = URLEncoder.encode(value.toString(), "UTF-8")
                queryParams.add("$key=eq.$encodedValue")
            }
            
            // Add ordering
            if (orderBy != null) {
                val orderDirection = if (ascending) "asc" else "desc"
                queryParams.add("order=$orderBy.$orderDirection")
            }
            
            // Add limit
            if (limit != null) {
                queryParams.add("limit=$limit")
            }
            
            // Add offset
            if (offset != null) {
                queryParams.add("offset=$offset")
            }
            
            val queryString = queryParams.joinToString("&")
            
            // Use raw HTTP request
            val accessToken = client.auth.currentAccessTokenOrNull()
            val baseUrl = if (client.supabaseUrl.startsWith("http")) {
                client.supabaseUrl
            } else {
                "https://${client.supabaseUrl}"
            }
            
            val url = "$baseUrl/rest/v1/$table?$queryString"
            
            val response = client.httpClient.get(url) {
                header("Content-Type", "application/json")
                header("apikey", client.supabaseKey)
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
            }
            
            val responseText = response.bodyAsText()
            
            // Parse raw JSON response - NO REFLECTION
            val jsonArray = json.parseToJsonElement(responseText).jsonArray
            Result.success(jsonArray)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun insert(
        table: String,
        data: JsonElement,
        returning: Boolean
    ): Result<JsonElement?> {
        return try {
            // Use raw HTTP request to avoid reflection
            val jsonBody = data.toString()
            val accessToken = client.auth.currentAccessTokenOrNull()
            val baseUrl = if (client.supabaseUrl.startsWith("http")) {
                client.supabaseUrl
            } else {
                "https://${client.supabaseUrl}"
            }
            
            val response = client.httpClient.post("$baseUrl/rest/v1/$table") {
                header("Content-Type", "application/json")
                header("apikey", client.supabaseKey)
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
                if (returning) {
                    header("Prefer", "return=representation")
                }
                setBody(jsonBody)
            }
            
            if (returning) {
                val responseText = response.bodyAsText()
                
                // Supabase returns an array even for single inserts
                val jsonElement = json.parseToJsonElement(responseText)
                val result = if (jsonElement is JsonArray) {
                    jsonElement.firstOrNull()
                } else {
                    // If it's a single object, wrap it
                    jsonElement
                }
                Result.success(result)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun update(
        table: String,
        filters: Map<String, Any>,
        data: JsonElement,
        returning: Boolean
    ): Result<JsonElement?> {
        return try {
            // Build filter query string with URL encoding
            val filterQuery = filters.entries.joinToString("&") { (key, value) ->
                val encodedValue = URLEncoder.encode(value.toString(), "UTF-8")
                "$key=eq.$encodedValue"
            }
            
            // Use raw HTTP request to avoid reflection
            val jsonBody = data.toString()
            val accessToken = client.auth.currentAccessTokenOrNull()
            val baseUrl = if (client.supabaseUrl.startsWith("http")) {
                client.supabaseUrl
            } else {
                "https://${client.supabaseUrl}"
            }
            
            val response = client.httpClient.patch("$baseUrl/rest/v1/$table?$filterQuery") {
                header("Content-Type", "application/json")
                header("apikey", client.supabaseKey)
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
                if (returning) {
                    header("Prefer", "return=representation")
                }
                setBody(jsonBody)
            }
            
            if (returning) {
                val responseText = response.bodyAsText()
                
                // Supabase returns an array even for single updates
                val jsonElement = json.parseToJsonElement(responseText)
                val result = if (jsonElement is JsonArray) {
                    jsonElement.firstOrNull()
                } else {
                    jsonElement
                }
                Result.success(result)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun delete(
        table: String,
        filters: Map<String, Any>
    ): Result<Unit> {
        return try {
            client.postgrest[table]
                .delete {
                    // Apply filters
                    filters.forEach { (key, value) ->
                        filter {
                            eq(key, value)
                        }
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun rpc(
        function: String,
        parameters: Map<String, Any>
    ): Result<JsonElement> {
        return try {
            // Use raw HTTP request to avoid reflection
            val jsonBody = buildJsonObject {
                parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> put(key, value)
                        is Int -> put(key, value)
                        is Long -> put(key, value)
                        is Boolean -> put(key, value)
                        is Double -> put(key, value)
                        is Float -> put(key, value.toDouble())
                        else -> put(key, value.toString())
                    }
                }
            }.toString()
            
            val accessToken = client.auth.currentAccessTokenOrNull()
            val baseUrl = if (client.supabaseUrl.startsWith("http")) {
                client.supabaseUrl
            } else {
                "https://${client.supabaseUrl}"
            }
            
            val response = client.httpClient.post("$baseUrl/rest/v1/rpc/$function") {
                header("Content-Type", "application/json")
                header("apikey", client.supabaseKey)
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
                setBody(jsonBody)
            }
            
            // Parse raw JSON response
            val responseText = response.bodyAsText()
            val jsonElement = json.parseToJsonElement(responseText)
            Result.success(jsonElement)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun upsert(
        table: String,
        data: JsonElement,
        onConflict: String?,
        returning: Boolean
    ): Result<JsonElement?> {
        return try {
            val jsonBody = data.toString()
            val accessToken = client.auth.currentAccessTokenOrNull()
            val baseUrl = if (client.supabaseUrl.startsWith("http")) {
                client.supabaseUrl
            } else {
                "https://${client.supabaseUrl}"
            }
            
            val response = client.httpClient.post("$baseUrl/rest/v1/$table") {
                header("Content-Type", "application/json")
                header("apikey", client.supabaseKey)
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
                // Upsert with resolution preference
                val preferHeader = buildString {
                    append("resolution=merge-duplicates")
                    if (returning) {
                        append(",return=representation")
                    }
                }
                header("Prefer", preferHeader)
                setBody(jsonBody)
            }
            
            if (returning) {
                val responseText = response.bodyAsText()
                
                val jsonElement = json.parseToJsonElement(responseText)
                val result = if (jsonElement is JsonArray) {
                    jsonElement.firstOrNull()
                } else {
                    jsonElement
                }
                Result.success(result)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
