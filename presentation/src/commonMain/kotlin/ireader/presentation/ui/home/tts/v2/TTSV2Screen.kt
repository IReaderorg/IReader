package ireader.presentation.ui.home.tts.v2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.services.tts_service.v2.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.StateFlow

/**
 * TTS V2 Screen - Example implementation using the new TTS v2 architecture
 * 
 * This is a reference implementation showing how to use:
 * - TTSViewModelAdapter for state observation
 * - TTSCommand dispatch for actions
 * - TTSEvent handling for one-time events
 * 
 * Features demonstrated:
 * - Play/Pause/Stop controls
 * - Paragraph navigation
 * - Chapter navigation
 * - Speed control
 * - Chunk mode for remote TTS
 * - Progress display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSV2Screen(
    adapter: TTSViewModelAdapter,
    sleepTimerState: StateFlow<TTSSleepTimerUseCase.SleepTimerState>? = null,
    onSleepTimerStart: ((Int) -> Unit)? = null,
    onSleepTimerCancel: (() -> Unit)? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect state
    val state by adapter.state.collectAsState()
    val isPlaying by adapter.isPlaying.collectAsState()
    val isLoading by adapter.isLoading.collectAsState()
    val currentParagraph by adapter.currentParagraph.collectAsState()
    val totalParagraphs by adapter.totalParagraphs.collectAsState()
    val progress by adapter.progress.collectAsState()
    val chapterTitle by adapter.chapterTitle.collectAsState()
    val bookTitle by adapter.bookTitle.collectAsState()
    val speed by adapter.speed.collectAsState()
    val hasError by adapter.hasError.collectAsState()
    val errorMessage by adapter.errorMessage.collectAsState()
    val chunkModeEnabled by adapter.chunkModeEnabled.collectAsState()
    val currentChunkIndex by adapter.currentChunkIndex.collectAsState()
    val totalChunks by adapter.totalChunks.collectAsState()
    
    // Sleep timer state
    val sleepTimer = sleepTimerState?.collectAsState()?.value
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    
    // Snackbar for errors
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle events
    LaunchedEffect(Unit) {
        adapter.events.collectLatest { event ->
            when (event) {
                is TTSEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = when (event.error) {
                            is TTSError.NoContent -> "No content to read"
                            is TTSError.EngineNotReady -> "TTS engine not ready"
                            is TTSError.SpeechFailed -> "Speech failed: ${(event.error as TTSError.SpeechFailed).message}"
                            else -> "An error occurred"
                        }
                    )
                }
                is TTSEvent.ChapterCompleted -> {
                    snackbarHostState.showSnackbar("Chapter completed")
                }
                else -> {}
            }
        }
    }
    
    // Auto-scroll to current paragraph
    val listState = rememberLazyListState()
    LaunchedEffect(currentParagraph) {
        if (currentParagraph > 0 && state.paragraphs.isNotEmpty()) {
            listState.animateScrollToItem(currentParagraph)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapterTitle.ifEmpty { "TTS Player" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (bookTitle.isNotEmpty()) {
                            Text(
                                text = bookTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Sleep timer button
                    if (sleepTimerState != null) {
                        IconButton(onClick = { showSleepTimerDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (sleepTimer?.isEnabled == true) {
                                        Badge { Text(sleepTimer.remainingMinutes.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = "Sleep Timer",
                                    tint = if (sleepTimer?.isEnabled == true) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Settings button
                    IconButton(onClick = { /* TODO: Show settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            TTSV2ControlBar(
                isPlaying = isPlaying,
                isLoading = isLoading,
                progress = progress,
                currentParagraph = currentParagraph,
                totalParagraphs = totalParagraphs,
                speed = speed,
                chunkModeEnabled = chunkModeEnabled,
                currentChunk = currentChunkIndex,
                totalChunks = totalChunks,
                onPlayPause = { adapter.togglePlayPause() },
                onStop = { adapter.stop() },
                onPrevious = { 
                    if (chunkModeEnabled) adapter.previousChunk() 
                    else adapter.previousParagraph() 
                },
                onNext = { 
                    if (chunkModeEnabled) adapter.nextChunk() 
                    else adapter.nextParagraph() 
                },
                onPreviousChapter = { adapter.previousChapter() },
                onNextChapter = { adapter.nextChapter() },
                onSpeedChange = { adapter.setSpeed(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.paragraphs.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No content loaded",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Paragraph list
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.paragraphs) { index, paragraph ->
                        ParagraphItem(
                            text = paragraph,
                            index = index,
                            isCurrentParagraph = index == currentParagraph,
                            isInCurrentChunk = chunkModeEnabled && index in state.currentChunkParagraphs,
                            onClick = { adapter.jumpToParagraph(index) }
                        )
                    }
                }
            }
            
            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Sleep timer dialog
    if (showSleepTimerDialog && sleepTimerState != null) {
        SleepTimerDialog(
            currentState = sleepTimer,
            onStart = { minutes -> onSleepTimerStart?.invoke(minutes) },
            onCancel = { onSleepTimerCancel?.invoke() },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}


/**
 * Paragraph item in the content list
 */
@Composable
private fun ParagraphItem(
    text: String,
    index: Int,
    isCurrentParagraph: Boolean,
    isInCurrentChunk: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCurrentParagraph -> MaterialTheme.colorScheme.primaryContainer
        isInCurrentChunk -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val textColor = when {
        isCurrentParagraph -> MaterialTheme.colorScheme.onPrimaryContainer
        isInCurrentChunk -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Paragraph number
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.width(32.dp)
            )
            
            // Paragraph text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isCurrentParagraph) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

/**
 * TTS Control Bar at the bottom of the screen
 */
@Composable
private fun TTSV2ControlBar(
    isPlaying: Boolean,
    isLoading: Boolean,
    progress: Float,
    currentParagraph: Int,
    totalParagraphs: Int,
    speed: Float,
    chunkModeEnabled: Boolean,
    currentChunk: Int,
    totalChunks: Int,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSpeedDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (chunkModeEnabled) {
                        "Chunk ${currentChunk + 1} / $totalChunks"
                    } else {
                        "Paragraph ${currentParagraph + 1} / $totalParagraphs"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Speed indicator
                TextButton(onClick = { showSpeedDialog = true }) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${speed}x")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous chapter
                IconButton(onClick = onPreviousChapter) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Chapter")
                }
                
                // Previous paragraph/chunk
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.FastRewind, contentDescription = "Previous")
                }
                
                // Play/Pause
                FilledTonalIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(56.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Next paragraph/chunk
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.FastForward, contentDescription = "Next")
                }
                
                // Next chapter
                IconButton(onClick = onNextChapter) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next Chapter")
                }
            }
        }
    }
    
    // Speed dialog
    if (showSpeedDialog) {
        SpeedDialog(
            currentSpeed = speed,
            onSpeedSelected = { 
                onSpeedChange(it)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false }
        )
    }
}

/**
 * Sleep timer dialog
 */
@Composable
fun SleepTimerDialog(
    currentState: TTSSleepTimerUseCase.SleepTimerState?,
    onStart: (Int) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val timerOptions = listOf(5, 10, 15, 30, 45, 60, 90, 120)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep Timer") },
        text = {
            Column {
                if (currentState?.isEnabled == true) {
                    // Show active timer info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Timer Active",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentState.formatRemaining(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { currentState.progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Add more time:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Stop playback after:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Timer options grid
                Column {
                    timerOptions.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { minutes ->
                                FilterChip(
                                    selected = false,
                                    onClick = { 
                                        onStart(minutes)
                                        onDismiss()
                                    },
                                    label = { Text("${minutes}m") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if row is incomplete
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            if (currentState?.isEnabled == true) {
                TextButton(onClick = {
                    onCancel()
                    onDismiss()
                }) {
                    Text("Cancel Timer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Speed selection dialog
 */
@Composable
private fun SpeedDialog(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playback Speed") },
        text = {
            Column {
                speeds.forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = speed == currentSpeed,
                            onClick = { onSpeedSelected(speed) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${speed}x")
                    }
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
