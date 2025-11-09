# Design Document

## Overview

This design document outlines the comprehensive UI improvement strategy for the IReader application. The improvements focus on enhancing user experience through modernized UI components, consistent design patterns, and improved functionality across all settings screens, explore screen, webview, and detail screens. The design follows Material Design 3 principles and clean code architecture patterns already established in the codebase.

## Architecture

### Component Architecture

The application uses a component-based architecture with the following layers:

1. **Presentation Layer**: Composable UI components built with Jetpack Compose
2. **ViewModel Layer**: State management and business logic
3. **Domain Layer**: Use cases and business rules
4. **Data Layer**: Repositories and data sources

### UI Component System

The existing component system uses a sealed class hierarchy (`Components`) that provides a declarative way to build settings screens. We will extend this system with new component types and enhance existing ones.

```
Components (sealed class)
├── Header
├── Row
├── Switch
├── Slider
├── Chip
├── Dynamic
├── Space
└── [NEW] RowPreference variants
```

## Components and Interfaces

### 1. Enhanced RowPreference Component

**Purpose**: Create a more flexible and visually appealing row preference component with support for various layouts and interactions.

**Interface**:
```kotlin
@Composable
fun RowPreference(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
)
```

**Design Decisions**:
- Use Material Design 3 typography and spacing
- Support both icon and painter for flexibility
- Provide trailing content slot for custom actions
- Implement proper touch feedback with ripple effects
- Support disabled state with visual feedback

### 2. Settings Screen Component System

**Purpose**: Standardize all settings screens with consistent layouts and components.

**Components to Enhance**:

#### a. PreferenceRow
- Add elevation and card-like appearance option
- Improve spacing and padding consistency
- Add support for leading icons with proper sizing
- Enhance subtitle typography and color

#### b. SwitchPreference
- Improve switch positioning and alignment
- Add haptic feedback on toggle
- Enhance visual feedback for state changes

#### c. ChoicePreference
- Modernize dialog appearance
- Add search functionality for long lists
- Improve radio button styling
- Add animation for selection changes

#### d. SliderPreference
- Enhance slider thumb and track styling
- Improve value display formatting
- Add step indicators for discrete values
- Better label positioning

### 3. Explore Screen Enhancements

**Purpose**: Create a more engaging and efficient browsing experience.

**Key Components**:

#### a. Novel Card Component
```kotlin
@Composable
fun NovelCard(
    novel: Book,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Features**:
- Enhanced cover image loading with shimmer effect
- Improved aspect ratio handling (3:4 standard)
- Better text overlay for title and metadata
- Smooth animations for interactions
- Badge support for status indicators

#### b. Filter Bottom Sheet
- Modernized sheet appearance with rounded corners
- Better organization of filter options
- Clear visual hierarchy
- Smooth animations for show/hide

#### c. Grid Layout
- Adaptive column count based on screen size
- Improved spacing between items
- Better scroll performance with lazy loading
- Support for different layout modes (grid/list)

### 4. WebView Screen Improvements

**Purpose**: Enhance the webview functionality for better novel fetching experience.

**Key Changes**:

#### a. Fetch Button State Management
```kotlin
sealed class FetchButtonState {
    object Enabled : FetchButtonState()
    object Fetching : FetchButtonState()
    object Success : FetchButtonState()
    data class Error(val message: String) : FetchButtonState()
}
```

**Design Decisions**:
- Fetch button always enabled regardless of page load state
- Show loading indicator during fetch operation
- Display success/error feedback with snackbar
- Allow retry on failure

#### b. Automatic Fetching System
```kotlin
interface AutoFetchDetector {
    suspend fun detectNovelContent(url: String): NovelDetectionResult
    suspend fun autoFetch(url: String): FetchResult
}
```

**Features**:
- Content detection using URL patterns and DOM analysis
- Configurable auto-fetch preference
- User notification system for fetch results
- Fallback to manual fetch if auto-detection fails

### 5. Browser Engine Optimization

**Purpose**: Improve page loading speed and novel parsing accuracy.

**Optimizations**:

#### a. Resource Loading
- Implement selective resource loading (block ads, images when not needed)
- Use caching strategies for frequently accessed sources
- Optimize JavaScript execution

#### b. Novel Parsing
- Improved HTML parsing algorithms
- Better error handling and recovery
- Support for more source formats
- Caching of parsed data

### 6. Theme System Enhancement

**Purpose**: Provide more customization options and better theme management.

**Components**:

#### a. Theme Selector
- Expanded preset theme collection (add 5-10 new themes)
- Better theme preview with live updates
- Improved theme card design with better visual representation
- Support for theme categories (light/dark/colorful/minimal)

#### b. Color Customization
- Real-time color preview
- Color harmony suggestions
- Better color picker UI
- Preset color palettes

### 7. Detail Screen Improvements

**Purpose**: Create a more informative and visually appealing book detail screen.

**Layout Structure**:
```
┌─────────────────────────────┐
│     Enhanced Header         │
│  (Cover + Title + Metadata) │
├─────────────────────────────┤
│    Action Buttons Row       │
├─────────────────────────────┤
│    Description Section      │
├─────────────────────────────┤
│    Chapters List            │
│    (Optimized Rendering)    │
└─────────────────────────────┘
```

**Enhancements**:
- Parallax effect for cover image
- Better metadata organization with icons
- Improved chapter list performance
- Enhanced action button styling
- Better loading states

### 8. About Screen Enhancement

**Purpose**: Create a more polished and informative about screen.

**Components**:

#### a. Logo Header
- Larger, centered logo
- Animated entrance
- Better spacing

#### b. Version Information
- Card-based layout
- Better typography hierarchy
- Copy-to-clipboard functionality

#### c. Social Links
- Larger touch targets
- Better icon styling
- Hover/press effects

### 9. Download Screen Enhancement

**Purpose**: Improve download management UI.

**Components**:

#### a. Download Item
- Enhanced progress indicators
- Better status icons and colors
- Improved action button layout
- Swipe actions for quick operations

#### b. Download Controls
- Better FAB positioning
- Clear pause/resume states
- Batch operation support

### 10. Category Screen Enhancement

**Purpose**: Improve category management experience.

**Components**:

#### a. Category Item
- Better drag handle visibility
- Improved reorder animations
- Enhanced delete confirmation
- Category count badges

#### b. Add Category Dialog
- Better input field styling
- Validation feedback
- Keyboard handling improvements

## Data Models

### UI State Models

```kotlin
// Settings Screen State
data class SettingsScreenState(
    val preferences: List<PreferenceItem>,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Explore Screen State
data class ExploreScreenState(
    val novels: List<Book>,
    val filters: List<Filter>,
    val layout: LayoutType,
    val isLoading: Boolean,
    val error: String? = null
)

// WebView State
data class WebViewState(
    val url: String,
    val isLoading: Boolean,
    val fetchButtonState: FetchButtonState,
    val autoFetchEnabled: Boolean,
    val detectedContent: NovelDetectionResult? = null
)

// Theme State
data class ThemeState(
    val currentTheme: Theme,
    val availableThemes: List<Theme>,
    val customColors: CustomColors,
    val isDirty: Boolean = false
)
```

### Preference Models

```kotlin
sealed class PreferenceItem {
    data class Header(val text: String) : PreferenceItem()
    data class Row(val title: String, val subtitle: String?, val action: () -> Unit) : PreferenceItem()
    data class Switch(val title: String, val value: Boolean, val onChange: (Boolean) -> Unit) : PreferenceItem()
    data class Choice<T>(val title: String, val value: T, val choices: Map<T, String>) : PreferenceItem()
    data class Slider(val title: String, val value: Float, val range: ClosedFloatingPointRange<Float>) : PreferenceItem()
}
```

## Error Handling

### Error Categories

1. **Network Errors**: Failed to load content, timeout
2. **Parsing Errors**: Failed to extract novel data
3. **Storage Errors**: Failed to save preferences or data
4. **UI Errors**: Component rendering failures

### Error Handling Strategy

```kotlin
sealed class UiError {
    data class Network(val message: String, val retry: () -> Unit) : UiError()
    data class Parsing(val message: String) : UiError()
    data class Storage(val message: String) : UiError()
    data class Unknown(val throwable: Throwable) : UiError()
}

// Error Display Component
@Composable
fun ErrorDisplay(
    error: UiError,
    onDismiss: () -> Unit
)
```

**Error Display Patterns**:
- Snackbar for transient errors
- Dialog for critical errors requiring user action
- Inline error messages for form validation
- Retry buttons for recoverable errors

## Testing Strategy

### Unit Testing

**Components to Test**:
1. ViewModel logic for all screens
2. Preference state management
3. Novel parsing algorithms
4. Theme color calculations
5. Auto-fetch detection logic

**Testing Approach**:
```kotlin
class ExploreViewModelTest {
    @Test
    fun `when filters applied, novels are filtered correctly`()
    
    @Test
    fun `when layout changed, state updates correctly`()
    
    @Test
    fun `when error occurs, error state is set`()
}
```

### UI Testing

**Screens to Test**:
1. Settings screens navigation and preference changes
2. Explore screen filtering and layout switching
3. WebView fetch functionality
4. Theme switching and customization
5. Detail screen interactions

**Testing Approach**:
```kotlin
@Test
fun testSettingsScreenPreferenceChange() {
    // Given: Settings screen is displayed
    // When: User toggles a switch preference
    // Then: Preference value is updated and persisted
}
```

### Integration Testing

**Scenarios to Test**:
1. End-to-end novel fetching flow
2. Theme application across all screens
3. Preference persistence and restoration
4. Navigation between screens with state preservation

## Performance Considerations

### Optimization Strategies

1. **Lazy Loading**:
   - Use LazyColumn/LazyRow for all lists
   - Implement pagination for large datasets
   - Load images on-demand with caching

2. **State Management**:
   - Use remember and derivedStateOf appropriately
   - Minimize recomposition scope
   - Use keys for list items

3. **Image Loading**:
   - Implement proper image caching
   - Use appropriate image sizes
   - Lazy load images in lists

4. **WebView Optimization**:
   - Block unnecessary resources
   - Cache parsed content
   - Use background threads for parsing

### Performance Metrics

- Screen load time: < 500ms
- List scroll performance: 60 FPS
- Image load time: < 1s
- Theme switch time: < 200ms

## Accessibility

### Accessibility Features

1. **Content Descriptions**: All interactive elements have proper content descriptions
2. **Touch Targets**: Minimum 48dp touch target size
3. **Color Contrast**: WCAG AA compliance for all text
4. **Screen Reader Support**: Proper semantic structure
5. **Keyboard Navigation**: Support for external keyboards

### Implementation

```kotlin
@Composable
fun AccessiblePreferenceRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .semantics {
                contentDescription = "$title. ${subtitle ?: ""}"
                role = Role.Button
            }
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        // Content
    }
}
```

## Clean Code Principles

### Code Organization

```
presentation/
├── ui/
│   ├── settings/
│   │   ├── appearance/
│   │   │   ├── AppearanceScreen.kt
│   │   │   ├── AppearanceViewModel.kt
│   │   │   └── components/
│   │   │       ├── ThemeSelector.kt
│   │   │       └── ColorPicker.kt
│   │   ├── general/
│   │   ├── advanced/
│   │   └── components/
│   │       ├── SettingsRow.kt
│   │       └── SettingsSection.kt
│   ├── explore/
│   │   ├── ExploreScreen.kt
│   │   ├── ExploreViewModel.kt
│   │   └── components/
│   │       ├── NovelCard.kt
│   │       ├── FilterSheet.kt
│   │       └── NovelGrid.kt
│   └── component/
│       └── components/
│           ├── PreferenceRow.kt
│           ├── RowPreference.kt (NEW)
│           └── EnhancedComponents.kt (NEW)
```

### Naming Conventions

- **Composables**: PascalCase with descriptive names (e.g., `EnhancedNovelCard`)
- **ViewModels**: PascalCase ending with `ViewModel` (e.g., `ExploreViewModel`)
- **State Classes**: PascalCase ending with `State` (e.g., `ExploreScreenState`)
- **Use Cases**: PascalCase describing action (e.g., `FetchNovelUseCase`)

### Component Design Principles

1. **Single Responsibility**: Each composable has one clear purpose
2. **Composition Over Inheritance**: Use composition for reusability
3. **Stateless When Possible**: Prefer stateless composables with state hoisting
4. **Clear Parameters**: Use named parameters with default values
5. **Documentation**: Add KDoc for public APIs

### Example Clean Component

```kotlin
/**
 * A preference row component that displays a title, optional subtitle,
 * and optional trailing content.
 *
 * @param title The main text to display
 * @param subtitle Optional secondary text
 * @param icon Optional leading icon
 * @param onClick Callback when the row is clicked
 * @param trailing Optional trailing content composable
 * @param modifier Modifier for the row
 */
@Composable
fun RowPreference(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

## Migration Strategy

### Phase 1: Component Enhancement
1. Create new enhanced components alongside existing ones
2. Test new components in isolation
3. Update one screen at a time to use new components

### Phase 2: Screen Updates
1. Update settings screens (Appearance, General, Advanced)
2. Update explore screen
3. Update detail screen
4. Update about, download, and category screens

### Phase 3: WebView and Auto-Fetch
1. Implement fetch button state management
2. Add auto-fetch detection system
3. Integrate with existing webview

### Phase 4: Theme System
1. Add new preset themes
2. Enhance color customization
3. Improve theme preview

### Rollback Plan
- Keep old components available during migration
- Use feature flags for new UI elements
- Monitor crash reports and user feedback
- Quick rollback capability if issues arise

## Design Patterns

### State Management Pattern

```kotlin
// ViewModel
class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    fun onPreferenceChanged(preference: Preference) {
        viewModelScope.launch {
            // Update state
            _state.update { it.copy(/* updated values */) }
        }
    }
}

// Screen
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    
    SettingsContent(
        state = state,
        onPreferenceChanged = viewModel::onPreferenceChanged
    )
}
```

### Component Composition Pattern

```kotlin
// Base component
@Composable
fun BasePreferenceRow(/* params */) { /* implementation */ }

// Specialized components
@Composable
fun SwitchPreferenceRow(/* params */) {
    BasePreferenceRow(
        trailing = { Switch(/* params */) }
    )
}

@Composable
fun ChoicePreferenceRow(/* params */) {
    BasePreferenceRow(
        trailing = { ChoiceIndicator(/* params */) }
    )
}
```

## Visual Design Specifications

### Spacing
- Small: 4dp
- Medium: 8dp
- Large: 16dp
- Extra Large: 24dp

### Typography
- Title: MaterialTheme.typography.titleLarge
- Body: MaterialTheme.typography.bodyLarge
- Caption: MaterialTheme.typography.bodyMedium

### Colors
- Primary: MaterialTheme.colorScheme.primary
- Secondary: MaterialTheme.colorScheme.secondary
- Background: MaterialTheme.colorScheme.background
- Surface: MaterialTheme.colorScheme.surface

### Elevation
- Card: 2dp
- Dialog: 8dp
- FAB: 6dp

### Corner Radius
- Small: 4dp
- Medium: 8dp
- Large: 16dp
- Extra Large: 28dp

## Dependencies

### Required Libraries
- Jetpack Compose (already in use)
- Material Design 3 (already in use)
- Coil for image loading (already in use)
- Voyager for navigation (already in use)
- Kotlin Coroutines (already in use)

### No New Dependencies Required
All improvements can be implemented using existing dependencies.

## Conclusion

This design provides a comprehensive approach to improving the UI across the IReader application. By following Material Design 3 principles, maintaining clean code practices, and implementing enhancements incrementally, we can significantly improve the user experience while maintaining code quality and maintainability.
