package ireader.presentation.ui.component.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun rememberMutableString() = remember {
    mutableStateOf("")
}
@Composable
fun rememberMutableBoolean(initState: Boolean = false) = remember {
    mutableStateOf(initState)
}