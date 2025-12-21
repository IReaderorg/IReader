package ireader.presentation.ui.sourcecreator.autodetect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ireader.domain.usersource.autodetect.SelectorAutoDetector
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Screen for auto-detecting CSS selectors from a webpage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoDetectScreen(
    state: AutoDetectState,
    onBack: () -> Unit,
    onUrlChange: (String) -> Unit,
    onPageTypeChange: (PageType) -> Unit,
    onAnalyze: () -> Unit,
    onSelectResult: (String, SelectorAutoDetector.DetectionResult) -> Unit,
    onApplySelections: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.auto_detect_selectors)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Help card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.enter_a_page_url_and),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // URL input
            OutlinedTextField(
                value = state.url,
                onValueChange = onUrlChange,
                label = { Text(localizeHelper.localize(Res.string.page_url)) },
                placeholder = { Text("https://example.com/search?q=test") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Page type selection
            Text(
                text = localizeHelper.localize(Res.string.what_type_of_page_is_this),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PageType.entries.forEach { type ->
                    FilterChip(
                        selected = state.pageType == type,
                        onClick = { onPageTypeChange(type) },
                        label = { Text(type.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Analyze button
            Button(
                onClick = onAnalyze,
                enabled = state.url.isNotBlank() && !state.isAnalyzing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.analyzing))
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.analyze_page))
                }
            }
            
            // Error display
            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Results
            if (state.detectionResult != null) {
                DetectionResults(
                    result = state.detectionResult,
                    selectedResults = state.selectedResults,
                    onSelectResult = onSelectResult,
                    onApply = onApplySelections
                )
            }
        }
    }
}

@Composable
private fun DetectionResults(
    result: SelectorAutoDetector.PageDetectionResult,
    selectedResults: Map<String, SelectorAutoDetector.DetectionResult>,
    onSelectResult: (String, SelectorAutoDetector.DetectionResult) -> Unit,
    onApply: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = localizeHelper.localize(Res.string.detected_selectors),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = localizeHelper.localize(Res.string.tap_to_select_the_best_option_for_each_field),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (result.bookList.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.book_list_container),
                        fieldKey = "bookList",
                        results = result.bookList,
                        selectedResult = selectedResults["bookList"],
                        onSelect = onSelectResult
                    )
                }
            }
            
            if (result.title.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.title),
                        fieldKey = "title",
                        results = result.title,
                        selectedResult = selectedResults["title"],
                        onSelect = onSelectResult
                    )
                }
            }
            
            if (result.author.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.author),
                        fieldKey = "author",
                        results = result.author,
                        selectedResult = selectedResults["author"],
                        onSelect = onSelectResult
                    )
                }
            }
            
            if (result.cover.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.cover_image),
                        fieldKey = "cover",
                        results = result.cover,
                        selectedResult = selectedResults["cover"],
                        onSelect = onSelectResult
                    )
                }
            }
            
            if (result.description.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.description),
                        fieldKey = "description",
                        results = result.description,
                        selectedResult = selectedResults["description"],
                        onSelect = onSelectResult
                    )
                }
            }
            
            if (result.link.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.link_url),
                        fieldKey = "link",
                        results = result.link,
                        selectedResult = selectedResults["link"],
                        onSelect = onSelectResult
                    )
                }
            }
            
            if (result.chapterList.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.chapter_list),
                        fieldKey = "chapterList",
                        results = result.chapterList,
                        selectedResult = selectedResults["chapterList"],
                        onSelect = onSelectResult
                    )
                }
            }
            
            if (result.content.isNotEmpty()) {
                item {
                    DetectionCategory(
                        title = localizeHelper.localize(Res.string.content),
                        fieldKey = "content",
                        results = result.content,
                        selectedResult = selectedResults["content"],
                        onSelect = onSelectResult
                    )
                }
            }
        }
        
        // Apply button
        if (selectedResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply ${selectedResults.size} Selector(s)")
            }
        }
    }
}


@Composable
private fun DetectionCategory(
    title: String,
    fieldKey: String,
    results: List<SelectorAutoDetector.DetectionResult>,
    selectedResult: SelectorAutoDetector.DetectionResult?,
    onSelect: (String, SelectorAutoDetector.DetectionResult) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            results.forEach { result ->
                val isSelected = selectedResult == result
                
                Card(
                    onClick = { onSelect(fieldKey, result) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelect(fieldKey, result) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.selector,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = result.sampleText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        
                        // Confidence indicator
                        ConfidenceBadge(confidence = result.confidence)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val color = when {
        confidence >= 0.8f -> MaterialTheme.colorScheme.primary
        confidence >= 0.6f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Page types for auto-detection.
 */
enum class PageType(val displayName: String) {
    SEARCH("Search"),
    BOOK_INFO("Book Info"),
    CHAPTERS("Chapters"),
    CONTENT("Content")
}

/**
 * State for auto-detect screen.
 */
@Stable
data class AutoDetectState(
    val url: String = "",
    val pageType: PageType = PageType.SEARCH,
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    val detectionResult: SelectorAutoDetector.PageDetectionResult? = null,
    val selectedResults: Map<String, SelectorAutoDetector.DetectionResult> = emptyMap()
)
