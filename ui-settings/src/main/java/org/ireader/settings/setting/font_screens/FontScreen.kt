package org.ireader.settings.setting.font_screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.reusable_composable.AppIcon
import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.Roboto

@Composable
fun FontScreen(
    vm: FontScreenViewModel
) {

    LazyColumn {
        items(count = vm.uiFonts.size) { index ->
            PreferenceRow(
                title = vm.uiFonts[index].fontName,
                onClick = {
                    vm.readerPreferences.font()
                        .set(FontType(vm.uiFonts[index].fontName, Roboto.fontFamily))
                },
                action = {
                    if (vm.uiFonts[index].fontName == vm.font.value.fontName) {
                        AppIcon(
                            modifier = Modifier.padding(16.dp) ,
                            imageVector = Icons.Default.Check,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    }
}