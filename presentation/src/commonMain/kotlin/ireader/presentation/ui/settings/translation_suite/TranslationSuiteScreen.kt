package ireader.presentation.ui.settings.translation_suite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSuiteScreen(
    state: TranslationSuiteState,
    onNavigateUp: () -> Unit,
    onSelectEngine: (TranslationEngineChoice) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onToggleAutoTranslateChapters: (Boolean) -> Unit,
    onToggleAutoTranslateNovelNames: (Boolean) -> Unit,
    onTogglePreset: (String, Boolean) -> Unit,
    onGlossarySearch: (String) -> Unit,
    onAddGlossaryTerm: (String, String, String) -> Unit,
    onDeleteGlossaryTerm: (Long) -> Unit,
    onSetShowAddGlossaryDialog: (Boolean) -> Unit,
    onSetShowAddRuleDialog: (Boolean) -> Unit,
    onAddCustomReplacement: (String, String, Boolean) -> Unit,
    onDeleteCustomReplacement: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Engine & Languages", "Glossary", "Text Cleanup")

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Translation & Text Suite",
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Tab Bar
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // Tab 0: Engine & Languages
            if (selectedTab == 0) {
                item {
                    EngineSelectionCard(
                        selectedEngine = state.selectedEngine,
                        onSelectEngine = onSelectEngine
                    )
                }

                item {
                    LanguageAndApiCard(
                        state = state,
                        onSelectLanguage = onSelectLanguage,
                        onApiKeyChange = onApiKeyChange
                    )
                }

                item {
                    AutomationCard(
                        state = state,
                        onToggleAutoTranslateChapters = onToggleAutoTranslateChapters,
                        onToggleAutoTranslateNovelNames = onToggleAutoTranslateNovelNames
                    )
                }
            }

            // Tab 1: Glossary Dictionary
            if (selectedTab == 1) {
                item {
                    GlossaryHeaderSection(
                        searchQuery = state.glossarySearchQuery,
                        onSearchChange = onGlossarySearch,
                        onAddClick = { onSetShowAddGlossaryDialog(true) }
                    )
                }

                val filteredTerms = if (state.glossarySearchQuery.isBlank()) {
                    state.glossaryTerms
                } else {
                    state.glossaryTerms.filter {
                        it.sourceTerm.contains(state.glossarySearchQuery, ignoreCase = true) ||
                        it.targetTerm.contains(state.glossarySearchQuery, ignoreCase = true)
                    }
                }

                if (filteredTerms.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Icon(
                                    imageVector = Icons.Outlined.Translate,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Glossary Terms Added", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Add custom translations for character names and cultivation terms.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTerms, key = { it.id }) { term ->
                        GlossaryTermCard(
                            term = term,
                            onDelete = { onDeleteGlossaryTerm(term.id) }
                        )
                    }
                }
            }

            // Tab 2: Text Cleanup & Regex
            if (selectedTab == 2) {
                item {
                    Text("1-Tap Text Cleanup Presets", style = MaterialTheme.typography.titleMedium)
                }

                items(state.activePresets, key = { it.id }) { preset ->
                    PresetCleanupCard(
                        preset = preset,
                        onToggle = { onTogglePreset(preset.id, it) }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Custom Regex Replacements", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { onSetShowAddRuleDialog(true) }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add Rule")
                        }
                    }
                }

                if (state.textReplacements.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No custom regex rules yet. Tap + to create one.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(state.textReplacements, key = { it.id }) { rule ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rule.findText, style = MaterialTheme.typography.titleSmall)
                                    Text("➔ \"${rule.replaceText}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDeleteCustomReplacement(rule.id) }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        // Add Glossary Dialog
        if (state.showAddGlossaryDialog) {
            AddGlossaryDialog(
                onDismiss = { onSetShowAddGlossaryDialog(false) },
                onAdd = onAddGlossaryTerm
            )
        }

        // Add Regex Rule Dialog
        if (state.showAddRuleDialog) {
            AddRuleDialog(
                onDismiss = { onSetShowAddRuleDialog(false) },
                onAdd = onAddCustomReplacement
            )
        }
    }
}

@Composable
private fun EngineSelectionCard(
    selectedEngine: TranslationEngineChoice,
    onSelectEngine: (TranslationEngineChoice) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Translation Engine", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineOptionRow(
                    title = "Google Translate",
                    subtitle = "Fast, free, and supports 100+ languages",
                    isSelected = selectedEngine == TranslationEngineChoice.GOOGLE_TRANSLATE,
                    onClick = { onSelectEngine(TranslationEngineChoice.GOOGLE_TRANSLATE) }
                )

                EngineOptionRow(
                    title = "DeepL Translator",
                    subtitle = "High quality natural translations (API Key required)",
                    isSelected = selectedEngine == TranslationEngineChoice.DEEPL,
                    onClick = { onSelectEngine(TranslationEngineChoice.DEEPL) }
                )

                EngineOptionRow(
                    title = "LibreTranslate",
                    subtitle = "Open source self-hosted translation engine",
                    isSelected = selectedEngine == TranslationEngineChoice.LIBRE_TRANSLATE,
                    onClick = { onSelectEngine(TranslationEngineChoice.LIBRE_TRANSLATE) }
                )

                EngineOptionRow(
                    title = "Cloudflare AI",
                    subtitle = "Fast serverless neural machine translation",
                    isSelected = selectedEngine == TranslationEngineChoice.CLOUDFLARE_AI,
                    onClick = { onSelectEngine(TranslationEngineChoice.CLOUDFLARE_AI) }
                )
            }
        }
    }
}

@Composable
private fun EngineOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LanguageAndApiCard(
    state: TranslationSuiteState,
    onSelectLanguage: (String) -> Unit,
    onApiKeyChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Target Language", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(state.targetLanguage, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Outlined.Language, contentDescription = null)
                }
            }

            if (state.selectedEngine == TranslationEngineChoice.DEEPL || state.selectedEngine == TranslationEngineChoice.OPENAI) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("API Key", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = onApiKeyChange,
                    placeholder = { Text("Enter API key...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun AutomationCard(
    state: TranslationSuiteState,
    onToggleAutoTranslateChapters: (Boolean) -> Unit,
    onToggleAutoTranslateNovelNames: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAutoTranslateNovelNames(!state.autoTranslateNovelNames) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Translate Novel Titles", style = MaterialTheme.typography.bodyLarge)
                    Text("Translate non-English titles in browse catalog", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.autoTranslateNovelNames, onCheckedChange = onToggleAutoTranslateNovelNames)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAutoTranslateChapters(!state.autoTranslateChapters) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Translate Chapters", style = MaterialTheme.typography.bodyLarge)
                    Text("Translate next chapter in background when reading", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.autoTranslateChapters, onCheckedChange = onToggleAutoTranslateChapters)
            }
        }
    }
}

@Composable
private fun GlossaryHeaderSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search terms...") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        Button(onClick = onAddClick) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Term")
        }
    }
}

@Composable
private fun GlossaryTermCard(
    term: GlossaryTermItem,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(term.sourceTerm, style = MaterialTheme.typography.titleSmall)
                Text("➔ ${term.targetTerm}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                if (term.notes.isNotBlank()) {
                    Text(term.notes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun PresetCleanupCard(
    preset: TextCleanupPreset,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!preset.isEnabled) }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(preset.title, style = MaterialTheme.typography.bodyLarge)
                Text(preset.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = preset.isEnabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun AddGlossaryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var source by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Glossary Term") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Original Term / Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Translated Term / Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(source, target, notes) },
                enabled = source.isNotBlank() && target.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Boolean) -> Unit
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var isRegex by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Text Replacement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("Find Text / Regex") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("Replace With") },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isRegex, onCheckedChange = { isRegex = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Treat as Regular Expression (Regex)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(findText, replaceText, isRegex) },
                enabled = findText.isNotBlank()
            ) {
                Text("Add Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
