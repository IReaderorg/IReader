package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.ui.component.components.TitleText
import ireader.presentation.ui.component.components.TitleToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotImplementedScreen(title: String) {
    val navigator = LocalNavigator.currentOrThrow
    Scaffold(topBar = {
        TitleToolbar(
                title = title,
                popBackStack = {
                    popBackStack(navigator)
                }
        )
    },) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TitleText("Not Implemented Yet")
        }
    }
}