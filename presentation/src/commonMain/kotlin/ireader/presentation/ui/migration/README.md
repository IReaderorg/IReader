# Modern Source Migration UI

A completely redesigned migration interface with modern Material 3 design, smooth animations, and enhanced user experience.

## Features

### 🎨 Modern Design
- **Material 3 Components**: Uses latest Material Design 3 guidelines
- **Smooth Animations**: Spring-based animations for natural feel
- **Visual Hierarchy**: Clear information architecture with proper spacing
- **Adaptive Colors**: Respects system theme and color schemes

### 📱 Three Main Screens

#### 1. Migration List Screen (`ModernMigrationListScreen.kt`)
Select books to migrate with an intuitive interface:
- **Large Top App Bar**: Collapsing header with selection count
- **Smart Search**: Real-time filtering with clear button
- **Filter Chips**: Quick filters for migratable books and sorting
- **Animated Cards**: Scale and color transitions on selection
- **Floating Action Button**: Animated FAB for migration action
- **Empty State**: Helpful message when no books available

**Key Improvements:**
- Circular selection indicators with smooth transitions
- Book metadata displayed with icons (author, source)
- Animated selection count in header
- Better visual feedback on card selection

#### 2. Migration Config Screen (`ModernMigrationConfigScreen.kt`)
Configure migration settings with a beautiful interface:
- **Migration Options Card**: Toggle what data to transfer
  - Chapters & reading progress
  - Bookmarks
  - Categories
  - Custom covers
  - Reading position
- **Target Sources Card**: Select which sources to search
  - Visual selection state
  - Priority badges
  - Disabled source indicators
  - Selection counter

**Key Improvements:**
- Icon-based option items for better scannability
- Highlighted selected items with background tint
- Chip-based metadata display
- Save button in top bar

#### 3. Migration Progress Screen (`ModernMigrationProgressScreen.kt`)
Real-time migration tracking with detailed feedback:
- **Overall Progress Card**: 
  - Animated circular and linear progress indicators
  - Statistics (completed, failed, remaining)
  - Status indicators (running, paused, completed)
- **Current Migration Card**:
  - Rotating sync icon animation
  - Book title and progress bar
  - Smooth fade in/out transitions
- **Completed Migrations List**: Success indicators
- **Failed Migrations List**: Error messages with retry options

**Key Improvements:**
- Animated progress with smooth easing
- Real-time status updates
- Color-coded success/failure states
- Pause/Resume/Cancel controls

## Usage

### Basic Implementation

```kotlin
// Migration List
ModernMigrationListScreen(
    state = viewModel.state,
    onBookSelect = viewModel::selectBook,
    onSelectAll = viewModel::selectAll,
    onClearSelection = viewModel::clearSelection,
    onSearchQueryChange = viewModel::updateSearchQuery,
    onSortOrderChange = viewModel::updateSortOrder,
    onToggleShowOnlyMigratable = viewModel::toggleMigratableFilter,
    onStartMigration = viewModel::startMigration
)

// Migration Config
ModernMigrationConfigScreen(
    state = viewModel.configState,
    onToggleSource = viewModel::toggleSource,
    onReorderSources = viewModel::reorderSources,
    onUpdateFlags = viewModel::updateFlags,
    onSave = viewModel::saveConfig,
    onBack = navController::navigateUp
)

// Migration Progress
ModernMigrationProgressScreen(
    state = viewModel.progressState,
    onPause = viewModel::pauseMigration,
    onResume = viewModel::resumeMigration,
    onCancel = viewModel::cancelMigration,
    onBack = navController::navigateUp
)
```

### State Management

```kotlin
data class MigrationListState(
    val books: List<Book> = emptyList(),
    val selectedBooks: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val sortOrder: MigrationSortOrder = MigrationSortOrder.TITLE,
    val showOnlyMigratableBooks: Boolean = false,
    val isLoading: Boolean = false
)

data class MigrationConfigState(
    val migrationFlags: MigrationFlags = MigrationFlags(),
    val availableSources: List<MigrationSource> = emptyList(),
    val selectedSources: List<MigrationSource> = emptyList(),
    val isLoading: Boolean = false
)

data class MigrationProgressState(
    val totalBooks: Int = 0,
    val completedBooks: Int = 0,
    val failedBooks: Int = 0,
    val currentBook: Book? = null,
    val currentProgress: Float = 0f,
    val completedMigrations: List<MigrationResult> = emptyList(),
    val failedMigrations: List<MigrationResult> = emptyList(),
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false
)
```

## Design Principles

### 1. **Visual Feedback**
Every interaction provides immediate visual feedback through:
- Scale animations on selection
- Color transitions
- Icon changes
- Progress indicators

### 2. **Information Hierarchy**
Clear visual hierarchy using:
- Typography scale (headline → title → body → label)
- Color emphasis (primary → on-surface → on-surface-variant)
- Spacing (24dp → 16dp → 12dp → 8dp)

### 3. **Accessibility**
- Sufficient color contrast ratios
- Touch targets minimum 48dp
- Screen reader support with content descriptions
- Keyboard navigation support

### 4. **Performance**
- Lazy loading for large lists
- Animated values with proper easing
- Efficient recomposition with remember and derivedStateOf
- Key-based list items for smooth animations

## Animation Details

### Spring Animations
```kotlin
spring(stiffness = Spring.StiffnessLow)  // Smooth, natural feel
spring(stiffness = Spring.StiffnessMedium)  // Balanced response
```

### Tween Animations
```kotlin
tween(durationMillis = 500, easing = FastOutSlowInEasing)  // Progress bars
tween(durationMillis = 1000, easing = LinearEasing)  // Rotation
```

### Transition Specs
```kotlin
scaleIn() + fadeIn()  // Entry animations
scaleOut() + fadeOut()  // Exit animations
expandVertically() + fadeIn()  // Expanding content
shrinkVertically() + fadeOut()  // Collapsing content
```

## Color Scheme

The UI adapts to Material 3 dynamic color:
- **Primary Container**: Selected items, main actions
- **Secondary Container**: Current operations, secondary info
- **Surface Container High**: Elevated cards
- **Error Container**: Failed operations
- **Surface Variant**: Disabled/inactive states

## Future Enhancements

- [ ] Drag-and-drop source reordering
- [ ] Batch operations (select by source, author, etc.)
- [ ] Migration history and analytics
- [ ] Undo/redo migration actions
- [ ] Export/import migration configurations
- [ ] Advanced filtering (date ranges, chapter counts)
- [ ] Migration scheduling
- [ ] Conflict resolution UI

## Usage

The migration screens are now the default implementation. Simply use:

```kotlin
// In your navigation
MigrationListScreen()  // Main migration screen
MigrationConfigScreen(onBack = { navController.navigateUp() })  // Settings
ModernMigrationProgressScreen(...)  // Progress tracking
```

All screens are automatically connected to their ViewModels via Koin DI.

## Contributing

When adding new features:
1. Follow Material 3 design guidelines
2. Use consistent spacing (8dp grid)
3. Add smooth animations for state changes
4. Ensure accessibility compliance
5. Test on different screen sizes
6. Document new components

---

**Created**: November 2024  
**Design System**: Material 3  
**Framework**: Jetpack Compose Multiplatform
