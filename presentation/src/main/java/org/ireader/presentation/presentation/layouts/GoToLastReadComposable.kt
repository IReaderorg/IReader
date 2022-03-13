package org.ireader.presentation.presentation.layouts

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.presentation.presentation.reusable_composable.AppIconButton

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