package ireader.presentation.ui.home.tts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.tts.PiperVoice
import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceQuality
import ireader.presentation.ui.core.modifier.supportDesktopHorizontalLazyListScroll
import ireader.presentation.ui.core.modifier.supportDesktopScroll
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Enhanced Piper Voice Selection UI with search, filters, and better UX.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiperVoiceSelectionContent(
    voices: List<PiperVoice>,
    selectedVoiceId: String?,
    isLoading: Boolean,
    isRefreshing: Boolean,
    refreshError: String?,
    downloadingVoiceId: String?,
    downloadProgress: Float,
    filterLanguage: String?,
    availableLanguages: List<String>,
    onVoiceSelect: (PiperVoice) -> Unit,
    onVoiceDownload: (PiperVoice) -> Unit,
    onRefresh: () -> Unit,
    onFilterLanguageChange: (String?) -> Unit,
    onDismissError: () -> Unit,
    onVoiceDelete: ((PiperVoice) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var searchQuery by remember { mutableStateOf("") }
    var filterGender by remember { mutableStateOf<VoiceGender?>(null) }
    var filterQuality by remember { mutableStateOf<VoiceQuality?>(null) }
    var showDownloadedOnly by remember { mutableStateOf(false) }
    var expandedFilters by remember { mutableStateOf(false) }
    
    // Apply all filters
    val filteredVoices = remember(voices, filterLanguage, filterGender, filterQuality, searchQuery, showDownloadedOnly) {
        voices.filter { voice ->
            val matchesLanguage = filterLanguage == null || voice.language == filterLanguage
            val matchesGender = filterGender == null || voice.gender == filterGender
            val matchesQuality = filterQuality == null || voice.quality == filterQuality
            val matchesSearch = searchQuery.isBlank() || 
                voice.name.contains(searchQuery, ignoreCase = true) ||
                voice.locale.contains(searchQuery, ignoreCase = true) ||
                voice.language.contains(searchQuery, ignoreCase = true)
            val matchesDownloaded = !showDownloadedOnly || voice.isDownloaded
            matchesLanguage && matchesGender && matchesQuality && matchesSearch && matchesDownloaded
        }
    }
    
    val downloadedCount = voices.count { it.isDownloaded }
    val totalCount = voices.size
    
    val mainListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Split voices into downloaded and available
    val downloadedVoices = filteredVoices.filter { it.isDownloaded }
    val availableVoices = filteredVoices.filter { !it.isDownloaded }
    
    LazyColumn(
        state = mainListState,
        modifier = modifier
            .fillMaxSize()
            .supportDesktopScroll(mainListState, scope)
    ) {
        // Header with stats
        item(key = "header") {
            VoiceSelectionHeader(
                totalVoices = totalCount,
                downloadedVoices = downloadedCount,
                filteredVoices = filteredVoices.size,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh
            )
        }
        
        // Search bar
        item(key = "search") {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Filter chips row
        item(key = "filters") {
            FilterChipsRow(
                availableLanguages = availableLanguages,
                selectedLanguage = filterLanguage,
                selectedGender = filterGender,
                selectedQuality = filterQuality,
                showDownloadedOnly = showDownloadedOnly,
                expandedFilters = expandedFilters,
                onLanguageChange = onFilterLanguageChange,
                onGenderChange = { filterGender = it },
                onQualityChange = { filterQuality = it },
                onDownloadedOnlyChange = { showDownloadedOnly = it },
                onExpandFilters = { expandedFilters = !expandedFilters }
            )
        }
        
        // Error message
        if (refreshError != null) {
            item(key = "error") {
                ErrorBanner(
                    message = refreshError,
                    onDismiss = onDismissError,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        item(key = "divider") {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        // Content based on state
        when {
            isLoading -> {
                item(key = "loading") {
                    LoadingState()
                }
            }
            filteredVoices.isEmpty() -> {
                item(key = "empty") {
                    EmptyState(
                        hasFilters = filterLanguage != null || filterGender != null || 
                            filterQuality != null || searchQuery.isNotBlank() || showDownloadedOnly,
                        onRefresh = onRefresh,
                        onClearFilters = {
                            searchQuery = ""
                            filterGender = null
                            filterQuality = null
                            showDownloadedOnly = false
                            onFilterLanguageChange(null)
                        }
                    )
                }
            }
            else -> {
                // Downloaded section
                if (downloadedVoices.isNotEmpty()) {
                    item(key = "downloaded_header") {
                        SectionHeader(
                            title = localizeHelper.localize(Res.string.downloaded),
                            count = downloadedVoices.size,
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(downloadedVoices, key = { "downloaded_${it.id}" }) { voice ->
                        EnhancedVoiceCard(
                            voice = voice,
                            isSelected = voice.id == selectedVoiceId,
                            isDownloading = downloadingVoiceId == voice.id,
                            downloadProgress = if (downloadingVoiceId == voice.id) downloadProgress else 0f,
                            onSelect = { onVoiceSelect(voice) },
                            onDownload = { },
                            onDelete = onVoiceDelete?.let { { it(voice) } },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Available for download section
                if (availableVoices.isNotEmpty()) {
                    item(key = "available_header") {
                        if (downloadedVoices.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        SectionHeader(
                            title = localizeHelper.localize(Res.string.available_for_download),
                            count = availableVoices.size,
                            icon = Icons.Default.CloudDownload,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    items(availableVoices, key = { "available_${it.id}" }) { voice ->
                        EnhancedVoiceCard(
                            voice = voice,
                            isSelected = false,
                            isDownloading = downloadingVoiceId == voice.id,
                            downloadProgress = if (downloadingVoiceId == voice.id) downloadProgress else 0f,
                            onSelect = { },
                            onDownload = { onVoiceDownload(voice) },
                            onDelete = null,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Bottom padding
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
private fun VoiceSelectionHeader(
    totalVoices: Int,
    downloadedVoices: Int,
    filteredVoices: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.piper_voice_catalog),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatChip(
                        icon = Icons.Default.CloudDownload,
                        label = "$downloadedVoices downloaded",
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatChip(
                        icon = Icons.Default.Language,
                        label = "$totalVoices total",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (filteredVoices != totalVoices) {
                        StatChip(
                            icon = Icons.Default.FilterList,
                            label = "$filteredVoices shown",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            
            FilledTonalIconButton(
                onClick = onRefresh,
                enabled = !isRefreshing
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, "Refresh catalog")
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(localizeHelper.localize(Res.string.search_voices_by_name_language)) },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    availableLanguages: List<String>,
    selectedLanguage: String?,
    selectedGender: VoiceGender?,
    selectedQuality: VoiceQuality?,
    showDownloadedOnly: Boolean,
    expandedFilters: Boolean,
    onLanguageChange: (String?) -> Unit,
    onGenderChange: (VoiceGender?) -> Unit,
    onQualityChange: (VoiceQuality?) -> Unit,
    onDownloadedOnlyChange: (Boolean) -> Unit,
    onExpandFilters: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val hasActiveFilters = selectedLanguage != null || selectedGender != null || 
        selectedQuality != null || showDownloadedOnly
    
    val scope = rememberCoroutineScope()
    
    // LazyRow states for horizontal scroll support
    val mainRowState = rememberLazyListState()
    val genderRowState = rememberLazyListState()
    val qualityRowState = rememberLazyListState()
    val languageRowState = rememberLazyListState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Main filter row - using LazyRow for desktop mouse wheel support
        LazyRow(
            state = mainRowState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .supportDesktopHorizontalLazyListScroll(mainRowState, scope),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/collapse filters button
            item {
                FilterChip(
                    selected = expandedFilters || hasActiveFilters,
                    onClick = onExpandFilters,
                    label = { Text(localizeHelper.localize(Res.string.filters)) },
                    leadingIcon = {
                        Icon(
                            if (expandedFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            "Toggle filters",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (hasActiveFilters) {
                        { Badge { } }
                    } else null
                )
            }
            
            // Downloaded only toggle
            item {
                FilterChip(
                    selected = showDownloadedOnly,
                    onClick = { onDownloadedOnlyChange(!showDownloadedOnly) },
                    label = { Text(localizeHelper.localize(Res.string.downloaded)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Download,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            
            // Quick language filters (top 5)
            items(availableLanguages.take(5).size) { index ->
                val lang = availableLanguages[index]
                FilterChip(
                    selected = selectedLanguage == lang,
                    onClick = { 
                        onLanguageChange(if (selectedLanguage == lang) null else lang) 
                    },
                    label = { Text(lang.uppercase()) }
                )
            }
            
            // More languages indicator
            if (availableLanguages.size > 5) {
                item {
                    AssistChip(
                        onClick = onExpandFilters,
                        label = { Text("+${availableLanguages.size - 5} more") }
                    )
                }
            }
        }
        
        // Expanded filters section
        AnimatedVisibility(
            visible = expandedFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gender filter
                Text(
                    text = localizeHelper.localize(Res.string.gender),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    state = genderRowState,
                    modifier = Modifier.supportDesktopHorizontalLazyListScroll(genderRowState, scope),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedGender == null,
                            onClick = { onGenderChange(null) },
                            label = { Text(localizeHelper.localize(Res.string.all)) }
                        )
                    }
                    items(VoiceGender.entries.size, key = { VoiceGender.entries[it].name }) { index ->
                        val gender = VoiceGender.entries[index]
                        FilterChip(
                            selected = selectedGender == gender,
                            onClick = { onGenderChange(if (selectedGender == gender) null else gender) },
                            label = { Text(gender.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            leadingIcon = {
                                Icon(
                                    when (gender) {
                                        VoiceGender.FEMALE -> Icons.Default.Female
                                        VoiceGender.MALE -> Icons.Default.Male
                                        VoiceGender.NEUTRAL -> Icons.Default.Person
                                    },
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                
                // Quality filter
                Text(
                    text = localizeHelper.localize(Res.string.quality),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    state = qualityRowState,
                    modifier = Modifier.supportDesktopHorizontalLazyListScroll(qualityRowState, scope),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedQuality == null,
                            onClick = { onQualityChange(null) },
                            label = { Text(localizeHelper.localize(Res.string.all)) }
                        )
                    }
                    items(VoiceQuality.entries.size, key = { VoiceQuality.entries[it].name }) { index ->
                        val quality = VoiceQuality.entries[index]
                        FilterChip(
                            selected = selectedQuality == quality,
                            onClick = { onQualityChange(if (selectedQuality == quality) null else quality) },
                            label = { Text(quality.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                
                // All languages (scrollable with desktop mouse wheel support)
                if (availableLanguages.size > 5) {
                    Text(
                        text = localizeHelper.localize(Res.string.all_languages),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        state = languageRowState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .supportDesktopHorizontalLazyListScroll(languageRowState, scope),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedLanguage == null,
                                onClick = { onLanguageChange(null) },
                                label = { Text(localizeHelper.localize(Res.string.all)) }
                            )
                        }
                        items(availableLanguages.size, key = { availableLanguages[it] }) { index ->
                            val lang = availableLanguages[index]
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { 
                                    onLanguageChange(if (selectedLanguage == lang) null else lang) 
                                },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "Dismiss", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LoadingState() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = localizeHelper.localize(Res.string.loading_voice_catalog),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = localizeHelper.localize(Res.string.fetching_from_rhasspygithubio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyState(
    hasFilters: Boolean,
    onRefresh: () -> Unit,
    onClearFilters: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                if (hasFilters) Icons.Default.FilterListOff else Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = if (hasFilters) "No voices match your filters" else "No voices available",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (hasFilters) 
                    "Try adjusting your search or filter criteria" 
                else 
                    "Tap refresh to fetch the voice catalog",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (hasFilters) {
                    OutlinedButton(onClick = onClearFilters) {
                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.clear_filters))
                    }
                }
                Button(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.refresh))
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Badge(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ) {
            Text(count.toString())
        }
    }
}


/**
 * Enhanced voice card with better visual design and more information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedVoiceCard(
    voice: PiperVoice,
    isSelected: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress,
        label = localizeHelper.localize(Res.string.download_progress_1)
    )
    
    Card(
        onClick = { if (voice.isDownloaded && !isDownloading) onSelect() },
        modifier = modifier.fillMaxWidth(),
        enabled = voice.isDownloaded && !isDownloading,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                voice.isDownloaded -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice avatar/icon
                VoiceAvatar(
                    gender = voice.gender,
                    isDownloaded = voice.isDownloaded,
                    isSelected = isSelected
                )
                
                // Voice info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voice.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Tags row with horizontal scroll for overflow
                    val tagsRowState = rememberLazyListState()
                    val tagsScope = rememberCoroutineScope()
                    
                    LazyRow(
                        state = tagsRowState,
                        modifier = Modifier.supportDesktopHorizontalLazyListScroll(tagsRowState, tagsScope),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            VoiceTag(
                                text = voice.locale,
                                icon = Icons.Default.Language
                            )
                        }
                        item {
                            VoiceTag(
                                text = voice.quality.name.lowercase().replaceFirstChar { it.uppercase() },
                                icon = when (voice.quality) {
                                    VoiceQuality.LOW -> Icons.Default.SignalCellularAlt1Bar
                                    VoiceQuality.MEDIUM -> Icons.Default.SignalCellularAlt2Bar
                                    VoiceQuality.HIGH, VoiceQuality.PREMIUM -> Icons.Default.SignalCellularAlt
                                }
                            )
                        }
                        if (voice.modelSize > 0) {
                            item {
                                VoiceTag(
                                    text = formatFileSize(voice.modelSize),
                                    icon = Icons.Default.Storage
                                )
                            }
                        }
                    }
                    
                    // Description if available
                    if (voice.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = voice.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Action buttons
                VoiceActions(
                    voice = voice,
                    isSelected = isSelected,
                    isDownloading = isDownloading,
                    onDownload = onDownload,
                    onDelete = onDelete
                )
            }
            
            // Download progress
            AnimatedVisibility(
                visible = isDownloading,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.downloading_1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun VoiceAvatar(
    gender: VoiceGender,
    isDownloaded: Boolean,
    isSelected: Boolean
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isDownloaded -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val iconColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isDownloaded -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (gender) {
                VoiceGender.FEMALE -> Icons.Default.Female
                VoiceGender.MALE -> Icons.Default.Male
                VoiceGender.NEUTRAL -> Icons.Default.Person
            },
            contentDescription = gender.name,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        // Downloaded indicator
        if (isDownloaded && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun VoiceTag(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VoiceActions(
    voice: PiperVoice,
    isSelected: Boolean,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            isSelected -> {
                FilledTonalIconButton(
                    onClick = { },
                    enabled = false,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Check, "Selected")
                }
            }
            isDownloading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
            }
            voice.isDownloaded -> {
                // Delete button for downloaded voices
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            "Delete voice",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            else -> {
                FilledTonalButton(
                    onClick = onDownload,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Download,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(localizeHelper.localize(Res.string.download))
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

// Keep the old PiperVoiceCard for backward compatibility
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiperVoiceCard(
    voice: PiperVoice,
    isSelected: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    EnhancedVoiceCard(
        voice = voice,
        isSelected = isSelected,
        isDownloading = isDownloading,
        downloadProgress = downloadProgress,
        onSelect = onSelect,
        onDownload = onDownload,
        onDelete = null,
        modifier = modifier
    )
}
