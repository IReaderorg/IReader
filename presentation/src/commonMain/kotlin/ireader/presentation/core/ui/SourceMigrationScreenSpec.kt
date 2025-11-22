package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import ireader.presentation.ui.home.sources.migration.MigrationViewModel
import ireader.presentation.ui.home.sources.migration.ModernSourceMigrationScreen
import org.koin.compose.koinInject

data class SourceMigrationScreenSpec(
    val sourceId: Long,
    val onBackPressed: () -> Unit = {}
) : Screen {
    
    @Composable
    override fun Content() {
        val viewModel: MigrationViewModel = koinInject()
        
        LaunchedEffect(sourceId) {
            viewModel.loadNovelsFromSource(sourceId)
        }
        
        ModernSourceMigrationScreen(
            viewModel = viewModel,
            onBackPressed = onBackPressed
        )
    }
}
