package org.ireader.components.components.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
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
    icon: ImageVector? = null,
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
    icon: ImageVector? = null,
) {
    PreferenceRow(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        painter = painter,
        icon = icon,
        action = {
            Switch(
                checked = preference.value,
                onCheckedChange = null,
            )
        },
        onClick = { preference.value = !preference.value },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceRow(
    title: String,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
) {
    val height = if (subtitle != null) 72.dp else 56.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(height)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(24.dp),
                contentDescription = null,
                imageVector = icon,
                tint = MaterialTheme.colorScheme.primary,

            )
        }
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                overflow = Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    overflow = Ellipsis,
                    maxLines = 1,
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                    style = MaterialTheme.typography.labelMedium
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
    preference: PreferenceMutableState<Boolean>,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    PreferenceRow(
        title = title,
        subtitle = subtitle,
        icon = icon,
        action = { Switch(checked = preference.value, onCheckedChange = null) },
        onClick = { preference.value = !preference.value }
    )
}



@Composable
fun <Key> ChoicePreference(
    preference: PreferenceMutableState<Key>,
    choices: Map<Key, Int>,
    title: Int,
    subtitle: String? = null,
    onValue:((Key)->Unit)? = null
) {
    ChoicePreference(
        preference,
        choices.mapValues {map -> stringResource(map.value) },
        stringResource(title),
        subtitle,
        onValue
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <Key> ChoicePreference(
    preference: PreferenceMutableState<Key>,
    choices: Map<Key, String>,
    title: String,
    subtitle: String? = null,
    onValue:((Key)->Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    PreferenceRow(
        title = title,
        subtitle = if (subtitle == null) choices[preference.value] else null,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                LazyColumn {
                    items(choices.toList()) { (value, text) ->
                        Row(
                            modifier = Modifier
                                .requiredHeight(48.dp)
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    if (onValue != null) {
                                        onValue(value)
                                    } else {
                                        preference.value = value
                                    }

                                    showDialog = false
                                }),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value == preference.value,
                                onClick = null,
                            )
                            Text(text = text, modifier = Modifier.padding(start = 24.dp))
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ColorPreference(
    preference: PreferenceMutableState<Color>,
    title: String,
    subtitle: String? = null,
    unsetColor: Color = Color.Unspecified
) {
    var showDialog by remember { mutableStateOf(false) }
    val initialColor = preference.value.takeOrElse { unsetColor }

    PreferenceRow(
        title = title,
        subtitle = subtitle,
        onClick = { showDialog = true },
        onLongClick = { preference.value = Color.Unspecified },
        action = {
            if (preference.value != Color.Unspecified || unsetColor != Color.Unspecified) {
                val borderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.54f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color = initialColor)
                        .border(BorderStroke(1.dp, borderColor), CircleShape)
                )
            }
        }
    )

    if (showDialog) {
        ColorPickerDialog(
            title = { Text(title) },
            onDismissRequest = { showDialog = false },
            onSelected = {
                preference.value = it
                showDialog = false
            },
            initialColor = initialColor
        )
    }
}

