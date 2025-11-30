package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.presentation.core.ui.EmptyScreenAction
import ireader.presentation.core.ui.IReaderEmptyScreen
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyScreenComposable(errorResId: UiText, onPopBackStack: () -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Scaffold(
        topBar = {
            Toolbar(
                title = {},
                navigationIcon = { TopAppBarBackButton(onClick = onPopBackStack) },
            )
        }
    ) { padding ->
        IReaderEmptyScreen(
            message = errorResId.asString(localizeHelper),
            modifier = Modifier.padding(padding),
            actions = listOf(
                EmptyScreenAction(
                    title = localizeHelper.localize(Res.string.go_back),
                    icon = Icons.Default.Refresh,
                    onClick = onPopBackStack
                )
            )
        )
    }
}
