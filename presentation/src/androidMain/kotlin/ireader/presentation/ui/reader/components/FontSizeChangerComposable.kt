package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.ui.string
import ireader.presentation.R


@Composable
fun SettingItemComposable(
    text: String,
    value: String,
    onAdd: () -> Unit,
    onMinus: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.width(100.dp),
            text = text,
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconButton(
                imageVector = Icons.Default.Remove,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = UiText.DynamicString(
                    string(id = R.string.decrease) + text
                ).asString(LocalContext.current),
                onClick = { onMinus() }
            )
            MidSizeTextComposable(text = value)
            AppIconButton(
                imageVector = Icons.Default.Add,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = UiText.DynamicString(
                    string(id = R.string.increase) + text
                ).asString(LocalContext.current),
                onClick = { onAdd() }
            )
        }
    }
}

@Composable
fun SettingItemToggleComposable(
    text: UiText,
    value: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.width(100.dp),
            text = text.asString(LocalContext.current),
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(checked = value, onCheckedChange = onToggle)
    }
}
