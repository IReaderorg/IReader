package ireader.presentation.core.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

import ireader.presentation.ui.component.Controller
import ireader.i18n.R
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.category.CategoryScreen
import ireader.presentation.ui.settings.category.CategoryScreenViewModel
import org.koin.androidx.compose.getViewModel

class CategoryScreenSpec : VoyagerScreen() {


    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
    ) {
        val navigator = LocalNavigator.currentOrThrow

        val vm: CategoryScreenViewModel = getIViewModel()

        IScaffold(
            topBar = {scrollBehavior ->
            TitleToolbar(
                    title = stringResource(R.string.edit_category),
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
