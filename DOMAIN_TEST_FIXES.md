# Domain Test Compilation Fixes

## Summary
Fixed all compilation errors in the domain module's test files. The build now completes successfully without any errors.

## Issues Fixed

### 1. Missing Test Dependencies
**Problem:** The domain module was missing essential test dependencies:
- `kotlin("test")` - Kotlin test framework
- `kotlinx-coroutines-test` - Coroutine testing utilities
- `mockk` - Mocking framework

**Solution:** Added `commonTest` source set with proper dependencies in `domain/build.gradle.kts`:
```kotlin
commonTest {
    dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        implementation("io.mockk:mockk:1.13.8")
    }
}
```

### 2. Incorrect CatalogRemote Constructor Usage
**Problem:** Test files were creating `CatalogRemote` instances without required parameters:
- Missing `source` parameter
- Missing `description` parameter
- Missing `jarUrl` parameter
- Missing `nsfw` parameter

**Solution:** Updated `createTestCatalogRemote()` helper method in test files to include all required parameters:
```kotlin
private fun createTestCatalogRemote(
    id: Long = 1,
    name: String = "Test Extension"
): CatalogRemote {
    return CatalogRemote(
        sourceId = id,
        source = id,
        name = name,
        description = "Test extension description",
        lang = "en",
        versionName = "1.0.0",
        versionCode = 1,
        pkgName = "ireader.extension.test",
        iconUrl = "",
        pkgUrl = "",
        jarUrl = "",
        nsfw = false,
        repositoryId = -1L,
        repositoryType = "IREADER"
    )
}
```

### 3. Incorrect CatalogInstalled Usage
**Problem:** Test files were trying to instantiate `CatalogInstalled` directly, but it's a sealed class with two subclasses:
- `CatalogInstalled.SystemWide`
- `CatalogInstalled.Locally`

The tests were also trying to use `.copy()` method which doesn't exist on the sealed class.

**Solution:** Updated test helper methods to use `CatalogInstalled.SystemWide` data class:
```kotlin
private fun createTestCatalogInstalled(
    id: Long = 1,
    name: String = "Test Extension",
    versionCode: Int = 1
): CatalogInstalled.SystemWide {
    return CatalogInstalled.SystemWide(
        name = name,
        description = "Test extension description",
        source = null,
        pkgName = "ireader.extension.test",
        versionName = "1.0.0",
        versionCode = versionCode,
        nsfw = false,
        isPinned = false,
        hasUpdate = false,
        iconUrl = "",
        installDir = null
    )
}
```

### 4. Import Consolidation
**Problem:** Test files had scattered imports for test annotations and assertions.

**Solution:** Consolidated imports to use `kotlin.test.*` wildcard import for cleaner code:
```kotlin
import kotlin.test.*
```

## Files Modified

1. **domain/build.gradle.kts**
   - Added `commonTest` source set with test dependencies

2. **domain/src/commonTest/kotlin/ireader/domain/catalogs/ExtensionManagerTest.kt**
   - Fixed `createTestCatalogRemote()` to include all required parameters
   - Fixed `createTestCatalogInstalled()` to use `CatalogInstalled.SystemWide`
   - Consolidated imports

3. **domain/src/commonTest/kotlin/ireader/domain/catalogs/ExtensionSecurityManagerTest.kt**
   - Fixed all helper methods to use `CatalogInstalled.SystemWide`
   - Fixed `.copy()` usage to work with data class
   - Consolidated imports

4. **domain/src/commonTest/kotlin/ireader/domain/usecases/book/GetBookTest.kt**
   - Consolidated imports to use `kotlin.test.*`

5. **domain/src/commonTest/kotlin/ireader/domain/usecases/book/ToggleFavoriteTest.kt**
   - Consolidated imports to use `kotlin.test.*`

## Build Result

```
BUILD SUCCESSFUL in 3s
52 actionable tasks: 4 executed, 48 up-to-date
```

All test files now compile successfully without errors. The tests are properly structured and ready for implementation.

## Test Structure

The test files follow best practices:
- Use of `@BeforeTest` for setup
- Use of `@Test` annotation for test methods
- Proper use of `runTest` for coroutine testing
- Mock-ready structure (commented out mock usage for future implementation)
- Clear test naming following "should_when" pattern
- Helper methods for creating test data

## Next Steps

1. Implement actual test logic by uncommocking the commented-out mock usage
2. Add more test cases for edge cases and error scenarios
3. Implement integration tests for the extension management system
4. Add test coverage reporting to ensure comprehensive testing

## Related Tasks

This fix supports **Task 11: Extension Management and Repository System** from the Mihon-inspired improvements, specifically:
- Extension trust system with signature verification
- Extension security scanning with permission analysis
- Extension statistics and usage tracking
