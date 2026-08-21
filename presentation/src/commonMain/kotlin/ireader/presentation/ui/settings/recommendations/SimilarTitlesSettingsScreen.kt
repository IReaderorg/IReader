package ireader.presentation.ui.settings.recommendations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import ireader.domain.models.prefs.PreferenceValues
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.components.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarTitlesSettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SimilarTitlesSettingsViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val listState = rememberSaveable(
        key = "settings_similar_titles_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    val showSimilarTitles by viewModel.showSimilarTitles.collectAsState()
    val similarTitlesSource by viewModel.similarTitlesSource.collectAsState()
    val similarTitlesMatchMode by viewModel.similarTitlesMatchMode.collectAsState()
    val similarTitlesMaxCount by viewModel.similarTitlesMaxCount.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.similar_titles_settings),
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
        ) {
            item {
                SettingsSectionHeader(
                    title = localizeHelper.localize(Res.string.similar_titles_section_title),
                    icon = Icons.Outlined.Search
                )
            }

            item {
                SettingsHighlightCard(
                    title = localizeHelper.localize(Res.string.show_similar_titles),
                    description = localizeHelper.localize(Res.string.show_similar_titles_subtitle),
                    icon = if (showSimilarTitles) Icons.Outlined.Search else Icons.Outlined.SearchOff,
                    onClick = { viewModel.setShowSimilarTitles(!showSimilarTitles) },
                    containerColor = if (showSimilarTitles) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.similar_titles_source),
                    description = localizeHelper.localize(Res.string.similar_titles_source_subtitle),
                    icon = Icons.Outlined.Search,
                    onClick = { viewModel.showSourceSelectionDialog() },
                    enabled = showSimilarTitles
                ) {
                    Text(
                        text = stringResource(
                            when (similarTitlesSource) {
                                PreferenceValues.SimilarTitlesSource.SameSource -> Res.string.similar_titles_source_same_source
                                PreferenceValues.SimilarTitlesSource.OtherSources -> Res.string.similar_titles_source_other_sources
                                PreferenceValues.SimilarTitlesSource.AllSources -> Res.string.similar_titles_source_all_sources
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (showSimilarTitles) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }

            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.similar_titles_match_mode),
                    description = localizeHelper.localize(Res.string.similar_titles_match_mode_subtitle),
                    icon = Icons.Outlined.Search,
                    onClick = { viewModel.showMatchModeSelectionDialog() },
                    enabled = showSimilarTitles
                ) {
                    Text(
                        text = stringResource(
                            when (similarTitlesMatchMode) {
                                PreferenceValues.SimilarTitlesMatchMode.ByName -> Res.string.similar_titles_mode_by_name
                                PreferenceValues.SimilarTitlesMatchMode.ByGenre -> Res.string.similar_titles_mode_by_genre
                                PreferenceValues.SimilarTitlesMatchMode.ByCategory -> Res.string.similar_titles_mode_by_category
                            }
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (showSimilarTitles) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }

            item {
                SettingsItemWithTrailing(
                    title = localizeHelper.localize(Res.string.max_similar_titles_count),
                    description = localizeHelper.localize(Res.string.max_similar_titles_count_subtitle),
                    icon = Icons.Outlined.List,
                    onClick = { viewModel.showMaxCountDialog() },
                    enabled = showSimilarTitles
                ) {
                    Text(
                        text = if (similarTitlesMaxCount <= 0) 
                            localizeHelper.localize(Res.string.similar_titles_max_count_unlimited) 
                        else 
                            similarTitlesMaxCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (showSimilarTitles) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
        }
    }

    if (viewModel.showSourceDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSourceDialog() },
            title = { Text(localizeHelper.localize(Res.string.similar_titles_source)) },
            text = {
                Column {
                    val sources = PreferenceValues.SimilarTitlesSource.entries
                    sources.forEach { source ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = similarTitlesSource == source,
                                onClick = {
                                    viewModel.setSimilarTitlesSource(source)
                                    viewModel.dismissSourceDialog()
                                }
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text(
                                text = stringResource(
                                    when (source) {
                                        PreferenceValues.SimilarTitlesSource.SameSource -> Res.string.similar_titles_source_same_source
                                        PreferenceValues.SimilarTitlesSource.OtherSources -> Res.string.similar_titles_source_other_sources
                                        PreferenceValues.SimilarTitlesSource.AllSources -> Res.string.similar_titles_source_all_sources
                                    }
                                ),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSourceDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }

    if (viewModel.showMatchModeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMatchModeDialog() },
            title = { Text(localizeHelper.localize(Res.string.similar_titles_match_mode)) },
            text = {
                Column {
                    val modes = PreferenceValues.SimilarTitlesMatchMode.entries
                    modes.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = similarTitlesMatchMode == mode,
                                onClick = {
                                    viewModel.setSimilarTitlesMatchMode(mode)
                                    viewModel.dismissMatchModeDialog()
                                }
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text(
                                text = stringResource(
                                    when (mode) {
                                        PreferenceValues.SimilarTitlesMatchMode.ByName -> Res.string.similar_titles_mode_by_name
                                        PreferenceValues.SimilarTitlesMatchMode.ByGenre -> Res.string.similar_titles_mode_by_genre
                                        PreferenceValues.SimilarTitlesMatchMode.ByCategory -> Res.string.similar_titles_mode_by_category
                                    }
                                ),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissMatchModeDialog() }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }

    if (viewModel.showMaxCountDialog) {
        var isUnlimited by remember { mutableStateOf(similarTitlesMaxCount <= 0) }
        var textFieldValue by remember { mutableStateOf(if (similarTitlesMaxCount <= 0) "" else similarTitlesMaxCount.toString()) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissMaxCountDialog() },
            title = { Text(localizeHelper.localize(Res.string.max_similar_titles_count)) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isUnlimited,
                            onCheckedChange = { checked ->
                                isUnlimited = checked
                            }
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Text(localizeHelper.localize(Res.string.similar_titles_max_count_unlimited))
                    }
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            if (!isUnlimited) {
                                val filtered = newValue.filter { it.isDigit() }
                                textFieldValue = filtered
                            }
                        },
                        enabled = !isUnlimited,
                        label = { Text(localizeHelper.localize(Res.string.max_similar_titles_count)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val count = if (isUnlimited) 0 else (textFieldValue.toIntOrNull() ?: 10)
                    viewModel.setSimilarTitlesMaxCount(count)
                    viewModel.dismissMaxCountDialog()
                }) {
                    Text(localizeHelper.localize(Res.string.ok))
                }
            }
        )
    }
}
