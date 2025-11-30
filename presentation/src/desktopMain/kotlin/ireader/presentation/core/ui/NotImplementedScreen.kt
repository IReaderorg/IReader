package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.presentation.ui.component.components.TitleText
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotImplementedScreen(title: String) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
    Scaffold(topBar = {
        TitleToolbar(
                title = title,
                popBackStack = {
                    navController.popBackStack()
                }
        )
    },) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(localizeHelper.localize(Res.string.not_implemented_yet))
        }
    }
}