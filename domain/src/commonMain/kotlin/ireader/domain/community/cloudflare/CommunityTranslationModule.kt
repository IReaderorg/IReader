package ireader.domain.community.cloudflare

import ireader.core.http.HttpClients
import ireader.domain.community.CommunityPreferences
import ireader.domain.data.repository.RemoteRepository
import org.koin.dsl.module

/**
 * Koin module for Cloudflare-based community translation storage.
 */
val communityTranslationModule = module {
    
    // Cloudflare configuration - built from preferences
    single<CloudflareConfig> {
        val prefs: CommunityPreferences = get()
        CloudflareConfig(
            accountId = prefs.getEffectiveCloudflareAccountId(),
            apiToken = prefs.getEffectiveCloudflareApiToken(),
            d1DatabaseId = prefs.getEffectiveD1DatabaseId(),
            r2BucketName = prefs.getEffectiveR2BucketName(),
            r2PublicUrl = prefs.getEffectiveR2PublicUrl(),
            enableCompression = prefs.cloudflareCompressionEnabled().get()
        )
    }
    
    // D1 Client
    single<CloudflareD1Client> {
        val config: CloudflareConfig = get()
        val httpClients: HttpClients = get()
        CloudflareD1Client(httpClients.default, config)
    }
    
    // R2 Client
    single<CloudflareR2Client> {
        val config: CloudflareConfig = get()
        val httpClients: HttpClients = get()
        CloudflareR2Client(httpClients.default, config)
    }
    
    // Community Translation Repository
    single<CommunityTranslationRepository> {
        val config: CloudflareConfig = get()
        val d1Client: CloudflareD1Client = get()
        val r2Client: CloudflareR2Client = get()
        CommunityTranslationRepository(d1Client, r2Client, config)
    }
    
    // Auto-share use case
    single<AutoShareTranslationUseCase> {
        AutoShareTranslationUseCase(
            communityPreferences = get(),
            translationRepository = get(),
            remoteRepository = getOrNull() // Only this one is optional - for user auth
        )
    }
}
