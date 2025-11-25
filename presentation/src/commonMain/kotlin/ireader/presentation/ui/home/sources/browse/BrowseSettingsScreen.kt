package ireader.presentation.ui.home.sources.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseSettingsScreen(
    vm: BrowseSettingsViewModel,
    onNavigateUp: () -> Unit,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    LaunchedEffect(Unit) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(localizeHelper)
                    )
                }
                else -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Browse Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.resetToDefaults() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset to defaults"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Performance Section
            item {
                SectionHeader(
                    icon = Icons.Default.Speed,
                    title = "Search Performance"
                )
            }

            item {
                ConcurrentSearchesControl(
                    value = vm.state.concurrentSearches,
                    onValueChange = { vm.setConcurrentSearches(it) }
                )
            }

            // Language Filters Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    icon = Icons.Default.Language,
                    title = "Language Preferences"
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Choose Your Languages",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Select the languages you want to see when browsing and searching for content. You can select multiple languages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                LanguageToggleGroup(
                    selectedLanguages = vm.state.selectedLanguages,
                    onLanguageToggle = { vm.toggleLanguage(it) }
                )
            }

            // Advanced Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    icon = Icons.Default.Settings,
                    title = "Advanced"
                )
            }

            item {
                SearchTimeoutControl(
                    value = vm.state.searchTimeout,
                    onValueChange = { vm.setSearchTimeout(it) }
                )
            }

            item {
                MaxResultsControl(
                    value = vm.state.maxResultsPerSource,
                    onValueChange = { vm.setMaxResultsPerSource(it) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ConcurrentSearchesControl(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Concurrent Searches",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Number of sources to search simultaneously. Higher values are faster but use more resources.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchTimeoutControl(
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search Timeout",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${value / 1000}s",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = (value / 1000).toFloat(),
            onValueChange = { onValueChange((it * 1000).toLong()) },
            valueRange = 10f..60f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Maximum time to wait for search results from each source",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MaxResultsControl(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Max Results Per Source",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 10f..100f,
            steps = 17,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Maximum number of results to display from each source",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
