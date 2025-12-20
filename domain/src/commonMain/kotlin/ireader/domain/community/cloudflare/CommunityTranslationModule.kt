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
        val config = CloudflareConfig(
            accountId = prefs.getEffectiveCloudflareAccountId(),
            apiToken = prefs.getEffectiveCloudflareApiToken(),
            d1DatabaseId = prefs.getEffectiveD1DatabaseId(),
            r2BucketName = prefs.getEffectiveR2BucketName(),
            r2PublicUrl = prefs.getEffectiveR2PublicUrl(),
            r2S3Endpoint = prefs.getEffectiveR2S3Endpoint(),
            r2AccessKeyId = prefs.getEffectiveR2AccessKeyId(),
            r2SecretAccessKey = prefs.getEffectiveR2SecretAccessKey(),
            enableCompression = prefs.cloudflareCompressionEnabled().get()
        )
        println("[CommunityTranslationModule] CloudflareConfig created:")
        println("[CommunityTranslationModule]   accountId = ${config.accountId.take(10)}...")
        println("[CommunityTranslationModule]   apiToken = ${config.apiToken.take(10)}...")
        println("[CommunityTranslationModule]   d1DatabaseId = ${config.d1DatabaseId.take(10)}...")
        println("[CommunityTranslationModule]   r2BucketName = ${config.r2BucketName}")
        println("[CommunityTranslationModule]   isValid = ${config.isValid()}")
        println("[CommunityTranslationModule]   isR2Configured = ${config.isR2Configured()}")
        config
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
