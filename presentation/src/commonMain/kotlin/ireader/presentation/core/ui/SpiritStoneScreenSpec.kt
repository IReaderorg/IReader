package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.spiritstone.SpiritStoneScreen
import ireader.presentation.ui.spiritstone.SpiritStoneViewModel

class SpiritStoneScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: SpiritStoneViewModel = getIViewModel()

        SpiritStoneScreen(
            vm = viewModel,
            onBack = { navController.safePopBackStack() }
        )
    }
}
