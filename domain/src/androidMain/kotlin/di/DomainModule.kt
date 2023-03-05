package ireader.domain.di

import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnImpl
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

actual val DomainModule: DI.Module = DI.Module("domainModulePlatform") {
    bindProvider<ScreenAlwaysOn> {
        ScreenAlwaysOnImpl(instance())
    }
}