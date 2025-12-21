package ireader.presentation.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlin.math.roundToInt
import ireader.i18n.resources.*

/**
 * TTS Text Merging and Chapter Caching Settings Section
 * 
 * Features:
 * - Slider for merge word count (remote engines)
 * - Slider for merge word count (native engines)
 * - Toggle for chapter audio caching (remote only)
 * - Slider for cache duration (days)
 * - Clear cache button
 */
@Composable
fun TTSMergeAndCacheSection(
    // Text merging settings
    mergeWordsRemote: Int,
    onMergeWordsRemoteChange: (Int) -> Unit,
    mergeWordsNative: Int,
    onMergeWordsNativeChange: (Int) -> Unit,
    // Chapter caching settings
    chapterCacheEnabled: Boolean,
    onChapterCacheEnabledChange: (Boolean) -> Unit,
    chapterCacheDays: Int,
    onChapterCacheDaysChange: (Int) -> Unit,
    // Cache stats
    cacheEntryCount: Int,
    cacheSizeMB: Float,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Text(
            text = localizeHelper.localize(Res.string.tts_text_merging),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Info card about text merging
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = localizeHelper.localize(Res.string.merge_multiple_paragraphs_into_larger) +
                           "This reduces server requests and provides better audio continuity. " +
                           "Set to 0 to disable merging.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Remote engines merge setting
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.tts_remote_engines),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Text(
                        text = if (mergeWordsRemote == 0) "Disabled" else "$mergeWordsRemote words",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = localizeHelper.localize(Res.string.tts_merge_words_remote),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Slider(
                    value = mergeWordsRemote.toFloat(),
                    onValueChange = { onMergeWordsRemoteChange(it.roundToInt()) },
                    valueRange = 0f..500f,
                    steps = 9, // 0, 50, 100, 150, 200, 250, 300, 350, 400, 450, 500
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // Quick select chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 100, 200, 300).forEach { value ->
                        FilterChip(
                            selected = mergeWordsRemote == value,
                            onClick = { onMergeWordsRemoteChange(value) },
                            label = { 
                                Text(
                                    if (value == 0) "Off" else "$value",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
        
        // Native engines merge setting
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.tts_native_engines),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Text(
                        text = if (mergeWordsNative == 0) "Disabled" else "$mergeWordsNative words",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Text(
                    text = localizeHelper.localize(Res.string.tts_merge_words_native),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Slider(
                    value = mergeWordsNative.toFloat(),
                    onValueChange = { onMergeWordsNativeChange(it.roundToInt()) },
                    valueRange = 0f..500f,
                    steps = 9,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                // Quick select chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 100, 200, 300).forEach { value ->
                        FilterChip(
                            selected = mergeWordsNative == value,
                            onClick = { onMergeWordsNativeChange(value) },
                            label = { 
                                Text(
                                    if (value == 0) "Off" else "$value",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        }
        
        // Divider
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        // Chapter caching section header
        Text(
            text = localizeHelper.localize(Res.string.tts_chapter_caching),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Chapter cache toggle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (chapterCacheEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = if (chapterCacheEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.download_chapter_audio),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Text(
                        text = localizeHelper.localize(Res.string.tts_download_chapter_audio_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = chapterCacheEnabled,
                    onCheckedChange = onChapterCacheEnabledChange
                )
            }
        }
        
        // Cache duration setting (only show when enabled)
        if (chapterCacheEnabled) {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.tts_cache_duration),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "$chapterCacheDays days",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = localizeHelper.localize(Res.string.tts_cache_duration_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Slider(
                        value = chapterCacheDays.toFloat(),
                        onValueChange = { onChapterCacheDaysChange(it.roundToInt()) },
                        valueRange = 1f..30f,
                        steps = 28,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    // Quick select chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 3, 7, 14, 30).forEach { value ->
                            FilterChip(
                                selected = chapterCacheDays == value,
                                onClick = { onChapterCacheDaysChange(value) },
                                label = { 
                                    Text(
                                        "${value}d",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
            
            // Cache stats and clear button
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = localizeHelper.localize(Res.string.tts_cache_status),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "$cacheEntryCount chapters • ${((cacheSizeMB * 10).roundToInt() / 10.0)} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        OutlinedButton(
                            onClick = onClearCache,
                            enabled = cacheEntryCount > 0,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(localizeHelper.localize(Res.string.tts_clear_cache))
                        }
                    }
                }
            }
        }
    }
}
