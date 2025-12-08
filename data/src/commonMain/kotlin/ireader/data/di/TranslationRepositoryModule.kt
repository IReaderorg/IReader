package ireader.data.di

import ireader.data.translation.GlossaryRepositoryImpl
import ireader.data.translation.TranslatedChapterRepositoryImpl
import ireader.domain.data.repository.GlossaryRepository
import ireader.domain.data.repository.TranslatedChapterRepository
import org.koin.dsl.module

/**
 * DI module for translation-related repositories.
 * Contains TranslatedChapterRepository and GlossaryRepository.
 */
val translationRepositoryModule = module {
    single<TranslatedChapterRepository> { TranslatedChapterRepositoryImpl(get()) }
    single<GlossaryRepository> { GlossaryRepositoryImpl(get()) }
}
