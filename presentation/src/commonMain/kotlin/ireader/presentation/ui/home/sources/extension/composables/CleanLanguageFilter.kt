package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.home.sources.extension.LanguageChoice
import ireader.presentation.ui.home.sources.extension.LocaleHelper

/**
 * Clean and simple language filter
 */
@Composable
fun CleanLanguageFilter(
    choices: List<LanguageChoice>,
    selected: LanguageChoice,
    onClick: (LanguageChoice) -> Unit,
    isVisible: Boolean,
    onToggleVisibility: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Language",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(onClick = { onToggleVisibility(!isVisible) }) {
                Icon(
                    imageVector = if (isVisible) 
                        Icons.Default.ExpandLess 
                    else 
                        Icons.Default.ExpandMore,
                    contentDescription = if (isVisible) "Collapse" else "Expand"
                )
            }
        }
        
        AnimatedVisibility(
            visible = isVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                choices.forEach { choice ->
                    CleanLanguageChip(
                        choice = choice,
                        isSelected = choice == selected,
                        onClick = { onClick(choice) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanLanguageChip(
    choice: LanguageChoice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = when (choice) {
        is LanguageChoice.All -> "All"
        is LanguageChoice.One -> {
            LocaleHelper.getDisplayName(choice.language.code)
        }
        is LanguageChoice.Others -> "Others"
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier
    )
}
