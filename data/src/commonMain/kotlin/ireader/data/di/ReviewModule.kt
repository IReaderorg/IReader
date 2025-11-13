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
    
    // Review Use Cases
    factory { GetBookReviewsUseCase(get()) }
    factory { GetChapterReviewsUseCase(get()) }
    factory { SubmitBookReviewUseCase(get()) }
    factory { SubmitChapterReviewUseCase(get()) }
    
    // Badge Use Cases
    factory { GetUserBadgesUseCase(get()) }
}
