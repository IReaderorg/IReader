package ireader.ui.home.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
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
    ) {
        androidx.compose.material3.Checkbox(
            checked = isChecked != null,
            onCheckedChange = {
                onChecked(next)
            },
            colors = if (isChecked == false) androidx.compose.material3.CheckboxDefaults.colors(
                checkmarkColor = Color.Black.copy(.0f),
                checkedColor = Color.Black.copy(.2f),
            ) else androidx.compose.material3.CheckboxDefaults.colors()
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
        isNullable = true
    )
}
