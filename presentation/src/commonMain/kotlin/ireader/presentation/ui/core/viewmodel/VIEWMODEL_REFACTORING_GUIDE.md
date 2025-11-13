# ViewModel Refactoring Guide

## Overview

This guide explains the refactoring pattern applied to simplify and improve ViewModels in the IReader application. The refactoring follows clean architecture principles and separates concerns into distinct layers.

## Problems Addressed

1. **Large ViewModels** - ViewModels exceeding 300 lines with too many responsibilities
2. **Mixed Concerns** - UI state, business logic, and data access mixed together
3. **Inconsistent State Management** - Mix of mutableStateOf, StateFlow, and other patterns
4. **Direct Data Access** - ViewModels directly accessing repositories instead of use cases
5. **Difficult Testing** - Large ViewModels with many dependencies are hard to test

## Refactoring Pattern

### 1. Sealed UI State Classes

Create sealed classes to represent different states of the UI:

```kotlin
sealed class FeatureUiState {
    object Idle : FeatureUiState()
    object Loading : FeatureUiState()
    data class Success(val data: YourData) : FeatureUiState()
    data class Error(val message: UiText) : FeatureUiState()
}
```

**Benefits:**
- Clear state representation
- Type-safe state handling
- Easy to test
- Prevents invalid states

### 2. Extract Business Logic to Use Cases

Move business logic from ViewModels to domain layer use cases:

```kotlin
// Before (in ViewModel):
fun toggleFilter(type: FilterType) {
    val newFilters = filters.value.map { ... }
    filters.value = newFilters
}

// After (in Use Case):
class ManageFiltersUseCase {
    fun toggleFilter(currentFilters: List<Filter>, type: FilterType): List<Filter> {
        return currentFilters.map { ... }
    }
}

// In ViewModel:
fun toggleFilter(type: FilterType) {
    filters.value = manageFiltersUseCase.toggleFilter(filters.value, type)
}
```

**Benefits:**
- Business logic is reusable
- Easy to unit test
- ViewModel focuses on coordination
- Follows Single Responsibility Principle

### 3. Create State Managers

Extract state management logic into dedicated state manager classes:

```kotlin
class FeatureStateManager(
    private val scope: CoroutineScope,
    private val useCase: YourUseCase
) {
    var isLoading by mutableStateOf(false)
        private set
    
    fun performAction() {
        scope.launch {
            isLoading = true
            try {
                useCase.execute()
            } finally {
                isLoading = false
            }
        }
    }
}
```

**Benefits:**
- Separates state management from ViewModel
- Easier to test state transitions
- Reduces ViewModel size
- Reusable across similar features

### 4. Use ViewModel Delegate

Use the ViewModelDelegate for common operations:

```kotlin
class YourViewModel(...) : BaseViewModel() {
    private val delegate = ViewModelDelegate(scope, ::showSnackBar)
    
    fun performAction() {
        delegate.launchWithErrorHandling(key = "action") {
            // Your code here
            // Errors are automatically handled
        }
    }
}
```

**Benefits:**
- Automatic error handling
- Job management
- Reduces boilerplate
- Consistent error reporting

### 5. Consistent State Flow Pattern

Use StateFlow for reactive state:

```kotlin
private val _uiState = MutableStateFlow<UiState<Data>>(UiState.Idle)
val uiState: StateFlow<UiState<Data>> = _uiState.asStateFlow()

fun loadData() {
    scope.launch {
        _uiState.value = UiState.Loading
        try {
            val data = useCase.execute()
            _uiState.value = UiState.Success(data)
        } catch (e: Exception) {
            _uiState.value = UiState.Error(UiText.ExceptionString(e))
        }
    }
}
```

## Step-by-Step Refactoring Process

### Step 1: Identify Responsibilities

Analyze the ViewModel and identify distinct responsibilities:
- UI state management
- Business logic
- Data access
- Navigation
- User interactions

### Step 2: Create Use Cases

For each business logic responsibility, create a use case in the domain layer:

```kotlin
// domain/src/commonMain/kotlin/ireader/domain/usecases/feature/YourUseCase.kt
class YourUseCase(
    private val repository: YourRepository
) {
    suspend fun execute(params: Params): Result<Data> {
        // Business logic here
    }
}
```

### Step 3: Create State Classes

Create sealed classes for different states:

```kotlin
// presentation/.../viewmodel/YourUiState.kt
sealed class YourUiState {
    object Idle : YourUiState()
    object Loading : YourUiState()
    data class Success(val data: Data) : YourUiState()
    data class Error(val message: UiText) : YourUiState()
}
```

### Step 4: Create State Manager (Optional)

If the ViewModel is still large, create a state manager:

```kotlin
// presentation/.../viewmodel/YourStateManager.kt
class YourStateManager(
    private val scope: CoroutineScope,
    private val useCase: YourUseCase
) {
    var state by mutableStateOf<YourUiState>(YourUiState.Idle)
        private set
    
    fun loadData() {
        scope.launch {
            state = YourUiState.Loading
            val result = useCase.execute()
            state = when {
                result.isSuccess -> YourUiState.Success(result.getOrNull()!!)
                else -> YourUiState.Error(UiText.ExceptionString(result.exceptionOrNull()!!))
            }
        }
    }
}
```

### Step 5: Refactor ViewModel

Update the ViewModel to use the new components:

```kotlin
class YourViewModel(
    private val useCase: YourUseCase,
    private val stateManager: YourStateManager
) : BaseViewModel() {
    
    val uiState = stateManager.state
    
    fun onUserAction() {
        stateManager.loadData()
    }
}
```

### Step 6: Update UI

Update the UI to use the new state:

```kotlin
@Composable
fun YourScreen(viewModel: YourViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is YourUiState.Idle -> { /* Show idle state */ }
        is YourUiState.Loading -> { /* Show loading */ }
        is YourUiState.Success -> { /* Show data */ }
        is YourUiState.Error -> { /* Show error */ }
    }
}
```

## Examples

### Example 1: ReaderScreenViewModel

**Before:** 1641 lines with mixed responsibilities

**After:** 
- `ReaderStateManager` - Manages UI state (100 lines)
- `ManageReaderUiStateUseCase` - Reader UI logic (80 lines)
- `ManageChapterNavigationUseCase` - Navigation logic (70 lines)
- `ReaderScreenViewModel` - Coordination only (300 lines)

### Example 2: LibraryViewModel

**Before:** 594 lines with filter/sort logic mixed in

**After:**
- `LibraryStateManager` - Manages filter/sort state (100 lines)
- `ManageLibraryFiltersUseCase` - Filter/sort logic (90 lines)
- `LibraryViewModel` - Coordination only (250 lines)

## Testing Strategy

### Testing Use Cases

```kotlin
class YourUseCaseTest {
    @Test
    fun `test business logic`() {
        val useCase = YourUseCase(mockRepository)
        val result = runBlocking { useCase.execute(params) }
        assertEquals(expected, result)
    }
}
```

### Testing State Managers

```kotlin
class YourStateManagerTest {
    @Test
    fun `test state transitions`() {
        val stateManager = YourStateManager(testScope, mockUseCase)
        stateManager.loadData()
        assertEquals(YourUiState.Loading, stateManager.state)
    }
}
```

### Testing ViewModels

```kotlin
class YourViewModelTest {
    @Test
    fun `test coordination`() {
        val viewModel = YourViewModel(mockUseCase, mockStateManager)
        viewModel.onUserAction()
        verify(mockStateManager).loadData()
    }
}
```

## Best Practices

1. **Keep ViewModels Small** - Aim for under 300 lines
2. **Single Responsibility** - Each class should have one clear purpose
3. **Use Sealed Classes** - For state management
4. **Extract Business Logic** - Move to use cases
5. **Consistent Patterns** - Follow the same pattern across all ViewModels
6. **Test Everything** - Use cases, state managers, and ViewModels
7. **Document Decisions** - Explain why you made certain choices

## Migration Checklist

- [ ] Identify ViewModel responsibilities
- [ ] Create use cases for business logic
- [ ] Create sealed state classes
- [ ] Create state manager (if needed)
- [ ] Refactor ViewModel to use new components
- [ ] Update UI to use new state
- [ ] Write tests for all components
- [ ] Update documentation

## Common Pitfalls

1. **Over-engineering** - Don't create too many layers for simple features
2. **Incomplete Migration** - Ensure all business logic is moved to use cases
3. **Inconsistent Patterns** - Use the same pattern across all ViewModels
4. **Missing Tests** - Always write tests for new components
5. **Breaking Changes** - Ensure UI still works after refactoring

## Resources

- `UiState.kt` - Generic UI state classes
- `ViewModelDelegate.kt` - Common ViewModel operations
- `BaseViewModel.kt` - Base ViewModel class
- `ReaderStateManager.kt` - Example state manager
- `ManageReaderUiStateUseCase.kt` - Example use case
