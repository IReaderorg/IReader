package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Divider
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.reader.components.DesktopTTSControlPanel
import ireader.presentation.ui.reader.components.TTSSettingsPanel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

actual class TTSScreenSpec actual constructor(
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val readingParagraph: Int
) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val ttsService: DesktopTTSService = koinInject()
        
        // TTS Settings State - Use theme colors by default
        var useCustomColors by remember { mutableStateOf(false) }
        var customBackgroundColor by remember { mutableStateOf(Color(0xFF1E1E1E)) }
        var customTextColor by remember { mutableStateOf(Color.White) }
        var fontSize by remember { mutableStateOf(18) }
        var textAlignment by remember { mutableStateOf(TextAlign.Start) }
        var showSettings by remember { mutableStateOf(false) }
        var sleepModeEnabled by remember { mutableStateOf(false) }
        var sleepTimeMinutes by remember { mutableStateOf(30) }
        
        // Download state
        var isDownloading by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0 to 0) }
        var downloadError by remember { mutableStateOf<String?>(null) }
        var showDownloadSuccess by remember { mutableStateOf(false) }
        var showDownloadDialog by remember { mutableStateOf(false) }
        var downloadStartTime by remember { mutableStateOf(0L) }
        var downloadJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
        val scope = rememberCoroutineScope()
        
        // Determine actual colors to use
        val backgroundColor = if (useCustomColors) customBackgroundColor else MaterialTheme.colorScheme.background
        val textColor = if (useCustomColors) customTextColor else MaterialTheme.colorScheme.onBackground
        
        // Initialize TTS with the chapter
        LaunchedEffect(bookId, chapterId) {
            ttsService.startReading(bookId, chapterId)
            ttsService.state.currentReadingParagraph = readingParagraph
        }
        
        // Show snackbar if source is not installed
        LaunchedEffect(ttsService.state.sourceNotInstalledError) {
            if (ttsService.state.sourceNotInstalledError) {
                // Reset the error state after showing
                kotlinx.coroutines.delay(5000)
                ttsService.state.sourceNotInstalledError = false
            }
        }
        
        // Cleanup on dispose
        DisposableEffect(Unit) {
            onDispose {
                ttsService.startService(DesktopTTSService.ACTION_PAUSE)
            }
        }
        
        val lazyListState = rememberLazyListState()
        
        // Auto-scroll to current paragraph
        LaunchedEffect(ttsService.state.currentReadingParagraph) {
            if (ttsService.state.isPlaying) {
                lazyListState.animateScrollToItem(ttsService.state.currentReadingParagraph)
            }
        }
        
        IScaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Text-to-Speech",
                                style = MaterialTheme.typography.titleMedium
                            )
                            ttsService.state.ttsChapter?.let { chapter ->
                                Text(
                                    text = chapter.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        TopAppBarBackButton(
                            onClick = { navController.popBackStack() }
                        )
                    },
                    actions = {
                        // Download Chapter Button
                        IconButton(
                            onClick = {
                                ttsService.state.ttsChapter?.let { chapter ->
                                    showDownloadDialog = true
                                    isDownloading = true
                                    downloadStartTime = System.currentTimeMillis()
                                    downloadProgress = 0 to 0
                                    
                                    downloadJob = scope.launch {
                                        val result = ttsService.downloadChapterAudio(
                                            chapterId = chapter.id,
                                            onProgress = { current, total ->
                                                downloadProgress = current to total
                                            }
                                        )
                                        isDownloading = false
                                        result.onSuccess {
                                            kotlinx.coroutines.delay(500)
                                            showDownloadDialog = false
                                            showDownloadSuccess = true
                                            downloadError = null
                                        }.onFailure { error ->
                                            showDownloadDialog = false
                                            downloadError = error.message ?: "Download failed"
                                        }
                                    }
                                }
                            },
                            enabled = ttsService.state.ttsChapter != null && !isDownloading
                        ) {
                            Icon(Icons.Default.Download, "Download Chapter")
                        }
                        
                        IconButton(onClick = { showSettings = !showSettings }) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Background - use solid color for better readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                )
                
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Content area with text
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        ttsService.state.ttsContent?.value?.let { content ->
                            if (content.isNotEmpty()) {
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(content.size) { index ->
                                        val isCurrentParagraph = index == ttsService.state.currentReadingParagraph
                                        
                                        Text(
                                            text = content[index],
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    ttsService.state.currentReadingParagraph = index
                                                    if (ttsService.state.isPlaying) {
                                                        ttsService.startService(DesktopTTSService.ACTION_PAUSE)
                                                        ttsService.startService(DesktopTTSService.ACTION_PLAY)
                                                    }
                                                }
                                                .padding(vertical = 4.dp),
                                            fontSize = fontSize.sp,
                                            color = textColor.copy(alpha = if (isCurrentParagraph) 1f else 0.6f),
                                            textAlign = textAlignment,
                                            fontWeight = if (isCurrentParagraph) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No content available",
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    // TTS Controls at bottom
                    DesktopTTSControlPanel(
                        ttsService = ttsService,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Settings Panel (overlay)
                if (showSettings) {
                    TTSSettingsPanel(
                        useCustomColors = useCustomColors,
                        customBackgroundColor = customBackgroundColor,
                        customTextColor = customTextColor,
                        fontSize = fontSize,
                        textAlignment = textAlignment,
                        sleepModeEnabled = sleepModeEnabled,
                        sleepTimeMinutes = sleepTimeMinutes,
                        onUseCustomColorsChange = { useCustomColors = it },
                        onBackgroundColorChange = { customBackgroundColor = it },
                        onTextColorChange = { customTextColor = it },
                        onFontSizeChange = { fontSize = it },
                        onTextAlignmentChange = { textAlignment = it },
                        onSleepModeChange = { sleepModeEnabled = it },
                        onSleepTimeChange = { sleepTimeMinutes = it },
                        onDismiss = { showSettings = false },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Download Progress Dialog
                if (showDownloadDialog) {
                    val (current, total) = downloadProgress
                    val progress = if (total > 0) current.toFloat() / total else 0f
                    val elapsedSeconds = (System.currentTimeMillis() - downloadStartTime) / 1000
                    
                    AlertDialog(
                        onDismissRequest = { /* Prevent dismissal during download */ },
                        title = { Text("Downloading Chapter Audio") },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Generating audio for entire chapter...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Text(
                                        text = "${elapsedSeconds}s",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Text(
                                    text = "This may take a few minutes for long chapters...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    downloadJob?.cancel()
                                    isDownloading = false
                                    showDownloadDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
                // Download Success Dialog
                if (showDownloadSuccess) {
                    val downloadedFiles = ttsService.getDownloadedChapters()
                    val latestFile = downloadedFiles.maxByOrNull { it.createdTime }
                    
                    AlertDialog(
                        onDismissRequest = { showDownloadSuccess = false },
                        title = { Text("Download Complete") },
                        text = { 
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Chapter audio has been downloaded successfully!")
                                
                                latestFile?.let { fileInfo ->
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    Text(
                                        text = "File: ${fileInfo.file.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Size: ${fileInfo.sizeBytes / 1024 / 1024} MB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Location: ${fileInfo.file.parent}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDownloadSuccess = false }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            latestFile?.let { fileInfo ->
                                TextButton(
                                    onClick = {
                                        // Open file location in explorer
                                        scope.launch {
                                            try {
                                                val command = if (System.getProperty("os.name").lowercase().contains("win")) {
                                                    listOf("explorer.exe", "/select,", fileInfo.file.absolutePath)
                                                } else if (System.getProperty("os.name").lowercase().contains("mac")) {
                                                    listOf("open", "-R", fileInfo.file.absolutePath)
                                                } else {
                                                    listOf("xdg-open", fileInfo.file.parent)
                                                }
                                                ProcessBuilder(command).start()
                                            } catch (e: Exception) {
                                                // Fallback: just open the folder
                                                java.awt.Desktop.getDesktop().open(fileInfo.file.parentFile)
                                            }
                                        }
                                    }
                                ) {
                                    Text("Show in Folder")
                                }
                            }
                        }
                    )
                }
                
                // Download Error Dialog
                downloadError?.let { error ->
                    AlertDialog(
                        onDismissRequest = { downloadError = null },
                        title = { Text("Download Failed") },
                        text = { 
                            Column {
                                Text(error)
                                if (error.contains("not initialized") || error.contains("select a voice")) {
                                    Text(
                                        "\n\nPlease select a voice model from the Voice button before downloading.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { downloadError = null }) {
                                Text("OK")
                            }
                        }
                    )
                }
                
                // Source Not Installed Warning
                if (ttsService.state.sourceNotInstalledError) {
                    AlertDialog(
                        onDismissRequest = { ttsService.state.sourceNotInstalledError = false },
                        title = { Text("Source Extension Not Installed") },
                        text = { 
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "The source extension for this book is not installed.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "You can still read the current chapter, but you won't be able to navigate to other chapters until you install the source extension.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { ttsService.state.sourceNotInstalledError = false }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    ttsService.state.sourceNotInstalledError = false
                                    // Navigate to extensions/sources screen
                                    navController.navigate("extensions")
                                }
                            ) {
                                Text("Install Source")
                            }
                        }
                    )
                }
            }
        }
    }
}
