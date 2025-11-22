package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.settings.statistics.ModernStatisticsScreen

class StatisticsScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        // Use the new modern statistics screen with gamification and charts
        ModernStatisticsScreen().Content()
    }
}
