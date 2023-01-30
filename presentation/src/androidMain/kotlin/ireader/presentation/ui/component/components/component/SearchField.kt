package ireader.presentation.ui.component.components.component

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchField(
    modifier: Modifier = Modifier,
    query: String,
    onChangeQuery: (String) -> Unit,
    onDone: KeyboardActionScope.() -> Unit
) {
    BasicTextField(
        query,
        onChangeQuery,
        modifier = modifier,
        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
        cursorBrush = SolidColor(LocalContentColor.current),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onDone = onDone)
    )
}
