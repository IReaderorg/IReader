package ireader.domain.community.cloudflare

import ireader.core.http.HttpClients
import ireader.domain.community.CommunityPreferences
import ireader.domain.data.repository.RemoteRepository
import org.koin.dsl.module

/**
 * Koin module for Cloudflare-based community translation storage.
 */
val communityTranslationModule = module {
    
    // Cloudflare configuration - built from preferences (user override or platform default)
    factory<CloudflareConfig?> {
        val prefs: CommunityPreferences = get()
        
        if (!prefs.isCloudflareConfigured()) {
            null
        } else {
            CloudflareConfig(
                accountId = prefs.getEffectiveCloudflareAccountId(),
                apiToken = prefs.getEffectiveCloudflareApiToken(),
                d1DatabaseId = prefs.getEffectiveD1DatabaseId(),
                r2BucketName = prefs.getEffectiveR2BucketName(),
                r2PublicUrl = prefs.getEffectiveR2PublicUrl(),
                enableCompression = prefs.cloudflareCompressionEnabled().get()
            )
        }
    }
    
    // D1 Client
    factory<CloudflareD1Client?> {
        val config: CloudflareConfig? = get()
        val httpClients: HttpClients = get()
        
        config?.let { CloudflareD1Client(httpClients.default, it) }
    }
    
    // R2 Client
    factory<CloudflareR2Client?> {
        val config: CloudflareConfig? = get()
        val httpClients: HttpClients = get()
        
        config?.let { CloudflareR2Client(httpClients.default, it) }
    }
    
    // Community Translation Repository
    single<CommunityTranslationRepository?> {
        val config: CloudflareConfig? = get()
        val d1Client: CloudflareD1Client? = get()
        val r2Client: CloudflareR2Client? = get()
        
        if (config != null && d1Client != null && r2Client != null) {
            CommunityTranslationRepository(d1Client, r2Client, config)
        } else {
            null
        }
    }
    
    // Auto-share use case
    single<AutoShareTranslationUseCase> {
        AutoShareTranslationUseCase(
            communityPreferences = get(),
            translationRepository = get(),
            remoteRepository = getOrNull() // Optional - for getting user ID if authenticated
        )
    }
}
