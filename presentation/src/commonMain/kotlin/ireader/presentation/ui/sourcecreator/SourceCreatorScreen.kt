package ireader.presentation.ui.sourcecreator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.sourcecreator.components.*

/**
 * Screen for creating and editing user-defined sources.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceCreatorScreen(
    state: SourceCreatorState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onShowJson: () -> Unit,
    onTabChange: (Int) -> Unit,
    onSourceNameChange: (String) -> Unit,
    onSourceUrlChange: (String) -> Unit,
    onSourceGroupChange: (String) -> Unit,
    onLangChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onHeaderChange: (String) -> Unit,
    onSearchUrlChange: (String) -> Unit,
    onExploreUrlChange: (String) -> Unit,
    onSearchBookListChange: (String) -> Unit,
    onSearchNameChange: (String) -> Unit,
    onSearchAuthorChange: (String) -> Unit,
    onSearchIntroChange: (String) -> Unit,
    onSearchBookUrlChange: (String) -> Unit,
    onSearchCoverUrlChange: (String) -> Unit,
    onSearchKindChange: (String) -> Unit,
    onBookInfoNameChange: (String) -> Unit,
    onBookInfoAuthorChange: (String) -> Unit,
    onBookInfoIntroChange: (String) -> Unit,
    onBookInfoCoverUrlChange: (String) -> Unit,
    onBookInfoKindChange: (String) -> Unit,
    onBookInfoTocUrlChange: (String) -> Unit,
    onTocChapterListChange: (String) -> Unit,
    onTocChapterNameChange: (String) -> Unit,
    onTocChapterUrlChange: (String) -> Unit,
    onTocNextUrlChange: (String) -> Unit,
    onTocIsReverseChange: (Boolean) -> Unit,
    onContentSelectorChange: (String) -> Unit,
    onContentNextUrlChange: (String) -> Unit,
    onContentPurifyChange: (String) -> Unit,
    onContentReplaceRegexChange: (String) -> Unit,
    onExploreBookListChange: (String) -> Unit,
    onExploreNameChange: (String) -> Unit,
    onExploreAuthorChange: (String) -> Unit,
    onExploreBookUrlChange: (String) -> Unit,
    onExploreCoverUrlChange: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val tabs = SourceCreatorTab.entries
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Source" else "Create Source") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShowJson) {
                        Icon(Icons.Default.Code, contentDescription = "View/Import JSON")
                    }
                    IconButton(onClick = onSave, enabled = !state.isSaving) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Validation errors
            if (state.validationErrors.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Validation Errors:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        state.validationErrors.forEach { error ->
                            Text(
                                "• $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = state.currentTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.currentTab == index,
                        onClick = { onTabChange(index) },
                        text = { Text(tab.title) }
                    )
                }
            }
            
            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (state.currentTab) {
                    0 -> BasicInfoTab(
                        state = state,
                        onSourceNameChange = onSourceNameChange,
                        onSourceUrlChange = onSourceUrlChange,
                        onSourceGroupChange = onSourceGroupChange,
                        onLangChange = onLangChange,
                        onCommentChange = onCommentChange,
                        onEnabledChange = onEnabledChange,
                        onHeaderChange = onHeaderChange,
                        onSearchUrlChange = onSearchUrlChange,
                        onExploreUrlChange = onExploreUrlChange
                    )
                    1 -> SearchRulesTab(
                        state = state,
                        onBookListChange = onSearchBookListChange,
                        onNameChange = onSearchNameChange,
                        onAuthorChange = onSearchAuthorChange,
                        onIntroChange = onSearchIntroChange,
                        onBookUrlChange = onSearchBookUrlChange,
                        onCoverUrlChange = onSearchCoverUrlChange,
                        onKindChange = onSearchKindChange
                    )
                    2 -> BookInfoRulesTab(
                        state = state,
                        onNameChange = onBookInfoNameChange,
                        onAuthorChange = onBookInfoAuthorChange,
                        onIntroChange = onBookInfoIntroChange,
                        onCoverUrlChange = onBookInfoCoverUrlChange,
                        onKindChange = onBookInfoKindChange,
                        onTocUrlChange = onBookInfoTocUrlChange
                    )
                    3 -> TocRulesTab(
                        state = state,
                        onChapterListChange = onTocChapterListChange,
                        onChapterNameChange = onTocChapterNameChange,
                        onChapterUrlChange = onTocChapterUrlChange,
                        onNextUrlChange = onTocNextUrlChange,
                        onIsReverseChange = onTocIsReverseChange
                    )
                    4 -> ContentRulesTab(
                        state = state,
                        onContentSelectorChange = onContentSelectorChange,
                        onNextUrlChange = onContentNextUrlChange,
                        onPurifyChange = onContentPurifyChange,
                        onReplaceRegexChange = onContentReplaceRegexChange
                    )
                    5 -> ExploreRulesTab(
                        state = state,
                        onBookListChange = onExploreBookListChange,
                        onNameChange = onExploreNameChange,
                        onAuthorChange = onExploreAuthorChange,
                        onBookUrlChange = onExploreBookUrlChange,
                        onCoverUrlChange = onExploreCoverUrlChange
                    )
                }
            }
        }
    }
}
