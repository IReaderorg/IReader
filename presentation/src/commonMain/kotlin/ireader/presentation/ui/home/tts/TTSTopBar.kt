package ireader.presentation.ui.home.tts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSTopBar(
    modifier: Modifier = Modifier,
    onPopBackStack:() -> Unit,
    onContent:() -> Unit,
    onSetting:() -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    title:String,
    subtitle:String
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    Toolbar(
        title = {
                PreferenceRow(title = title, subtitle = subtitle)
        },
        applyInsets = true,
        contentColor = MaterialTheme.colorScheme.onBackground,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = {
                onContent()
            }) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = localizeHelper.localize(MR.strings.content),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = {
                onSetting()
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = localizeHelper.localize(MR.strings.settings),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        },
        scrollBehavior = scrollBehavior
    )
}