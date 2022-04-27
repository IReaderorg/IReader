package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.ireader.presentation.feature_sources.presentation.extension.LanguageChoice
import org.ireader.core_ui.ui_components.reusable_composable.MidSizeTextComposable

@Composable
fun LanguageChip(choice: LanguageChoice, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.onSurface.copy(alpha = 0.25f)
        },
        modifier = Modifier
            .widthIn(min = 56.dp)
            .requiredHeight(40.dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        val text = when (choice) {
            is LanguageChoice.All -> "All"
            is LanguageChoice.One -> choice.language.code
            is LanguageChoice.Others -> "Others"
        }
        MidSizeTextComposable(text = text.uppercase(),
            modifier = Modifier.wrapContentSize(Alignment.Center),
            color = if (isSelected) {
                MaterialTheme.colors.onBackground
            } else {
                MaterialTheme.colors.onBackground.copy(.5f)
            })

    }
}
