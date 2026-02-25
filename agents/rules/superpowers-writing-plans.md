---
inclusion: auto
description: Guide for writing detailed implementation plans with bite-sized tasks, exact file paths, and TDD steps
---

# Writing Implementation Plans

## Overview

Write comprehensive implementation plans assuming the engineer has zero context for our codebase and questionable taste. Document everything they need to know: which files to touch for each task, code, testing, docs they might need to check, how to test it. Give them the whole plan as bite-sized tasks. DRY. YAGNI. TDD. Frequent commits.

Assume they are a skilled developer, but know almost nothing about our toolset or problem domain. Assume they don't know good test design very well.

**Context:** This should be run after design approval (brainstorming).

**Save plans to:** `docs/plans/YYYY-MM-DD-<feature-name>.md`

## Bite-Sized Task Granularity

**Each step is one action (2-5 minutes):**
- "Write the failing test" - step
- "Run it to make sure it fails" - step
- "Implement the minimal code to make the test pass" - step
- "Run the tests and make sure they pass" - step
- "Commit" - step

## Plan Document Header

**Every plan MUST start with this header:**

```markdown
# [Feature Name] Implementation Plan

**Goal:** [One sentence describing what this builds]

**Architecture:** [2-3 sentences about approach]

**Tech Stack:** [Key technologies/libraries]

---
```

## Task Structure

````markdown
### Task N: [Component Name]

**Files:**
- Create: `exact/path/to/file.kt`
- Modify: `exact/path/to/existing.kt:123-145`
- Test: `tests/exact/path/to/test.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun `test specific behavior`() {
    val result = function(input)
    assertEquals(expected, result)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :module:testDebugUnitTest --tests "TestClass.test specific behavior"`
Expected: FAIL with "function not defined"

**Step 3: Write minimal implementation**

```kotlin
fun function(input: Type): ReturnType {
    return expected
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :module:testDebugUnitTest --tests "TestClass.test specific behavior"`
Expected: PASS

**Step 5: Commit**

```bash
git add tests/path/test.kt src/path/file.kt
git commit -m "feat: add specific feature"
```
````

## IReader-Specific Plan Considerations

### Kotlin Multiplatform

When planning KMP features:

```markdown
**Files:**
- Common: `core/src/commonMain/kotlin/path/Feature.kt`
- Android: `core/src/androidMain/kotlin/path/Feature.android.kt`
- iOS: `core/src/iosMain/kotlin/path/Feature.ios.kt`
- Desktop: `core/src/desktopMain/kotlin/path/Feature.desktop.kt`
- Test: `core/src/commonTest/kotlin/path/FeatureTest.kt`
```

### Compose Multiplatform

When planning UI features:

```markdown
**Step 1: Write the composable test**

```kotlin
@Test
fun `composable displays correctly`() {
    composeTestRule.setContent {
        MyComposable(data = testData)
    }
    
    composeTestRule
        .onNodeWithText("Expected Text")
        .assertIsDisplayed()
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :presentation:testDebugUnitTest`
Expected: FAIL with "composable not found"
```

### Extension Sources

When planning extension features:

```markdown
**Files:**
- Source: `sources/en/sitename/main/src/ireader/sitename/SiteName.kt`
- Test: `sources/en/sitename/test/src/ireader/sitename/SiteNameTest.kt`
- Build: `sources/en/sitename/build.gradle.kts`

**Step 1: Write the selector test**

```kotlin
@Test
fun `parses novel list correctly`() {
    val html = loadTestHtml("novel-list.html")
    val novels = source.parseNovelList(html)
    
    assertEquals(20, novels.size)
    assertEquals("Expected Title", novels.first().title)
}
```
```

### Database Changes

When planning database migrations:

```markdown
**Files:**
- Migration: `data/src/commonMain/sqldelight/migrations/XX.sqm`
- Schema: `data/src/commonMain/sqldelight/ireader/db/Book.sq`
- Test: `data/src/commonTest/kotlin/ireader/db/MigrationTest.kt`

**Step 1: Write migration test**

```kotlin
@Test
fun `migration XX adds column correctly`() {
    val driver = createTestDriver()
    val oldSchema = Schema.XX_MINUS_1
    val newSchema = Schema.XX
    
    // Insert test data with old schema
    oldSchema.create(driver)
    // ... insert data
    
    // Run migration
    newSchema.migrate(driver, XX_MINUS_1, XX)
    
    // Verify new column exists and data preserved
    val result = driver.executeQuery(...)
    assertEquals(expected, result)
}
```
```

## Remember

- Exact file paths always
- Complete code in plan (not "add validation")
- Exact commands with expected output
- DRY, YAGNI, TDD, frequent commits
- Platform-specific considerations for KMP
- Test composables with ComposeTestRule
- Test database migrations thoroughly

## Execution

After saving the plan:

1. Work through tasks sequentially
2. Follow TDD strictly (RED-GREEN-REFACTOR)
3. Commit after each task
4. Run full test suite before moving to next task
5. If a task reveals design issues, stop and update design doc

## Verification Checklist

Before marking plan complete:

- [ ] All tasks have exact file paths
- [ ] All tasks have complete code (not pseudocode)
- [ ] All tasks have test-first steps
- [ ] All tasks have verification steps
- [ ] All tasks have commit steps
- [ ] Plan follows DRY principles
- [ ] Plan follows YAGNI principles
- [ ] Plan considers all platforms (if KMP)
- [ ] Plan includes error handling
- [ ] Plan includes edge cases
