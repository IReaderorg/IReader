package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.ui.home.sources.migration.MigrationViewModel
import ireader.presentation.ui.home.sources.migration.SourceMigrationScreen
import org.koin.compose.koinInject

data class SourceMigrationScreenSpec(
    val sourceId: Long
) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MigrationViewModel = koinInject()
        
        LaunchedEffect(sourceId) {
            viewModel.loadNovelsFromSource(sourceId)
        }
        
        SourceMigrationScreen(
            viewModel = viewModel,
            onBackPressed = {
                navigator.pop()
            }
        )
    }
}
