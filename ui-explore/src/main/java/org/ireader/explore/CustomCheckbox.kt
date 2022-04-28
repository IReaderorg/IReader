package org.ireader.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UnCheckedCheckBox(
    isChecked: Boolean?,
    isNullable: Boolean = false,
    onChecked: (Boolean?) -> Unit,
) {
    val next = if (isChecked == null) {
        true
    } else if (isChecked == true && !isNullable) {
        null
    } else if (isChecked == true && isNullable) {
        false
    } else {
        null
    }
    Box(
        contentAlignment = Alignment.Center,
    )
    {
        Checkbox(
            checked = isChecked != null,
            onCheckedChange = {
                onChecked(next)
            },
            colors = if (isChecked == false) CheckboxDefaults.colors(
                checkmarkColor = Color.Black.copy(.0f),
                checkedColor = Color.Black.copy(.2f),
            ) else CheckboxDefaults.colors()
        )
        if (isChecked == false) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "", tint = Color.White)
        }

    }
}

@Preview
@Composable
fun CheckBoxPrev() {

    var state by remember {
        mutableStateOf<Boolean?>(null)
    }

    UnCheckedCheckBox(
        isChecked = state,
        onChecked = {
            state = it
        },
        isNullable = true)
}