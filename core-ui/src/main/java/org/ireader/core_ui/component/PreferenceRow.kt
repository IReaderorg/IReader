package org.ireader.core_ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.ui.PreferenceMutableState
import org.ireader.core_ui.utils.horizontalPadding

@Composable
fun Divider(
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Divider(
        modifier = modifier,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceRow(
    modifier: Modifier = Modifier,
    title: String,
    painter: Painter? = null,
    icon : ImageVector? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
) {
    val height = if (subtitle != null) 72.dp else 56.dp

    val titleTextStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge
    val subtitleTextStyle = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (painter != null) {
            Icon(
                painter = painter,
                modifier = Modifier
                    .padding(horizontal = horizontalPadding)
                    .size(24.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                modifier = Modifier
                    .padding(horizontal = horizontalPadding)
                    .size(24.dp),
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )
        }
        Column(
            Modifier
                .padding(horizontal = horizontalPadding)
                .weight(1f)
        ) {
            Text(
                text = title,
                style = titleTextStyle,
            )
            if (subtitle != null) {
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = subtitle,
                    style = subtitleTextStyle,
                )
            }
        }
        if (action != null) {
            Box(Modifier.widthIn(min = 56.dp)) {
                action()
            }
        }
    }
}

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    preference: PreferenceMutableState<Boolean>,
    title: String,
    subtitle: String? = null,
    painter: Painter? = null,
) {
    PreferenceRow(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        painter = painter,
        action = {
            Switch(checked = preference.value, onCheckedChange = null)
            // TODO: remove this once switch checked state is fixed: https://issuetracker.google.com/issues/228336571
            Text(preference.value.toString())
        },
        onClick = { preference.value = !preference.value },
    )
}
