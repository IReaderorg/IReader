package ireader.domain.di

import ireader.domain.usecases.reader.ScreenAlwaysOn
import ireader.domain.usecases.reader.ScreenAlwaysOnInterface
import org.kodein.di.DI
import org.kodein.di.bindProvider


actual val DomainModule: DI.Module = org.kodein.di.DI.Module("domainModulePlatform") {
    bindProvider<ScreenAlwaysOnInterface> {
        ScreenAlwaysOn()
    }
}