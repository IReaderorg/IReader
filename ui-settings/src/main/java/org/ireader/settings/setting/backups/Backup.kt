package org.ireader.settings.setting.backups

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.settings.setting.SettingsSection
import org.ireader.settings.setting.SetupLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackUpAndRestoreScreen(
    modifier : Modifier = Modifier,
    items: List<SettingsSection>,
    onBackStack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    SetupLayout(items = items, modifier = modifier)
}