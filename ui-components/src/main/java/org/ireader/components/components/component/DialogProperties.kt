package org.ireader.components.components.component

import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Immutable
class DialogProperties(
  val dismissOnBackPress: Boolean = true,
  val dismissOnClickOutside: Boolean = true,
  val usePlatformDefaultWidth: Boolean = true
)


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AlertDialog(
  onDismissRequest: () -> Unit,
  buttons: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  title: (@Composable () -> Unit)? = null,
  text: @Composable (() -> Unit)? = null,
  shape: Shape = MaterialTheme.shapes.medium,
  backgroundColor: Color = MaterialTheme.colors.surface,
  contentColor: Color = contentColorFor(backgroundColor),
  properties: DialogProperties = DialogProperties()
) {
  androidx.compose.material.AlertDialog(
    onDismissRequest = onDismissRequest,
    buttons = buttons,
    modifier = modifier,
    title = title,
    text = text,
    shape = shape,
    backgroundColor = backgroundColor,
    contentColor = contentColor,
    properties = androidx.compose.ui.window.DialogProperties(
      dismissOnBackPress = properties.dismissOnBackPress,
      dismissOnClickOutside = properties.dismissOnClickOutside,
      usePlatformDefaultWidth = properties.usePlatformDefaultWidth
    )
  )
}
