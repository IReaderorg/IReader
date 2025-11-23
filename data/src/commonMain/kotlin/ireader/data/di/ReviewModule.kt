package ireader.data.di

import io.github.jan.supabase.SupabaseClient
import ireader.data.badge.BadgeRepositoryImpl
import ireader.data.review.ReviewRepositoryImpl
import ireader.domain.data.repository.BadgeRepository
import ireader.domain.data.repository.ReviewRepository
import ireader.domain.usecases.badge.GetUserBadgesUseCase
import ireader.domain.usecases.review.GetBookReviewsUseCase
import ireader.domain.usecases.review.GetChapterReviewsUseCase
import ireader.domain.usecases.review.SubmitBookReviewUseCase
import ireader.domain.usecases.review.SubmitChapterReviewUseCase
import org.koin.dsl.module

val reviewModule = module {
    // Repositories - conditionally provide NoOp implementations
    single<ReviewRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.repository.NoOpReviewRepository()
        } else {
            val supabaseClient = (provider as ireader.data.remote.SupabaseClientProviderImpl)
                .getSupabaseClient(ireader.domain.models.remote.SupabaseEndpoint.USERS) as SupabaseClient
            ReviewRepositoryImpl(
                handler = get(),
                supabaseClient = supabaseClient,
                backendService = get()
            )
        }
    }
    
    single<BadgeRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            ireader.data.repository.NoOpBadgeRepository()
        } else {
            val supabaseClient = (provider as ireader.data.remote.SupabaseClientProviderImpl)
                .getSupabaseClient(ireader.domain.models.remote.SupabaseEndpoint.USERS) as SupabaseClient
            BadgeRepositoryImpl(
                handler = get(),
                supabaseClient = supabaseClient,
                backendService = get()
            )
        }
    }

     single<ireader.domain.data.repository.NFTRepository> {
         val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
         if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
             ireader.data.repository.NoOpNFTRepository()
         } else {
             val supabaseClient = (provider as ireader.data.remote.SupabaseClientProviderImpl)
                 .getSupabaseClient(ireader.domain.models.remote.SupabaseEndpoint.USERS) as SupabaseClient
             ireader.data.nft.NFTRepositoryImpl(
                 handler = get(),
                 supabaseClient = supabaseClient,
                 backendService = get()
             )
         }
     }
    
    // Review Use Cases
    factory { GetBookReviewsUseCase(get()) }
    factory { GetChapterReviewsUseCase(get()) }
    factory { SubmitBookReviewUseCase(get()) }
    factory { SubmitChapterReviewUseCase(get()) }
    
    // Badge Use Cases
    factory { GetUserBadgesUseCase(get()) }

     factory { ireader.domain.usecases.badge.GetAvailableBadgesUseCase(get()) }
     factory { ireader.domain.usecases.badge.SubmitPaymentProofUseCase(get()) }
     factory { ireader.domain.usecases.badge.SetPrimaryBadgeUseCase(get()) }
     factory { ireader.domain.usecases.badge.SetFeaturedBadgesUseCase(get()) }

     factory { ireader.domain.usecases.nft.SaveWalletAddressUseCase(get()) }
     factory { ireader.domain.usecases.nft.VerifyNFTOwnershipUseCase(get()) }
     factory { ireader.domain.usecases.nft.GetNFTVerificationStatusUseCase(get()) }
     factory { ireader.domain.usecases.nft.GetNFTMarketplaceUrlUseCase() }
}

