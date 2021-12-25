package ir.kazemcodes.infinity.presentation.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtonWithTitleComposable(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        RadioButton(selected = selected, onClick = { onClick() })
        Spacer(modifier = modifier.width(8.dp))
        Text(text = text)
    }
}