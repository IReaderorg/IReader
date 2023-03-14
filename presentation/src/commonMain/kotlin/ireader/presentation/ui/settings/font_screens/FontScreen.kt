package ireader.presentation.ui.settings.font_screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.reusable_composable.AppIcon

@Composable
fun FontScreen(
        vm: FontScreenViewModel,
        onFont: (String) -> Unit
) {

    Column {
        if (vm.previewMode.value) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = """
                    Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum
                """.trimIndent(),
                maxLines = 5,
                fontFamily = vm.font?.value?.fontFamily
            )
        }

        LazyColumn {
            items(count = vm.uiFonts.size) { index ->
                PreferenceRow(
                    title = vm.uiFonts[index],
                    onClick = {
                        onFont(vm.uiFonts[index])
                    },
                    action = {
                        if (vm.uiFonts[index] == vm.font?.value?.name) {
                            AppIcon(
                                modifier = Modifier.padding(16.dp),
                                imageVector = Icons.Default.Check,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}
