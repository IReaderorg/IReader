# Reader Plugin System

This directory contains the UI integration for feature plugins in the reader screen. The system allows plugins like AI Summarizer, Dictionary, Translation, and other reader tools to display their UI within the reader interface.

## Overview

The reader plugin system is built on top of the existing plugin-api infrastructure:

- **PluginUIProvider** - Interface that plugins implement to provide declarative UI
- **PluginUIComponent** - Sealed class defining UI components (Text, Button, Card, etc.)
- **FeaturePlugin** - Base interface for reader-screen plugins
- **ReaderPluginPanel** - Main UI component that displays plugins in the reader

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Reader Screen                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐    │
│  │              ReaderPluginPanel                       │    │
│  │  ┌─────────────────────────────────────────────┐    │    │
│  │  │  Plugin Tab Row (switch between plugins)     │    │    │
│  │  └─────────────────────────────────────────────┘    │    │
│  │  ┌─────────────────────────────────────────────┐    │    │
│  │  │  PluginUIRenderer                            │    │    │
│  │  │  (renders declarative UI from plugin)        │    │    │
│  │  └─────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Plugin (External)                        │
│  implements PluginUIProvider, FeaturePlugin                 │
│  - getScreen() → PluginUIScreen                             │
│  - handleEvent() → PluginUIScreen (updated)                 │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### ReaderPluginPanel

Main composable that displays feature plugins in the reader screen:

```kotlin
@Composable
fun ReaderPluginPanel(
    plugins: List<FeaturePlugin>,      // Available feature plugins
    context: PluginScreenContext,       // Reader context (book, chapter, text)
    onDismiss: () -> Unit,              // Callback to close panel
    modifier: Modifier = Modifier,
    initialPluginId: String? = null     // Optional plugin to select first
)
```

### ReaderPluginFab

Floating action button to open the plugin panel:

```kotlin
@Composable
fun ReaderPluginFab(
    hasPlugins: Boolean,    // Whether any plugins are available
    onClick: () -> Unit,    // Callback to open panel
    modifier: Modifier = Modifier
)
```

### ReaderPluginQuickAccess

Quick access toolbar for frequently used plugins:

```kotlin
@Composable
fun ReaderPluginQuickAccess(
    plugins: List<FeaturePlugin>,       // Available plugins
    onPluginClick: (FeaturePlugin) -> Unit,  // Plugin selected callback
    modifier: Modifier = Modifier
)
```

### ReaderPluginMenuItem

Menu item for the reader's overflow menu:

```kotlin
@Composable
fun ReaderPluginMenuItem(
    plugin: FeaturePlugin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

## PluginScreenContext

Context data passed to plugins:

```kotlin
data class PluginScreenContext(
    val bookId: Long? = null,           // Current book ID
    val chapterId: Long? = null,        // Current chapter ID
    val chapterTitle: String? = null,   // Chapter title
    val bookTitle: String? = null,      // Book title
    val selectedText: String? = null,   // User-selected text
    val chapterContent: String? = null, // Full chapter content
    val onDismiss: () -> Unit = {},     // Dismiss callback
    val dataStorage: PluginDataStorageApi? = null  // Plugin storage
)
```

## Creating a Reader Plugin

### Step 1: Implement Plugin Interfaces

```kotlin
class MyReaderPlugin : FeaturePlugin, PluginUIProvider {
    
    override val manifest = PluginManifest(
        id = "com.example.myplugin",
        name = "My Reader Tool",
        type = PluginType.FEATURE,
        permissions = listOf(PluginPermission.READER_CONTEXT)
    )
    
    // FeaturePlugin implementation
    override fun getMenuItems(): List<PluginMenuItem> {
        return listOf(
            PluginMenuItem(
                id = "action",
                label = "My Tool",
                icon = "auto_awesome"
            )
        )
    }
    
    override fun getScreens(): List<PluginScreen> = emptyList()
    
    override fun onReaderContext(context: ReaderContext): PluginAction? {
        return null // or return action to execute
    }
    
    // PluginUIProvider implementation
    override fun getScreen(screenId: String, context: PluginScreenContext): PluginUIScreen? {
        return PluginUIScreen(
            id = "main",
            title = "My Tool",
            components = buildUI(context)
        )
    }
    
    override suspend fun handleEvent(
        screenId: String,
        event: PluginUIEvent,
        context: PluginScreenContext
    ): PluginUIScreen? {
        // Handle user interactions and return updated screen
        when (event.eventType) {
            UIEventType.CLICK -> {
                if (event.componentId == "myButton") {
                    // Perform action
                }
            }
            // ... handle other events
        }
        return getScreen(screenId, context)
    }
}
```

### Step 2: Build Declarative UI

Use PluginUIComponent to define your UI:

```kotlin
private fun buildUI(context: PluginScreenContext): List<PluginUIComponent> {
    return listOf(
        // Title
        PluginUIComponent.Text(
            text = "Analysis Results",
            style = TextStyle.TITLE_MEDIUM
        ),
        
        // Spacer
        PluginUIComponent.Spacer(height = 16),
        
        // Card with content
        PluginUIComponent.Card(
            children = listOf(
                PluginUIComponent.Text(
                    text = context.selectedText ?: "No text selected",
                    style = TextStyle.BODY
                )
            )
        ),
        
        // Action button
        PluginUIComponent.Button(
            id = "analyze",
            label = "Analyze",
            style = ButtonStyle.PRIMARY,
            icon = "auto_awesome"
        ),
        
        // Tabs example
        PluginUIComponent.Tabs(
            tabs = listOf(
                Tab(
                    id = "summary",
                    title = "Summary",
                    icon = "list",
                    content = listOf(
                        PluginUIComponent.Text("Summary content...")
                    )
                ),
                Tab(
                    id = "details",
                    title = "Details",
                    icon = "info",
                    content = listOf(
                        PluginUIComponent.Text("Details content...")
                    )
                )
            )
        )
    )
}
```

## Available UI Components

### Text
```kotlin
PluginUIComponent.Text(
    text = "Hello World",
    style = TextStyle.BODY  // TITLE_LARGE, TITLE_MEDIUM, TITLE_SMALL, BODY, BODY_SMALL, LABEL
)
```

### TextField
```kotlin
PluginUIComponent.TextField(
    id = "searchField",
    label = "Search",
    value = "",
    multiline = false,
    maxLines = 1
)
```

### Button
```kotlin
PluginUIComponent.Button(
    id = "submitBtn",
    label = "Submit",
    style = ButtonStyle.PRIMARY,  // PRIMARY, SECONDARY, OUTLINED, TEXT
    icon = "check"
)
```

### Card
```kotlin
PluginUIComponent.Card(
    children = listOf(
        PluginUIComponent.Text("Card content")
    )
)
```

### Row / Column
```kotlin
PluginUIComponent.Row(
    spacing = 8,
    children = listOf(...)
)

PluginUIComponent.Column(
    spacing = 8,
    children = listOf(...)
)
```

### Switch
```kotlin
PluginUIComponent.Switch(
    id = "darkMode",
    label = "Dark Mode",
    checked = false
)
```

### Chip / ChipGroup
```kotlin
PluginUIComponent.ChipGroup(
    id = "categories",
    chips = listOf(
        PluginUIComponent.Chip(id = "cat1", label = "Category 1", selected = true),
        PluginUIComponent.Chip(id = "cat2", label = "Category 2", selected = false)
    ),
    singleSelection = true
)
```

### Tabs
```kotlin
PluginUIComponent.Tabs(
    tabs = listOf(
        Tab(id = "tab1", title = "Tab 1", content = listOf(...)),
        Tab(id = "tab2", title = "Tab 2", content = listOf(...))
    )
)
```

### ItemList
```kotlin
PluginUIComponent.ItemList(
    id = "historyList",
    items = listOf(
        ListItem(id = "item1", title = "Item 1", subtitle = "Description"),
        ListItem(id = "item2", title = "Item 2", subtitle = "Description")
    )
)
```

### Loading / Empty / Error
```kotlin
PluginUIComponent.Loading(message = "Loading...")

PluginUIComponent.Empty(
    icon = "search",
    message = "No results",
    description = "Try a different search term"
)

PluginUIComponent.Error(message = "Something went wrong")
```

### ProgressBar
```kotlin
PluginUIComponent.ProgressBar(
    progress = 0.5f,
    label = "Processing..."
)
```

## Event Handling

When users interact with UI components, events are sent to the plugin:

```kotlin
data class PluginUIEvent(
    val componentId: String,          // ID of the component
    val eventType: UIEventType,       // Type of event
    val data: Map<String, String>     // Additional data
)

enum class UIEventType {
    CLICK,              // Button clicked
    TEXT_CHANGED,       // Text field value changed
    SWITCH_TOGGLED,     // Switch toggled
    CHIP_SELECTED,      // Chip selected
    LIST_ITEM_CLICKED,  // List item clicked
    TAB_SELECTED        // Tab selected
}
```

### Event Data

- **TEXT_CHANGED**: `data["value"]` contains new text
- **SWITCH_TOGGLED**: `data["checked"]` contains "true" or "false"
- **CHIP_SELECTED**: `data["value"]` contains chip ID
- **LIST_ITEM_CLICKED**: `data["itemId"]` contains item ID
- **TAB_SELECTED**: `data["index"]` contains tab index

## Available Icons

The following icon names can be used in buttons, tabs, and list items:

- `notes`, `note_add` - Notes icon
- `people`, `person_add` - People icon
- `person` - Person icon
- `timeline` - Timeline icon
- `auto_awesome` - Sparkle/AI icon
- `list` - List icon
- `history` - History icon
- `settings` - Settings icon
- `save` - Save icon
- `delete` - Delete icon
- `add` - Add icon
- `refresh` - Refresh icon
- `close` - Close icon
- `check` - Check icon
- `error` - Error icon
- `warning` - Warning icon
- `info` - Info icon
- `search` - Search icon
- `bookmark`, `bookmark_add` - Bookmark icon
- `label` - Label icon
- `folder` - Folder icon
- `highlight`, `format_quote` - Quote icon
- `timer` - Timer icon
- `play_arrow` - Play icon
- `stop` - Stop icon
- `analytics`, `trending_up` - Analytics icon
- `local_fire_department` - Fire/Streak icon
- `flag` - Goal icon
- `emoji_events` - Achievement icon
- `school` - Learning icon
- `book` - Book icon
- `download_done` - Download complete icon
- `extension` - Extension/Plugin icon
- `coffee` - Coffee icon
- `image` - Image icon

## Integration in Reader Screen

To integrate the plugin panel in your reader screen:

```kotlin
@Composable
fun ReaderScreen(
    viewModel: ReaderScreenViewModel,
    // ... other parameters
) {
    val plugins by viewModel.featurePlugins.collectAsState()
    var showPluginPanel by remember { mutableStateOf(false) }
    val selectedText by viewModel.selectedText.collectAsState()
    
    // ... reader content
    
    // Plugin FAB
    if (plugins.isNotEmpty()) {
        ReaderPluginFab(
            hasPlugins = plugins.isNotEmpty(),
            onClick = { showPluginPanel = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
    
    // Plugin Panel (Bottom Sheet)
    if (showPluginPanel) {
        ModalBottomSheet(
            onDismissRequest = { showPluginPanel = false }
        ) {
            ReaderPluginPanel(
                plugins = plugins,
                context = PluginScreenContext(
                    bookId = viewModel.bookId,
                    chapterId = viewModel.chapterId,
                    bookTitle = viewModel.bookTitle,
                    chapterTitle = viewModel.chapterTitle,
                    selectedText = selectedText,
                    chapterContent = viewModel.chapterContent
                ),
                onDismiss = { showPluginPanel = false }
            )
        }
    }
}
```

## Example Plugins

### AI Summarizer Plugin

```kotlin
class AISummarizerPlugin : FeaturePlugin, PluginUIProvider {
    override val manifest = PluginManifest(
        id = "com.example.aisummarizer",
        name = "AI Summarizer",
        type = PluginType.FEATURE
    )
    
    override fun getScreen(screenId: String, context: PluginScreenContext): PluginUIScreen? {
        return PluginUIScreen(
            id = "main",
            title = "AI Summarizer",
            components = listOf(
                PluginUIComponent.Text("Summarize Content", TextStyle.TITLE_MEDIUM),
                PluginUIComponent.Spacer(16),
                PluginUIComponent.Card(
                    children = listOf(
                        PluginUIComponent.Text(
                            context.chapterContent?.take(500) ?: "No content",
                            TextStyle.BODY_SMALL
                        )
                    )
                ),
                PluginUIComponent.Button("summarize", "Generate Summary", ButtonStyle.PRIMARY, "auto_awesome")
            )
        )
    }
    
    override suspend fun handleEvent(screenId: String, event: PluginUIEvent, context: PluginScreenContext): PluginUIScreen? {
        if (event.componentId == "summarize" && event.eventType == UIEventType.CLICK) {
            // Call AI API and return updated screen with summary
        }
        return getScreen(screenId, context)
    }
}
```

### Dictionary Plugin

```kotlin
class DictionaryPlugin : FeaturePlugin, PluginUIProvider {
    override val manifest = PluginManifest(
        id = "com.example.dictionary",
        name = "Dictionary",
        type = PluginType.FEATURE
    )
    
    override fun getScreen(screenId: String, context: PluginScreenContext): PluginUIScreen? {
        val word = context.selectedText ?: return getEmptyScreen()
        
        return PluginUIScreen(
            id = "main",
            title = "Dictionary",
            components = listOf(
                PluginUIComponent.Text(word, TextStyle.TITLE_LARGE),
                PluginUIComponent.Spacer(8),
                PluginUIComponent.Text("Loading definition...", TextStyle.BODY)
            )
        )
    }
    
    private fun getEmptyScreen(): PluginUIScreen {
        return PluginUIScreen(
            id = "main",
            title = "Dictionary",
            components = listOf(
                PluginUIComponent.Empty(
                    icon = "book",
                    message = "Select a word",
                    description = "Highlight text to look up its definition"
                )
            )
        )
    }
}
```

## UI Builder DSL

The `ReaderPluginUIBuilder` provides a convenient DSL for creating plugin UIs without boilerplate:

### Basic Usage

```kotlin
import ireader.presentation.ui.reader.plugins.*

// Create a screen using the DSL
val screen = pluginScreen("main", "My Plugin") {
    title("Welcome")
    spacer(16)
    textField("input", "Enter text")
    row {
        primaryButton("submit", "Submit")
        secondaryButton("cancel", "Cancel")
    }
}
```

### Available DSL Methods

| Method | Description |
|--------|-------------|
| `text(text, style)` | Add text with optional style |
| `title(text)` | Add large title text |
| `subtitle(text)` | Add medium subtitle text |
| `body(text)` | Add body text |
| `textField(id, label, value, multiline, maxLines)` | Add text input |
| `textArea(id, label, value, maxLines)` | Add multiline text input |
| `button(id, label, style, icon)` | Add button |
| `primaryButton(id, label, icon)` | Add primary button |
| `secondaryButton(id, label, icon)` | Add secondary button |
| `outlinedButton(id, label, icon)` | Add outlined button |
| `textButton(id, label, icon)` | Add text button |
| `card { }` | Add card container |
| `row(spacing) { }` | Add row layout |
| `column(spacing) { }` | Add column layout |
| `switch(id, label, checked)` | Add switch |
| `chip(id, label, selected)` | Add chip |
| `chipGroup(id, singleSelection) { }` | Add chip group |
| `list(id, items)` | Add list |
| `tabs { }` | Add tabs |
| `loading(message)` | Add loading indicator |
| `empty(message, icon, description)` | Add empty state |
| `error(message)` | Add error message |
| `spacer(height)` | Add spacer |
| `divider(thickness)` | Add divider |
| `progressBar(progress, label)` | Add progress bar |
| `image(url, width, height, contentDescription)` | Add image |
| `ifElse(condition, { }, { })` | Conditional content |

### Complete Example with DSL

```kotlin
class MyPlugin : FeaturePlugin, PluginUIProvider {
    
    override fun getScreen(screenId: String, context: PluginScreenContext): PluginUIScreen? {
        return when (screenId) {
            "main" -> pluginScreen("main", "My Plugin") {
                // Header with book info
                context.bookTitle?.let { title(it) }
                context.chapterTitle?.let { subtitle(it) }
                spacer(16)
                
                // Options
                text("Options:", TextStyle.LABEL)
                chipGroup("options", singleSelection = true) {
                    chip("opt1", "Option 1", selected = true)
                    chip("opt2", "Option 2")
                    chip("opt3", "Option 3")
                }
                spacer(16)
                
                // Settings
                switch("setting1", "Enable feature", checked = true)
                switch("setting2", "Show details")
                spacer(16)
                
                // Actions
                row(spacing = 8) {
                    primaryButton("action", "Run", icon = "auto_awesome")
                    textButton("settings", "Settings", icon = "settings")
                }
            }
            
            "settings" -> pluginScreen("settings", "Settings") {
                text("Configure the plugin", TextStyle.BODY)
                spacer(16)
                textField("api_key", "API Key")
                spacer(8)
                switch("auto_run", "Run automatically")
                spacer(16)
                primaryButton("save", "Save", icon = "save")
            }
            
            else -> null
        }
    }
    
    override suspend fun handleEvent(
        screenId: String,
        event: PluginUIEvent,
        context: PluginScreenContext
    ): PluginUIScreen? {
        return when (event.componentId) {
            "action" -> pluginScreen("result", "Results") {
                loading("Processing...")
            }
            "settings" -> getScreen("settings", context)
            "save" -> pluginScreen("saved", "Saved") {
                card {
                    text("✓ Settings saved!", TextStyle.TITLE_MEDIUM)
                }
                spacer(16)
                primaryButton("back", "Back")
            }
            "back" -> getScreen("main", context)
            else -> getScreen(screenId, context)
        }
    }
}
```

## Built-in Plugins

The reader plugin system includes these built-in plugins:

### AI Summarizer (`SummarizerPlugin.kt`)
- Summarizes chapter content using AI
- Configurable summary length (short, medium, detailed)
- Multiple output styles (bullet points, paragraph, key points)
- Options to include quotes and character list
- History of previous summaries

### Smart Dictionary (`DictionaryPlugin.kt`)
- Word definitions with examples
- Translation to multiple languages
- Synonyms and related words
- Vocabulary bookmarking
- Lookup history

## Related Files

- [`ReaderPluginUIBuilder.kt`](./ReaderPluginUIBuilder.kt) - DSL for building plugin UIs
- [`SummarizerPlugin.kt`](./SummarizerPlugin.kt) - AI Summarizer example plugin
- [`DictionaryPlugin.kt`](./DictionaryPlugin.kt) - Smart Dictionary example plugin
- [`ReaderPluginModule.kt`](./ReaderPluginModule.kt) - Koin module for built-in plugins
- [`plugin-api/src/commonMain/kotlin/ireader/plugin/api/PluginUI.kt`](../../../../../../../plugin-api/src/commonMain/kotlin/ireader/plugin/api/PluginUI.kt) - Core UI definitions
- [`plugin-api/src/commonMain/kotlin/ireader/plugin/api/FeaturePlugin.kt`](../../../../../../../plugin-api/src/commonMain/kotlin/ireader/plugin/api/FeaturePlugin.kt) - Feature plugin interface
- [`presentation/src/commonMain/kotlin/ireader/presentation/ui/plugins/PluginUIRenderer.kt`](../../plugins/PluginUIRenderer.kt) - Renders declarative UI to Compose

## Best Practices

1. **Keep UI Simple**: Use declarative components efficiently. Complex UIs should be broken into cards.

2. **Handle Loading States**: Show loading indicators during async operations.

3. **Error Handling**: Display user-friendly error messages using the Error component.

4. **Context Awareness**: Use PluginScreenContext to provide relevant functionality based on the current reading state.

5. **Event Processing**: Process events asynchronously and return updated screens.

6. **Resource Management**: Don't hold references to Context or other Android resources.

7. **Use the DSL**: The `ReaderPluginUIBuilder` DSL makes code more readable and maintainable.

8. **State Management**: Use `rememberSaveable` in the renderer ensures text field state survives recomposition.
