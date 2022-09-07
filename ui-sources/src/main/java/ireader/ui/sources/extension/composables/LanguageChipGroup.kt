package ireader.ui.sources.extension.composables

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.ui.sources.extension.LanguageChoice

@Composable
fun LanguageChipGroup(
    choices: List<LanguageChoice>,
    selected: LanguageChoice?,
    onClick: (LanguageChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier) {
        items(
            items = choices,
            key = { choice ->
                when (choice) {
                    is LanguageChoice.All -> "All"
                    is LanguageChoice.One -> choice.language.code
                    is LanguageChoice.Others -> "others"
                }
            }
        ) { choice ->
            LanguageChip(
                choice = choice,
                isSelected = choice == selected,
                onClick = { onClick(choice) }
            )
        }
    }
}
