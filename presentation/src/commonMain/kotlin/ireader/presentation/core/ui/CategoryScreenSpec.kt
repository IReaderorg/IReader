package ireader.presentation.core.ui


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.edit_category
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.category.CategoryScreen
import ireader.presentation.ui.settings.category.CategoryScreenViewModel

class CategoryScreenSpec {


    @OptIn(
         ExperimentalMaterial3Api::class
    )
    @Composable
    fun Content(
    ) {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }

        val vm: CategoryScreenViewModel = getIViewModel()

        IScaffold(
            topBar = {scrollBehavior ->
            TitleToolbar(
                    title = localize(Res.string.edit_category),
                    scrollBehavior = scrollBehavior,
                popBackStack = {
                    navController.safePopBackStack()
                }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                CategoryScreen(
                    vm = vm
                )
            }
        }

    }
}
