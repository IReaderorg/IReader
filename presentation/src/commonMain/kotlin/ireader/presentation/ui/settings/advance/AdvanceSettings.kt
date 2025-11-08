package ireader.presentation.ui.settings.advance


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.log.Log
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.IAlertDialog
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.serialization.ExperimentalSerializationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AdvanceSettings(
    vm: AdvanceSettingViewModel,
    padding: PaddingValues
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val showImport = remember { mutableStateOf(false) }
    var showDeleteAllDb by remember { mutableStateOf(false) }
    var showClearAllDatabase by remember { mutableStateOf(false) }
    var showClearNotInLibrary by remember { mutableStateOf(false) }
    var showClearAllChapters by remember { mutableStateOf(false) }
    var showClearCache by remember { mutableStateOf(false) }
    var showClearCoverCache by remember { mutableStateOf(false) }
    var showResetReaderSettings by remember { mutableStateOf(false) }
    var showResetThemes by remember { mutableStateOf(false) }
    var showResetCategories by remember { mutableStateOf(false) }
    
    OnShowImportEpub(showImport.value, onFileSelected = {
        try {
            vm.importEpub.parse(it)
            vm.showSnackBar(UiText.MStringResource(MR.strings.success))
        } catch (e: Throwable) {
            Log.error(e, "epub parser throws an exception")
            vm.showSnackBar(UiText.ExceptionString(e))
        }
    })

    val items = remember {
        listOf<Components>(
            // Data Section
            Components.Header(
                text = localizeHelper.localize(MR.strings.data),
                icon = Icons.Default.Storage
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_database),
                subtitle = "Remove all books and chapters from the database",
                icon = Icons.Default.DeleteSweep,
                onClick = {
                    showClearAllDatabase = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_not_in_library_books),
                subtitle = "Remove books that are not in your library",
                icon = Icons.Default.CleaningServices,
                onClick = {
                    showClearNotInLibrary = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_chapters),
                subtitle = "Remove all downloaded chapter content",
                icon = Icons.Default.MenuBook,
                onClick = {
                    showClearAllChapters = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_cache),
                subtitle = vm.importEpub.getCacheSize(),
                icon = Icons.Default.FolderDelete,
                onClick = {
                    showClearCache = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_cover_cache),
                subtitle = "Clear cached book cover images",
                icon = Icons.Default.Image,
                onClick = {
                    showClearCoverCache = true
                }
            ),
            
            Components.Space,
            
            // Reset Settings Section
            Components.Header(
                text = localizeHelper.localize(MR.strings.reset_setting),
                icon = Icons.Default.RestartAlt
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.reset_reader_screen_settings),
                subtitle = "Restore default reader preferences",
                icon = Icons.Default.AutoStories,
                onClick = {
                    showResetReaderSettings = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.reset_themes),
                subtitle = "Remove all custom themes",
                icon = Icons.Default.Palette,
                onClick = {
                    showResetThemes = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.reset_categories),
                subtitle = "Restore default category list",
                icon = Icons.Default.Category,
                onClick = {
                    showResetCategories = true
                }
            ),
            
            Components.Space,
            
            // EPUB Section
            Components.Header(
                text = localizeHelper.localize(MR.strings.epub),
                icon = Icons.Default.Book
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.import_epub),
                subtitle = "Import EPUB files into your library",
                icon = Icons.Default.Upload,
                onClick = {
                    showImport.value = true
                }
            ),
            
            Components.Space,
            
            // Database Section
            Components.Header(
                text = "Database",
                icon = Icons.Default.DataObject
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.delete_all_database),
                subtitle = "Permanently delete the entire database",
                icon = Icons.Default.Warning,
                onClick = {
                    showDeleteAllDb = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.repair_database),
                subtitle = "Fix database structure and integrity issues",
                icon = Icons.Default.Build,
                onClick = {
                    vm.repairDatabase()
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.repair_categories),
                subtitle = "Fix book category assignments",
                icon = Icons.Default.Construction,
                onClick = {
                    vm.repairBookCategories()
                }
            ),
        )
    }


    SetupSettingComponents(scaffoldPadding = padding, items = items)
    
    // Confirmation Dialogs
    if (showClearAllDatabase) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.clear_all_database),
            message = "This will permanently remove all books and chapters from the database. This action cannot be undone.",
            confirmText = "Clear",
            onConfirm = {
                vm.deleteAllDatabase()
                vm.showSnackBar(UiText.MStringResource(MR.strings.database_was_cleared))
                showClearAllDatabase = false
            },
            onDismiss = { showClearAllDatabase = false },
            isDestructive = true
        )
    }
    
    if (showClearNotInLibrary) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.clear_not_in_library_books),
            message = "This will remove all books that are not in your library. This action cannot be undone.",
            confirmText = "Clear",
            onConfirm = {
                vm.scope.launchIO {
                    vm.deleteUseCase.deleteNotInLibraryBooks()
                    vm.showSnackBar(UiText.MStringResource(MR.strings.success))
                }
                showClearNotInLibrary = false
            },
            onDismiss = { showClearNotInLibrary = false },
            isDestructive = true
        )
    }
    
    if (showClearAllChapters) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.clear_all_chapters),
            message = "This will remove all downloaded chapter content. You will need to re-download chapters to read offline.",
            confirmText = "Clear",
            onConfirm = {
                vm.deleteAllChapters()
                vm.showSnackBar(UiText.MStringResource(MR.strings.chapters_was_cleared))
                showClearAllChapters = false
            },
            onDismiss = { showClearAllChapters = false },
            isDestructive = true
        )
    }
    
    if (showClearCache) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.clear_all_cache),
            message = "This will clear all cached data (${vm.importEpub.getCacheSize()}). The app may need to re-download some content.",
            confirmText = "Clear",
            onConfirm = {
                vm.importEpub.removeCache()
                vm.showSnackBar(UiText.DynamicString("Cache was cleared."))
                showClearCache = false
            },
            onDismiss = { showClearCache = false },
            isDestructive = false
        )
    }
    
    if (showClearCoverCache) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.clear_all_cover_cache),
            message = "This will clear all cached book cover images. Covers will be re-downloaded when needed.",
            confirmText = "Clear",
            onConfirm = {
                vm.getSimpleStorage.clearImageCache()
                vm.showSnackBar(UiText.DynamicString("Cover cache was cleared."))
                showClearCoverCache = false
            },
            onDismiss = { showClearCoverCache = false },
            isDestructive = false
        )
    }
    
    if (showResetReaderSettings) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.reset_reader_screen_settings),
            message = "This will restore all reader settings to their default values. Your custom preferences will be lost.",
            confirmText = "Reset",
            onConfirm = {
                vm.deleteDefaultSettings()
                vm.showSnackBar(UiText.DynamicString("Reader settings have been reset."))
                showResetReaderSettings = false
            },
            onDismiss = { showResetReaderSettings = false },
            isDestructive = true
        )
    }
    
    if (showResetThemes) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.reset_themes),
            message = "This will remove all custom themes. Only default themes will remain.",
            confirmText = "Reset",
            onConfirm = {
                vm.resetThemes()
                vm.showSnackBar(UiText.MStringResource(MR.strings.success))
                showResetThemes = false
            },
            onDismiss = { showResetThemes = false },
            isDestructive = true
        )
    }
    
    if (showResetCategories) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.reset_categories),
            message = "This will restore the default category list. Your custom categories will be removed.",
            confirmText = "Reset",
            onConfirm = {
                vm.resetCategories()
                vm.showSnackBar(UiText.MStringResource(MR.strings.success))
                showResetCategories = false
            },
            onDismiss = { showResetCategories = false },
            isDestructive = true
        )
    }
    
    if (showDeleteAllDb) {
        ConfirmationDialog(
            title = localizeHelper.localize(MR.strings.delete_all_database),
            message = "⚠️ WARNING: This will permanently delete the entire database. All your data will be lost and cannot be recovered. Are you absolutely sure?",
            confirmText = "Delete Everything",
            onConfirm = {
                vm.deleteAllDatabase()
                vm.showSnackBar(UiText.DynamicString("Database has been deleted."))
                showDeleteAllDb = false
            },
            onDismiss = { showDeleteAllDb = false },
            isDestructive = true
        )
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
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
                    text = "Cancel",
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}
