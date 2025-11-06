package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.home.sources.extension.LanguageChoice


@Composable
fun LanguageChip(choice: LanguageChoice, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            },
            modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onClick),
            tonalElevation = if (isSelected) 3.dp else 0.dp
    ) {
        val text = when (choice) {
            is LanguageChoice.All -> "🌐 All"
            is LanguageChoice.One -> {
                val emoji = choice.language.toEmoji() ?: ""
                val name = ireader.presentation.ui.home.sources.extension.LocaleHelper.getDisplayName(choice.language.code)
                if (emoji.isNotEmpty()) "$emoji $name" else name
            }
            is LanguageChoice.Others -> "🌍 Others"
        }
        MidSizeTextComposable(
                text = text,
                modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .wrapContentSize(Alignment.Center),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
        )
    }
}
