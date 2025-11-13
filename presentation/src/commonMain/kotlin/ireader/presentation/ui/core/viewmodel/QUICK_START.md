# ViewModel Refactoring - Quick Start Guide

## TL;DR

This guide shows you how to quickly refactor a large ViewModel using the established patterns.

## 5-Minute Refactoring Checklist

### Step 1: Create Use Case (Domain Layer)
```kotlin
// domain/src/.../usecases/YourUseCase.kt
class YourUseCase(private val repository: YourRepository) {
    suspend fun execute(params: Params): Result<Data> {
        // Move business logic here
        return repository.getData(params)
    }
}
```

### Step 2: Create State Class (Presentation Layer)
```kotlin
// presentation/.../viewmodel/YourUiState.kt
sealed class YourUiState {
    object Idle : YourUiState()
    object Loading : YourUiState()
    data class Success(val data: Data) : YourUiState()
    data class Error(val message: UiText) : YourUiState()
}
```

### Step 3: Create State Manager (Optional, for complex state)
```kotlin
// presentation/.../viewmodel/YourStateManager.kt
class YourStateManager(
    private val scope: CoroutineScope,
    private val useCase: YourUseCase
) {
    var state by mutableStateOf<YourUiState>(YourUiState.Idle)
        private set
    
    fun loadData(params: Params) {
        scope.launch {
            state = YourUiState.Loading
            val result = useCase.execute(params)
            state = when {
                result.isSuccess -> YourUiState.Success(result.getOrNull()!!)
                else -> YourUiState.Error(UiText.ExceptionString(result.exceptionOrNull()!!))
            }
        }
    }
}
```

### Step 4: Refactor ViewModel
```kotlin
// presentation/.../viewmodel/YourViewModel.kt
class YourViewModel(
    private val useCase: YourUseCase
) : BaseViewModel() {
    
    private val stateManager = YourStateManager(scope, useCase)
    val uiState = stateManager.state
    
    fun onUserAction(params: Params) {
        stateManager.loadData(params)
    }
}
```

### Step 5: Update UI
```kotlin
@Composable
fun YourScreen(viewModel: YourViewModel) {
    val uiState by viewModel.uiState
    
    when (uiState) {
        is YourUiState.Idle -> { /* Initial state */ }
        is YourUiState.Loading -> CircularProgressIndicator()
        is YourUiState.Success -> SuccessContent((uiState as YourUiState.Success).data)
        is YourUiState.Error -> ErrorContent((uiState as YourUiState.Error).message)
    }
}
```

## Common Patterns

### Pattern 1: Simple State Management
Use when ViewModel is straightforward:
```kotlin
class SimpleViewModel(private val useCase: UseCase) : BaseViewModel() {
    private val _state = MutableStateFlow<UiState<Data>>(UiState.Idle)
    val state = _state.asStateFlow()
    
    fun load() {
        scope.launch {
            _state.value = UiState.Loading
            try {
                val data = useCase.execute()
                _state.value = UiState.Success(data)
            } catch (e: Exception) {
                _state.value = UiState.Error(UiText.ExceptionString(e))
            }
        }
    }
}
```

### Pattern 2: State Manager
Use when state logic is complex:
```kotlin
class ComplexViewModel(
    private val useCase: UseCase,
    private val stateManager: StateManager
) : BaseViewModel() {
    val state = stateManager.state
    fun action() = stateManager.performAction()
}
```

### Pattern 3: Multiple States
Use when tracking multiple operations:
```kotlin
class MultiStateViewModel(...) : BaseViewModel() {
    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState = _loadState.asStateFlow()
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState = _saveState.asStateFlow()
    
    // Separate functions for each operation
}
```

## Quick Reference

### Use Cases
- ✅ Put in `domain/src/.../usecases/`
- ✅ No UI dependencies
- ✅ Return `Result<T>` or domain models
- ✅ Single responsibility

### State Classes
- ✅ Put in `presentation/.../viewmodel/`
- ✅ Use sealed classes
- ✅ Include all possible states
- ✅ Type-safe

### State Managers
- ✅ Put in `presentation/.../viewmodel/`
- ✅ Manage state transitions
- ✅ Delegate to use cases
- ✅ Optional for simple cases

### ViewModels
- ✅ Coordination only
- ✅ Delegate to use cases
- ✅ Expose state
- ✅ Handle user actions

## Examples in Codebase

### Reader Screen
- Use Case: `ManageReaderUiStateUseCase`
- State: `ReaderUiState`
- State Manager: `ReaderStateManager`
- ViewModel: `ReaderScreenViewModel`

### Library Screen
- Use Case: `ManageLibraryFiltersUseCase`
- State Manager: `LibraryStateManager`
- ViewModel: `LibraryViewModel`

## Common Mistakes

❌ **Don't** put business logic in ViewModel
✅ **Do** put it in use cases

❌ **Don't** access repositories directly from ViewModel
✅ **Do** use use cases

❌ **Don't** use multiple state types inconsistently
✅ **Do** use sealed classes consistently

❌ **Don't** make ViewModels too large
✅ **Do** extract to state managers

## Need Help?

1. Check `VIEWMODEL_REFACTORING_GUIDE.md` for detailed guide
2. Check `REFACTORING_SUMMARY.md` for examples
3. Look at existing refactored ViewModels
4. Ask team for code review

## Checklist

Before submitting PR:
- [ ] Business logic moved to use cases
- [ ] State management uses sealed classes
- [ ] ViewModel is under 300 lines
- [ ] All components have tests
- [ ] Documentation updated
- [ ] No compilation errors
- [ ] Code follows patterns

---

*For detailed information, see VIEWMODEL_REFACTORING_GUIDE.md*
