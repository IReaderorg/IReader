package ireader.domain.di


import io.ktor.client.plugins.cookies.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton

val localModule = DI.Module("localModule") {

    bindSingleton<CookiesStorage> { AcceptAllCookiesStorage() }

    bindProvider<CoroutineScope> { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
}