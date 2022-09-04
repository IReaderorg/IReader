package org.ireader.components.list.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.UiText
import org.ireader.common_resources.asString
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.SuperSmallTextComposable
import org.ireader.ui_components.R

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
                contentDescription = stringResource(R.string.open_last_chapter),
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
    Box(
        modifier = Modifier
            .padding(5.dp)
            .size(width = 60.dp, height = 20.dp)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        SuperSmallTextComposable(
            text = text.asString(LocalContext.current),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLine = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
