package org.ireader.core_ui.ui_components.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.ui_components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui_components.reusable_composable.SuperSmallTextComposable

@Composable
fun GoToLastReadComposable(onClick: () -> Unit) {
    Box {
        OutlinedButton(onClick = {},
            modifier = Modifier
                .padding(5.dp)
                .size(40.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colors.background.copy(alpha = .3f)),
            contentPadding = PaddingValues(0.dp),  //avoid the little icon
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.background,
                backgroundColor = MaterialTheme.colors.onBackground.copy(
                    alpha = .4f)
            )
        ) {
            AppIconButton(
                imageVector = Icons.Default.ImportContacts,
                title = "Open last chapter",
                onClick = {
                    onClick()
                },
                tint = MaterialTheme.colors.background.copy(alpha = .4f)
            )
        }

    }
}

@Composable
fun TextBadge(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = Modifier
            .padding(5.dp)
            .size(width = 60.dp, height = 20.dp)
            .background(MaterialTheme.colors.primary),
        contentAlignment = Alignment.Center,
    ) {
        SuperSmallTextComposable(text = text,
            color = Color.White,
            maxLine = 1,
            overflow = TextOverflow.Ellipsis)
    }
}