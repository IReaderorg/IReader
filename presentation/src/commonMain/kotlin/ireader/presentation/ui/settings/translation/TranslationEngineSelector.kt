package ireader.presentation.ui.settings.translation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.data.engines.TranslateEngine

/**
 * Engine selection component with Gemini prioritized first
 * Optimized for mobile and desktop
 */
@Composable
fun TranslationEngineSelector(
    engines: List<TranslateEngine>,
    selectedEngineId: Long,
    onEngineSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Reorder engines to put Gemini (ID 8) first
    val orderedEngines = remember(engines) {
        val gemini = engines.find { it.id == 8L }
        val others = engines.filter { it.id != 8L }
        listOfNotNull(gemini) + others
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        items(orderedEngines, key = { it.id }) { engine ->
            EngineChip(
                engine = engine,
                isSelected = engine.id == selectedEngineId,
                isPrimary = engine.id == 8L,
                onClick = { onEngineSelected(engine.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EngineChip(
    engine: TranslateEngine,
    isSelected: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isPrimary -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        }
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Primary badge
            if (isPrimary && !isSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "★",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            Icon(
                imageVector = getEngineIcon(engine.id),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = getShortEngineName(engine.id, engine.engineName),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Returns short name for engine to fit in compact cards
 */
private fun getShortEngineName(engineId: Long, fullName: String): String {
    return when (engineId) {
        0L -> "Google ML"
        1L -> "Dict"
        2L -> "OpenAI"
        3L -> "DeepSeek"
        4L -> "Libre"
        5L -> "Ollama"
        6L -> "ChatGPT"
        7L -> "DeepSeek WV"
        8L -> "Gemini"
        else -> fullName.take(10)
    }
}

/**
 * Returns appropriate icon for each translation engine
 */
fun getEngineIcon(engineId: Long): ImageVector {
    return when (engineId) {
        0L -> Icons.Default.Translate
        1L -> Icons.Default.Book
        2L -> Icons.Default.Psychology
        3L -> Icons.Default.AutoAwesome
        4L -> Icons.Default.Public
        5L -> Icons.Default.Computer
        6L -> Icons.Default.Chat
        7L -> Icons.Default.SmartToy
        8L -> Icons.Default.Stars
        else -> Icons.Default.Translate
    }
}
