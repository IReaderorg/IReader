package ireader.core.http

import org.koin.dsl.module

/**
 * iOS HTTP module for dependency injection
 */
actual val httpModule: Any = module {
    single { HttpClients() }
    single { BrowserEngine() }
    single { CookieSynchronizer() }
    single { WebViewManger() }
    single { SSLConfiguration() }
}
