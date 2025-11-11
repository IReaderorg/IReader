package ireader.presentation.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.reader.components.DesktopTTSControlPanel
import ireader.presentation.ui.reader.components.TTSSettingsPanel
import org.koin.compose.koinInject

actual class TTSScreenSpec actual constructor(
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val readingParagraph: Int
) : VoyagerScreen() {

    override val key: ScreenKey
        get() = "TTS_SCREEN#$chapterId"

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
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
        
        // Determine actual colors to use
        val backgroundColor = if (useCustomColors) customBackgroundColor else MaterialTheme.colorScheme.background
        val textColor = if (useCustomColors) customTextColor else MaterialTheme.colorScheme.onBackground
        
        // Initialize TTS with the chapter
        LaunchedEffect(bookId, chapterId) {
            ttsService.startReading(bookId, chapterId)
            ttsService.state.currentReadingParagraph = readingParagraph
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
                            onClick = { navigator.pop() }
                        )
                    },
                    actions = {
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
            }
        }
    }
}
