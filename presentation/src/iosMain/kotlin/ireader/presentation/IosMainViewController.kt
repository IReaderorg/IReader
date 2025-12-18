@file:OptIn(kotlin.experimental.ExperimentalObjCName::class)

package ireader.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import ireader.i18n.LocalizeHelper
import ireader.presentation.core.CommonNavHost
import ireader.presentation.core.ProvideNavigator
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Object to expose iOS entry points with clean Swift names.
 */
@ObjCName("IosMainViewControllerKt")
object IosMainViewController {
    /**
     * Creates the main UIViewController for the iOS app that hosts Compose UI.
     * This is called from Swift to get the Compose-based UI.
     */
    @ObjCName("MainViewController")
    fun MainViewController(): UIViewController = ComposeUIViewController {
        IReaderApp()
    }
}

/**
 * The main composable entry point for the iOS app.
 * This wraps the shared presentation layer with full navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IReaderApp() {
    KoinContext {
        val scope = rememberCoroutineScope()
        val localizeHelper: LocalizeHelper = koinInject()
        
        CompositionLocalProvider(
            LocalLocalizeHelper provides localizeHelper
        ) {
            AppTheme(scope) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    val navController = rememberNavController()
                    
                    ProvideNavigator(navController) {
                        IScaffold {
                            CommonNavHost(navController)
                            // Required plugin handler - shows dialog when JS engine or Piper TTS is needed
                            ireader.presentation.ui.plugins.required.RequiredPluginHandler()
                        }
                    }
                }
            }
        }
    }
}
