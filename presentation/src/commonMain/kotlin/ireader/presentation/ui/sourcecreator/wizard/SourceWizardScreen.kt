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
    val steps = WizardStep.entries
    val currentIndex = steps.indexOf(state.currentStep)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Source") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                label = "wizard_step"
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
                    Text("Back")
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
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "How would you like to create your source?",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Start from scratch option
        item {
            MethodCard(
                icon = Icons.Default.Create,
                title = "Start from Scratch",
                description = "Create a completely custom source",
                onClick = onStartFromScratch
            )
        }
        
        item {
            Text(
                text = "Or choose a template:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Templates
        items(SourceTemplates.allTemplates) { template ->
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HelpCard(
            text = "Enter the website's name and URL. The URL should be the main page of the novel site (e.g., https://example.com)"
        )
        
        OutlinedTextField(
            value = sourceName,
            onValueChange = onSourceNameChange,
            label = { Text("Source Name *") },
            placeholder = { Text("My Novel Site") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = sourceUrl,
            onValueChange = onSourceUrlChange,
            label = { Text("Website URL *") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = sourceGroup,
            onValueChange = onSourceGroupChange,
            label = { Text("Group (optional)") },
            placeholder = { Text("English, Chinese, etc.") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = lang,
            onValueChange = onLangChange,
            label = { Text("Language Code") },
            placeholder = { Text("en, zh, ko, etc.") },
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = "Configure how to search for books. The 'Book List' selector finds each book in search results. Use {{key}} for the search term in the URL."
            )
        }
        
        item {
            OutlinedTextField(
                value = source.searchUrl,
                onValueChange = { onSelectorChange("searchUrl", it) },
                label = { Text("Search URL *") },
                placeholder = { Text("{{baseUrl}}/search?q={{key}}") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Use {{key}} for search term, {{page}} for page number") }
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleSearch.bookList,
                onValueChange = { onSelectorChange("search.bookList", it) },
                label = "Book List Selector *",
                suggestions = suggestions
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleSearch.name,
                onValueChange = { onSelectorChange("search.name", it) },
                label = "Book Name Selector *",
                suggestions = SelectorSuggestions.titleSelectors
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleSearch.bookUrl,
                onValueChange = { onSelectorChange("search.bookUrl", it) },
                label = "Book URL Selector *",
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = "Configure how to get book details from the book's page. These selectors find the title, author, description, etc."
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleBookInfo.name,
                onValueChange = { onSelectorChange("bookInfo.name", it) },
                label = "Book Name Selector",
                suggestions = SelectorSuggestions.titleSelectors
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleBookInfo.author,
                onValueChange = { onSelectorChange("bookInfo.author", it) },
                label = "Author Selector",
                suggestions = SelectorSuggestions.authorSelectors
            )
        }
        
        item {
            OutlinedTextField(
                value = source.ruleBookInfo.intro,
                onValueChange = { onSelectorChange("bookInfo.intro", it) },
                label = { Text("Description Selector") },
                placeholder = { Text("div.description") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleBookInfo.coverUrl,
                onValueChange = { onSelectorChange("bookInfo.coverUrl", it) },
                label = "Cover Image Selector",
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = "Configure how to get the chapter list. The 'Chapter List' selector finds each chapter item."
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleToc.chapterList,
                onValueChange = { onSelectorChange("toc.chapterList", it) },
                label = "Chapter List Selector *",
                suggestions = suggestions
            )
        }
        
        item {
            OutlinedTextField(
                value = source.ruleToc.chapterName,
                onValueChange = { onSelectorChange("toc.chapterName", it) },
                label = { Text("Chapter Name Selector *") },
                placeholder = { Text("a") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleToc.chapterUrl,
                onValueChange = { onSelectorChange("toc.chapterUrl", it) },
                label = "Chapter URL Selector *",
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            HelpCard(
                text = "Configure how to read chapter content. The 'Content' selector finds the main text of each chapter."
            )
        }
        
        item {
            SelectorFieldWithSuggestions(
                value = source.ruleContent.content,
                onValueChange = { onSelectorChange("content.content", it) },
                label = "Content Selector *",
                suggestions = suggestions
            )
        }
        
        item {
            OutlinedTextField(
                value = source.ruleContent.purify,
                onValueChange = { onSelectorChange("content.purify", it) },
                label = { Text("Remove Elements (optional)") },
                placeholder = { Text("div.ads, script") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Comma-separated selectors for ads, scripts, etc.") }
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HelpCard(
            text = "Review your source configuration and test it before saving."
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        when {
            result == null -> Icon(Icons.Default.HorizontalRule, contentDescription = "Not tested")
            result.success -> Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.primary)
            else -> Icon(Icons.Default.Error, contentDescription = "Failed", tint = MaterialTheme.colorScheme.error)
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
                        contentDescription = "Suggestions"
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
