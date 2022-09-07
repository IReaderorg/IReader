package ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import ireader.ui.component.Controller
import ireader.common.resources.R
import ireader.ui.component.components.TitleToolbar
import ireader.ui.settings.category.CategoryScreen
import ireader.ui.settings.category.CategoryScreenViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

object CategoryScreenSpec : ScreenSpec {

    override val navHostRoute: String = "edit_category_screen_route"
    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.edit_category),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: CategoryScreenViewModel = getViewModel(owner = controller.navBackStackEntry)
        Box(modifier = Modifier.padding(controller.scaffoldPadding)) {
            CategoryScreen(
                vm = vm
            )
        }
    }
}
