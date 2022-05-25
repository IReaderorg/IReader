package org.ireader.components.reusable_composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
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
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    align: TextAlign? = null,
    maxLine: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style ?: MaterialTheme.typography.titleSmall,
        fontWeight = fontWeight ?: FontWeight.Bold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
        maxLines = maxLine
    )
}

@Composable
fun MidSizeTextComposable(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style ?: MaterialTheme.typography.bodyMedium,
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
    color: Color = Color.Unspecified,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text.asString(LocalContext.current),
        color = color,
        style = style ?: MaterialTheme.typography.labelSmall,
        fontWeight = fontWeight ?: FontWeight.SemiBold,
        overflow = overflow ?: TextOverflow.Ellipsis,
        textAlign = align ?: TextAlign.Start,
        maxLines = maxLine
    )
}

@Composable
fun SuperSmallTextComposable(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,

    ) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style ?: MaterialTheme.typography.labelSmall,
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
    text: String,
    color: Color? = null,
    style: TextStyle? = null,
    fontWeight: FontWeight? = null,
    overflow: TextOverflow? = null,
    maxLine: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
) {
    Text(
        modifier = modifier,
        text = text,
        color = color ?: Color.Unspecified,
        style = style ?: MaterialTheme.typography.labelSmall,
        fontWeight = fontWeight ?: FontWeight.Normal,
        overflow = overflow ?: TextOverflow.Ellipsis,
        maxLines = maxLine,
        textAlign = align ?: TextAlign.Start,
    )
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String? = null,
    tint: Color? = null,
) {
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint ?: MaterialTheme.colorScheme.onSurface
        )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit = {},
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    IconButton(
        onClick = {
            onClick()
        },
    ) {
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
fun TopAppBarBackButton(tint: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit,) {
    IconButton(onClick = {
        onClick()
    }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
           contentDescription = stringResource( R.string.return_to_previous_screen),
            tint = tint,
        )
    }
}

@Composable
fun AppTextField(
    query: String,
    onValueChange: (value: String) -> Unit,
    onConfirm: () -> Unit,
    hint: String = stringResource( R.string.search_hint),
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
                modifier = if (mode == 1) Modifier.padding(horizontal = 16.dp) else Modifier.padding(horizontal = 0.dp),
                text = hint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
                textStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),


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
                textStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),

            )
        } else if (mode == 2) {
            androidx.compose.material3.TextField(
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
                textStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}
