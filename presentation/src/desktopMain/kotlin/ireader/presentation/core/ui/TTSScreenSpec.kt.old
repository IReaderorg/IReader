package ireader.presentation.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.reader.components.DesktopTTSControls
import ireader.presentation.ui.reader.components.DesktopTTSIndicator
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
        
        // Initialize TTS with the chapter
        LaunchedEffect(bookId, chapterId) {
            ttsService.startReading(bookId, chapterId)
            // Start from the specified paragraph
            ttsService.state.currentReadingParagraph = readingParagraph
        }
        
        // Cleanup on dispose
        DisposableEffect(Unit) {
            onDispose {
                ttsService.startService(DesktopTTSService.ACTION_PAUSE)
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Book info
                    ttsService.state.ttsBook?.let { book ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                book.author.takeIf { it.isNotBlank() }?.let { author ->
                                    Text(
                                        text = "by $author",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Current paragraph preview
                    ttsService.state.ttsContent?.value?.let { content ->
                        if (content.isNotEmpty() && ttsService.state.currentReadingParagraph < content.size) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Current Paragraph",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Display text with word highlighting
                                    TTSTextWithHighlighting(
                                        text = content[ttsService.state.currentReadingParagraph],
                                        ttsService = ttsService
                                    )
                                }
                            }
                        }
                    }
                    
                    // TTS Controls
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DesktopTTSControls(
                            ttsService = ttsService,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // TTS Indicator
                if (ttsService.state.isPlaying) {
                    DesktopTTSIndicator(
                        ttsService = ttsService,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}