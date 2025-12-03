package ireader.presentation.ui.settings.advance


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.log.Log
import ireader.domain.utils.extensions.ioDispatcher
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.cache_cleared
import ireader.i18n.resources.cache_management
import ireader.i18n.resources.cancel
import ireader.i18n.resources.chapters_was_cleared
import ireader.i18n.resources.clear_all_cache
import ireader.i18n.resources.clear_all_cached_data_to_free_up_storage_space
import ireader.i18n.resources.clear_all_chapters
import ireader.i18n.resources.clear_all_cover_cache
import ireader.i18n.resources.clear_all_database
import ireader.i18n.resources.clear_cached_book_cover_images_to_free_up_storage
import ireader.i18n.resources.clear_not_in_library_books
import ireader.i18n.resources.confirm
import ireader.i18n.resources.cover_cache_cleared
import ireader.i18n.resources.danger_zone
import ireader.i18n.resources.database_deleted
import ireader.i18n.resources.database_maintenance
import ireader.i18n.resources.database_was_cleared
import ireader.i18n.resources.delete_all_database
import ireader.i18n.resources.epub
import ireader.i18n.resources.fix_book_category_assignments
import ireader.i18n.resources.fix_database_structure_and_integrity_issues
import ireader.i18n.resources.import_epub
import ireader.i18n.resources.import_epub_files_into_your_library
import ireader.i18n.resources.permanently_delete_entire_database
import ireader.i18n.resources.reader_settings_reset
import ireader.i18n.resources.remove_all_books_and_chapters
import ireader.i18n.resources.remove_all_custom_themes
import ireader.i18n.resources.remove_all_downloaded_chapters
import ireader.i18n.resources.remove_books_not_in_library
import ireader.i18n.resources.repair_categories
import ireader.i18n.resources.repair_database
import ireader.i18n.resources.reset_categories
import ireader.i18n.resources.reset_reader_screen_settings
import ireader.i18n.resources.reset_themes
import ireader.i18n.resources.restore_default_categories
import ireader.i18n.resources.restore_default_reader_settings
import ireader.i18n.resources.success
import ireader.i18n.resources.text_does_not_match
import ireader.i18n.resources.these_actions_are_destructive_and
import ireader.i18n.resources.type_1
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.IAlertDialog
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.coroutines.withContext

/**
 * State holder for dialog visibility to reduce individual state variables
 */
private class AdvanceSettingsDialogState {
    var showImport by mutableStateOf(false)
    var showDeleteAllDb by mutableStateOf(false)
    var showClearAllDatabase by mutableStateOf(false)
    var showClearNotInLibrary by mutableStateOf(false)
    var showClearAllChapters by mutableStateOf(false)
    var showClearCache by mutableStateOf(false)
    var showClearCoverCache by mutableStateOf(false)
    var showResetReaderSettings by mutableStateOf(false)
    var showResetThemes by mutableStateOf(false)
    var showResetCategories by mutableStateOf(false)
}

@Composable
fun AdvanceSettings(
    vm: AdvanceSettingViewModel,
    padding: PaddingValues
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scope = rememberCoroutineScope()
    
    // Consolidated dialog state to reduce state management overhead
    val dialogState = remember { AdvanceSettingsDialogState() }
    
    // Cache sizes loaded asynchronously to avoid blocking main thread
    var cacheSize by remember { mutableStateOf("...") }
    var coverCacheSize by remember { mutableStateOf("...") }
    
    // Load cache sizes asynchronously
    LaunchedEffect(Unit) {
        withContext(ioDispatcher) {
            cacheSize = vm.cacheManager.getCacheSize()
            coverCacheSize = vm.getCoverCacheSize()
        }
    }
    
    OnShowImportEpub(dialogState.showImport, onFileSelected = {
        try {
            vm.importEpub.parse(it)
            vm.showSnackBar(UiText.MStringResource(Res.string.success))
        } catch (e: Throwable) {
            Log.error(e, "epub parser throws an exception")
            vm.showSnackBar(UiText.ExceptionString(e))
        }
    })

    // Pre-compute items list with stable keys - uses async loaded cache sizes
    val items = remember(cacheSize, coverCacheSize) {
        listOf<Components>(
            // Cache Management Section (Safe operations)
            Components.Header(
                text = localizeHelper.localize(Res.string.cache_management),
                icon = Icons.Default.Storage
            ),
            Components.Row(
                title = "${localizeHelper.localize(Res.string.clear_all_cache)} ($cacheSize)",
                subtitle = localizeHelper.localize(Res.string.clear_all_cached_data_to_free_up_storage_space),
                icon = Icons.Default.FolderDelete,
                onClick = {
                    dialogState.showClearCache = true
                }
            ),
            Components.Row(
                title = "${localizeHelper.localize(Res.string.clear_all_cover_cache)} ($coverCacheSize)",
                subtitle = localizeHelper.localize(Res.string.clear_cached_book_cover_images_to_free_up_storage),
                icon = Icons.Default.Image,
                onClick = {
                    dialogState.showClearCoverCache = true
                }
            ),
            
            Components.Space,
            
            // EPUB Section (Safe operations)
            Components.Header(
                text = localizeHelper.localize(Res.string.epub),
                icon = Icons.Default.Book
            ),
            Components.Row(
                title = localizeHelper.localize(Res.string.import_epub),
                subtitle = localizeHelper.localize(Res.string.import_epub_files_into_your_library),
                icon = Icons.Default.Upload,
                onClick = {
                    dialogState.showImport = true
                }
            ),
            
            Components.Space,
            
            // Database Maintenance Section (Safe operations)
            Components.Header(
                text = localizeHelper.localize(Res.string.database_maintenance),
                icon = Icons.Default.DataObject
            ),
            Components.Row(
                title = localizeHelper.localize(Res.string.repair_database),
                subtitle = localizeHelper.localize(Res.string.fix_database_structure_and_integrity_issues),
                icon = Icons.Default.Build,
                onClick = {
                    vm.repairDatabase()
                }
            ),
            Components.Row(
                title = localizeHelper.localize(Res.string.repair_categories),
                subtitle = localizeHelper.localize(Res.string.fix_book_category_assignments),
                icon = Icons.Default.Construction,
                onClick = {
                    vm.repairBookCategories()
                }
            ),
            
            Components.Space,
            
            // Danger Zone Section (Destructive operations)
            Components.Dynamic {
                DangerZoneSection(
                    onClearAllDatabase = { dialogState.showClearAllDatabase = true },
                    onClearNotInLibrary = { dialogState.showClearNotInLibrary = true },
                    onClearAllChapters = { dialogState.showClearAllChapters = true },
                    onResetReaderSettings = { dialogState.showResetReaderSettings = true },
                    onResetThemes = { dialogState.showResetThemes = true },
                    onResetCategories = { dialogState.showResetCategories = true },
                    onDeleteAllDatabase = { dialogState.showDeleteAllDb = true }
                )
            },
        )
    }


    SetupSettingComponents(scaffoldPadding = padding, items = items)
    
    // Destructive Action Dialogs with Typed Confirmation
    // Using consolidated dialogState for better performance
    if (dialogState.showClearAllDatabase) {
        DestructiveActionDialog(
            title = localizeHelper.localize(Res.string.clear_all_database),
            message = "⚠️ WARNING: This will permanently remove all books and chapters from the database. This action cannot be undone and all your library data will be lost.",
            confirmationWord = "DELETE",
            onConfirm = {
                vm.deleteAllDatabase()
                vm.showSnackBar(UiText.MStringResource(Res.string.database_was_cleared))
                dialogState.showClearAllDatabase = false
            },
            onDismiss = { dialogState.showClearAllDatabase = false }
        )
    }
    
    if (dialogState.showClearNotInLibrary) {
        DestructiveActionDialog(
            title = localizeHelper.localize(Res.string.clear_not_in_library_books),
            message = "This will remove all books that are not in your library. This action cannot be undone.",
            confirmationWord = "DELETE",
            onConfirm = {
                vm.scope.launchIO {
                    vm.deleteUseCase.deleteNotInLibraryBooks()
                    vm.showSnackBar(UiText.MStringResource(Res.string.success))
                }
                dialogState.showClearNotInLibrary = false
            },
            onDismiss = { dialogState.showClearNotInLibrary = false }
        )
    }
    
    if (dialogState.showClearAllChapters) {
        DestructiveActionDialog(
            title = localizeHelper.localize(Res.string.clear_all_chapters),
            message = "This will remove all downloaded chapter content. You will need to re-download chapters to read offline. This action cannot be undone.",
            confirmationWord = "DELETE",
            onConfirm = {
                vm.deleteAllChapters()
                vm.showSnackBar(UiText.MStringResource(Res.string.chapters_was_cleared))
                dialogState.showClearAllChapters = false
            },
            onDismiss = { dialogState.showClearAllChapters = false }
        )
    }
    
    if (dialogState.showClearCache) {
        ConfirmationDialog(
            title = localizeHelper.localize(Res.string.clear_all_cache),
            message = "This will clear all cached data ($cacheSize). The app may need to re-download some content.",
            confirmText = "Clear",
            onConfirm = {
                vm.cacheManager.clearAllCache()
                vm.showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.cache_cleared))
                dialogState.showClearCache = false
            },
            onDismiss = { dialogState.showClearCache = false },
            isDestructive = false
        )
    }
    
    if (dialogState.showClearCoverCache) {
        ConfirmationDialog(
            title = localizeHelper.localize(Res.string.clear_all_cover_cache),
            message = "This will clear all cached book cover images. Covers will be re-downloaded when needed.",
            confirmText = "Clear",
            onConfirm = {
                vm.clearImageCache()
                vm.showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.cover_cache_cleared))
                dialogState.showClearCoverCache = false
            },
            onDismiss = { dialogState.showClearCoverCache = false },
            isDestructive = false
        )
    }
    
    if (dialogState.showResetReaderSettings) {
        DestructiveActionDialog(
            title = localizeHelper.localize(Res.string.reset_reader_screen_settings),
            message = "This will restore all reader settings to their default values. Your custom preferences (font size, colors, spacing, etc.) will be lost. This action cannot be undone.",
            confirmationWord = "RESET",
            onConfirm = {
                vm.deleteDefaultSettings()
                vm.showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.reader_settings_reset))
                dialogState.showResetReaderSettings = false
            },
            onDismiss = { dialogState.showResetReaderSettings = false }
        )
    }
    
    if (dialogState.showResetThemes) {
        DestructiveActionDialog(
            title = localizeHelper.localize(Res.string.reset_themes),
            message = "This will remove all custom themes. Only default themes will remain. This action cannot be undone.",
            confirmationWord = "RESET",
            onConfirm = {
                vm.resetThemes()
                vm.showSnackBar(UiText.MStringResource(Res.string.success))
                dialogState.showResetThemes = false
            },
            onDismiss = { dialogState.showResetThemes = false }
        )
    }
    
    if (dialogState.showResetCategories) {
        DestructiveActionDialog(
            title = localizeHelper.localize(Res.string.reset_categories),
            message = "This will restore the default category list. Your custom categories will be removed and books will be reassigned. This action cannot be undone.",
            confirmationWord = "RESET",
            onConfirm = {
                vm.resetCategories()
                vm.showSnackBar(UiText.MStringResource(Res.string.success))
                dialogState.showResetCategories = false
            },
            onDismiss = { dialogState.showResetCategories = false }
        )
    }
    
    if (dialogState.showDeleteAllDb) {
        DestructiveActionDialog(
            title = localizeHelper.localize(Res.string.delete_all_database),
            message = "⚠️⚠️⚠️ CRITICAL WARNING ⚠️⚠️⚠️\n\nThis will PERMANENTLY DELETE the ENTIRE database. ALL your data including:\n• All books in your library\n• All reading history\n• All bookmarks\n• All settings\n• All categories\n\nThis action CANNOT be recovered. Are you absolutely sure?",
            confirmationWord = "DELETE",
            onConfirm = {
                vm.deleteAllDatabase()
                vm.showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.database_deleted))
                dialogState.showDeleteAllDb = false
            },
            onDismiss = { dialogState.showDeleteAllDb = false }
        )
    }
}

/**
 * Confirmation Dialog for non-destructive or less critical actions
 * 
 * Simple confirmation dialog without typed confirmation requirement.
 */
@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    IAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (isDestructive) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        },
        title = {
            androidx.compose.material3.Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            androidx.compose.material3.Text(
                text = message,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = if (isDestructive) {
                        androidx.compose.material3.MaterialTheme.colorScheme.error
                    } else {
                        androidx.compose.material3.MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                androidx.compose.material3.Text(
                    text = confirmText,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                androidx.compose.material3.Text(
                    text = localizeHelper.localize(Res.string.cancel),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

/**
 * Destructive Action Dialog with Typed Confirmation
 * 
 * Requires the user to type a specific confirmation word (DELETE or RESET)
 * to proceed with the destructive action. This prevents accidental data loss.
 * 
 * @param title Dialog title
 * @param message Warning message explaining the consequences
 * @param confirmationWord The word user must type (e.g., "DELETE", "RESET")
 * @param onConfirm Callback when user confirms with correct word
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
private fun DestructiveActionDialog(
    title: String,
    message: String,
    confirmationWord: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var inputText by remember { mutableStateOf("") }
    val isConfirmEnabled = inputText.trim().equals(confirmationWord, ignoreCase = true)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "${localizeHelper.localize(Res.string.type_1)}$confirmationWord\" to confirm:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.error,
                        focusedLabelColor = MaterialTheme.colorScheme.error
                    ),
                    placeholder = {
                        Text(
                            text = confirmationWord,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
                
                if (inputText.isNotEmpty() && !isConfirmEnabled) {
                    Text(
                        text = localizeHelper.localize(Res.string.text_does_not_match),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    inputText = "" // Reset for next time
                },
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.confirm),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    inputText = "" // Reset on cancel
                    onDismiss()
                }
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

/**
 * Danger Zone Section Component
 * 
 * Displays a visually distinct section for destructive actions with:
 * - Red error container background
 * - Warning icon and "Danger Zone" header
 * - Grouped dangerous actions with clear styling
 * 
 * This component helps prevent accidental data loss by clearly marking
 * dangerous operations and separating them from regular settings.
 */
@Composable
fun DangerZoneSection(
    onClearAllDatabase: () -> Unit,
    onClearNotInLibrary: () -> Unit,
    onClearAllChapters: () -> Unit,
    onResetReaderSettings: () -> Unit,
    onResetThemes: () -> Unit,
    onResetCategories: () -> Unit,
    onDeleteAllDatabase: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Danger Zone Header
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.danger_zone),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Text(
                text = localizeHelper.localize(Res.string.these_actions_are_destructive_and),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Dangerous Actions
            DangerButton(
                text = localizeHelper.localize(Res.string.clear_all_database),
                subtitle = localizeHelper.localize(Res.string.remove_all_books_and_chapters),
                onClick = onClearAllDatabase
            )
            
            DangerButton(
                text = localizeHelper.localize(Res.string.clear_not_in_library_books),
                subtitle = localizeHelper.localize(Res.string.remove_books_not_in_library),
                onClick = onClearNotInLibrary
            )
            
            DangerButton(
                text = localizeHelper.localize(Res.string.clear_all_chapters),
                subtitle = localizeHelper.localize(Res.string.remove_all_downloaded_chapters),
                onClick = onClearAllChapters
            )
            
            DangerButton(
                text = localizeHelper.localize(Res.string.reset_reader_screen_settings),
                subtitle = localizeHelper.localize(Res.string.restore_default_reader_settings),
                onClick = onResetReaderSettings
            )
            
            DangerButton(
                text = localizeHelper.localize(Res.string.reset_themes),
                subtitle = localizeHelper.localize(Res.string.remove_all_custom_themes),
                onClick = onResetThemes
            )
            
            DangerButton(
                text = localizeHelper.localize(Res.string.reset_categories),
                subtitle = localizeHelper.localize(Res.string.restore_default_categories),
                onClick = onResetCategories
            )
            
            // Most dangerous action - Delete All Database
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
            
            DangerButton(
                text = localizeHelper.localize(Res.string.delete_all_database),
                subtitle = localizeHelper.localize(Res.string.permanently_delete_entire_database),
                onClick = onDeleteAllDatabase,
                isHighRisk = true
            )
        }
    }
}

/**
 * Danger Button Component
 * 
 * A button styled for dangerous actions within the Danger Zone.
 */
@Composable
private fun DangerButton(
    text: String,
    subtitle: String,
    onClick: () -> Unit,
    isHighRisk: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (isHighRisk) {
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isHighRisk) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
