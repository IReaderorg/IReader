package ireader.domain.services.tts_service.v2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

/**
 * Koin module for TTS v2 architecture
 * 
 * This module provides the new clean TTS architecture components.
 * It can be used alongside the existing TTS implementation for gradual migration.
 */
val ttsV2Module = module {
    // Content loader - uses existing repositories
    single<TTSContentLoader> {
        TTSContentLoaderImpl(
            bookRepository = get(),
            chapterRepository = get(),
            chapterUseCase = get(),
            remoteUseCases = get(),
            catalogStore = get(),
            contentFilterUseCase = getOrNull() // Optional - applies regex content filtering
        )
    }
    
    // Text merger for remote TTS
    factory { TTSTextMergerV2() }
    
    // Cache use case - wraps existing TTSChapterCache
    // Note: TTSChapterCache is provided by platform-specific modules
    single {
        TTSCacheUseCase(
            chapterCache = get()
        )
    }
    
    // Chunk player for remote TTS with caching
    factory {
        TTSChunkPlayer(
            textMerger = get(),
            cacheUseCase = getOrNull() // Optional - may not be available on all platforms
        )
    }
    
    // TTS Controller - main coordinator
    // Note: Engine factory is platform-specific (expect/actual)
    // Using single instead of factory to maintain state across the app
    // No ChapterController - TTS has its own independent state, sync happens via onPop
    single {
        TTSController(
            contentLoader = get(),
            nativeEngineFactory = { TTSEngineFactory.createNativeEngine() },
            gradioEngineFactory = { config -> TTSEngineFactory.createGradioEngine(config) },
            initialGradioConfig = null, // Can be set via SetGradioConfig command
            cacheUseCase = getOrNull() // Optional - for offline playback of cached audio
        )
    }
    
    // ViewModel adapter for UI layer
    factory { (scope: CoroutineScope) ->
        TTSViewModelAdapter(
            controller = get(),
            scope = scope
        )
    }
    
    // Notification use case - bridges v2 with existing notification system
    // Note: TTSNotificationManager is provided by platform-specific modules
    factory {
        TTSNotificationUseCase(
            notificationManager = getOrNull() ?: ireader.domain.usecases.tts.NoOpTTSNotificationManager()
        )
    }
    
    // Sleep timer use case
    factory { TTSSleepTimerUseCase() }
    
    // Preferences use case - bridges v2 with existing preferences
    factory {
        TTSPreferencesUseCase(
            readerPreferences = get()
        )
    }
    
    // Service starter - platform-specific (provided by platform modules)
    // Note: This is provided by platform-specific modules via expect/actual
}
