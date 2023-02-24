package ireader.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.awaitApplication
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.DelicateCoroutinesApi
import java.lang.reflect.Modifier
import kotlin.system.exitProcess


@OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class)
suspend fun main() {
    awaitApplication {
        val state = rememberWindowState()
        Window(
            onCloseRequest = { exitProcess(0) },
            title = "IReader",
            state = state
        ) {

            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Text("Library")
                    })
                }
            ) {
                Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("IReader")
                }
            }

        }
    }
}


//fun main() = application {
//    val state = rememberWindowState()
//    Window(
//        onCloseRequest = { exitProcess(0) },
//        title = localize(MR.strings.app_name),
//        state = state
//    ) {
//        val window = remember { tachiyomi.ui.core.providers.Window() }
//        LaunchedEffect(state) {
//            snapshotFlow { state.size }
//                .collect { window.setSize(it.width, it.height) }
//        }
//        CompositionLocalProvider(LocalWindow provides window) {
//            MainApp()
//        }
//    }
//}
