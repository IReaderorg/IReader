# Design Document

## Overview

This design document outlines the architecture for implementing advanced reader features in the IReader application. The features are prioritized by impact: reliability improvements (Auto-Chapter Repair, Smart Source Switching), AI enhancements (Custom AI Glossary), reading experience improvements (TTS Read-Along, Voice Commands, Reading Break Reminders, Resume Last Read), lifecycle management (End of Life Management), visual enhancements (Dynamic Reader Theme), and monetization features (Cryptocurrency Donations).

### Design Principles

1. **Reliability First**: Auto-repair and source switching prevent reading disruptions
2. **Non-Intrusive UX**: Banners and notifications should enhance, not interrupt
3. **Performance**: Cache expensive operations (color extraction, source checks)
4. **Platform Support**: Android and Desktop where applicable
5. **Clean Architecture**: Maintain separation of concerns
6. **Offline-First**: Core features work without network
7. **Privacy**: No data collection without consent
8. **Extensibility**: Easy to add new sources and features

---

## Architecture

### Layer Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Screens    │  │  ViewModels  │  │   Dialogs    │      │
│  │  (Compose)   │  │   (State)    │  │  (Banners)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Use Cases   │  │  Repositories│  │   Entities   │      │
│  │  (Business)  │  │  (Interfaces)│  │   (Models)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Repositories │  │   Database   │  │   Cache      │      │
│  │    (Impl)    │  │  (SQLDelight)│  │   (Memory)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

---

## Components and Interfaces

### 1. Auto-Chapter Repair System

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     ReaderScreen                             │
│  - Detects broken chapters                                  │
│  - Shows repair banner                                      │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              ChapterHealthChecker                            │
│  + isChapterBroken(content: String): Boolean                │
│  + getBreakReason(content: String): BreakReason             │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              AutoRepairChapterUseCase                        │
│  + invoke(chapterNum, novelTitle): Result<Chapter>          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              SourceRepository                                │
│  + searchNovelInAllSources(title): List<Novel>              │
│  + getChapterFromSource(sourceId, chapterNum): Chapter      │
└─────────────────────────────────────────────────────────────┘
```

#### Detection Algorithm

```kotlin
fun isChapterBroken(content: String): Boolean {
    val wordCount = content.split("\\s+".toRegex()).size
    val alphaRatio = content.count { it.isLetter() }.toFloat() / content.length
    
    return when {
        wordCount < 50 -> true
        content.isBlank() -> true
        alphaRatio < 0.5 -> true // Scrambled
        else -> false
    }
}
```


#### Repair Flow

```
User opens chapter
    → ReaderViewModel.loadChapter(chapterId)
    → ChapterHealthChecker.isChapterBroken(content)
    → If broken:
        → Show banner "This chapter appears broken"
        → AutoRepairChapterUseCase.invoke(chapterNum, novelTitle)
        → Search all installed sources
        → Find matching novel
        → Fetch same chapter number
        → Validate replacement (wordCount > 50)
        → Replace content
        → Show "Found working chapter from [Source]"
```

#### Caching Strategy

- Cache repair attempts for 24 hours to avoid repeated searches
- Store successful repairs in database for offline access
- Clear cache on source updates

---

### 2. Smart Source Switching

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                  NovelDetailScreen                           │
│  - Shows source switching banner                            │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│           CheckSourceAvailabilityUseCase                     │
│  + invoke(novelTitle): SourceComparison                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              MigrateToSourceUseCase                          │
│  + invoke(novelId, targetSourceId): Result<Unit>            │
└─────────────────────────────────────────────────────────────┘
```

#### Detection Logic

```kotlin
data class SourceComparison(
    val currentSource: Source,
    val betterSource: Source?,
    val chapterDifference: Int
)

suspend fun checkSourceAvailability(novelTitle: String): SourceComparison {
    val allSources = sourceRepository.getAllInstalledSources()
    val currentChapterCount = currentSource.getChapterCount(novelTitle)
    
    val betterSource = allSources
        .filter { it.id != currentSource.id }
        .map { source ->
            val count = source.getChapterCount(novelTitle)
            source to (count - currentChapterCount)
        }
        .filter { (_, diff) -> diff >= 5 }
        .maxByOrNull { (_, diff) -> diff }
    
    return SourceComparison(
        currentSource = currentSource,
        betterSource = betterSource?.first,
        chapterDifference = betterSource?.second ?: 0
    )
}
```

#### Banner UI

```kotlin
@Composable
fun SourceSwitchingBanner(
    sourceName: String,
    chapterDifference: Int,
    onSwitch: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = true) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Source $sourceName has $chapterDifference new chapters")
                Row {
                    TextButton(onClick = onSwitch) { Text("Switch") }
                    TextButton(onClick = onDismiss) { Text("Dismiss") }
                }
            }
        }
    }
}
```

---

### 3. Custom AI Glossary

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     ReaderScreen                             │
│  - Long-press context menu                                  │
│  - "Create Glossary Entry" option                           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│           CreateGlossaryEntryDialog                          │
│  - Original term (pre-filled)                               │
│  - Preferred term (user input)                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│           CreateGlossaryEntryUseCase                         │
│  + invoke(novelId, original, preferred): Result<Unit>       │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              GlossaryRepository                              │
│  + insertEntry(entry: GlossaryEntry)                        │
│  + getEntriesForNovel(novelId): List<GlossaryEntry>         │
│  + updateEntry(id, preferred)                               │
│  + deleteEntry(id)                                          │
└─────────────────────────────────────────────────────────────┘
```

#### Translation Integration

```kotlin
suspend fun translateWithGlossary(
    text: String,
    novelId: Long,
    targetLanguage: String
): String {
    // 1. Get glossary entries
    val glossary = glossaryRepository.getEntriesForNovel(novelId)
    
    // 2. Replace original terms with placeholders
    var processedText = text
    val replacements = mutableMapOf<String, String>()
    
    glossary.sortedByDescending { it.originalTerm.length }.forEach { entry ->
        val placeholder = "{{GLOSS_${entry.id}}}"
        processedText = processedText.replace(
            entry.originalTerm,
            placeholder,
            ignoreCase = true
        )
        replacements[placeholder] = entry.preferredTerm
    }
    
    // 3. Translate
    val translated = translationEngine.translate(processedText, targetLanguage)
    
    // 4. Replace placeholders with preferred terms
    var finalText = translated
    replacements.forEach { (placeholder, preferred) ->
        finalText = finalText.replace(placeholder, preferred)
    }
    
    return finalText
}
```


---

### 4. True Read-Along TTS

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     ReaderScreen                             │
│  - Highlighted word/sentence                                │
│  - Auto-scroll to current position                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  TTSReadAlongManager                         │
│  + startReadAlong(text: String)                             │
│  + onWordBoundary(wordIndex: Int)                           │
│  + getCurrentWordPosition(): Int                            │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  Platform TTS Engine                         │
│  - Android: TextToSpeech                                    │
│  - Desktop: Java Speech API                                 │
└─────────────────────────────────────────────────────────────┘
```

#### Word Highlighting Implementation

```kotlin
@Composable
fun ReadAlongText(
    text: String,
    currentWordIndex: Int,
    modifier: Modifier = Modifier
) {
    val words = remember(text) { text.split(" ") }
    
    LazyColumn(modifier = modifier) {
        itemsIndexed(words) { index, word ->
            Text(
                text = word,
                modifier = Modifier.background(
                    if (index == currentWordIndex) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    } else {
                        Color.Transparent
                    }
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

#### Auto-Scroll Logic

```kotlin
class TTSAutoScroller(
    private val listState: LazyListState,
    private val scope: CoroutineScope
) {
    private var userScrollPauseTime: Long = 0
    private val pauseDuration = 5000L // 5 seconds
    
    fun scrollToWord(wordIndex: Int) {
        val currentTime = System.currentTimeMillis()
        
        // Don't auto-scroll if user recently scrolled manually
        if (currentTime - userScrollPauseTime < pauseDuration) {
            return
        }
        
        scope.launch {
            listState.animateScrollToItem(
                index = wordIndex,
                scrollOffset = -200 // Keep word centered
            )
        }
    }
    
    fun onUserScroll() {
        userScrollPauseTime = System.currentTimeMillis()
    }
}
```

---

### 5. Voice Command Navigation

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     ReaderScreen                             │
│  - Microphone indicator                                     │
│  - Command feedback                                         │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              VoiceCommandManager                             │
│  + startListening()                                         │
│  + stopListening()                                          │
│  + onCommandRecognized(command: String)                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              CommandParser                                   │
│  + parseCommand(text: String): ReaderCommand?               │
└─────────────────────────────────────────────────────────────┘
```

#### Command Mapping

```kotlin
sealed class ReaderCommand {
    object NextChapter : ReaderCommand()
    object PreviousChapter : ReaderCommand()
    object PauseReading : ReaderCommand()
    object ResumeReading : ReaderCommand()
    object CreateBookmark : ReaderCommand()
    object StopReading : ReaderCommand()
}

class CommandParser {
    private val commandPatterns = mapOf(
        "next chapter" to ReaderCommand.NextChapter,
        "go to next chapter" to ReaderCommand.NextChapter,
        "previous chapter" to ReaderCommand.PreviousChapter,
        "go back" to ReaderCommand.PreviousChapter,
        "pause reading" to ReaderCommand.PauseReading,
        "pause" to ReaderCommand.PauseReading,
        "resume reading" to ReaderCommand.ResumeReading,
        "resume" to ReaderCommand.ResumeReading,
        "continue" to ReaderCommand.ResumeReading,
        "bookmark this" to ReaderCommand.CreateBookmark,
        "save bookmark" to ReaderCommand.CreateBookmark,
        "stop reading" to ReaderCommand.StopReading,
        "stop" to ReaderCommand.StopReading
    )
    
    fun parseCommand(text: String): ReaderCommand? {
        val normalized = text.lowercase().trim()
        return commandPatterns.entries
            .firstOrNull { (pattern, _) -> normalized.contains(pattern) }
            ?.value
    }
}
```

#### Platform Implementation

**Android:**
```kotlin
class AndroidVoiceCommandManager(
    private val context: Context
) : VoiceCommandManager {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    
    override fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }
}
```

**Desktop:**
```kotlin
class DesktopVoiceCommandManager : VoiceCommandManager {
    // Use Java Speech API or external library
    override fun startListening() {
        // Implementation for desktop
    }
}
```

---

### 6. Reading Break Reminder

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     ReaderScreen                             │
│  - Reading timer                                            │
│  - Break reminder dialog                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              ReadingTimerManager                             │
│  + startTimer(intervalMinutes: Int)                         │
│  + pauseTimer()                                             │
│  + resumeTimer()                                            │
│  + resetTimer()                                             │
└─────────────────────────────────────────────────────────────┘
```

#### Timer Implementation

```kotlin
class ReadingTimerManager(
    private val scope: CoroutineScope,
    private val onIntervalReached: () -> Unit
) {
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var intervalMillis: Long = 0
    private var timerJob: Job? = null
    
    fun startTimer(intervalMinutes: Int) {
        intervalMillis = intervalMinutes * 60 * 1000L
        startTime = System.currentTimeMillis()
        
        timerJob = scope.launch {
            while (isActive) {
                delay(1000) // Check every second
                
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed >= intervalMillis) {
                    onIntervalReached()
                    resetTimer()
                    break
                }
            }
        }
    }
    
    fun pauseTimer() {
        pausedTime = System.currentTimeMillis()
        timerJob?.cancel()
    }
    
    fun resumeTimer() {
        val pauseDuration = System.currentTimeMillis() - pausedTime
        startTime += pauseDuration
        startTimer((intervalMillis / 60000).toInt())
    }
}
```


---

### 7. Resume Last Read Button

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    LibraryScreen                             │
│  - Resume card at top                                       │
│  - Shows last read novel + chapter                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              GetLastReadNovelUseCase                         │
│  + invoke(): LastReadInfo?                                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              ReadingHistoryRepository                        │
│  + getLastReadChapter(): Chapter?                           │
│  + getReadingProgress(chapterId): Int                       │
└─────────────────────────────────────────────────────────────┘
```

#### UI Implementation

```kotlin
@Composable
fun ResumeReadingCard(
    lastRead: LastReadInfo,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onResume),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = lastRead.coverUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp, 120.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continue Reading",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = lastRead.novelTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Chapter ${lastRead.chapterNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = lastRead.progressPercent / 100f,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Resume",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
```

---

### 8. End of Life Management

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                  NovelDetailScreen                           │
│  - "Mark as Completed" button                               │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              EndOfLifeOptionsDialog                          │
│  - Archive option                                           │
│  - Download as ePub option                                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  ArchiveNovelUseCase  |  ExportNovelAsEpubUseCase           │
└─────────────────────────────────────────────────────────────┘
```

#### ePub Export Implementation

```kotlin
class ExportNovelAsEpubUseCase(
    private val chapterRepository: ChapterRepository,
    private val epubGenerator: EpubGenerator
) {
    suspend fun invoke(novelId: Long): Result<File> {
        return try {
            // 1. Fetch all chapters
            val chapters = chapterRepository.getAllChaptersForNovel(novelId)
            
            // 2. Clean content
            val cleanedChapters = chapters.map { chapter ->
                chapter.copy(
                    content = cleanHtmlContent(chapter.content)
                )
            }
            
            // 3. Generate ePub
            val epubFile = epubGenerator.generate(
                novel = novel,
                chapters = cleanedChapters
            )
            
            Result.Success(epubFile)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    private fun cleanHtmlContent(html: String): String {
        return html
            .replace(Regex("<script[^>]*>.*?</script>"), "")
            .replace(Regex("<style[^>]*>.*?</style>"), "")
            .replace(Regex("<!--.*?-->"), "")
            .replace(Regex("<div class=\"ad\".*?</div>"), "")
            .trim()
    }
}
```

#### ePub Structure

```
novel.epub
├── META-INF/
│   └── container.xml
├── OEBPS/
│   ├── content.opf (metadata)
│   ├── toc.ncx (table of contents)
│   ├── cover.jpg
│   ├── chapter001.xhtml
│   ├── chapter002.xhtml
│   └── ...
└── mimetype
```

---

### 9. Dynamic Reader Theme

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     ReaderScreen                             │
│  - Dynamic background tint                                  │
│  - Dynamic toolbar color                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              CoverColorExtractor                             │
│  + extractColors(imageUrl: String): ColorPalette            │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              ColorPaletteCache                               │
│  + get(novelId): ColorPalette?                              │
│  + put(novelId, palette)                                    │
└─────────────────────────────────────────────────────────────┘
```

#### Color Extraction

```kotlin
class CoverColorExtractor {
    suspend fun extractColors(imageUrl: String): ColorPalette {
        return withContext(Dispatchers.IO) {
            val bitmap = loadBitmap(imageUrl)
            val palette = Palette.from(bitmap).generate()
            
            ColorPalette(
                primary = palette.getDominantColor(Color.Gray.toArgb()),
                accent = palette.getVibrantColor(Color.Blue.toArgb()),
                background = palette.getLightMutedColor(Color.White.toArgb())
            )
        }
    }
    
    private fun adjustForReadability(color: Int): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)
        
        // Ensure lightness is in readable range
        hsl[2] = hsl[2].coerceIn(0.2f, 0.8f)
        
        return ColorUtils.HSLToColor(hsl)
    }
}

data class ColorPalette(
    val primary: Int,
    val accent: Int,
    val background: Int
)
```

#### Theme Application

```kotlin
@Composable
fun DynamicReaderTheme(
    colorPalette: ColorPalette?,
    content: @Composable () -> Unit
) {
    val colors = if (colorPalette != null) {
        MaterialTheme.colorScheme.copy(
            primary = Color(colorPalette.accent),
            primaryContainer = Color(colorPalette.primary).copy(alpha = 0.2f),
            surface = Color(colorPalette.background).copy(alpha = 0.05f)
        )
    } else {
        MaterialTheme.colorScheme
    }
    
    MaterialTheme(colorScheme = colors) {
        content()
    }
}
```


---

### 10. Cryptocurrency Donation System

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   DonationScreen                             │
│  - Wallet addresses with QR codes                           │
│  - Copy to clipboard buttons                                │
│  - Fund-a-Feature progress                                  │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              DonationTriggerManager                          │
│  + checkTriggers(event: UserEvent)                          │
│  + shouldShowPrompt(): Boolean                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              WalletIntegrationManager                        │
│  + openWallet(walletApp: String, address: String)           │
│  + generatePaymentUri(crypto: String, address: String)      │
└─────────────────────────────────────────────────────────────┘
```

#### Donation Triggers

```kotlin
sealed class DonationTrigger {
    data class BookCompleted(val chapterCount: Int) : DonationTrigger()
    object MigrationSuccess : DonationTrigger()
    data class ChapterMilestone(val count: Int) : DonationTrigger()
}

class DonationTriggerManager(
    private val preferences: PreferencesRepository
) {
    fun checkTriggers(event: UserEvent): DonationTrigger? {
        return when (event) {
            is UserEvent.BookMarkedCompleted -> {
                if (event.chapterCount >= 500) {
                    DonationTrigger.BookCompleted(event.chapterCount)
                } else null
            }
            is UserEvent.SourceMigrationCompleted -> {
                if (isFirstMigration()) {
                    DonationTrigger.MigrationSuccess
                } else null
            }
            is UserEvent.ChapterRead -> {
                val totalChapters = preferences.getTotalChaptersRead()
                if (totalChapters % 1000 == 0) {
                    DonationTrigger.ChapterMilestone(totalChapters)
                } else null
            }
            else -> null
        }
    }
    
    fun shouldShowPrompt(): Boolean {
        val lastPromptTime = preferences.getLastDonationPromptTime()
        val daysSinceLastPrompt = 
            (System.currentTimeMillis() - lastPromptTime) / (1000 * 60 * 60 * 24)
        return daysSinceLastPrompt >= 30
    }
}
```

#### Wallet Integration

```kotlin
class WalletIntegrationManager(private val context: Context) {
    
    fun openWallet(walletApp: WalletApp, address: String, amount: Double? = null) {
        val uri = when (walletApp) {
            WalletApp.TRUST_WALLET -> "trust://send?asset=ETH&address=$address"
            WalletApp.METAMASK -> "metamask://send/$address"
            WalletApp.COINBASE -> "coinbase://send?address=$address"
        }
        
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Show "Wallet not installed" message
        }
    }
    
    fun generateQRCode(address: String): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(
            address,
            BarcodeFormat.QR_CODE,
            512,
            512
        )
        return bitMatrix.toBitmap()
    }
}

enum class WalletApp {
    TRUST_WALLET,
    METAMASK,
    COINBASE
}
```

---

### 11. Supporter Badge System

#### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│           SupporterBadgeVerificationDialog                   │
│  - Transaction hash input                                   │
│  - User ID input                                            │
│  - Submit button                                            │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              SubmitSupporterVerificationUseCase              │
│  + invoke(txHash: String, userId: String): Result<Unit>     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              SupabaseRepository                              │
│  + submitVerification(data: VerificationData)               │
│  + getSupporterStatus(userId): Boolean                      │
└─────────────────────────────────────────────────────────────┘
```

#### Verification Flow

```
User donates crypto
    → User taps "I've donated! Get my badge"
    → SupporterBadgeVerificationDialog opens
    → User enters TXID and User ID
    → SubmitSupporterVerificationUseCase.invoke()
    → Send to Supabase for manual review
    → Admin verifies TXID on blockchain explorer
    → Admin sets is_supporter = true in database
    → User syncs and receives supporter status
    → Unlock supporter themes and badge
```

#### Supporter Features

```kotlin
data class SupporterStatus(
    val isSupporter: Boolean,
    val supporterSince: Long?,
    val tier: SupporterTier
)

enum class SupporterTier {
    NONE,
    BRONZE,  // $5+
    SILVER,  // $20+
    GOLD     // $50+
}

class SupporterFeatureManager(
    private val supporterStatus: SupporterStatus
) {
    fun getAvailableThemes(): List<Theme> {
        return when (supporterStatus.tier) {
            SupporterTier.NONE -> emptyList()
            SupporterTier.BRONZE -> listOf(Theme.SupporterGold)
            SupporterTier.SILVER -> listOf(
                Theme.SupporterGold,
                Theme.SupporterPlatinum
            )
            SupporterTier.GOLD -> listOf(
                Theme.SupporterGold,
                Theme.SupporterPlatinum,
                Theme.SupporterDiamond
            )
        }
    }
    
    fun shouldShowBadge(): Boolean = supporterStatus.isSupporter
}
```

---

## Data Models

### ChapterHealth

```kotlin
data class ChapterHealth(
    val chapterId: Long,
    val isBroken: Boolean,
    val breakReason: BreakReason?,
    val checkedAt: Long
)

enum class BreakReason {
    LOW_WORD_COUNT,
    EMPTY_CONTENT,
    SCRAMBLED_TEXT,
    HTTP_ERROR
}
```

### SourceComparison

```kotlin
data class SourceComparison(
    val novelId: Long,
    val currentSource: Source,
    val betterSource: Source?,
    val chapterDifference: Int,
    val cachedAt: Long
)
```

### GlossaryEntry

```kotlin
data class GlossaryEntry(
    val id: Long,
    val novelId: Long,
    val originalTerm: String,
    val preferredTerm: String,
    val createdAt: Long,
    val updatedAt: Long
)
```

### LastReadInfo

```kotlin
data class LastReadInfo(
    val novelId: Long,
    val novelTitle: String,
    val coverUrl: String,
    val chapterId: Long,
    val chapterNumber: Int,
    val chapterTitle: String,
    val progressPercent: Int,
    val scrollPosition: Int,
    val lastReadAt: Long
)
```

### DonationConfig

```kotlin
data class DonationConfig(
    val wallets: Map<CryptoType, String>,
    val fundingGoals: List<FundingGoal>
)

enum class CryptoType {
    BITCOIN,
    ETHEREUM,
    LITECOIN
}

data class FundingGoal(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String = "USD"
)
```


---

## Error Handling

### Error Categories

1. **Source Errors**: Failed to fetch from alternative sources
2. **Translation Errors**: API failures, quota exceeded
3. **TTS Errors**: Engine not available, language not supported
4. **Voice Recognition Errors**: Permission denied, no speech detected
5. **Export Errors**: Insufficient storage, file write failures
6. **Network Errors**: Blockchain verification, Supabase sync
7. **Payment Errors**: Invalid wallet address, QR generation failures

### Error Handling Strategy

```kotlin
sealed class AppError {
    data class SourceError(val message: String) : AppError()
    data class TranslationError(val message: String) : AppError()
    data class TTSError(val message: String) : AppError()
    data class VoiceError(val message: String) : AppError()
    data class ExportError(val message: String) : AppError()
    data class NetworkError(val message: String) : AppError()
    data class PaymentError(val message: String) : AppError()
}

fun AppError.toUserMessage(): String {
    return when (this) {
        is AppError.SourceError -> "Unable to find working chapter: $message"
        is AppError.TranslationError -> "Translation failed: $message"
        is AppError.TTSError -> "Text-to-speech unavailable: $message"
        is AppError.VoiceError -> "Voice command failed: $message"
        is AppError.ExportError -> "Export failed: $message"
        is AppError.NetworkError -> "Network error: $message"
        is AppError.PaymentError -> "Payment error: $message"
    }
}
```

---

## Testing Strategy

### Unit Tests

**Auto-Repair Logic:**
```kotlin
class ChapterHealthCheckerTest {
    @Test
    fun `should detect broken chapter with low word count`() {
        val checker = ChapterHealthChecker()
        val content = "Short text"
        
        assertTrue(checker.isChapterBroken(content))
        assertEquals(BreakReason.LOW_WORD_COUNT, checker.getBreakReason(content))
    }
    
    @Test
    fun `should not detect healthy chapter as broken`() {
        val checker = ChapterHealthChecker()
        val content = "This is a healthy chapter with more than fifty words " +
            "and proper content that should not be flagged as broken..."
        
        assertFalse(checker.isChapterBroken(content))
    }
}
```

**Glossary Application:**
```kotlin
class GlossaryTranslationTest {
    @Test
    fun `should replace original terms with preferred terms`() = runTest {
        val glossary = listOf(
            GlossaryEntry(1, 1, "Lee Kang", "Li Gang", 0, 0),
            GlossaryEntry(2, 1, "Rhee Gang", "Li Gang", 0, 0)
        )
        
        val text = "Lee Kang met Rhee Gang at the market"
        val result = translateWithGlossary(text, glossary)
        
        assertTrue(result.contains("Li Gang"))
        assertFalse(result.contains("Lee Kang"))
        assertFalse(result.contains("Rhee Gang"))
    }
}
```

### Integration Tests

**Source Switching:**
```kotlin
class SourceSwitchingIntegrationTest {
    @Test
    fun `should successfully migrate to better source`() = runTest {
        // Setup
        val currentSource = mockSource(chapterCount = 100)
        val betterSource = mockSource(chapterCount = 120)
        
        // Execute
        val result = migrateToSourceUseCase(novelId, betterSource.id)
        
        // Verify
        assertTrue(result is Result.Success)
        assertEquals(betterSource.id, novel.sourceId)
    }
}
```

### UI Tests

**Donation Flow:**
```kotlin
@Test
fun donationScreen_showsWalletAddresses() {
    composeTestRule.setContent {
        DonationScreen()
    }
    
    composeTestRule
        .onNodeWithText("Bitcoin")
        .assertIsDisplayed()
    
    composeTestRule
        .onNodeWithText("Ethereum")
        .assertIsDisplayed()
}
```

---

## Performance Considerations

### Caching Strategy

1. **Source Comparison Cache**: 24-hour TTL
2. **Color Palette Cache**: Permanent (per novel)
3. **Glossary Cache**: In-memory during reading session
4. **Chapter Health Cache**: 1-hour TTL

### Memory Management

```kotlin
class CacheManager {
    private val sourceComparisonCache = LruCache<Long, SourceComparison>(50)
    private val colorPaletteCache = LruCache<Long, ColorPalette>(100)
    
    fun getSourceComparison(novelId: Long): SourceComparison? {
        val cached = sourceComparisonCache.get(novelId)
        return if (cached != null && !isCacheExpired(cached.cachedAt, 24.hours)) {
            cached
        } else {
            null
        }
    }
}
```

### Background Processing

1. **Source Checking**: Use coroutines with IO dispatcher
2. **Color Extraction**: Offload to background thread
3. **ePub Generation**: Use WorkManager for long operations
4. **TTS Processing**: Use dedicated TTS thread

---

## Security Considerations

### Cryptocurrency Safety

1. **No Private Keys**: Never store or handle private keys
2. **Address Validation**: Validate wallet addresses before display
3. **HTTPS Only**: All blockchain API calls use HTTPS
4. **Disclaimer**: Display clear disclaimer about crypto donations

```kotlin
const val CRYPTO_DISCLAIMER = """
    Cryptocurrency donations are non-refundable. 
    Please verify the wallet address before sending.
    IReader does not store or have access to your private keys.
"""
```

### User Privacy

1. **No Tracking**: Don't track donation amounts or user identity
2. **Optional Verification**: Badge verification is completely optional
3. **Local Storage**: Store supporter status locally, sync optionally
4. **Data Minimization**: Only collect TXID and user ID for verification

---

## Migration Strategy

### Database Migrations

```sql
-- Migration 1: Chapter Health Tracking
CREATE TABLE IF NOT EXISTS ChapterHealth (
    chapterId INTEGER PRIMARY KEY,
    isBroken INTEGER NOT NULL,
    breakReason TEXT,
    checkedAt INTEGER NOT NULL,
    FOREIGN KEY (chapterId) REFERENCES Chapter(id)
);

-- Migration 2: Source Comparison Cache
CREATE TABLE IF NOT EXISTS SourceComparisonCache (
    novelId INTEGER PRIMARY KEY,
    currentSourceId INTEGER,
    betterSourceId INTEGER,
    chapterDifference INTEGER,
    cachedAt INTEGER,
    FOREIGN KEY (novelId) REFERENCES Novel(id)
);

-- Migration 3: Glossary Entries
CREATE TABLE IF NOT EXISTS GlossaryEntry (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    novelId INTEGER NOT NULL,
    originalTerm TEXT NOT NULL,
    preferredTerm TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (novelId) REFERENCES Novel(id)
);

-- Migration 4: Reading Timer State
CREATE TABLE IF NOT EXISTS ReadingTimerState (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    startTime INTEGER,
    intervalMinutes INTEGER,
    isPaused INTEGER
);

-- Migration 5: Supporter Status
CREATE TABLE IF NOT EXISTS SupporterStatus (
    userId TEXT PRIMARY KEY,
    isSupporter INTEGER NOT NULL,
    supporterSince INTEGER,
    tier TEXT
);
```

### Preference Migrations

```kotlin
fun migratePreferences(prefs: Preferences): Preferences {
    return prefs.copy(
        voiceCommandsEnabled = false,
        readingBreakInterval = 60,
        dynamicThemeEnabled = true,
        lastDonationPromptTime = 0L
    )
}
```

---

## Deployment Plan

### Phase 1: Reliability Features (Week 1-2)
- Auto-Chapter Repair
- Smart Source Switching
- Chapter health detection
- Source comparison caching

### Phase 2: AI Enhancements (Week 3)
- Custom AI Glossary
- Glossary management UI
- Translation integration

### Phase 3: Reading Experience (Week 4-5)
- True Read-Along TTS
- Voice Command Navigation
- Reading Break Reminders
- Resume Last Read Button

### Phase 4: Lifecycle & Visual (Week 6)
- End of Life Management
- ePub Export
- Dynamic Reader Theme
- Color extraction

### Phase 5: Monetization (Week 7-8)
- Cryptocurrency Donation Page
- Wallet Integration
- Fund-a-Feature Progress
- Supporter Badge System

### Phase 6: Testing & Polish (Week 9)
- Comprehensive testing
- Bug fixes
- Performance optimization
- Documentation

---

## Rollback Strategy

### Feature Flags

```kotlin
object FeatureFlags {
    val AUTO_REPAIR_ENABLED = true
    val SMART_SOURCE_SWITCHING_ENABLED = true
    val CUSTOM_GLOSSARY_ENABLED = true
    val TTS_READ_ALONG_ENABLED = true
    val VOICE_COMMANDS_ENABLED = false // Gradual rollout
    val READING_BREAK_ENABLED = true
    val DYNAMIC_THEME_ENABLED = true
    val CRYPTO_DONATIONS_ENABLED = true
    val SUPPORTER_BADGES_ENABLED = true
}
```

### Rollback Procedure

1. Disable feature flag remotely
2. Clear related caches
3. Revert UI changes
4. Keep database tables (for future re-enable)
5. Monitor error rates

---

## Documentation

### User Documentation

1. **Auto-Repair Guide**: How chapter repair works
2. **Source Switching**: Understanding source recommendations
3. **Glossary Tutorial**: Creating and managing glossary entries
4. **Voice Commands**: List of available commands
5. **Donation Guide**: How to donate with cryptocurrency
6. **Supporter Benefits**: What supporters get

### Developer Documentation

1. **Architecture Overview**: System design
2. **API Reference**: Use cases and repositories
3. **Integration Guide**: Adding new features
4. **Testing Guide**: Writing tests
5. **Deployment Guide**: Release process

---

## Conclusion

This design provides a comprehensive architecture for implementing high-impact features that improve reliability, enhance the reading experience, and enable sustainable monetization. The phased approach allows for iterative development and testing while maintaining code quality and user experience. The focus on caching, performance, and error handling ensures the features work smoothly even in challenging network conditions.
