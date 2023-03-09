package ireader.domain.di

import androidx.compose.ui.text.ExperimentalTextApi
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.usecases.file.AndroidFileSaver
import ireader.domain.usecases.preferences.*
import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import org.kodein.di.*

@OptIn(ExperimentalTextApi::class)
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
    bindSingleton<PlatformUiPreferences> {
        new(::AndroidUiPreferences)
    }
}