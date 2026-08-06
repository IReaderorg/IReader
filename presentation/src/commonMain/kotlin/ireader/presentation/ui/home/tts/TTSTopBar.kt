package ireader.presentation.ui.home.tts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.content
import ireader.i18n.resources.settings
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.Toolbar
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
    subtitle:String,
    isLoading: Boolean = false,
    currentEngine: String = ""
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Toolbar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PreferenceRow(title = title, subtitle = subtitle, modifier = Modifier.weight(1f, fill = false))
                if (isLoading && currentEngine.contains("Gradio", ignoreCase = true)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
                    contentDescription = localizeHelper.localize(Res.string.content),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = {
                onSetting()
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = localizeHelper.localize(Res.string.settings),
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
