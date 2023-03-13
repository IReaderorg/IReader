package ireader.presentation.core.di

import ireader.presentation.core.PlatformHelper
import ireader.presentation.core.theme.LocaleHelper
import org.kodein.di.DI
import org.kodein.di.bindSingleton

actual val presentationPlatformModule: DI.Module = DI.Module("desktopPresentationModule") {
    bindSingleton { LocaleHelper() }
    bindSingleton { PlatformHelper() }
}