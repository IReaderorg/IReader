package ir.kazemcodes.infinity.presentation.extension_creator

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarTitle

@Composable
fun ExtensionCreatorScreen() {
    val backStack = LocalBackstack.current
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = {
                TopAppBarTitle("Extension Creator")
            },
            backgroundColor = MaterialTheme.colors.background,
            actions = {
            },
            navigationIcon = {
                TopAppBarBackButton(backStack = backStack)

            }
        )
    }) {

    }
}