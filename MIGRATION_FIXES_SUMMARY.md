# Migration Screen Fixes Summary

## Files Fixed

### 1. presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreenModel.kt

#### Issues Fixed:
- ✅ Changed `subscribeBooksByFavorite(true)` to `getFavoritesAsFlow()` - correct repository method
- ✅ All type mismatches resolved by using domain models consistently

### 2. presentation/src/commonMain/kotlin/ireader/presentation/ui/migration/MigrationScreens.kt

#### Issues Fixed:
- ✅ Removed duplicate `MigrationFlags` data class definition (conflicts with domain model)
- ✅ Removed duplicate `MigrationSource` data class definition (conflicts with domain model)
- ✅ Updated all references to use fully qualified domain model names: `ireader.domain.models.migration.MigrationFlags` and `ireader.domain.models.migration.MigrationSource`
- ✅ Updated function signatures to use domain models
- ✅ Updated MigrationConfigState to use domain models

## Root Cause

The presentation layer had duplicate definitions of `MigrationFlags` and `MigrationSource` that conflicted with the domain layer models. This caused type mismatch errors throughout the migration screens.

## Solution

1. Removed duplicate model definitions from presentation layer
2. Used fully qualified names for domain models where needed to avoid ambiguity
3. Updated repository method call to use correct Flow-based method

## Verification

All migration-related compilation errors should now be resolved:
- ✅ Unresolved reference 'subscribeBooksByFavorite' - Fixed
- ✅ Argument type mismatch for MigrationSource - Fixed
- ✅ Argument type mismatch for MigrationFlags - Fixed
- ✅ Type inference errors in Flow operations - Fixed

The migration screens now properly use domain models and follow the correct repository patterns.
