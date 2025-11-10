package ireader.presentation.ui.book.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.SourceComparison
import ireader.domain.usecases.source.MigrateToSourceUseCase

/**
 * State holder for source switching feature
 */
class SourceSwitchingState {
    var sourceComparison by mutableStateOf<SourceComparison?>(null)
    var showBanner by mutableStateOf(false)
    var showMigrationDialog by mutableStateOf(false)
    var migrationProgress by mutableStateOf<MigrateToSourceUseCase.MigrationProgress?>(null)
    var betterSourceName by mutableStateOf<String?>(null)
    
    fun reset() {
        sourceComparison = null
        showBanner = false
        showMigrationDialog = false
        migrationProgress = null
        betterSourceName = null
    }
}
