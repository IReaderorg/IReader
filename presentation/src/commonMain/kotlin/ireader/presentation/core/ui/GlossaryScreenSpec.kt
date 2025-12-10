package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.community.GlossaryScreen
import ireader.presentation.ui.community.GlossaryViewModel

/**
 * Screen specification for the Community Glossary Screen.
 * This screen allows users to manage glossaries across all their books.
 */
class GlossaryScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: GlossaryViewModel = getIViewModel()
        
        GlossaryScreen(
            state = vm.state,
            onBack = {
                navController.popBackStack()
            },
            onSelectBook = { bookId, bookTitle ->
                vm.selectBook(bookId, bookTitle)
            },
            onClearSelectedBook = {
                vm.clearSelectedBook()
            },
            onSearchQueryChange = { query ->
                vm.updateSearchQuery(query)
            },
            onFilterTypeChange = { type ->
                vm.setFilterType(type)
            },
            onShowAddDialog = {
                vm.showAddDialog()
            },
            onHideAddDialog = {
                vm.hideAddDialog()
            },
            onSetEditingEntry = { entry ->
                vm.setEditingEntry(entry)
            },
            onAddEntry = { source, target, type, notes ->
                vm.addGlossaryEntry(source, target, type, notes)
            },
            onEditEntry = { entry ->
                vm.updateGlossaryEntry(entry)
            },
            onDeleteEntry = { id ->
                vm.deleteGlossaryEntry(id)
            },
            onExport = { onSuccess ->
                vm.exportGlossary(onSuccess)
            },
            onImport = { json ->
                vm.importGlossary(json)
            }
        )
    }
}
