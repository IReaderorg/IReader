package ireader.domain.di

import ireader.domain.usecases.file.AndroidFileSaver
import ireader.domain.usecases.preferences.*
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import org.kodein.di.*

actual val DomainModule: DI.Module = DI.Module("domainModulePlatform") {
    bindProvider<ScreenAlwaysOn> {
        ScreenAlwaysOnImpl(instance())
    }
    bindSingleton {
        new(::AndroidFileSaver)
    }
    bindSingleton {
        AndroidReaderPrefUseCases(
                selectedFontStateUseCase = SelectedFontStateUseCase(instance(),instance()),
                backgroundColorUseCase = BackgroundColorUseCase(instance()),
                textAlignmentUseCase = TextAlignmentUseCase(instance()),
                textColorUseCase = TextColorUseCase(instance())
        )
    }
}