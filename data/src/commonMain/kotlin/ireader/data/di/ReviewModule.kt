package ireader.data.di

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
    // Repositories
    single<ReviewRepository> { 
        ReviewRepositoryImpl(
            handler = get(),
            supabaseClient = get()
        )
    }
    
    single<BadgeRepository> {
        BadgeRepositoryImpl(
            handler = get(),
            supabaseClient = get()
        )
    }
    
    // NFT Repository - Uncomment when NFTRepositoryImpl is implemented (Task 4)
    // NOTE: Requires NFTRepositoryImpl class to be created in data/src/commonMain/kotlin/ireader/data/nft/
    // single<ireader.domain.data.repository.NFTRepository> {
    //     ireader.data.nft.NFTRepositoryImpl(
    //         handler = get(),
    //         supabaseClient = get()
    //     )
    // }
    
    // Review Use Cases
    factory { GetBookReviewsUseCase(get()) }
    factory { GetChapterReviewsUseCase(get()) }
    factory { SubmitBookReviewUseCase(get()) }
    factory { SubmitChapterReviewUseCase(get()) }
    
    // Badge Use Cases
    factory { GetUserBadgesUseCase(get()) }
    
    // Additional Badge Use Cases - Uncomment when implemented (Task 7)
    // NOTE: Requires use case classes to be created in domain/src/commonMain/kotlin/ireader/domain/usecases/badge/
    // factory { ireader.domain.usecases.badge.GetAvailableBadgesUseCase(get()) }
    // factory { ireader.domain.usecases.badge.SubmitPaymentProofUseCase(get()) }
    // factory { ireader.domain.usecases.badge.SetPrimaryBadgeUseCase(get()) }
    // factory { ireader.domain.usecases.badge.SetFeaturedBadgesUseCase(get()) }
    
    // NFT Use Cases - Uncomment when implemented (Task 8)
    // NOTE: Requires use case classes to be created in domain/src/commonMain/kotlin/ireader/domain/usecases/nft/
    // factory { ireader.domain.usecases.nft.SaveWalletAddressUseCase(get()) }
    // factory { ireader.domain.usecases.nft.VerifyNFTOwnershipUseCase(get()) }
    // factory { ireader.domain.usecases.nft.GetNFTVerificationStatusUseCase(get()) }
    // factory { ireader.domain.usecases.nft.GetNFTMarketplaceUrlUseCase() }
}

