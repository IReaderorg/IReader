package ireader.data.remote

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Extension functions to safely decode Supabase responses without using Kotlin reflection.
 * 
 * This avoids the KotlinReflectionInternalError that occurs with Kotlin 2.x when
 * Supabase tries to resolve java.util.List through reflection.
 */

internal val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Safely decode a list response using explicit serializer
 */
suspend fun <T> PostgrestResult.decodeListSafe(serializer: KSerializer<T>): List<T> {
    val bodyText = this.data
    return json.decodeFromString(ListSerializer(serializer), bodyText)
}

/**
 * Safely decode a single item response using explicit serializer
 */
suspend fun <T> PostgrestResult.decodeSingleSafe(serializer: KSerializer<T>): T {
    val bodyText = this.data
    val list = json.decodeFromString(ListSerializer(serializer), bodyText)
    return list.first()
}

/**
 * Safely decode a single item or null response using explicit serializer
 */
suspend fun <T> PostgrestResult.decodeSingleOrNullSafe(serializer: KSerializer<T>): T? {
    val bodyText = this.data
    val list = json.decodeFromString(ListSerializer(serializer), bodyText)
    return list.firstOrNull()
}

/**
 * Helper to encode data to JSON string for insert/update operations
 */
fun <T> encodeToJsonString(value: T, serializer: KSerializer<T>): String {
    return json.encodeToString(serializer, value)
}

/**
 * Safely insert data without using reflection - returns the result directly
 */
suspend fun <T : Any> insertSafe(
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    table: String,
    value: T,
    serializer: KSerializer<T>
): String {
    val jsonBody = json.encodeToString(serializer, value)
    
    // Get user access token if available
    val accessToken = supabaseClient.auth.currentAccessTokenOrNull()
    
    // Ensure URL has protocol
    val baseUrl = if (supabaseClient.supabaseUrl.startsWith("http")) {
        supabaseClient.supabaseUrl
    } else {
        "https://${supabaseClient.supabaseUrl}"
    }
    
    val response = supabaseClient.httpClient.post("$baseUrl/rest/v1/$table") {
        header("Content-Type", "application/json")
        header("Prefer", "return=representation")
        header("apikey", supabaseClient.supabaseKey)
        if (accessToken != null) {
            header("Authorization", "Bearer $accessToken")
        }
        setBody(jsonBody)
    }
    
    return response.bodyAsText()
}

/**
 * Safely update data without using reflection - returns the result directly
 */
suspend fun <T : Any> updateSafe(
    supabaseClient: io.github.jan.supabase.SupabaseClient,
    table: String,
    value: T,
    serializer: KSerializer<T>,
    filterQuery: String
): String {
    val jsonBody = json.encodeToString(serializer, value)
    
    // Get user access token if available
    val accessToken = supabaseClient.auth.currentAccessTokenOrNull()
    
    // Ensure URL has protocol
    val baseUrl = if (supabaseClient.supabaseUrl.startsWith("http")) {
        supabaseClient.supabaseUrl
    } else {
        "https://${supabaseClient.supabaseUrl}"
    }
    
    val response = supabaseClient.httpClient.patch("$baseUrl/rest/v1/$table?$filterQuery") {
        header("Content-Type", "application/json")
        header("Prefer", "return=representation")
        header("apikey", supabaseClient.supabaseKey)
        if (accessToken != null) {
            header("Authorization", "Bearer $accessToken")
        }
        setBody(jsonBody)
    }
    
    return response.bodyAsText()
}

/**
 * Decode a JSON string to a single object
 */
fun <T> String.decodeSingleFromJson(serializer: KSerializer<T>): T {
    val list = json.decodeFromString(ListSerializer(serializer), this)
    return list.first()
}
