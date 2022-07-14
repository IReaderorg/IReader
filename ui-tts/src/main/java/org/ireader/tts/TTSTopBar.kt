package org.ireader.tts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.components.components.Toolbar
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.ui_tts.R

@Composable
fun TTSTopBar(
    modifier: Modifier = Modifier,
    onPopBackStack:() -> Unit,
    onContent:() -> Unit,
    onSetting:() -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    vm:TTSViewModel
) {
    Toolbar(
        title = {
                PreferenceRow(title = vm.ttsChapter?.name?:"", subtitle = vm.ttsBook?.title?:"")
        },
        applyInsets = true,
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = {
                onSetting()
            }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.settings),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = {
                onContent()
            }) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = stringResource(id = R.string.content),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        }
    )
}