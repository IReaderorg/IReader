# Design Document

## Overview

This design document outlines the technical approach for refactoring the IReader application to improve maintainability, reduce codebase size, enforce clean architecture principles, and fix critical bugs. The refactoring will be executed in focused phases to minimize risk while delivering incremental improvements.

### Key Objectives

1. **Fix Critical Bugs**: Address category duplication and archived books access issues
2. **Enforce Clean Architecture**: Ensure proper separation of concerns across layers
3. **Optimize Dependencies**: Remove unused dependencies and consolidate duplicates
4. **Reduce Code Duplication**: Extract common code into shared utilities
5. **Improve Code Quality**: Simplify complex components and improve testability

### Current Architecture Analysis

The IReader application follows a multi-module Kotlin Multiplatform (KMP) architecture:

**Modules:**
- `domain` - Business logic, use cases, repository interfaces
- `data` - Repository implementations, database, network
- `presentation` - UI layer with Compose, ViewModels
- `core` - Shared utilities and platform abstractions
- `source-api` - Extension API contracts
- `i18n` - Localization resources
- `android` - Android-specific entry point
- `desktop` - Desktop-specific entry point

**Current Issues Identified:**
1. **Category Priority Bug**: `ReorderCategory` use case calls `insert()` instead of `update()`, causing duplication
2. **Archived Books Access**: Library filtering logic doesn't properly handle archived books visibility
3. **Settings UI Inconsistency**: Settings screens use different UI patterns than other screens
4. **Dependency Bloat**: Multiple unused or redundant dependencies across modules
5. **Code Duplication**: Similar logic exists in multiple places (mappers, validators, UI components)

## Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (UI, ViewModels, Compose Screens)      │
└──────────────┬──────────────────────────┘
               │ depends on
               ▼
┌─────────────────────────────────────────┐
│          Domain Layer                   │
│  (Use Cases, Entities, Repository       │
│   Interfaces, Business Rules)           │
└──────────────┬──────────────────────────┘
               │ depends on
               ▼
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  (Repository Implementations,           │
│   Database, Network, Mappers)           │
└─────────────────────────────────────────┘
```

### Dependency Flow Rules

1. **Domain Layer**: No dependencies on presentation or data implementation
2. **Data Layer**: Depends only on domain interfaces
3. **Presentation Layer**: Depends on domain, not on data implementations
4. **Core Module**: Shared utilities only, no business logic

## Components and Interfaces

### Phase 1: Critical Bug Fixes

#### 1.1 Category Priority Bug Fix

**Problem**: `ReorderCategory.await()` uses `categoryRepository.insert()` which creates duplicates instead of updating existing categories.

**Solution**:
```kotlin
// Current (buggy):
categoryRepository.insert(updates)

// Fixed:
categoryRepository.updateBatch(updates)
```

**Implementation**:
- Add `updateBatch()` method to `CategoryRepository` interface
- Implement in `CategoryRepositoryImpl` using SQL UPDATE instead of INSERT
- Update `ReorderCategory` use case to call the new method

#### 1.2 Archived Books Access Fix

**Problem**: Archived books cannot be accessed after archiving due to filtering logic.

**Solution**:
- Add "Archived" smart category to library navigation
- Ensure `showArchivedBooks` preference properly filters library queries
- Add visual indicators for archived books in all views

**Implementation**:
- Update `SmartCategory` to include `ARCHIVED_ID`
- Modify `GetLibraryCategory` use case to handle archived filter
- Add archived books tab/filter in library UI

#### 1.3 Settings Screen UI Redesign

**Problem**: Settings screens don't follow the same UI patterns as other screens (inconsistent styling, layout, navigation).

**Solution**:
- Create unified settings component library
- Standardize settings screen layouts
- Apply consistent Material3 theming

**Implementation**:
- Extract common settings UI patterns into reusable components
- Update all settings screens to use new components
- Ensure consistent spacing, typography, and colors

### Phase 2: Dependency Optimization

#### 2.1 Dependency Audit

**Approach**:
1. Analyze each module's `build.gradle.kts` for unused dependencies
2. Identify duplicate dependencies across modules
3. Find opportunities to replace heavy libraries with lighter alternatives

**Key Areas**:
- **Compose**: Consolidate to Material3, remove Material2 if unused
- **Ktor**: Ensure only necessary client engines are included
- **Supabase**: Verify all supabase modules are actively used
- **Image Loading**: Coil is already used, ensure no duplicate image libraries
- **Serialization**: Consolidate to kotlinx-serialization where possible

#### 2.2 Version Catalog Consolidation

**Approach**:
- Ensure all dependencies use version catalogs
- Remove hardcoded versions from build files
- Consolidate related dependencies into bundles

### Phase 3: Clean Architecture Enforcement

#### 3.1 Layer Boundary Violations

**Violations to Fix**:
1. Domain layer importing Compose UI types
2. ViewModels directly accessing database
3. Use cases containing UI logic

**Solution Pattern**:
```kotlin
// Bad: Domain depending on UI
class UseCase {
    fun execute(): ComposeColor // ❌ Compose type in domain
}

// Good: Domain returns domain model
class UseCase {
    fun execute(): ColorModel // ✅ Domain model
}
```

#### 3.2 Repository Pattern Consistency

**Current State**: Some data access bypasses repositories

**Solution**:
- Ensure all database queries go through repositories
- Ensure all network calls go through repositories
- Repository interfaces in domain, implementations in data

### Phase 4: Code Duplication Elimination

#### 4.1 Mapper Consolidation

**Current Issue**: Similar mapping logic exists in multiple files

**Solution**:
- Create base mapper interfaces
- Extract common mapping patterns
- Use extension functions for simple mappings

**Example**:
```kotlin
// Base mapper interface
interface EntityMapper<Domain, Data> {
    fun toDomain(data: Data): Domain
    fun toData(domain: Domain): Data
}

// Reusable for all entities
```

#### 4.2 UI Component Consolidation

**Current Issue**: Duplicate dialog, card, and list components

**Solution**:
- Create component library in presentation/core/ui
- Extract common patterns (dialogs, cards, lists, buttons)
- Ensure all screens use shared components

#### 4.3 Validation Logic

**Current Issue**: Validation logic scattered across ViewModels

**Solution**:
- Create validation utilities in domain layer
- Use sealed classes for validation results
- Reuse validators across features

### Phase 5: Use Case Simplification

#### 5.1 Use Case Consolidation

**Pattern**: Combine similar use cases with parameters

**Example**:
```kotlin
// Before: Multiple use cases
class GetBookById
class GetBookByTitle
class GetBookByAuthor

// After: Single parameterized use case
class GetBook(val query: BookQuery)
sealed class BookQuery {
    data class ById(val id: Long) : BookQuery()
    data class ByTitle(val title: String) : BookQuery()
    data class ByAuthor(val author: String) : BookQuery()
}
```

#### 5.2 Use Case Organization

**Structure**:
```
domain/usecases/
├── book/
│   ├── GetBook.kt
│   ├── UpdateBook.kt
│   ├── DeleteBook.kt
│   └── BookUseCases.kt (facade)
├── category/
│   ├── GetCategories.kt
│   ├── CreateCategory.kt
│   ├── UpdateCategory.kt
│   ├── DeleteCategory.kt
│   ├── ReorderCategory.kt
│   └── CategoryUseCases.kt (facade)
```

### Phase 6: ViewModel Simplification

#### 6.1 ViewModel Size Reduction

**Approach**:
- Extract business logic to use cases
- Move UI state management to separate state holders
- Use delegation for common ViewModel functionality

**Pattern**:
```kotlin
// Before: Large ViewModel (300+ lines)
class LibraryViewModel : ViewModel() {
    // All logic here
}

// After: Focused ViewModel with delegation
class LibraryViewModel(
    private val getBooks: GetBooks,
    private val filterBooks: FilterBooks,
    private val sortBooks: SortBooks
) : ViewModel() {
    // Only UI state and coordination
}
```

#### 6.2 State Management Pattern

**Approach**:
- Use sealed classes for UI state
- Separate loading, success, error states
- Immutable state objects

**Pattern**:
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## Data Models

### Category Models

```kotlin
// Domain model
data class Category(
    val id: Long,
    val name: String,
    val order: Long,
    val isSystemCategory: Boolean
)

// Data model (database)
data class CategoryEntity(
    val id: Long,
    val name: String,
    val order: Long,
    val flags: Long
)

// UI model
data class CategoryUiModel(
    val id: Long,
    val name: String,
    val bookCount: Int,
    val isEditable: Boolean
)
```

### Book Models

```kotlin
// Domain model
data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val isArchived: Boolean,
    val isFavorite: Boolean,
    // ... other fields
)

// Update model (for partial updates)
data class BookUpdate(
    val id: Long,
    val isArchived: Boolean? = null,
    val isFavorite: Boolean? = null,
    // ... other optional fields
)
```

### Repository Interfaces

```kotlin
// Domain layer
interface CategoryRepository {
    suspend fun findAll(): List<CategoryWithCount>
    suspend fun findById(id: Long): Category?
    suspend fun insert(category: Category): Long
    suspend fun update(category: Category)
    suspend fun updateBatch(categories: List<Category>) // New method
    suspend fun delete(categoryId: Long)
    fun subscribeAll(): Flow<List<CategoryWithCount>>
}

interface BookRepository {
    suspend fun findAll(
        categoryId: Long? = null,
        showArchived: Boolean = false
    ): List<Book>
    suspend fun update(book: Book)
    suspend fun updatePartial(update: BookUpdate) // For efficient updates
    fun subscribe(
        categoryId: Long? = null,
        showArchived: Boolean = false
    ): Flow<List<Book>>
}
```

## Error Handling

### Error Types

```kotlin
sealed class DomainError {
    data class ValidationError(val field: String, val message: String) : DomainError()
    data class NotFoundError(val entityType: String, val id: Long) : DomainError()
    data class DuplicateError(val entityType: String, val field: String) : DomainError()
    data class DatabaseError(val cause: Throwable) : DomainError()
    data class NetworkError(val cause: Throwable) : DomainError()
}

// Result wrapper
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: DomainError) : Result<Nothing>()
}
```

### Error Handling Pattern

```kotlin
// Use case
class CreateCategory(private val repository: CategoryRepository) {
    suspend fun execute(name: String): Result<Category> {
        // Validation
        if (name.isBlank()) {
            return Result.Failure(
                DomainError.ValidationError("name", "Name cannot be empty")
            )
        }
        
        // Check for duplicates
        val existing = repository.findByName(name)
        if (existing != null) {
            return Result.Failure(
                DomainError.DuplicateError("Category", "name")
            )
        }
        
        // Execute
        return try {
            val category = Category(name = name, order = 0)
            val id = repository.insert(category)
            Result.Success(category.copy(id = id))
        } catch (e: Exception) {
            Result.Failure(DomainError.DatabaseError(e))
        }
    }
}

// ViewModel
class CategoryViewModel(private val createCategory: CreateCategory) {
    fun onCreateCategory(name: String) {
        viewModelScope.launch {
            when (val result = createCategory.execute(name)) {
                is Result.Success -> {
                    // Update UI with success
                }
                is Result.Failure -> {
                    // Show error message
                    when (result.error) {
                        is DomainError.ValidationError -> {
                            // Show validation error
                        }
                        is DomainError.DuplicateError -> {
                            // Show duplicate error
                        }
                        else -> {
                            // Show generic error
                        }
                    }
                }
            }
        }
    }
}
```

## Testing Strategy

### Unit Testing

**Domain Layer**:
- Test all use cases with mock repositories
- Test business logic validation
- Test error handling

**Data Layer**:
- Test repository implementations with in-memory database
- Test mappers
- Test network error handling

**Presentation Layer**:
- Test ViewModel state management
- Test UI state transformations
- Test user interaction handling

### Integration Testing

- Test complete flows (e.g., create category → reorder → delete)
- Test database migrations
- Test repository + use case integration

### UI Testing

- Test critical user flows
- Test error states
- Test loading states

## Implementation Phases

### Phase 1: Critical Bug Fixes (Priority: High)
**Estimated Effort**: 1-2 days
- Fix category duplication bug
- Fix archived books access
- Redesign settings UI for consistency

### Phase 2: Dependency Optimization (Priority: Medium)
**Estimated Effort**: 2-3 days
- Audit and remove unused dependencies
- Consolidate duplicate dependencies
- Update version catalogs

### Phase 3: Clean Architecture Enforcement (Priority: High)
**Estimated Effort**: 3-4 days
- Fix layer boundary violations
- Ensure repository pattern consistency
- Move business logic to domain layer

### Phase 4: Code Duplication Elimination (Priority: Medium)
**Estimated Effort**: 2-3 days
- Consolidate mappers
- Extract common UI components
- Create shared validation utilities

### Phase 5: Use Case Simplification (Priority: Low)
**Estimated Effort**: 2-3 days
- Consolidate similar use cases
- Improve use case organization
- Add use case facades

### Phase 6: ViewModel Simplification (Priority: Medium)
**Estimated Effort**: 3-4 days
- Extract business logic from ViewModels
- Implement consistent state management
- Reduce ViewModel complexity

## Migration Strategy

### Backward Compatibility

- All changes must maintain existing functionality
- Database migrations must be backward compatible
- API changes must not break existing code

### Rollout Plan

1. **Phase 1**: Deploy bug fixes immediately
2. **Phase 2-3**: Deploy architecture improvements incrementally
3. **Phase 4-6**: Deploy optimizations and refactoring in batches

### Rollback Plan

- Each phase is independent and can be rolled back
- Database migrations include rollback scripts
- Feature flags for major changes

## Performance Considerations

### Database Optimization

- Use batch operations for multiple updates
- Add indexes for frequently queried fields
- Optimize complex queries

### UI Performance

- Use LazyColumn for large lists
- Implement proper key management for recomposition
- Use remember and derivedStateOf appropriately

### Memory Management

- Avoid memory leaks in ViewModels
- Properly cancel coroutines
- Use weak references where appropriate

## Security Considerations

### Data Validation

- Validate all user input
- Sanitize data before database operations
- Prevent SQL injection with parameterized queries

### Access Control

- Ensure proper permission checks
- Validate user actions
- Implement rate limiting where appropriate

## Documentation

### Code Documentation

- KDoc for all public APIs
- Inline comments for complex logic
- Architecture decision records (ADRs)

### Developer Guide

- Module responsibilities
- Dependency flow
- Common patterns and practices
- How to add new features

### API Documentation

- Repository interfaces
- Use case contracts
- Data models

## Monitoring and Metrics

### Success Metrics

- **Build Time**: Reduce by 20%
- **APK Size**: Reduce by 15%
- **Code Coverage**: Increase to 70%
- **Bug Count**: Reduce critical bugs to zero
- **Code Duplication**: Reduce by 50%

### Monitoring

- Track build times
- Monitor crash rates
- Track performance metrics
- Monitor dependency updates
