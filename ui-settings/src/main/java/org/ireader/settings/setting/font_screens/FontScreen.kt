package org.ireader.settings.setting.font_screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.reusable_composable.AppIcon
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.Roboto

@Composable
fun FontScreen(
    vm: FontScreenViewModel
) {

    LazyColumn {
        items(count = vm.fonts.size) { index ->
            PreferenceRow(
                title = vm.fonts[index].fontName,
                onClick = {
                    vm.readerPreferences.font()
                        .set(FontType(vm.fonts[index].fontName, Roboto.fontFamily))
                },
                action = {
                    if (vm.fonts[index].fontName == vm.font.value.fontName) {
                        AppIcon(
                            imageVector = Icons.Default.Check,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    }
}