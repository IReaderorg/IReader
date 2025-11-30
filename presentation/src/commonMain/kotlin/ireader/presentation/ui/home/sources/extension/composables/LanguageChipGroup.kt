package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.home.sources.extension.LanguageChoice
import ireader.presentation.ui.home.sources.extension.LocaleHelper
import java.util.Locale
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res


@Composable
fun LanguageChipGroup(
        choices: List<LanguageChoice>,
        selected: LanguageChoice?,
        onClick: (LanguageChoice) -> Unit,
        isVisible: Boolean,
        onToggleVisibility: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showDialog by remember { mutableStateOf(false) }
    
    if (isVisible) {
        // Get device language
        val deviceLang = remember { Locale.getDefault().language }
        
        // Quick access languages: All, device language, and popular ones
        val quickAccessCodes = remember(deviceLang) {
            listOf("all", deviceLang, "en", "ja", "es", "fr", "zh", "pt", "de", "ru")
        }
        
        val quickAccessChoices = remember(choices, quickAccessCodes) {
            choices.filter { choice ->
                when (choice) {
                    is LanguageChoice.All -> true
                    is LanguageChoice.One -> choice.language.code in quickAccessCodes
                    is LanguageChoice.Others -> false
                }
            }.distinctBy { choice ->
                when (choice) {
                    is LanguageChoice.All -> "all"
                    is LanguageChoice.One -> choice.language.code
                    is LanguageChoice.Others -> "others"
                }
            }.take(6) // Limit to 6 quick access chips
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(
                    items = quickAccessChoices,
                    key = { choice ->
                        when (choice) {
                            is LanguageChoice.All -> "All"
                            is LanguageChoice.One -> choice.language.code
                            is LanguageChoice.Others -> "others"
                        }
                    }
                ) { choice ->
                    LanguageChip(
                        choice = choice,
                        isSelected = choice == selected,
                        onClick = { onClick(choice) }
                    )
                }
                
                // "More" button
                item(key = "more_button") {
                    MoreLanguagesChip(onClick = { showDialog = true })
                }
            }
            
            // Hide button
            IconButton(
                onClick = { onToggleVisibility(false) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = localizeHelper.localize(Res.string.hide_language_filter),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        // Show icon when hidden
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { onToggleVisibility(true) }
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Visibility,
                    contentDescription = localizeHelper.localize(Res.string.show_language_filter),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
    
    if (showDialog) {
        LanguageSelectionDialog(
            choices = choices,
            selected = selected,
            onSelect = { choice ->
                onClick(choice)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun MoreLanguagesChip(onClick: () -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = localizeHelper.localize(Res.string.more_languages),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            MidSizeTextComposable(
                text = localizeHelper.localize(Res.string.more),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    choices: List<LanguageChoice>,
    selected: LanguageChoice?,
    onSelect: (LanguageChoice) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredChoices = remember(choices, searchQuery) {
        if (searchQuery.isBlank()) {
            choices
        } else {
            choices.filter { choice ->
                when (choice) {
                    is LanguageChoice.All -> "all".contains(searchQuery, ignoreCase = true)
                    is LanguageChoice.One -> {
                        val name = LocaleHelper.getDisplayName(choice.language.code)
                        name.contains(searchQuery, ignoreCase = true) || 
                        choice.language.code.contains(searchQuery, ignoreCase = true)
                    }
                    is LanguageChoice.Others -> "others".contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.select_language),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localizeHelper.localize(Res.string.close),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(localizeHelper.localize(Res.string.search_languages)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = localizeHelper.localize(Res.string.search)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Language list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = filteredChoices,
                        key = { choice ->
                            when (choice) {
                                is LanguageChoice.All -> "All"
                                is LanguageChoice.One -> choice.language.code
                                is LanguageChoice.Others -> "others"
                            }
                        }
                    ) { choice ->
                        LanguageListItem(
                            choice = choice,
                            isSelected = choice == selected,
                            onClick = { onSelect(choice) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageListItem(
    choice: LanguageChoice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val text = when (choice) {
        is LanguageChoice.All -> "🌐 All Languages"
        is LanguageChoice.One -> {
            val emoji = choice.language.toEmoji() ?: ""
            val name = LocaleHelper.getDisplayName(choice.language.code)
            if (emoji.isNotEmpty()) "$emoji $name" else name
        }
        is LanguageChoice.Others -> "🌍 Other Languages"
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = localizeHelper.localize(Res.string.selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
