# IReader Presentation Core

This module contains the enhanced UI components and design system for IReader, following Mihon's proven Material Design 3 patterns.

## Enhanced Components

### Error Handling
- **IReaderErrorScreen**: Comprehensive error display with random kaomoji faces and contextual action buttons
- **IReaderLoadingScreen**: Enhanced loading states with optional progress indicators and messages
- **IReaderEmptyScreen**: Consistent empty state handling with helpful messaging and actions

### Layout Components
- **IReaderScaffold**: Enhanced scaffold with proper scroll behavior and Material Design 3 theming
- **TwoPanelBox**: Responsive tablet layouts with automatic WindowSizeClass detection
- **AppBar**: Consistent app bar components with proper scroll behavior

### UI Elements
- **ActionButton**: Material Design 3 button components with icon and text
- **Pill**: Pill-shaped badges for labels and tags
- **IReaderElevatedCard**: Consistent card styling with proper elevation
- **IReaderFastScrollLazyColumn**: Optimized list handling with performance improvements

### Theme System
- **IReaderTheme**: Comprehensive Material Design 3 theme with multiple color schemes
- **Typography**: Consistent text styling following Material Design 3 guidelines
- **Shapes**: Rounded corner shapes for consistent visual hierarchy
- **Padding**: Standardized spacing values across the application

## Usage Examples

### Basic Error Screen
```kotlin
IReaderErrorScreen(
    message = "Something went wrong",
    actions = listOf(
        ErrorScreenAction(
            title = "Retry",
            icon = Icons.Default.Refresh,
            onClick = { /* retry logic */ }
        )
    )
)
```

### Enhanced Scaffold
```kotlin
IReaderScaffold(
    topBar = { scrollBehavior ->
        AppBar(
            title = { Text("My Screen") },
            scrollBehavior = scrollBehavior
        )
    }
) { paddingValues ->
    // Content
}
```

### Responsive Layout
```kotlin
TwoPanelBox(
    startContent = { /* Left panel for tablets */ },
    endContent = { /* Main content */ }
)
```

### Optimized Lists
```kotlin
IReaderFastScrollLazyColumn {
    items(
        items = books,
        key = { book -> book.id },
        contentType = { "book_item" }
    ) { book ->
        BookItem(book = book)
    }
}
```

## Design Principles

1. **Consistency**: All components follow Material Design 3 guidelines
2. **Performance**: Optimized for smooth scrolling and efficient rendering
3. **Accessibility**: Proper content descriptions and touch targets
4. **Responsiveness**: Adaptive layouts for different screen sizes
5. **Theming**: Comprehensive theme support with AMOLED options

## Migration Guide

### From Basic Components
- Replace `ErrorScreen` with `IReaderErrorScreen`
- Replace `LoadingScreen` with `IReaderLoadingScreen`
- Replace `EmptyScreen` with `IReaderEmptyScreen`
- Replace `LazyColumn` with `IReaderFastScrollLazyColumn` for better performance

### From Current Scaffold
- Use `IReaderScaffold` for enhanced Material Design 3 support
- Use `TwoPanelScaffold` for responsive tablet layouts

### Theme Updates
- Apply `IReaderTheme` at the root of your app
- Use `MaterialTheme.padding` for consistent spacing
- Use `MaterialTheme.colorScheme` for proper theming

## Performance Optimizations

1. **List Performance**: Use proper `key` and `contentType` parameters in LazyColumn items
2. **State Management**: Minimize recomposition with stable state holders
3. **Memory Efficiency**: Proper lifecycle management and resource cleanup
4. **Responsive Design**: Efficient layout switching based on screen size

## Testing

All components are designed to be testable with:
- Proper semantic properties for accessibility testing
- Stable state management for UI testing
- Clear component boundaries for unit testing

## Future Enhancements

- Fast scroll implementation for large lists
- Advanced animation support
- Enhanced accessibility features
- Performance monitoring integration