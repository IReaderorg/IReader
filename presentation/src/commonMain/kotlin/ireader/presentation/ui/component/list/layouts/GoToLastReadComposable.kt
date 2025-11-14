package ireader.presentation.ui.component.list.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.SuperSmallTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper


@Composable
fun GoToLastReadComposable(modifier: Modifier = Modifier, size: Dp = 40.dp, onClick: () -> Unit) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .padding(5.dp)
                .size(size),
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.background.copy(alpha = .3f)),
            contentPadding = PaddingValues(0.dp), // avoid the little icon
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.background,
                containerColor = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = .4f
                )
            )
        ) {
            AppIconButton(
                imageVector = Icons.Default.ImportContacts,
                contentDescription = localize(Res.string.open_last_chapter),
                onClick = {
                    onClick()
                },
                tint = MaterialTheme.colorScheme.background.copy(alpha = .4f)
            )
        }
    }
}

@Composable
fun TextBadge(modifier: Modifier = Modifier, text: UiText) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier
            .padding(5.dp)
            .size(width = 60.dp, height = 20.dp)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        SuperSmallTextComposable(
            text = text.asString(localizeHelper),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLine = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
