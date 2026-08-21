package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.recommendations.SimilarTitlesSettingsScreen
import ireader.presentation.ui.settings.recommendations.SimilarTitlesSettingsViewModel
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack

@ExperimentalMaterial3Api
class SimilarTitlesScreenSpec {

    @Composable
    fun Content() {
        val vm: SimilarTitlesSettingsViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localizeHelper.localize(Res.string.similar_titles_settings),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.safePopBackStack()
                    }
                )
            }
        ) { scaffoldPadding ->
            SimilarTitlesSettingsScreen(
                onNavigateUp = { navController.safePopBackStack() },
                viewModel = vm,
                scaffoldPaddingValues = scaffoldPadding
            )
        }
    }
}
