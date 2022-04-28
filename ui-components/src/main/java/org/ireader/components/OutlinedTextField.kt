package org.ireader.explore.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ireader.components.reusable_composable.MidSizeTextComposable

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (text: String) -> Unit,
    onValueConfirm: (text: String) -> Unit,
    paddingLeadingIconEnd: Dp = 0.dp,
    paddingTrailingIconStart: Dp = 0.dp,
    leadingIcon: (@Composable() () -> Unit)? = null,
    trailingIcon: (@Composable() () -> Unit)? = null,
    hint: String = "",
    textColor: Color = MaterialTheme.colors.onBackground,
) {

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = paddingLeadingIconEnd, end = paddingTrailingIconStart)
        ) {
            BasicTextField(
                modifier = Modifier,
                value = value,
                onValueChange = onValueChange,
                maxLines = 1,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colors.onBackground),
                textStyle = TextStyle(color = textColor)
            )
            if (value.isEmpty()) {
                MidSizeTextComposable(
                    text = hint,
                    color = textColor
                )
            }
        }
        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}