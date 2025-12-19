package ireader.data.di

import ireader.data.pluginreview.NoOpPluginReviewRepository
import ireader.data.pluginreview.PluginReviewRepositoryImpl
import ireader.domain.data.repository.PluginReviewRepository
import org.koin.dsl.module

val pluginReviewModule = module {
    single<PluginReviewRepository> {
        val provider = get<ireader.domain.data.repository.SupabaseClientProvider>()
        if (provider is ireader.data.remote.NoOpSupabaseClientProvider) {
            // Use NoOp singleton when Supabase is not configured
            NoOpPluginReviewRepository
        } else {
            // Use bookReviewsClient which has Auth installed (reuse for plugin reviews)
            val supabaseClient = (provider as ireader.data.remote.MultiSupabaseClientProvider).bookReviewsClient
            PluginReviewRepositoryImpl(
                supabaseClient = supabaseClient,
                backendService = get()
            )
        }
    }
}
