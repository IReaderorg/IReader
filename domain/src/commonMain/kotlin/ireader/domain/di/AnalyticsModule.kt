package ireader.domain.di

import ireader.domain.analytics.*
import org.koin.dsl.module

/**
 * Koin module for analytics components
 */
val analyticsModule = module {
    // All analytics components are lazy-loaded since they're not needed at startup
    // This reduces startup time and memory pressure
    
    // Analytics Manager
    factory {
        AnalyticsManager(privacyMode = PrivacyMode.BALANCED)
    }
    
    // Network Analytics Interceptor
    factory {
        NetworkAnalyticsInterceptor(analyticsManager = get())
    }
    
    // Database Analytics Tracker
    factory {
        DatabaseAnalyticsTracker(analyticsManager = get())
    }
    
    // UI Performance Tracker
    factory {
        UIPerformanceTracker(analyticsManager = get())
    }
}
