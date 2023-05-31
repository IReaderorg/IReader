package ireader.domain.di


import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val localModule = module {

    single<CookiesStorage> { AcceptAllCookiesStorage() }

    factory <CoroutineScope> { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
}