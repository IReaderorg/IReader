package ireader.presentation.core.ui


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize

import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.category.CategoryScreen
import ireader.presentation.ui.settings.category.CategoryScreenViewModel

class CategoryScreenSpec : VoyagerScreen() {


    @OptIn(
         ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
    ) {
        val navigator = LocalNavigator.currentOrThrow

        val vm: CategoryScreenViewModel = getIViewModel()

        IScaffold(
            topBar = {scrollBehavior ->
            TitleToolbar(
                title = localize() { xml ->
                    xml.editCategory
                },
                scrollBehavior = scrollBehavior,
                popBackStack = {
                    popBackStack(navigator)
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
