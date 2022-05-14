package org.ireader.settings.setting.backups

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.common_resources.UiText
import org.ireader.components.components.ISnackBarHost
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.settings.setting.SettingsSection
import org.ireader.settings.setting.SetupLayout
import org.ireader.ui_settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackUpAndRestoreScreen(
    items: List<SettingsSection>,
    onBackStack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
        Toolbar(
            title = {
                BigSizeTextComposable(text = UiText.StringResource(R.string.backup_and_restore))
            },
            navigationIcon = { TopAppBarBackButton(onClick = onBackStack) },
        )
    },
        snackbarHost = { ISnackBarHost(snackBarHostState = snackbarHostState)}
    ) { padding ->
        SetupLayout(padding, items)
    }
}