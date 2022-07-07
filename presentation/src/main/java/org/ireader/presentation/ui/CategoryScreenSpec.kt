package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.ireader.common_resources.R
import org.ireader.components.components.TitleToolbar
import org.ireader.settings.setting.category.CategoryScreen
import org.ireader.settings.setting.category.CategoryScreenViewModel
import org.ireader.Controller

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
            navController = controller.navController
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
        val vm : CategoryScreenViewModel = hiltViewModel(   controller.navBackStackEntry)
        Box(modifier = Modifier.padding(controller.scaffoldPadding)) {
            CategoryScreen(
                vm = vm
            )
        }
    }

}

