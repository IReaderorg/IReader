package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

import kotlin.random.Random

/**
 * IReader Error Screen following Mihon's comprehensive error handling pattern.
 * Provides user-friendly error display with contextual actions and random kaomoji faces.
 */
@Composable
fun IReaderErrorScreen(
    message: String,
    modifier: Modifier = Modifier,
    actions: List<ErrorScreenAction>? = null,
) {
    val face = remember { getRandomErrorFace() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = face,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        Text(
            text = message,
            modifier = Modifier.paddingFromBaseline(top = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        if (!actions.isNullOrEmpty()) {
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                actions.forEach { action ->
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        title = action.title,
                        icon = action.icon,
                        onClick = action.onClick,
                    )
                }
            }
        }
    }
}

/**
 * Data class representing an action button for error screens
 */
data class ErrorScreenAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * Extension function to apply secondary item alpha
 */
@Composable
private fun Modifier.secondaryItemAlpha(): Modifier {
    return this.then(
        Modifier // Apply alpha through color instead of modifier
    )
}

/**
 * Random error faces following Mihon's pattern
 */
private val ErrorFaces = listOf(
    "(･o･;)", "Σ(ಠ_ಠ)", "ಥ_ಥ", "(˘･_･˘)", "(；￣Д￣)",
    "(･Д･。", "(╬ಠ益ಠ)", "(╥﹏╥)", "(⋟﹏⋞)", "Ò︵Ó", " ˙ᯅ˙)", "(¬_¬)",
    "(>_<)", "(T_T)", "(×_×)", "(◞‸◟)", "(´･ω･`)", "(-_-;)", "(・_・;)",
    "(｡•́︿•̀｡)", "(╯︵╰)", "(⌒_⌒;)", "( ˘︹˘ )", "(´-ω-`)", "(◡ ‿ ◡ ✿)",
)

private fun getRandomErrorFace(): String {
    return ErrorFaces[Random.nextInt(ErrorFaces.size)]
}