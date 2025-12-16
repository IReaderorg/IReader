package ireader.presentation.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.core.theme.AppLocales
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.ui.core.file.toPathString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unified onboarding screen with:
 * 1. Welcome + Language selection
 * 2. Storage setup (required)
 * 3. Dev server toggle (optional)
 */
@Composable
fun OnboardingScreen(
    uiPreferences: UiPreferences,
    supabasePreferences: SupabasePreferences? = null,
    localeHelper: LocaleHelper? = null,
    onFolderUriSelected: ((String) -> Unit)? = null, // Callback for platform to take SAF permissions
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    var storageSelected by remember { mutableStateOf(false) }
    var selectedFolderPath by remember { mutableStateOf<String?>(null) }
    var selectedLanguage by remember { mutableStateOf(uiPreferences.language().get().ifEmpty { "en" }) }
    var devServerEnabled by remember { mutableStateOf(supabasePreferences?.supabaseEnabled()?.get() ?: false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Progress indicator
            OnboardingProgressIndicator(
                currentPage = pagerState.currentPage,
                totalPages = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp)
            )
            
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = false // Controlled navigation only
            ) { page ->
                when (page) {
                    0 -> WelcomeLanguagePage(
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { lang ->
                            selectedLanguage = lang
                            uiPreferences.language().set(lang)
                            localeHelper?.updateLocal()
                        },
                        onContinue = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )
                    1 -> StorageSetupPage(
                        uiPreferences = uiPreferences,
                        selectedPath = selectedFolderPath,
                        onFolderSelected = { path ->
                            selectedFolderPath = path
                            storageSelected = true
                            // Notify platform to take SAF permissions
                            onFolderUriSelected?.invoke(path)
                            scope.launch { 
                                delay(500)
                                pagerState.animateScrollToPage(2) 
                            }
                        },
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(0) }
                        }
                    )
                    2 -> DevServerPage(
                        devServerEnabled = devServerEnabled,
                        onDevServerToggle = { enabled ->
                            devServerEnabled = enabled
                            supabasePreferences?.supabaseEnabled()?.set(enabled)
                        },
                        onComplete = {
                            // Mark first launch and onboarding as complete
                            uiPreferences.hasCompletedOnboarding().set(true)
                            uiPreferences.hasCompletedFirstLaunch().set(true)
                            onComplete()
                        },
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgressIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            val isActive = index <= currentPage
            val width by animateDpAsState(
                targetValue = if (index == currentPage) 32.dp else 12.dp,
                animationSpec = spring(dampingRatio = 0.8f),
                label = "indicator_width"
            )
            
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(width)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

@Composable
private fun WelcomeLanguagePage(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.8f)
        ) {
            // App logo/icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 30 }
        ) {
            Text(
                text = "Welcome to IReader",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 30 }
        ) {
            Text(
                text = "Your personal reading companion.\nLet's set up a few things to get started.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Language selector
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 350)) + slideInVertically(tween(500, 350)) { 30 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLanguageDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Language",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = AppLocales.getDisplayName(selectedLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 30 }
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { lang ->
                onLanguageSelected(lang)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Language",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(AppLocales.AVAILABLE_LOCALES) { langCode ->
                    val isSelected = langCode == selectedLanguage
                    ListItem(
                        headlineContent = {
                            Text(
                                text = AppLocales.getDisplayName(langCode),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        supportingContent = {
                            Text(
                                text = langCode.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onLanguageSelected(langCode) }
                            )
                        },
                        modifier = Modifier.clickable { onLanguageSelected(langCode) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun StorageSetupPage(
    uiPreferences: UiPreferences,
    selectedPath: String?,
    onFolderSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var isSelectingFolder by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Select IReader Storage Folder"
    ) { directory ->
        isSelectingFolder = false
        if (directory != null) {
            val folderPath = directory.toPathString()
            uiPreferences.selectedStorageFolderUri().set(folderPath)
            uiPreferences.hasRequestedStoragePermission().set(true)
            onFolderSelected(folderPath)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Icon
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 30 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Storage Setup",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "Required",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Description
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 30 }
        ) {
            Text(
                text = "Select a folder where IReader will store your data. A dedicated folder is recommended.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Feature cards
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 30 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StorageFeatureCard(
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    title = "Downloaded Chapters",
                    description = "Save chapters for offline reading",
                    accentColor = MaterialTheme.colorScheme.primary
                )
                
                StorageFeatureCard(
                    icon = Icons.Outlined.Extension,
                    title = "Extensions & Sources",
                    description = "Store installed extensions and plugins",
                    accentColor = MaterialTheme.colorScheme.secondary
                )
                
                StorageFeatureCard(
                    icon = Icons.Outlined.Backup,
                    title = "Backups",
                    description = "Keep your library and settings safe",
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
                
                StorageFeatureCard(
                    icon = Icons.Outlined.Storage,
                    title = "Persistent Storage",
                    description = "Data persists even if app is uninstalled",
                    accentColor = Color(0xFF4CAF50)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Important note
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 400))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "This folder will be used for all app data including downloads, extensions, and backups. Choose a location you can easily access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        // Selected path display
        if (selectedPath != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Folder Selected",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = selectedPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 500)) + slideInVertically(tween(500, 500)) { 30 }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        isSelectingFolder = true
                        directoryPicker.launch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSelectingFolder
                ) {
                    if (isSelectingFolder) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedPath != null) "Change Folder" else "Select Folder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StorageFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DevServerPage(
    devServerEnabled: Boolean,
    onDevServerToggle: (Boolean) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Icon
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 30 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Cloud Features",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Optional",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Description
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 30 }
        ) {
            Text(
                text = "Enable cloud features to sync your reading progress, access community reviews, and more.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Dev server toggle card
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 30 }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (devServerEnabled) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                        Text(
                            text = "Enable Cloud Features",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sync, reviews, badges, leaderboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = devServerEnabled,
                        onCheckedChange = onDevServerToggle
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Feature cards
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 350)) + slideInVertically(tween(500, 350)) { 30 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DevFeatureCard(
                    icon = Icons.Outlined.Sync,
                    title = "Reading Progress Sync",
                    description = "Continue reading from where you left off on any device",
                    enabled = devServerEnabled
                )
                
                DevFeatureCard(
                    icon = Icons.Outlined.RateReview,
                    title = "Community Reviews",
                    description = "Read and write reviews for novels",
                    enabled = devServerEnabled
                )
                
                DevFeatureCard(
                    icon = Icons.Outlined.EmojiEvents,
                    title = "Leaderboard & Badges",
                    description = "Earn achievements and compete with others",
                    enabled = devServerEnabled
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info note
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 400))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "You can change this setting later in Settings → Cloud Sync.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500, 500)) + slideInVertically(tween(500, 500)) { 30 }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DevFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean
) {
    val alpha = if (enabled) 1f else 0.5f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f * alpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f * alpha)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }
            
            if (enabled) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
