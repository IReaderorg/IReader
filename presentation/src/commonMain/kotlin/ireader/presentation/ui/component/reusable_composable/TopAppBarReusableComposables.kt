package ireader.presentation.ui.component.reusable_composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.i18n.localize
import ireader.i18n.resources.MR

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
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        maxLines = maxLine,
    )
}

@Composable
fun SmallTextComposable(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onBackground,
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
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        color = color ?: MaterialTheme.colorScheme.onBackground,
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    Box(
        modifier =
        modifier
            .size(48.dp) // Increased from 40dp to meet 48dp minimum touch target for accessibility
            .background(color = MaterialTheme.colorScheme.background)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                enabled = enabled,
                role = Role.Button,
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentColor = MaterialTheme.colorScheme.background
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    contentDescription: String? = null,
    onClick: () -> Unit = {},
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    IconButton(
        modifier = modifier
            .size(48.dp), // Ensure minimum 48dp touch target for accessibility
        onClick = {
            onClick()
        },
    ) {
        when {
            painter != null -> {
                Icon(
                    modifier = Modifier.size(24.dp), // Icon size separate from touch target
                    painter = painter,
                    contentDescription = contentDescription,
                    tint = tint
                )
            }
            imageVector != null -> {
                Icon(
                    modifier = Modifier.size(24.dp), // Icon size separate from touch target
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = tint
                )
            }
        }
    }
}

@Composable
fun TopAppBarBackButton(tint: Color = MaterialTheme.colorScheme.onSurface, onClick: () -> Unit,) {
    val layoutDirection = LocalLayoutDirection.current
    val isLeftToRight = remember {
        derivedStateOf { layoutDirection == LayoutDirection.Ltr }
    }
    IconButton(onClick = {
        onClick()
    }) {
        Icon(
            imageVector = if (isLeftToRight.value) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.ArrowForward,
            contentDescription = localize(MR.strings.return_to_previous_screen),
            tint = tint,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
        modifier: Modifier= Modifier,
        query: String,
        onValueChange: (value: String) -> Unit,
        onConfirm: () -> Unit,
        hint: String = localize(MR.strings.search_hint),
        mode: Int = 0,
        keyboardAction: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions: KeyboardActions = KeyboardActions(onSearch = {
        onConfirm()
    }, onDone = { onConfirm() }),
) {
    val focusManager = LocalFocusManager.current
    Box(contentAlignment = Alignment.CenterStart, modifier = modifier) {
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
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),


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
