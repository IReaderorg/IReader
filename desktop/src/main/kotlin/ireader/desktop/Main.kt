package ireader.desktop

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import ireader.data.di.dataPlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.domain.di.*
import ireader.presentation.core.DefaultNavigatorScreenTransition
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import ireader.presentation.core.theme.AppTheme
import org.ireader.app.di.DataModule
import org.kodein.di.DI
import org.kodein.di.compose.withDI
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
suspend fun main() {
    val di = DI.lazy {
        importAll(localModule,dataPlatformModule, CatalogModule, DataModule,preferencesInjectModule,
                repositoryInjectModule, UseCasesInject, PresentationModules,DomainServices,DomainModule,presentationPlatformModule)
    }
    awaitApplication {
        val state = rememberWindowState()
        Window(
                onCloseRequest = { exitProcess(0) },
                title = "IReader",
                state = state
        ) {
            val scope = rememberCoroutineScope()
            withDI(di) {
                AppTheme(scope) {
                    Navigator(
                            screen = ireader.presentation.core.MainStarterScreen,
                            disposeBehavior = NavigatorDisposeBehavior(
                                    disposeNestedNavigators = false,
                                    disposeSteps = true
                            ),
                    ) { navigator ->
                        DefaultNavigatorScreenTransition(navigator = navigator)
                    }
                }

            }
        }
    }
}
