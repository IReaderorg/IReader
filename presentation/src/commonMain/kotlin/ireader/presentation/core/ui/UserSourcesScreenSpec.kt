package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.ui.sourcecreator.SourceCreatorScreen
import ireader.presentation.ui.sourcecreator.SourceCreatorViewModel
import ireader.presentation.ui.sourcecreator.UserSourcesListScreen
import ireader.presentation.ui.sourcecreator.UserSourcesListViewModel
import org.koin.compose.koinInject

/**
 * Screen spec for the User Sources list.
 */
class UserSourcesListScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val vm: UserSourcesListViewModel = koinInject()
        val state by vm.state.collectAsState()
        val navController = LocalNavigator.current
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Show snackbar messages
        LaunchedEffect(state.snackbarMessage) {
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                vm.clearSnackbar()
            }
        }
        
        UserSourcesListScreen(
            state = state,
            onBack = { navController?.popBackStack() },
            onCreateNew = { navController?.navigate("userSourceCreator") },
            onCreateWithWizard = { navController?.navigate("userSourceCreator") },
            onOpenImportScreen = { vm.showImportDialog() },
            onOpenHelpScreen = { vm.showHelpDialog() },
            onOpenAutoDetect = { vm.showImportDialog() },
            onOpenLegadoImport = { navController?.navigate(NavigationRoutes.legadoSourceImport) },
            onEdit = { sourceUrl -> 
                navController?.navigate("userSourceCreator?sourceUrl=$sourceUrl")
            },
            onDelete = { source -> vm.showDeleteConfirm(source) },
            onToggleEnabled = { sourceUrl, enabled -> vm.toggleEnabled(sourceUrl, enabled) },
            onImport = { json -> vm.importFromJson(json) },
            onExportAll = { vm.exportAll() },
            onShare = { source -> vm.shareSource(source) },
            onShowImportDialog = { vm.showImportDialog() },
            onHideImportDialog = { vm.hideImportDialog() },
            onShowHelpDialog = { vm.showHelpDialog() },
            onHideHelpDialog = { vm.hideHelpDialog() },
            onConfirmDelete = { vm.confirmDelete() },
            onCancelDelete = { vm.cancelDelete() },
            onClearShareJson = { vm.clearShareJson() },
            onToggleCreateOptions = { vm.toggleCreateOptions() },
            onShowDeleteAllDialog = { vm.showDeleteAllConfirm() },
            onConfirmDeleteAll = { vm.confirmDeleteAll() },
            onCancelDeleteAll = { vm.cancelDeleteAll() },
            snackbarHostState = snackbarHostState
        )
    }
}

/**
 * Screen spec for creating/editing a user source.
 */
class UserSourceCreatorScreenSpec(
    private val sourceUrl: String?
) {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val vm: SourceCreatorViewModel = koinInject()
        val state by vm.state.collectAsState()
        val navController = LocalNavigator.current
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Load source if editing
        LaunchedEffect(sourceUrl) {
            if (sourceUrl != null) {
                vm.loadSource(sourceUrl)
            }
        }
        
        // Show snackbar messages
        LaunchedEffect(state.snackbarMessage) {
            state.snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                vm.clearSnackbar()
            }
        }
        
        SourceCreatorScreen(
            state = state,
            onBack = { navController?.popBackStack() },
            onSave = { vm.save() },
            onShowJson = { vm.showJsonDialog() },
            onTabChange = { vm.setTab(it) },
            onSourceNameChange = { vm.updateSourceName(it) },
            onSourceUrlChange = { vm.updateSourceUrl(it) },
            onSourceGroupChange = { vm.updateSourceGroup(it) },
            onLangChange = { vm.updateLang(it) },
            onCommentChange = { vm.updateComment(it) },
            onEnabledChange = { vm.updateEnabled(it) },
            onHeaderChange = { vm.updateHeader(it) },
            onSearchUrlChange = { vm.updateSearchUrl(it) },
            onExploreUrlChange = { vm.updateExploreUrl(it) },
            onSearchBookListChange = { vm.updateSearchBookList(it) },
            onSearchNameChange = { vm.updateSearchName(it) },
            onSearchAuthorChange = { vm.updateSearchAuthor(it) },
            onSearchIntroChange = { vm.updateSearchIntro(it) },
            onSearchBookUrlChange = { vm.updateSearchBookUrl(it) },
            onSearchCoverUrlChange = { vm.updateSearchCoverUrl(it) },
            onSearchKindChange = { vm.updateSearchKind(it) },
            onBookInfoNameChange = { vm.updateBookInfoName(it) },
            onBookInfoAuthorChange = { vm.updateBookInfoAuthor(it) },
            onBookInfoIntroChange = { vm.updateBookInfoIntro(it) },
            onBookInfoCoverUrlChange = { vm.updateBookInfoCoverUrl(it) },
            onBookInfoKindChange = { vm.updateBookInfoKind(it) },
            onBookInfoTocUrlChange = { vm.updateBookInfoTocUrl(it) },
            onTocChapterListChange = { vm.updateTocChapterList(it) },
            onTocChapterNameChange = { vm.updateTocChapterName(it) },
            onTocChapterUrlChange = { vm.updateTocChapterUrl(it) },
            onTocNextUrlChange = { vm.updateTocNextUrl(it) },
            onTocIsReverseChange = { vm.updateTocIsReverse(it) },
            onContentSelectorChange = { vm.updateContentSelector(it) },
            onContentNextUrlChange = { vm.updateContentNextUrl(it) },
            onContentPurifyChange = { vm.updateContentPurify(it) },
            onContentReplaceRegexChange = { vm.updateContentReplaceRegex(it) },
            onExploreBookListChange = { vm.updateExploreBookList(it) },
            onExploreNameChange = { vm.updateExploreName(it) },
            onExploreAuthorChange = { vm.updateExploreAuthor(it) },
            onExploreBookUrlChange = { vm.updateExploreBookUrl(it) },
            onExploreCoverUrlChange = { vm.updateExploreCoverUrl(it) },
            snackbarHostState = snackbarHostState
        )
        
        // JSON Dialog
        if (state.showJsonDialog) {
            ireader.presentation.ui.sourcecreator.components.JsonDialog(
                jsonContent = state.jsonContent,
                onDismiss = { vm.hideJsonDialog() },
                onImport = { json -> vm.importFromJsonDialog(json) }
            )
        }
    }
}
