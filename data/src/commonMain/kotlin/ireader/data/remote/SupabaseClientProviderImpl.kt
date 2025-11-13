package ireader.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import ireader.domain.data.repository.SupabaseClientProvider
import ireader.domain.models.remote.SupabaseConfig
import ireader.domain.models.remote.SupabaseEndpoint
import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Implementation of SupabaseClientProvider that manages multiple Supabase clients
 * Each endpoint can have its own Supabase project for better scalability
 */
class SupabaseClientProviderImpl(
    private val config: SupabaseConfig
) : SupabaseClientProvider {
    
    // Cache for Supabase clients
    private val clients = mutableMapOf<SupabaseEndpoint, SupabaseClient>()
    
    init {
        // Initialize clients for configured endpoints
        initializeClients()
    }
    
    private fun initializeClients() {
        try {
            // Always initialize users endpoint (required)
            clients[SupabaseEndpoint.USERS] = createClient(SupabaseEndpoint.USERS)
            Log.info("Initialized Supabase client for USERS endpoint")
            
            // Initialize books endpoint if configured
            val booksConfig = config.books
            if (booksConfig != null && booksConfig.enabled) {
                clients[SupabaseEndpoint.BOOKS] = createClient(SupabaseEndpoint.BOOKS)
                Log.info("Initialized Supabase client for BOOKS endpoint")
            }
            
            // Initialize progress endpoint if configured
            val progressConfig = config.progress
            if (progressConfig != null && progressConfig.enabled) {
                clients[SupabaseEndpoint.PROGRESS] = createClient(SupabaseEndpoint.PROGRESS)
                Log.info("Initialized Supabase client for PROGRESS endpoint")
            }
            
            // Initialize reviews endpoint if configured
            val reviewsConfig = config.reviews
            if (reviewsConfig != null && reviewsConfig.enabled) {
                clients[SupabaseEndpoint.REVIEWS] = createClient(SupabaseEndpoint.REVIEWS)
                Log.info("Initialized Supabase client for REVIEWS endpoint")
            }
            
            // Initialize community endpoint if configured
            val communityConfig = config.community
            if (communityConfig != null && communityConfig.enabled) {
                clients[SupabaseEndpoint.COMMUNITY] = createClient(SupabaseEndpoint.COMMUNITY)
                Log.info("Initialized Supabase client for COMMUNITY endpoint")
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to initialize Supabase clients")
        }
    }
    
    private fun createClient(endpoint: SupabaseEndpoint): SupabaseClient {
        val endpointConfig = config.getEndpointConfig(endpoint)
        
        return createSupabaseClient(
            supabaseUrl = endpointConfig.url,
            supabaseKey = endpointConfig.anonKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
    
    override fun getClient(endpoint: SupabaseEndpoint): Any {
        // Return cached client or fall back to users endpoint
        return clients[endpoint] ?: clients[SupabaseEndpoint.USERS]
            ?: throw IllegalStateException("No Supabase client available")
    }
    
    override fun isEndpointAvailable(endpoint: SupabaseEndpoint): Boolean {
        return clients.containsKey(endpoint) && config.isEndpointEnabled(endpoint)
    }
    
    /**
     * Get typed Supabase client
     */
    fun getSupabaseClient(endpoint: SupabaseEndpoint): SupabaseClient {
        return getClient(endpoint) as SupabaseClient
    }
    
    /**
     * Close all clients (cleanup)
     * Should be called from a coroutine scope
     */
    fun closeAll() {
        CoroutineScope(Dispatchers.Default).launch {
            clients.values.forEach { client ->
                try {
                    client.close()
                } catch (e: Exception) {
                    Log.error(e, "Failed to close Supabase client")
                }
            }
            clients.clear()
        }
    }
}
