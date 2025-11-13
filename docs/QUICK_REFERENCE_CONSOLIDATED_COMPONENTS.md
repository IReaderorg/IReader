# Quick Reference: Consolidated Components

A quick reference guide for using the consolidated components in IReader.

## Domain Layer

### Mappers
```kotlin
// Define a mapper
object BookMapper : EntityMapper<Book, BookEntity> {
    override fun toDomain(data: BookEntity) = Book(...)
    override fun toData(domain: Book) = BookEntity(...)
}

// Use it
val book = BookMapper.toDomain(bookEntity)
val books = BookMapper.toDomainList(bookEntities)
```

### Validation
```kotlin
// Validate email
val result = Validators.validateEmail(email)
if (result.isInvalid) {
    showError(result.getErrors().first().message)
}

// Combine validations
val result = combineValidationResults(
    Validators.validateEmail(email),
    Validators.validatePassword(password)
)
```

### Authentication
```kotlin
// Use consolidated auth use case
val authUseCase = AuthenticationUseCase(remoteRepository)

// Sign in
val result = authUseCase.signIn(email, password)

// Sign up
val result = authUseCase.signUp(email, password)

// Sign out
authUseCase.signOut()
```

## Presentation Layer

### UI State
```kotlin
// In ViewModel
private val _state = MutableStateFlow<UiState<List<Book>>>(UiState.Idle)
val state: StateFlow<UiState<List<Book>>> = _state

// Update state
_state.value = UiState.Loading
_state.value = UiState.Success(books)
_state.value = UiState.Error("Failed")

// In Composable
when (val s = state.collectAsState().value) {
    UiState.Idle -> { }
    UiState.Loading -> LoadingScreen()
    is UiState.Success -> BookList(s.data)
    is UiState.Error -> ErrorScreen(s.message)
}
```

### ViewModel Extensions
```kotlin
// Execute async
executeAsync(
    stateFlow = _booksState,
    operation = { repository.getBooks() }
)

// With Result
executeAsyncResult(
    stateFlow = _userState,
    operation = { authUseCase.signIn(email, password) }
)
```

### Dialogs
```kotlin
// Confirmation
ConfirmationDialog(
    title = "Delete",
    message = "Are you sure?",
    onConfirm = { delete() },
    onDismiss = { close() }
)

// Info
InfoDialog(
    title = "Success",
    message = "Saved!",
    onDismiss = { close() }
)

// Error
ErrorDialog(
    message = error,
    onDismiss = { close() }
)
```

### Cards
```kotlin
// Standard
StandardCard(
    title = "Settings",
    subtitle = "Configure"
) {
    // Content
}

// With icon
IconCard(
    title = "Library",
    icon = { Icon(Icons.Default.Book, null) },
    onClick = { navigate() }
)
```

### Lists
```kotlin
// Standard item
StandardListItem(
    title = "Settings",
    subtitle = "Configure app",
    onClick = { navigate() }
)

// With switch
SwitchListItem(
    title = "Dark Mode",
    checked = isDark,
    onCheckedChange = { toggle() }
)

// Section header
ListSectionHeader(title = "General")
```

### Text Inputs
```kotlin
// Email
EmailTextField(
    value = email,
    onValueChange = { email = it },
    errorMessage = emailError
)

// Password
PasswordTextField(
    value = password,
    onValueChange = { password = it },
    passwordVisible = showPassword,
    onPasswordVisibilityToggle = { showPassword = !showPassword }
)

// Validated
ValidatedTextField(
    value = name,
    onValueChange = { name = it },
    label = "Name",
    errorMessage = if (nameError) "Required" else null
)
```

## Common Patterns

### Loading Data
```kotlin
// In ViewModel
init {
    executeAsync(
        stateFlow = _booksState,
        operation = { repository.getBooks() }
    )
}

// In Composable
when (val state = booksState.collectAsState().value) {
    UiState.Loading -> LoadingScreen()
    is UiState.Success -> BookList(state.data)
    is UiState.Error -> ErrorScreen(state.message)
    else -> { }
}
```

### Form Validation
```kotlin
// In ViewModel
fun validateForm(): Boolean {
    val emailResult = Validators.validateEmail(email)
    val passwordResult = Validators.validatePassword(password)
    
    emailError = emailResult.getErrorOrNull()
    passwordError = passwordResult.getErrorOrNull()
    
    return emailResult.isValid && passwordResult.isValid
}
```

### Confirmation Dialog
```kotlin
// In Composable
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    ConfirmationDialog(
        title = "Delete Book",
        message = "This cannot be undone",
        onConfirm = {
            viewModel.deleteBook()
            showDialog = false
        },
        onDismiss = { showDialog = false }
    )
}
```

## Migration Checklist

- [ ] Replace custom mappers with `EntityMapper`
- [ ] Move validation to `Validators`
- [ ] Use `AuthenticationUseCase` instead of separate auth use cases
- [ ] Replace custom state with `UiState`
- [ ] Use `executeAsync` for async operations
- [ ] Replace `AlertDialog` with consolidated dialogs
- [ ] Use consolidated card components
- [ ] Use consolidated list components
- [ ] Use validated text fields

## See Also

- [Full Documentation](CODE_CONSOLIDATION_GUIDE.md)
- [Consolidation Summary](../.kiro/specs/architecture-refactoring/CONSOLIDATION_SUMMARY.md)
