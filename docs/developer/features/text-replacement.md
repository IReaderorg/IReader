# Text Replacement Feature - Developer Documentation

## Architecture Overview

The Text Replacement feature follows a clean architecture pattern with clear separation of concerns:

```
Presentation Layer (UI)
    ↓
ViewModel Layer (State Management)
    ↓
Domain Layer (Use Cases)
    ↓
Data Layer (Repository)
    ↓
Database (SQLDelight)
```

## Components

### 1. Presentation Layer

#### TextReplacementScreen.kt
- **Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/textreplacement/TextReplacementScreen.kt`
- **Purpose**: Main UI screen for managing text replacement rules
- **Key Composables**:
  - `TextReplacementScreen`: Main screen with list of replacements
  - `TextReplacementDialog`: Add/Edit dialog with real-time validation
  - `TextReplacementItem`: Individual replacement rule card
  - `ImportDialog`: Import replacements from JSON
  - `ExportDialog`: Export replacements to JSON

#### TextReplacementViewModel.kt
- **Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/textreplacement/TextReplacementViewModel.kt`
- **Purpose**: State management and business logic for the UI
- **Key Methods**:
  - `addReplacement()`: Add new replacement with validation
  - `updateReplacement()`: Update existing replacement
  - `deleteReplacement()`: Delete a replacement
  - `toggleReplacement()`: Enable/disable a replacement
  - `validateRegexPattern()`: Validate regex patterns with user-friendly errors
  - `isRegexPattern()`: Check if pattern contains regex metacharacters
  - `testReplacement()`: Test replacement against sample text
  - `exportToJson()`: Export all replacements
  - `importFromJson()`: Import replacements from JSON

### 2. Domain Layer

#### TextReplacementUseCase.kt
- **Location**: `domain/src/commonMain/kotlin/ireader/domain/usecases/reader/TextReplacementUseCase.kt`
- **Purpose**: Core business logic for applying text replacements
- **Key Methods**:
  - `applyReplacementsToPages()`: Apply to Reader pages
  - `applyReplacementsToStrings()`: Apply to TTS strings
  - `applyReplacementsToText()`: Apply to single string
  - `testReplacement()`: Test replacement without saving
- **Helper Methods**:
  - `isRegexPattern()`: Detect if pattern is regex (private)
  - `applyReplacements()`: Core replacement logic with error handling

#### TextReplacement Entity
- **Location**: `domain/src/commonMain/kotlin/ireader/domain/models/entities/TextReplacement.kt`
- **Fields**:
  - `id`: Unique identifier (negative for defaults)
  - `bookId`: Book-specific or null for global
  - `name`: Descriptive name
  - `findText`: Pattern to find
  - `replaceText`: Replacement text
  - `description`: Optional description
  - `enabled`: Whether rule is active
  - `caseSensitive`: Case sensitivity flag
  - `createdAt`: Creation timestamp
  - `updatedAt`: Last update timestamp

### 3. Data Layer

#### TextReplacementRepository
- **Location**: `domain/src/commonMain/kotlin/ireader/domain/data/repository/TextReplacementRepository.kt`
- **Purpose**: Repository interface for data operations

#### TextReplacementRepositoryImpl
- **Location**: `data/src/commonMain/kotlin/ireader/data/textreplacement/TextReplacementRepositoryImpl.kt`
- **Purpose**: SQLDelight implementation of repository

## Regex Validation

### Validation Flow

1. **Input**: User types pattern in dialog
2. **Real-time Validation**: `LaunchedEffect` triggers validation on each change
3. **Detection**: Check if pattern contains regex metacharacters
4. **Compilation Test**: Try to compile pattern as regex
5. **Error Handling**: Provide user-friendly error messages

### Regex Metacharacters

```kotlin
private val REGEX_META_CHARS = setOf('.', '*', '+', '?', '^', '$', '{', '}', '(', ')', '|', '[', ']', '\\')
```

### Error Messages

The validation provides specific error messages for common issues:

| Error Type | Message |
|------------|---------|
| Unclosed bracket | "Invalid pattern: Unclosed bracket '[' - make sure to close all brackets" |
| Unclosed parenthesis | "Invalid pattern: Unclosed parenthesis '(' - make sure to close all groups" |
| Unclosed brace | "Invalid pattern: Unclosed brace '{' - use \\{ for literal braces or close with '}'" |
| Unmatched closing brace | "Invalid pattern: Unmatched closing brace '}' - use \\} for literal braces" |
| Generic error | "Invalid pattern: [original error message]" |

## Error Handling Strategy

### 1. UI Layer (Dialog)
- Real-time validation feedback
- Inline error messages
- Disabled save button when invalid
- Help card for regex patterns

### 2. ViewModel Layer
- Input validation before use case call
- State-based error handling
- User-friendly error messages

### 3. Use Case Layer
- Try-catch around regex compilation
- Fallback to literal replacement on regex error
- Logging for debugging

### 4. Repository Layer
- Database error handling
- Transaction support for batch operations

## State Management

### TextReplacementState

```kotlin
sealed interface TextReplacementState {
    data object Loading : TextReplacementState
    data class Success(val replacements: List<TextReplacement>) : TextReplacementState
    data class Error(val message: String) : TextReplacementState
}
```

### State Transitions

```
Loading → Success (data loaded)
Loading → Error (load failed)
Success → Loading (add/update/delete)
Loading → Error (operation failed)
Error → Loading (retry)
```

## Default Replacements

Default replacements are initialized with negative IDs:

```kotlin
val defaults = listOf(
    Triple("Navigation Hint 1", "Use arrow keys.*chapter", ""),
    Triple("Navigation Hint 2", "(?:A|D|←|→).*(?:PREV|NEXT).*chapter", ""),
    Triple("Navigation Hint 3", "(?:Previous|Next).*Chapter.*(?:←|→|A|D)", ""),
    Triple("Promotion 1", "Read more at.*", ""),
    Triple("Promotion 2", "Visit.*for more chapters", "")
)
```

## Import/Export Format

### JSON Structure

```json
[
  {
    "id": 1,
    "bookId": null,
    "name": "Fix Khan",
    "findText": "khan",
    "replaceText": "khaaan",
    "description": "Fix common typo",
    "enabled": true,
    "caseSensitive": false,
    "createdAt": 1234567890,
    "updatedAt": 1234567890
  }
]
```

### Import Rules
- Skip default replacements (negative IDs)
- Validate each replacement before adding
- Return count of successfully imported replacements

## Performance Considerations

1. **Regex Compilation**: Patterns are compiled once and reused
2. **Batch Processing**: Multiple replacements applied in single pass
3. **Lazy Loading**: Replacements loaded on demand
4. **Flow-based Updates**: Real-time updates via Kotlin Flow

## Testing

### Unit Tests
- `TextReplacementUseCaseTest`: Tests for use case logic
- Pattern detection tests
- Regex vs literal replacement tests
- Error handling tests

### Integration Tests
- Repository tests with in-memory database
- End-to-end UI tests

## Common Issues and Solutions

### Issue: Invalid regex pattern error
**Cause**: User enters pattern with unmatched special characters
**Solution**: Real-time validation with helpful error messages

### Issue: Pattern not matching
**Cause**: Case sensitivity or incorrect regex syntax
**Solution**: Clear UI indicators and help text

### Issue: Unexpected replacements
**Cause**: Too broad regex pattern
**Solution**: Encourage specific patterns and provide examples

## Future Enhancements

1. **Pattern Testing**: Built-in preview with sample text
2. **Pattern Library**: Pre-built common patterns
3. **Priority Ordering**: Drag-and-drop to reorder rules
4. **Book-specific Rules**: Per-book replacement rules
5. **Import from File**: File picker for JSON import
6. **Export to File**: Save JSON to file system
7. **Pattern Groups**: Organize rules into categories
8. **Statistics**: Show how many times each rule matched

## Code Cleanup Notes

### Completed
- ✅ Extracted `validateRegexPattern()` to ViewModel
- ✅ Added `isRegexPattern()` helper method
- ✅ Real-time validation in dialog
- ✅ Inline error display
- ✅ User-friendly error messages
- ✅ Help card for regex patterns
- ✅ Disabled save button when invalid

### Pending
- ☐ Add pattern testing/preview feature
- ☐ Add pattern library
- ☐ Improve performance for large rule sets
- ☐ Add more comprehensive unit tests

## Related Files

- `TextReplacementScreen.kt`: UI components
- `TextReplacementViewModel.kt`: State management
- `TextReplacementUseCase.kt`: Business logic
- `TextReplacementRepository.kt`: Repository interface
- `TextReplacementRepositoryImpl.kt`: Repository implementation
- `TextReplacement.kt`: Data model
- `TextReplacementState.kt`: State definitions (in ViewModel file)
