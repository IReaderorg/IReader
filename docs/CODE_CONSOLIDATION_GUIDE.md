# Code Consolidation Guide

This document describes the consolidated components and patterns introduced to reduce code duplication across the IReader codebase.

## Overview

As part of the architecture refactoring effort, we've created reusable components and patterns to eliminate duplication and improve maintainability. This guide explains how to use these consolidated components.

## Domain Layer

### 1. Entity Mappers

**Location:** `domain/src/commonMain/kotlin/ireader/domain/utils/mappers/EntityMapper.kt`

Base interfaces for mapping between domain and data layer entities:

```kotlin
// Bidirectional mapping
interface EntityMapper<Domain, Data> {
    fun toDomain(data: Data): Domain
    fun toData(domain: Domain): Data
}

// One-way mapping
interface DataToDomainMapper<Domain, Data> {
    fun toDomain(data: Data): Domain
}

// Usage example
class BookMapper : EntityMapper<Book, BookEntity> {
    override fun toDomain(data: BookEntity): Book = Book(...)
    override fun toData(domain: Book): BookEntity = BookEntity(...)
}

// Map lists
val books = mapper.toDomainList(bookEntities)
```

### 2. Validation Utilities

**Location:** `domain/src/commonMain/kotlin/ireader/domain/utils/validation/`

Reusable validation logic for domain models:

```kotlin
// Validate email
val emailResult = Validators.validateEmail(email)
if (emailResult.isInvalid) {
    val errors = emailResult.getErrors()
    // Handle errors
}

// Validate password
val passwordResult = Validators.validatePassword(password, minLength = 8)

// Validate URL
val urlResult = Validators.validateUrl(url)

// Combine validations
val result = combineValidationResults(
    Validators.validateEmail(email),
    Validators.validatePassword(password)
)
```

Available validators:
- `validateEmail()` - Email format validation
- `validatePassword()` - Password strength validation
- `validateUrl()` - URL format validation
- `validateNotEmpty()` - Non-empty string validation
- `validateLength()` - String length validation
- `validateRange()` - Numeric range validation
- `validatePattern()` - Regex pattern validation

### 3. Base Repository Interfaces

**Location:** `domain/src/commonMain/kotlin/ireader/domain/data/repository/BaseRepository.kt`

Common repository operations:

```kotlin
// Basic CRUD
interface BaseRepository<T, ID> {
    suspend fun findById(id: ID): T?
    suspend fun findAll(): List<T>
    suspend fun insert(entity: T): ID
    suspend fun update(entity: T)
    suspend fun delete(id: ID)
}

// With reactive queries
interface ReactiveRepository<T, ID> : BaseRepository<T, ID> {
    fun subscribeById(id: ID): Flow<T?>
    fun subscribeAll(): Flow<List<T>>
}

// With batch operations
interface BatchRepository<T, ID> : BaseRepository<T, ID> {
    suspend fun insertBatch(entities: List<T>): List<ID>
    suspend fun updateBatch(entities: List<T>)
    suspend fun deleteBatch(ids: List<ID>)
}

// Full-featured repository
interface FullRepository<T, ID> : ReactiveRepository<T, ID>, BatchRepository<T, ID>
```

### 4. Consolidated Use Cases

**Location:** `domain/src/commonMain/kotlin/ireader/domain/usecases/remote/AuthenticationUseCase.kt`

The `AuthenticationUseCase` consolidates sign in, sign up, and sign out operations:

```kotlin
class AuthenticationUseCase(
    private val remoteRepository: RemoteRepository
) {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String): Result<User>
    suspend fun signOut()
}

// Usage
val authUseCase = AuthenticationUseCase(remoteRepository)
val result = authUseCase.signIn(email, password)
```

This replaces the separate `SignInUseCase`, `SignUpUseCase`, and `SignOutUseCase` classes.

## Presentation Layer

### 1. UI State Pattern

**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/UiState.kt`

Consistent state representation across ViewModels:

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}

// Usage in ViewModel
private val _booksState = MutableStateFlow<UiState<List<Book>>>(UiState.Idle)
val booksState: StateFlow<UiState<List<Book>>> = _booksState

// Update state
_booksState.value = UiState.Loading
_booksState.value = UiState.Success(books)
_booksState.value = UiState.Error("Failed to load books")

// In Composable
when (val state = booksState.collectAsState().value) {
    is UiState.Idle -> { /* Show initial state */ }
    is UiState.Loading -> { /* Show loading */ }
    is UiState.Success -> { /* Show data: state.data */ }
    is UiState.Error -> { /* Show error: state.message */ }
}
```

### 2. ViewModel Extensions

**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/ViewModelExtensions.kt`

Common ViewModel operations:

```kotlin
// Execute async operation
executeAsync(
    stateFlow = _booksState,
    operation = { bookRepository.findAll() },
    onSuccess = { books -> /* Handle success */ },
    onError = { error -> /* Handle error */ }
)

// Execute with Result type
executeAsyncResult(
    stateFlow = _userState,
    operation = { authUseCase.signIn(email, password) }
)

// Update state
updateState { currentState ->
    currentState.copy(isLoading = false)
}

// Launch coroutine
launchInScope {
    // Async work
}
```

### 3. Dialog Components

**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/components/DialogComponents.kt`

Reusable dialog components:

```kotlin
// Confirmation dialog
ConfirmationDialog(
    title = "Delete Book",
    message = "Are you sure you want to delete this book?",
    onConfirm = { /* Delete */ },
    onDismiss = { /* Cancel */ }
)

// Info dialog
InfoDialog(
    title = "Success",
    message = "Book added to library",
    onDismiss = { /* Close */ }
)

// Error dialog
ErrorDialog(
    message = "Failed to load book",
    onDismiss = { /* Close */ }
)

// Loading dialog
LoadingDialog(message = "Loading...")

// Custom dialog
CustomDialog(
    onDismissRequest = { /* Dismiss */ },
    title = { Text("Custom Title") },
    content = { /* Custom content */ },
    confirmButton = { TextButton(onClick = {}) { Text("OK") } }
)
```

### 4. Card Components

**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/components/CardComponents.kt`

Reusable card components:

```kotlin
// Standard card
StandardCard(
    title = "Settings",
    subtitle = "Configure app settings"
) {
    // Card content
}

// Outlined card
OutlinedCard(
    title = "Information",
    onClick = { /* Handle click */ }
) {
    // Card content
}

// Icon card
IconCard(
    title = "Library",
    subtitle = "View your books",
    icon = { Icon(Icons.Default.Book, null) },
    onClick = { /* Navigate */ }
)

// Content card
ContentCard {
    // Custom content without title
}
```

### 5. List Components

**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/components/ListComponents.kt`

Reusable list item components:

```kotlin
// Standard list item
StandardListItem(
    title = "Settings",
    subtitle = "Configure app",
    onClick = { /* Navigate */ },
    leadingIcon = { Icon(Icons.Default.Settings, null) }
)

// Switch list item
SwitchListItem(
    title = "Dark Mode",
    checked = isDarkMode,
    onCheckedChange = { /* Toggle */ }
)

// Checkbox list item
CheckboxListItem(
    title = "Select All",
    checked = isAllSelected,
    onCheckedChange = { /* Toggle */ }
)

// Radio list item
RadioListItem(
    title = "Option 1",
    selected = selectedOption == 1,
    onClick = { /* Select */ }
)

// Section header
ListSectionHeader(title = "General")

// Divider
ListDivider()
```

### 6. Text Input Components

**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/components/TextInputComponents.kt`

Reusable text input components with validation:

```kotlin
// Validated text field
ValidatedTextField(
    value = name,
    onValueChange = { name = it },
    label = "Name",
    errorMessage = if (nameError) "Name is required" else null
)

// Email field
EmailTextField(
    value = email,
    onValueChange = { email = it },
    errorMessage = emailError
)

// Password field
PasswordTextField(
    value = password,
    onValueChange = { password = it },
    passwordVisible = showPassword,
    onPasswordVisibilityToggle = { showPassword = !showPassword }
)

// URL field
UrlTextField(
    value = url,
    onValueChange = { url = it }
)

// Multi-line field
MultiLineTextField(
    value = description,
    onValueChange = { description = it },
    label = "Description",
    minLines = 3,
    maxLines = 5
)
```

## Migration Guide

### Migrating Mappers

**Before:**
```kotlin
val bookMapper = { id: Long, title: String, ... ->
    Book(id, title, ...)
}
```

**After:**
```kotlin
object BookMapper : DataToDomainMapper<Book, BookEntity> {
    override fun toDomain(data: BookEntity): Book {
        return Book(data.id, data.title, ...)
    }
}
```

### Migrating Validation

**Before:**
```kotlin
// In ViewModel
fun validateEmail(email: String): Boolean {
    return email.contains("@") && email.contains(".")
}
```

**After:**
```kotlin
// In ViewModel
fun validateEmail(email: String): ValidationResult {
    return Validators.validateEmail(email)
}
```

### Migrating UI State

**Before:**
```kotlin
data class BookState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**After:**
```kotlin
private val _booksState = MutableStateFlow<UiState<List<Book>>>(UiState.Idle)
val booksState: StateFlow<UiState<List<Book>>> = _booksState
```

### Migrating Dialogs

**Before:**
```kotlin
AlertDialog(
    onDismissRequest = { showDialog = false },
    title = { Text("Confirm") },
    text = { Text("Are you sure?") },
    confirmButton = {
        TextButton(onClick = { /* Confirm */ }) {
            Text("Yes")
        }
    },
    dismissButton = {
        TextButton(onClick = { showDialog = false }) {
            Text("No")
        }
    }
)
```

**After:**
```kotlin
ConfirmationDialog(
    title = "Confirm",
    message = "Are you sure?",
    onConfirm = { /* Confirm */ },
    onDismiss = { showDialog = false }
)
```

## Benefits

1. **Reduced Code Duplication**: Common patterns are defined once and reused
2. **Consistent UI/UX**: All components follow the same design patterns
3. **Easier Maintenance**: Changes to common patterns only need to be made in one place
4. **Better Type Safety**: Strongly typed interfaces and sealed classes
5. **Improved Testability**: Smaller, focused components are easier to test
6. **Faster Development**: Developers can use pre-built components instead of writing from scratch

## Best Practices

1. **Use consolidated components** instead of creating custom implementations
2. **Extend base interfaces** when creating new repositories
3. **Use validation utilities** for all user input validation
4. **Follow the UiState pattern** for all async operations in ViewModels
5. **Leverage ViewModel extensions** to reduce boilerplate code
6. **Document deviations** if you need to create custom implementations

## Future Improvements

- Add more specialized validators for domain-specific validation
- Create additional UI component variants as needed
- Add animation utilities for consistent transitions
- Create testing utilities for consolidated components
- Add accessibility helpers for UI components
