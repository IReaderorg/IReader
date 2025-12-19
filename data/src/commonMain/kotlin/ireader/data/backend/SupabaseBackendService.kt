package ireader.data.backend

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import kotlinx.serialization.json.*
import io.ktor.http.encodeURLParameter

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
                val encodedValue = value.toString().encodeURLParameter()
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
            val baseUrl = client.supabaseUrl.let { url ->
                when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> "https://$url"
                }
            }.trimEnd('/')
            
            val url = "$baseUrl/rest/v1/$table?$queryString"
            
            Log.debug("Supabase query: $url")
            
            val response = client.httpClient.get(url) {
                header("Content-Type", "application/json")
                header("apikey", client.supabaseKey)
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
            }
            
            val responseText = response.bodyAsText()
            Log.debug("Supabase query response (${response.status}): ${responseText.take(200)}")
            
            // Check for error responses
            if (!response.status.isSuccess()) {
                Log.error("Supabase query failed with status ${response.status}: $responseText")
                throw Exception("API request failed: ${response.status} - $responseText")
            }
            
            // Parse raw JSON response - NO REFLECTION
            val jsonElement = json.parseToJsonElement(responseText)
            val jsonArray = when (jsonElement) {
                is JsonArray -> jsonElement
                is JsonObject -> {
                    // If it's an error object, throw
                    if (jsonElement.containsKey("error") || jsonElement.containsKey("message")) {
                        val errorMsg = jsonElement["message"]?.toString() ?: jsonElement["error"]?.toString() ?: "Unknown error"
                        Log.error("Supabase query error: $errorMsg")
                        throw Exception("API error: $errorMsg")
                    }
                    // Otherwise wrap in array
                    buildJsonArray { add(jsonElement) }
                }
                else -> buildJsonArray { }
            }
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
            val baseUrl = client.supabaseUrl.let { url ->
                when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> "https://$url"
                }
            }.trimEnd('/')
            
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
            
            // Check for error responses
            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                Log.error("Supabase insert failed with status ${response.status}: $errorText")
                throw Exception("API request failed: ${response.status} - $errorText")
            }
            
            if (returning) {
                val responseText = response.bodyAsText()
                
                // Supabase returns an array even for single inserts
                val jsonElement = json.parseToJsonElement(responseText)
                
                // Check for error in response
                if (jsonElement is JsonObject && (jsonElement.containsKey("error") || jsonElement.containsKey("message"))) {
                    val errorMsg = jsonElement["message"]?.toString() ?: jsonElement["error"]?.toString() ?: "Unknown error"
                    Log.error("Supabase insert error: $errorMsg")
                    throw Exception("API error: $errorMsg")
                }
                
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
                val encodedValue = value.toString().encodeURLParameter()
                "$key=eq.$encodedValue"
            }
            
            // Use raw HTTP request to avoid reflection
            val jsonBody = data.toString()
            val accessToken = client.auth.currentAccessTokenOrNull()
            val baseUrl = client.supabaseUrl.let { url ->
                when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> "https://$url"
                }
            }.trimEnd('/')
            
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
            
            // Check for error responses
            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                Log.error("Supabase update failed with status ${response.status}: $errorText")
                throw Exception("API request failed: ${response.status} - $errorText")
            }
            
            if (returning) {
                val responseText = response.bodyAsText()
                
                // Supabase returns an array even for single updates
                val jsonElement = json.parseToJsonElement(responseText)
                
                // Check for error in response
                if (jsonElement is JsonObject && (jsonElement.containsKey("error") || jsonElement.containsKey("message"))) {
                    val errorMsg = jsonElement["message"]?.toString() ?: jsonElement["error"]?.toString() ?: "Unknown error"
                    Log.error("Supabase update error: $errorMsg")
                    throw Exception("API error: $errorMsg")
                }
                
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
            val baseUrl = client.supabaseUrl.let { url ->
                when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> "https://$url"
                }
            }.trimEnd('/')
            
            val response = client.httpClient.post("$baseUrl/rest/v1/rpc/$function") {
                header("Content-Type", "application/json")
                header("apikey", client.supabaseKey)
                if (accessToken != null) {
                    header("Authorization", "Bearer $accessToken")
                }
                setBody(jsonBody)
            }
            
            // Check for error responses
            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                Log.error("Supabase RPC failed with status ${response.status}: $errorText")
                throw Exception("API request failed: ${response.status} - $errorText")
            }
            
            // Parse raw JSON response
            val responseText = response.bodyAsText()
            val jsonElement = json.parseToJsonElement(responseText)
            
            // Check for error in response
            if (jsonElement is JsonObject && (jsonElement.containsKey("error") || jsonElement.containsKey("message"))) {
                val errorMsg = jsonElement["message"]?.toString() ?: jsonElement["error"]?.toString() ?: "Unknown error"
                Log.error("Supabase RPC error: $errorMsg")
                throw Exception("API error: $errorMsg")
            }
            
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
            val baseUrl = client.supabaseUrl.let { url ->
                when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> "https://$url"
                }
            }.trimEnd('/')
            
            // Build URL with on_conflict parameter if specified
            val url = buildString {
                append("$baseUrl/rest/v1/$table")
                if (onConflict != null) {
                    append("?on_conflict=$onConflict")
                }
            }
            
            Log.debug("Supabase upsert to $url: $jsonBody")
            
            val response = client.httpClient.post(url) {
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
            
            Log.debug("Supabase upsert response (${response.status})")
            
            // Check for error responses
            if (!response.status.isSuccess()) {
                val errorText = response.bodyAsText()
                Log.error("Supabase upsert failed with status ${response.status}: $errorText")
                throw Exception("API request failed: ${response.status} - $errorText")
            }
            
            if (returning) {
                val responseText = response.bodyAsText()
                Log.debug("Supabase upsert response body: ${responseText.take(200)}")
                
                val jsonElement = json.parseToJsonElement(responseText)
                
                // Check for error in response
                if (jsonElement is JsonObject && (jsonElement.containsKey("error") || jsonElement.containsKey("message"))) {
                    val errorMsg = jsonElement["message"]?.toString() ?: jsonElement["error"]?.toString() ?: "Unknown error"
                    Log.error("Supabase upsert error: $errorMsg")
                    throw Exception("API error: $errorMsg")
                }
                
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
