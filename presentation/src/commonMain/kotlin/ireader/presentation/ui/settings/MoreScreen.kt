package ireader.presentation.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.icerock.moko.resources.StringResource
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.Images.incognito
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Divider
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    vm: MainSettingScreenViewModel,
    onDownloadScreen: () -> Unit,
    onBackupScreen: () -> Unit,
    onCategory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    
    // Theme mode state
    var showThemeOptions by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier,
        state = rememberLazyListState(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App Logo and Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                LogoHeader()
            }
        }
        
        // Profile Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                // App Name
                Text(
                    text = "IReader",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // App Description
                Text(
                    text = "Your personal book reading companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Incognito Mode Switch with enhanced UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (vm.incognitoMode.value) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .clickable { vm.incognitoMode.value = !vm.incognitoMode.value }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Incognito Icon in a circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (vm.incognitoMode.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = incognito(),
                            contentDescription = null,
                            tint = if (vm.incognitoMode.value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = localize(MR.strings.pref_incognito_mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (vm.incognitoMode.value) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = localize(MR.strings.pref_incognito_mode_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (vm.incognitoMode.value) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = vm.incognitoMode.value,
                        onCheckedChange = { vm.incognitoMode.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
        
        // Library Management Section
        item {
            SectionHeader(
                title = "Library Management",
                icon = Icons.Filled.MenuBook
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(MR.strings.download),
                description = "Manage downloaded content",
                icon = Icons.Outlined.Download,
                onClick = onDownloadScreen
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(MR.strings.backup_and_restore),
                description = "Backup and restore your library",
                icon = Icons.Outlined.SettingsBackupRestore,
                onClick = onBackupScreen
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(MR.strings.category),
                description = "Organize books with categories",
                icon = Icons.Outlined.Label,
                onClick = onCategory
            )
        }
        
        // Appearance & Settings Section
        item {
            SectionHeader(
                title = "Appearance & Settings",
                icon = Icons.Filled.Palette
            )
        }
        
        // Theme selector item
        item {
            SettingsItemWithAction(
                title = "Theme",
                description = "Choose app theme and appearance",
                icon = Icons.Outlined.DarkMode,
                onClick = { showThemeOptions = !showThemeOptions },
                endContent = {
                    ThemeChip(
                        text = "System default",
                        onClick = { showThemeOptions = !showThemeOptions }
                    )
                },
                isExpanded = showThemeOptions,
                expandedContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        ThemeOption(
                            text = "Light",
                            isSelected = false,
                            onClick = { /* Handle light theme selection */ }
                        )
                        
                        ThemeOption(
                            text = "Dark",
                            isSelected = true,
                            onClick = { /* Handle dark theme selection */ }
                        )
                        
                        ThemeOption(
                            text = "System default",
                            isSelected = false,
                            onClick = { /* Handle system theme selection */ }
                        )
                    }
                }
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(MR.strings.settings),
                description = "Configure app preferences",
                icon = Icons.Outlined.Settings,
                onClick = onSettings
            )
        }
        
        // Information & Support Section
        item {
            SectionHeader(
                title = "Information & Support",
                icon = Icons.Filled.Info
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(MR.strings.about),
                description = "App info and credits",
                icon = Icons.Outlined.Info,
                onClick = onAbout
            )
        }
        
        item {
            SettingsItem(
                title = localizeHelper.localize(MR.strings.help),
                description = "Get help with the app",
                icon = Icons.Outlined.Help,
                onClick = onHelp
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsItemWithAction(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    endContent: @Composable () -> Unit,
    isExpanded: Boolean = false,
    expandedContent: @Composable () -> Unit = {}
) {
    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                endContent()
            }
        }
        
        AnimatedVisibility(visible = isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 56.dp, 
                        end = 16.dp,
                        top = 0.dp,
                        bottom = 8.dp
                    )
            ) {
                expandedContent()
            }
        }
    }
}

@Composable
private fun ThemeChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

data class SettingsSection(
    val titleRes: StringResource,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
)

@Composable
fun SetupLayout(
    modifier: Modifier = Modifier,
    items: List<SettingsSection>,
    padding: PaddingValues? = null,
) {
    LazyColumn(
        modifier = if (padding != null) modifier.padding(padding) else modifier,
    ) {
        items.map {
            item {
                PreferenceRow(
                    title = localize(it.titleRes),
                    icon = it.icon,
                    onClick = it.onClick,
                )
            }
        }
    }
}


class MainSettingScreenViewModel(
    uiPreferences: UiPreferences
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    val incognitoMode = uiPreferences.incognitoMode().asState()
}
