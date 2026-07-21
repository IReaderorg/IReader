package ireader.presentation.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * IReader Material Design 3 padding system following Mihon's patterns.
 * Provides consistent spacing values across the application.
 */
data class PaddingValues(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
)

val LocalPaddingValues = staticCompositionLocalOf { PaddingValues() }

val MaterialTheme.padding: PaddingValues
    @Composable
    @ReadOnlyComposable
    get() = LocalPaddingValues.current

/**
 * Standard Material Design 3 padding values
 */
object IReaderPadding {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    
    // Specific use case paddings
    val screenHorizontal = 16.dp
    val screenVertical = 16.dp
    val cardPadding = 16.dp
    val listItemPadding = 16.dp
    val buttonPadding = 16.dp
    val dialogPadding = 24.dp
    
    // Component-specific paddings
    val topAppBarPadding = 16.dp
    val bottomBarPadding = 16.dp
    val fabPadding = 16.dp
    val snackbarPadding = 16.dp
}