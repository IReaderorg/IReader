package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.community.GlossaryScreen
import ireader.presentation.ui.community.GlossaryViewModel

/**
 * Screen specification for the Community Glossary Screen.
 * This screen allows users to manage glossaries across all their books,
 * including global glossaries that may not exist in the user's library.
 * 
 * Features:
 * - Local book glossaries (tied to library books)
 * - Global glossaries (independent of library)
 * - Export/Import functionality
 * - Cloud sync with Supabase
 * - Integration with translation engine
 */
class GlossaryScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: GlossaryViewModel = getIViewModel()
        
        GlossaryScreen(
            state = vm.state,
            onBack = { navController.popBackStack() },
            onSelectBook = { bookId, bookTitle -> vm.selectBook(bookId, bookTitle) },
            onSelectGlobalBook = { bookKey, bookTitle -> vm.selectGlobalBook(bookKey, bookTitle) },
            onClearSelectedBook = { vm.clearSelectedBook() },
            onSearchQueryChange = { query -> vm.updateSearchQuery(query) },
            onFilterTypeChange = { type -> vm.setFilterType(type) },
            onShowAddDialog = { vm.showAddDialog() },
            onHideAddDialog = { vm.hideAddDialog() },
            onShowAddBookDialog = { vm.showAddBookDialog() },
            onHideAddBookDialog = { vm.hideAddBookDialog() },
            onSetEditingEntry = { entry -> vm.setEditingEntry(entry) },
            onSetEditingGlobalEntry = { entry -> vm.setEditingGlobalEntry(entry) },
            onAddEntry = { source, target, type, notes -> 
                vm.addGlossaryEntry(source, target, type, notes) 
            },
            onAddGlobalEntry = { source, target, type, notes -> 
                vm.addGlobalGlossaryEntry(source, target, type, notes) 
            },
            onEditEntry = { entry -> vm.updateGlossaryEntry(entry) },
            onEditGlobalEntry = { entry -> vm.updateGlobalGlossaryEntry(entry) },
            onDeleteEntry = { id -> vm.deleteGlossaryEntry(id) },
            onDeleteGlobalEntry = { id -> vm.deleteGlobalGlossaryEntry(id) },
            onExport = { onSuccess -> vm.exportGlossary(onSuccess) },
            onExportGlobal = { onSuccess -> vm.exportGlobalGlossary(onSuccess) },
            onImport = { json -> vm.importGlossary(json) },
            onImportGlobal = { json -> vm.importGlobalGlossary(json) },
            onViewModeChange = { mode -> vm.setViewMode(mode) },
            onSyncToRemote = { vm.syncToRemote() },
            onSyncFromRemote = { vm.syncFromRemote() },
            onSyncAll = { vm.syncAllFromRemote() },
            onAddGlobalBook = { bookKey, bookTitle, sourceLang, targetLang -> 
                vm.addGlobalBook(bookKey, bookTitle, sourceLang, targetLang) 
            },
            onClearError = { vm.clearError() },
            onClearSuccessMessage = { vm.clearSuccessMessage() }
        )
    }
}
