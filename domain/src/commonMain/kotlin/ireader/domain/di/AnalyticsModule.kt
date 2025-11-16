package ireader.domain.di

import ireader.domain.analytics.*
import org.koin.dsl.module

/**
 * Koin module for analytics components
 */
val analyticsModule = module {
    // Analytics Manager (singleton)
    single {
        AnalyticsManager(privacyMode = PrivacyMode.BALANCED)
    }
    
    // Network Analytics Interceptor
    single {
        NetworkAnalyticsInterceptor(analyticsManager = get())
    }
    
    // Database Analytics Tracker
    single {
        DatabaseAnalyticsTracker(analyticsManager = get())
    }
    
    // UI Performance Tracker
    single {
        UIPerformanceTracker(analyticsManager = get())
    }
}
