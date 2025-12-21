package ireader.presentation.ui.sourcecreator.wizard

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.usersource.templates.SourceTemplates
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Step-by-step wizard for creating sources.
 * Designed for users who don't understand CSS selectors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceWizardScreen(
    state: SourceWizardState,
    onBack: () -> Unit,
    onStepChange: (WizardStep) -> Unit,
    onSelectTemplate: (SourceTemplates.SourceTemplate) -> Unit,
    onStartFromScratch: () -> Unit,
    onSourceNameChange: (String) -> Unit,
    onSourceUrlChange: (String) -> Unit,
    onSourceGroupChange: (String) -> Unit,
    onLangChange: (String) -> Unit,
    onSelectorChange: (String, String) -> Unit,
    onTestSearch: () -> Unit,
    onTestChapters: () -> Unit,
    onTestContent: () -> Unit,
    onSave: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val steps = WizardStep.entries
    val currentIndex = steps.indexOf(state.currentStep)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.create_source)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = localizeHelper.localize(Res.string.close))
                    }
                }
            )
        },
        bottomBar = {
            WizardBottomBar(
                currentStep = state.currentStep,
                canGoBack = currentIndex > 0,
                canGoNext = canProceed(state),
                isLastStep = state.currentStep == WizardStep.TEST_AND_SAVE,
                onBack = { 
                    if (currentIndex > 0) onStepChange(steps[currentIndex - 1])
                },
                onNext = {
                    if (currentIndex < steps.lastIndex) {
                        onStepChange(steps[currentIndex + 1])
                    } else {
                        onSave()
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
        ) {
            // Progress indicator
            WizardProgressIndicator(
                steps = steps,
                currentStep = state.currentStep
            )
            
            // Step content
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = localizeHelper.localize(Res.string.wizard_step)
            ) { step ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (step) {
                        WizardStep.CHOOSE_METHOD -> ChooseMethodStep(
                            onSelectTemplate = onSelectTemplate,
                            onStartFromScratch = onStartFromScratch
                        )
                        WizardStep.BASIC_INFO -> BasicInfoStep(
                            sourceName = state.sourceName,
                            sourceUrl = state.sourceUrl,
                            sourceGroup = state.sourceGroup,
                            lang = state.lang,
                            onSourceNameChange = onSourceNameChange,
                            onSourceUrlChange = onSourceUrlChange,
                            onSourceGroupChange = onSourceGroupChange,
                            onLangChange = onLangChange
                        )
                        WizardStep.SEARCH_RULES -> SearchRulesStep(
                            source = state.currentSource,
                            suggestions = SelectorSuggestions.bookListSelectors,
                            onSelectorChange = onSelectorChange,
                            testResult = state.testSearchResult,
                            onTest = onTestSearch,
                            isTesting = state.isTesting
                        )
                        WizardStep.BOOK_INFO_RULES -> BookInfoRulesStep(
                            source = state.currentSource,
                            onSelectorChange = onSelectorChange
                        )
                        WizardStep.CHAPTER_RULES -> ChapterRulesStep(
                            source = state.currentSource,
                            suggestions = SelectorSuggestions.chapterListSelectors,
                            onSelectorChange = onSelectorChange,
                            testResult = state.testChaptersResult,
                            onTest = onTestChapters,
                            isTesting = state.isTesting
                        )
                        WizardStep.CONTENT_RULES -> ContentRulesStep(
                            source = state.currentSource,
                            suggestions = SelectorSuggestions.contentSelectors,
                            onSelectorChange = onSelectorChange,
                            testResult = state.testContentResult,
                            onTest = onTestContent,
                            isTesting = state.isTesting
                        )
                        WizardStep.TEST_AND_SAVE -> TestAndSaveStep(
                            source = state.currentSource,
                            searchResult = state.testSearchResult,
                            chaptersResult = state.testChaptersResult,
                            contentResult = state.testContentResult,
                            onTestAll = {
                                onTestSearch()
                                onTestChapters()
                                onTestContent()
                            },
                            isTesting = state.isTesting
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardProgressIndicator(
    steps: List<WizardStep>,
    currentStep: WizardStep
) {
    val currentIndex = steps.indexOf(currentStep)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Step title
        Text(
            text = "Step ${currentIndex + 1} of ${steps.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = currentStep.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = currentStep.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / steps.size },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WizardBottomBar(
    currentStep: WizardStep,
    canGoBack: Boolean,
    canGoNext: Boolean,
    isLastStep: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (canGoBack) {
                OutlinedButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.back))
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            Button(
                onClick = onNext,
                enabled = canGoNext
            ) {
                Text(if (isLastStep) "Save Source" else "Next")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (isLastStep) Icons.Default.Check else Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ChooseMethodStep(
    onSelectTemplate: (SourceTemplates.SourceTemplate) -> Unit,
    onStartFromScratch: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = localizeHelper.localize(Res.string.how_would_you_like_to_create_your_source),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Start from scratch option
        item {
            MethodCard(
                icon = Icons.Default.Create,
                title = localizeHelper.localize(Res.string.start_from_scratch),
                description = "Create a completely custom source",
                onClick = onStartFromScratch
            )
        }
        
        item {
            Text(
                text = localizeHelper.localize(Res.string.or_choose_a_template),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Templates
        items(SourceTemplates.allTemplates, key = { it.id }) { template ->
            TemplateCard(
                template = template,
                onClick = { onSelectTemplate(template) }
            )
        }
    }
}

@Composable
private fun MethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: SourceTemplates.SourceTemplate,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(template.category.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun BasicInfoStep(
    sourceName: String,
    sourceUrl: String,
    sourceGroup: String,
    lang: String,
    onSourceNameChange: (String) -> Unit,
    onSourceUrlChange: (String) -> Unit,
    onSourceGroupChange: (String) -> Unit,
    onLangChange: (String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HelpCard(
            text = localizeHelper.localize(Res.string.enter_the_websites_name_and)
        )
        
        OutlinedTextField(
            value = sourceName,
            onValueChange = onSourceNameChange,
            label = { Text(localizeHelper.localize(Res.string.source_name)) },
            placeholder = { Text(localizeHelper.localize(Res.string.my_novel_site)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = sourceUrl,
            onValueChange = onSourceUrlChange,
            label = { Text(localizeHelper.localize(Res.string.website_url)) },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = sourceGroup,
            onValueChange = onSourceGroupChange,
            label = { Text(localizeHelper.localize(Res.string.group_optional)) },
            placeholder = { Text(localizeHelper.localize(Res.string.english_chinese_etc)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = lang,
            onValueChange = onLangChange,
            label = { Text(localizeHelper.localize(Res.string.language_code)) },
            placeholder = { Text(localizeHelper.localize(Res.string.en_zh_ko_etc)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun SearchRulesStep(
    source: ireader.domain.usersource.model.UserSource,
    suggestions: List<Pair<String, String>>,
    onSelectorChange: (String, String) -> Unit,
    testResult: TestResult?,
    onTest: () -> Unit,
    isTesting: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = localizeHelper.localize(Res.string.configure_how_to_search_for)
            )
        }
        
        item {
            OutlinedTextField(
                value = source.searchUrl,
                onValueChange = { onSelectorChange("searchUrl", it) },
                label = { Text(localizeHelper.localize(Res.string.search_url_1)) },
                placeholder = { Text(localizeHelper.localize(Res.string.baseurlsearchqkey)) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text(localizeHelper.localize(Res.string.use_key_for_search_term_page_for_page_number)) }
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleSearch.bookList,
                onValueChange = { onSelectorChange("search.bookList", it) },
                label = localizeHelper.localize(Res.string.book_list_selector),
                suggestions = suggestions
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleSearch.name,
                onValueChange = { onSelectorChange("search.name", it) },
                label = localizeHelper.localize(Res.string.book_name_selector),
                suggestions = SelectorSuggestions.titleSelectors
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleSearch.bookUrl,
                onValueChange = { onSelectorChange("search.bookUrl", it) },
                label = localizeHelper.localize(Res.string.book_url_selector),
                suggestions = SelectorSuggestions.linkSelectors
            )
        }
        
        item {
            TestResultCard(
                result = testResult,
                onTest = onTest,
                isTesting = isTesting,
                testLabel = "Test Search"
            )
        }
    }
}

@Composable
private fun BookInfoRulesStep(
    source: ireader.domain.usersource.model.UserSource,
    onSelectorChange: (String, String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = localizeHelper.localize(Res.string.configure_how_to_get_book)
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleBookInfo.name,
                onValueChange = { onSelectorChange("bookInfo.name", it) },
                label = localizeHelper.localize(Res.string.book_name_selector_1),
                suggestions = SelectorSuggestions.titleSelectors
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleBookInfo.author,
                onValueChange = { onSelectorChange("bookInfo.author", it) },
                label = localizeHelper.localize(Res.string.author_selector),
                suggestions = SelectorSuggestions.authorSelectors
            )
        }
        
        item {
            OutlinedTextField(
                value = source.ruleBookInfo.intro,
                onValueChange = { onSelectorChange("bookInfo.intro", it) },
                label = { Text(localizeHelper.localize(Res.string.description_selector)) },
                placeholder = { Text(localizeHelper.localize(Res.string.divdescription)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleBookInfo.coverUrl,
                onValueChange = { onSelectorChange("bookInfo.coverUrl", it) },
                label = localizeHelper.localize(Res.string.cover_image_selector),
                suggestions = SelectorSuggestions.coverSelectors
            )
        }
    }
}

@Composable
private fun ChapterRulesStep(
    source: ireader.domain.usersource.model.UserSource,
    suggestions: List<Pair<String, String>>,
    onSelectorChange: (String, String) -> Unit,
    testResult: TestResult?,
    onTest: () -> Unit,
    isTesting: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = localizeHelper.localize(Res.string.configure_how_to_get_the)
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleToc.chapterList,
                onValueChange = { onSelectorChange("toc.chapterList", it) },
                label = localizeHelper.localize(Res.string.chapter_list_selector),
                suggestions = suggestions
            )
        }
        
        item {
            OutlinedTextField(
                value = source.ruleToc.chapterName,
                onValueChange = { onSelectorChange("toc.chapterName", it) },
                label = { Text(localizeHelper.localize(Res.string.chapter_name_selector)) },
                placeholder = { Text("a") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleToc.chapterUrl,
                onValueChange = { onSelectorChange("toc.chapterUrl", it) },
                label = localizeHelper.localize(Res.string.chapter_url_selector),
                suggestions = SelectorSuggestions.linkSelectors
            )
        }
        
        item {
            TestResultCard(
                result = testResult,
                onTest = onTest,
                isTesting = isTesting,
                testLabel = "Test Chapters"
            )
        }
    }
}

@Composable
private fun ContentRulesStep(
    source: ireader.domain.usersource.model.UserSource,
    suggestions: List<Pair<String, String>>,
    onSelectorChange: (String, String) -> Unit,
    testResult: TestResult?,
    onTest: () -> Unit,
    isTesting: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = localizeHelper.localize(Res.string.configure_how_to_read_chapter)
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleContent.content,
                onValueChange = { onSelectorChange("content.content", it) },
                label = localizeHelper.localize(Res.string.content_selector),
                suggestions = suggestions
            )
        }
        
        item {
            OutlinedTextField(
                value = source.ruleContent.purify,
                onValueChange = { onSelectorChange("content.purify", it) },
                label = { Text(localizeHelper.localize(Res.string.remove_elements_optional)) },
                placeholder = { Text(localizeHelper.localize(Res.string.divads_script)) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text(localizeHelper.localize(Res.string.comma_separated_selectors_for_ads_scripts_etc)) }
            )
        }
        
        item {
            TestResultCard(
                result = testResult,
                onTest = onTest,
                isTesting = isTesting,
                testLabel = "Test Content"
            )
        }
    }
}

@Composable
private fun TestAndSaveStep(
    source: ireader.domain.usersource.model.UserSource,
    searchResult: TestResult?,
    chaptersResult: TestResult?,
    contentResult: TestResult?,
    onTestAll: () -> Unit,
    isTesting: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HelpCard(
            text = localizeHelper.localize(Res.string.review_your_source_configuration_and)
        )
        
        // Summary card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = source.sourceName.ifBlank { "Unnamed Source" },
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = source.sourceUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Test results
        TestStatusRow("Search", searchResult)
        TestStatusRow("Chapters", chaptersResult)
        TestStatusRow("Content", contentResult)
        
        Button(
            onClick = onTestAll,
            enabled = !isTesting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isTesting) "Testing..." else "Test All")
        }
    }
}

@Composable
private fun TestStatusRow(label: String, result: TestResult?) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        when {
            result == null -> Icon(Icons.Default.HorizontalRule, contentDescription = localizeHelper.localize(Res.string.not_tested))
            result.success -> Icon(Icons.Default.CheckCircle, contentDescription = localizeHelper.localize(Res.string.notification_success), tint = MaterialTheme.colorScheme.primary)
            else -> Icon(Icons.Default.Error, contentDescription = localizeHelper.localize(Res.string.failed), tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun HelpCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SelectorFieldWithSuggestions(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<Pair<String, String>>
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = localizeHelper.localize(Res.string.suggestions)
                    )
                }
            }
        )
        
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Common selectors:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    suggestions.forEach { (selector, desc) ->
                        TextButton(
                            onClick = {
                                onValueChange(selector)
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(selector, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestResultCard(
    result: TestResult?,
    onTest: () -> Unit,
    isTesting: Boolean,
    testLabel: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                result == null -> MaterialTheme.colorScheme.surfaceVariant
                result.success -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result?.message ?: "Not tested yet",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Button(
                    onClick = onTest,
                    enabled = !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(testLabel)
                    }
                }
            }
            
            if (result?.sampleData?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sample results:",
                    style = MaterialTheme.typography.labelSmall
                )
                result.sampleData.take(3).forEach { sample ->
                    Text(
                        "• $sample",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun canProceed(state: SourceWizardState): Boolean {
    return when (state.currentStep) {
        WizardStep.CHOOSE_METHOD -> true
        WizardStep.BASIC_INFO -> state.sourceName.isNotBlank() && state.sourceUrl.isNotBlank()
        WizardStep.SEARCH_RULES -> state.currentSource.searchUrl.isNotBlank() &&
                state.currentSource.ruleSearch.bookList.isNotBlank()
        WizardStep.BOOK_INFO_RULES -> true // Optional
        WizardStep.CHAPTER_RULES -> state.currentSource.ruleToc.chapterList.isNotBlank()
        WizardStep.CONTENT_RULES -> state.currentSource.ruleContent.content.isNotBlank()
        WizardStep.TEST_AND_SAVE -> true
    }
}
