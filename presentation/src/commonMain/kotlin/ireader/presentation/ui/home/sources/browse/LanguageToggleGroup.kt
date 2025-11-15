package ireader.presentation.ui.home.sources.browse

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Language(
    val code: String,
    val name: String,
    val nativeName: String
)

@Composable
fun LanguageToggleGroup(
    selectedLanguages: Set<String>,
    onLanguageToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableLanguages = listOf(
        Language("en", "English", "English"),
        Language("fr", "French", "Français"),
        Language("es", "Spanish", "Español"),
        Language("de", "German", "Deutsch"),
        Language("ja", "Japanese", "日本語"),
        Language("zh", "Chinese", "中文"),
        Language("ko", "Korean", "한국어"),
        Language("pt", "Portuguese", "Português"),
        Language("ru", "Russian", "Русский"),
        Language("it", "Italian", "Italiano"),
        Language("ar", "Arabic", "العربية")
    )

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableLanguages.forEach { language ->
            FilterChip(
                selected = language.code in selectedLanguages,
                onClick = { onLanguageToggle(language.code) },
                label = {
                    Text(
                        text = if (language.nativeName != language.name) {
                            "${language.nativeName} (${language.name})"
                        } else {
                            language.name
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
