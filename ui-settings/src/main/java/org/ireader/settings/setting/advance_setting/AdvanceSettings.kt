package org.ireader.settings.setting.advance_setting

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.ExperimentalSerializationApi
import org.ireader.common_extensions.getCacheSize
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.text_related.TextSection
import org.ireader.settings.setting.SettingViewModel
import org.ireader.ui_settings.R

@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdvanceSettings(
    modifier: Modifier = Modifier,
    vm: SettingViewModel,
    onBackStack: () -> Unit,
    snackBarHostState:SnackbarHostState

) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
    ) {
        TextSection(text =stringResource(R.string.data), toUpper = false)
        PreferenceRow(title = stringResource(id = R.string.clear_all_database), onClick = {
            vm.deleteAllDatabase()
            vm.showSnackBar(
                UiText.StringResource(R.string.database_was_cleared)
            )
        })
        PreferenceRow(title = stringResource(R.string.clear_all_chapters), onClick = {
            vm.deleteAllChapters()
            vm.showSnackBar(UiText.StringResource(R.string.chapters_was_cleared))
        })
        PreferenceRow(
            title = stringResource(R.string.clear_all_cache),
            subtitle = getCacheSize(context = context),
            onClick = {
                context.cacheDir.deleteRecursively()
                vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
            }
        )
        TextSection(text = stringResource(R.string.reset_setting), toUpper = false)
        PreferenceRow(title = stringResource(R.string.reset_reader_screen_settings), onClick = {
            vm.deleteDefaultSettings()
        })
    }
}
