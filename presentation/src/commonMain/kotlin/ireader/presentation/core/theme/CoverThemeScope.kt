package ireader.presentation.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ireader.domain.models.theme.ExtraColors
import ireader.presentation.core.toDomainColor
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.isLight
import org.koin.compose.koinInject

/**
 * Scopes the cover-based dynamic color theme to the claiming screen's subtree.
 * Wrap a route's content (bookDetail / reader); screens outside the wrapper keep
 * the user's chosen theme untouched.
 *
 * Structurally stable: always wraps content in the same [AppColors] node, falling
 * through to the ambient (root) colors while no cover scheme is live — flipping
 * composition structure on claim/release would drop remembered state below.
 */
@Composable
fun CoverThemeScope(
    appThemeViewModel: AppThemeViewModel = koinInject(),
    content: @Composable () -> Unit,
) {
    val scoped = appThemeViewModel.getCoverScopedColors()
    val ambient = MaterialTheme.colorScheme

    // Single call site so animateScheme's remember state survives claim/release.
    // Target: cover scheme when live, otherwise fade back to ambient — feeding
    // ambient as target makes the exit animated too, for free.
    val target = scoped ?: ambient
    val displayed = appThemeViewModel.animateScheme(target, target.isLight())

    // Bars ride the same animation: derive from the displayed surface so the
    // scope's bars cross-fade with the content instead of snapping.
    val extras = ExtraColors(
        bars = displayed.surface.toDomainColor(),
        onBars = ThemeColorUtils.getOnColor(displayed.surface).toDomainColor()
    )

    AppColors(
        materialColors = displayed,
        extraColors = extras,
        typography = MaterialTheme.typography,
        shape = MaterialTheme.shapes,
        content = content,
    )
}
