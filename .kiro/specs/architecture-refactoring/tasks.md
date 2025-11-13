# Implementation Plan

- [x] 1. Fix category priority duplication bug





  - Add `updateBatch()` method to `CategoryRepository` interface in domain layer
  - Implement `updateBatch()` in `CategoryRepositoryImpl` using SQL UPDATE statements
  - Update `ReorderCategory` use case to call `updateBatch()` instead of `insert()`
  - Verify the fix by testing category reordering in the UI
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_
-

- [x] 2. Fix archived books access and visibility




  - Add `ARCHIVED_ID` constant to `SmartCategory` object
  - Update `GetLibraryCategory` use case to properly handle `showArchivedBooks` parameter
  - Ensure archived books are filtered correctly in library queries
  - Add visual "Archived" indicator in book list items and book detail screen
  - Test that archived books can be viewed when filter is enabled
  - _Requirements: 2.1, 2.2, 2.3, 2.6, 6.1, 6.2_
-

- [x] 3. Redesign settings screens for UI consistency




  - Create unified settings component library with consistent Material3 styling
  - Extract common settings patterns (preference rows, switches, dialogs) into reusable components
  - Update main settings screen (`MoreScreen.kt`) to use new components
  - Update category settings screen to match new design patterns
  - Ensure consistent spacing, typography, colors, and navigation across all settings screens
  - _Requirements: 2.1, 2.3, 2.4, 7.1, 7.2, 7.3, 7.4_

- [x] 4. Audit and optimize module dependencies




  - Analyze each module's `build.gradle.kts` to identify unused dependencies
  - Remove unused dependencies from domain, data, presentation, and core modules
  - Consolidate duplicate dependencies (ensure Compose Material3 is primary, remove Material2 if unused)
  - Verify all Ktor, Supabase, and image loading dependencies are actively used
  - Update version catalogs to ensure consistency
  - Test that all functionality works after dependency cleanup
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3_


- [x] 5. Enforce clean architecture layer boundaries




  - Audit domain layer for any Compose UI type imports and remove them
  - Ensure all database access goes through repository interfaces
  - Move any business logic from ViewModels to use cases
  - Verify that presentation layer doesn't directly access data layer implementations
  - Ensure repository interfaces are in domain, implementations in data
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 6.1, 6.2, 6.3, 6.4, 6.5_


- [x] 6. Consolidate code duplication across the codebase






  - Create base mapper interfaces and extract common mapping patterns
  - Consolidate duplicate UI components (dialogs, cards, lists) into shared component library
  - Extract validation logic from ViewModels into reusable domain validators
  - Identify and merge similar use cases using parameterized patterns
  - Remove duplicate data models across modules

  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 5.1, 5.2, 5.3, 5.4, 5.5_
-

- [x] 7. Simplify and refactor large ViewModels





  - Identify ViewModels exceeding 300 lines and extract business logic to use cases
  - Implement consistent state management pattern using sealed classes
  - Move data access logic from ViewModels to repositories
  - Extract common ViewModel functionality into base classes or utilities

  - Ensure ViewModels only handle UI state and coordination
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 8. Optimize build configuration and documentation




- [ ] 8. Optimize build configuration and documentation

  - Remove duplicate dependency declarations across build files
  - Consolidate common build configuration into buildSrc or convention plugins
  - Ensure all dependencies use version catalogs consistently
  - Document module responsibilities and dependency flow
  - Add KDoc comments to all public APIs in domain layer
  - Create developer guide explaining clean architecture implementation
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 10.4, 10.5_
