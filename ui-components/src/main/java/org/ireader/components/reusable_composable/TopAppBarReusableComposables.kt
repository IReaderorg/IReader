package org.ireader.components.reusable_composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.common_resources.UiText
import org.ireader.ui_components.R

@Composable
fun BigSizeTextComposable(
    modifier: Modifier = Modifier,
    text: UiText,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    align: TextAlign? = null,
    maxLine: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier,
        text = text.asString(LocalContext.current),
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.subtitle1,
        fontWeight = fontWeight ?: FontWeight.Bold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
        maxLines = maxLine
    )
}

@Composable
fun MidSizeTextComposable(
    modifier: Modifier = Modifier,
    text: UiText,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text.asString(LocalContext.current),
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.subtitle2,
        fontWeight = fontWeight ?: FontWeight.SemiBold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
        maxLines = maxLine
    )
}

@Composable
fun SmallTextComposable(
    modifier: Modifier = Modifier,
    text: UiText,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text.asString(LocalContext.current),
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.caption,
        fontWeight = fontWeight ?: FontWeight.SemiBold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
        maxLines = maxLine
    )
}

@Composable
fun SuperSmallTextComposable(
    modifier: Modifier = Modifier,
    text: UiText,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,

    ) {
    Text(
        modifier = modifier,
        text = text.asString(LocalContext.current),
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.caption,
        fontWeight = fontWeight ?: FontWeight.Normal,
        fontSize = 12.sp,
        overflow = overflow ?: TextOverflow.Ellipsis,
        maxLines = maxLine,
        textAlign = align ?: TextAlign.Start,
    )
}

@Composable
fun CaptionTextComposable(
    modifier: Modifier = Modifier,
    text: UiText,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text.asString(LocalContext.current),
        color = color ?: MaterialTheme.colors.onBackground,
        style = style ?: MaterialTheme.typography.caption,
        fontWeight = fontWeight ?: FontWeight.Normal,
        overflow = overflow ?: TextOverflow.Ellipsis,
        maxLines = maxLine,
        textAlign = align ?: TextAlign.Start,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: UiText,
    onClick: () -> Unit,
    tint: Color? = null,
) {
    IconButton(
        onClick = {
            onClick()
        },
    ) {
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = text.asString(LocalContext.current),
            tint = tint ?: MaterialTheme.colors.onBackground
        )
    }
}

@Composable
fun TopAppBarBackButton(onClick: () -> Unit) {
    IconButton(onClick = {
        onClick()
    }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = UiText.StringResource( R.string.return_to_previous_screen).asString(
                LocalContext.current),
            tint = MaterialTheme.colors.onBackground,
        )
    }
}

@Composable
fun AppTextField(
    query: String,
    onValueChange: (value: String) -> Unit,
    onConfirm: () -> Unit,
    hint: UiText = UiText.StringResource( R.string.search_hint),
    mode: Int = 0,
    keyboardAction: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions: KeyboardActions = KeyboardActions(onSearch = {
        onConfirm()
    }, onDone = { onConfirm() }),
) {
    val focusManager = LocalFocusManager.current
    Box(contentAlignment = Alignment.CenterStart) {
        if (query.isBlank() && mode != 2) {
            Text(
                modifier = Modifier.padding(horizontal = 0.dp),
                text = hint.asString(LocalContext.current),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground.copy(alpha = .7F)
            )
        }

        if (mode == 0) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = {
                    onValueChange(it)
                },
                maxLines = 1,
                keyboardOptions = keyboardAction,
                keyboardActions = keyboardActions,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colors.primary),
            )
        } else if (mode == 1) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onValueChange,
                maxLines = 1,
                keyboardOptions = keyboardAction,
                keyboardActions = keyboardActions,

                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
            )
        } else if (mode == 2) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = query,
                onValueChange = onValueChange,
                label = {
                    CaptionTextComposable(text = hint)
                },
                maxLines = 1,
                keyboardOptions = keyboardAction,
                keyboardActions = keyboardActions,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
            )
        }
    }
}
