# Test-Driven Development (TDD) Methodology

This steering document defines the TDD approach for IReader development. Follow these principles for all new features and bug fixes.

## 🎯 Core TDD Principle

**RED → GREEN → REFACTOR**

Write the test first. Watch it fail. Write minimal code to pass.
j
**Core principle:** If you didn't watch the test fail, you don't know if it tests the right thing.

**Violating the letter of the rules is violating the spirit of the rules.**

## The Iron Law

```
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
```

Write code before the test? Delete it. Start over.

**No exceptions:**
- Don't keep it as "reference"
- Don't "adapt" it while writing tests
- Don't look at it
- Delete means delete

Implement fresh from tests. Period.

## 📋 TDD Workflow

### Step 1: Write the Test First (RED)

Before writing any implementation code:

**Requirements:**
- One behavior per test
- Clear descriptive name
- Real code (no mocks unless unavoidable)
- Tests what SHOULD happen, not what DOES happen

```kotlin
// Example: Testing paragraph indent feature
@Test
fun `setText should add spaces for first-line indent`() {
    // Arrange
    val text = "This is a paragraph."
    val paragraphIndent = 8
    
    // Act
    val result = setText(
        text = text,
        index = 0,
        isLast = false,
        topContentPadding = 0,
        bottomContentPadding = 0,
        contentPadding = 0,
        paragraphIndent = paragraphIndent
    )
    
    // Assert
    assertTrue(result.startsWith("    ")) // 4 spaces for indent of 8
    assertTrue(result.contains("This is a paragraph."))
}
```

**Run the test** - It should FAIL because the feature doesn't exist yet.

**MANDATORY. Never skip.**

Confirm:
- Test fails (not errors)
- Failure message is expected
- Fails because feature missing (not typos)

**Test passes?** You're testing existing behavior. Fix test.

**Test errors?** Fix error, re-run until it fails correctly.

### Step 2: Write Minimal Implementation (GREEN)

Write just enough code to make the test pass:

**Don't add features, refactor other code, or "improve" beyond the test.**

```kotlin
private fun setText(
    text: String,
    index: Int,
    isLast: Boolean,
    topContentPadding: Int,
    bottomContentPadding: Int,
    contentPadding: Int,
    paragraphIndent: Int = 0,
): String {
    val indentSpaces = if (paragraphIndent > 0) {
        " ".repeat((paragraphIndent / 2).coerceAtLeast(0))
    } else {
        ""
    }
    
    return indentSpaces + text
}
```

**Run the test** - It should PASS now.

**MANDATORY.**

Confirm:
- Test passes
- Other tests still pass
- Output pristine (no errors, warnings)

**Test fails?** Fix code, not test.

**Other tests fail?** Fix now.

### Step 3: Refactor (REFACTOR)

Improve the code while keeping tests green:

```kotlin
private fun setText(
    text: String,
    index: Int,
    isLast: Boolean,
    topContentPadding: Int,
    bottomContentPadding: Int,
    contentPadding: Int,
    paragraphIndent: Int = 0,
): String {
    if (text.isEmpty()) return ""
    
    val stringBuilder = StringBuilder()
    
    // Add top padding
    if (index == 0 && topContentPadding > 0) {
        stringBuilder.append("\n".repeat(topContentPadding.coerceAtLeast(0)))
    }
    
    // Add first-line indent
    val indentSpaces = if (paragraphIndent > 0) {
        " ".repeat((paragraphIndent / 2).coerceAtLeast(0))
    } else {
        ""
    }
    
    stringBuilder.append(indentSpaces)
    stringBuilder.append(text.trimStart())
    
    // Add bottom padding
    if (isLast && bottomContentPadding > 0) {
        stringBuilder.append("\n".repeat(bottomContentPadding.coerceAtLeast(0)))
    }
    
    return stringBuilder.toString()
}
```

**Run all tests** - They should still PASS.

## 🧪 Test Types

### 1. Unit Tests
Test individual functions in isolation.

**Location**: `{module}/src/commonTest/kotlin/`

**What to test:**
- Pure functions (business logic, utilities, formatters)
- Use cases (domain layer)
- Data transformations
- Algorithms and calculations

**What NOT to test:**
- Compose components (too complex, requires UI testing framework)
- ViewModels with 10+ dependencies (impractical to mock)
- Platform-specific code (test at integration level)

```kotlin
class TextFormattingTest {
    @Test
    fun `setText with zero indent should not add spaces`() {
        val result = setText(
            text = "Hello",
            index = 0,
            isLast = false,
            topContentPadding = 0,
            bottomContentPadding = 0,
            contentPadding = 0,
            paragraphIndent = 0
        )
        
        assertEquals("Hello", result)
    }
    
    @Test
    fun `setText with indent 8 should add 4 spaces`() {
        val result = setText(
            text = "Hello",
            index = 0,
            isLast = false,
            topContentPadding = 0,
            bottomContentPadding = 0,
            contentPadding = 0,
            paragraphIndent = 8
        )
        
        assertTrue(result.startsWith("    "))
    }
}
```

### 2. Integration Tests
Test how components work together.

**What to test:**
- Use case orchestration
- Repository implementations
- Service interactions
- Simple ViewModels (≤5 dependencies)

**What NOT to test:**
- Complex ViewModels with many dependencies
- Full UI flows (use manual testing)

```kotlin
class ReaderUseCaseTest {
    private lateinit var useCase: GetChapterUseCase
    private lateinit var fakeRepository: FakeChapterRepository
    
    @Before
    fun setup() {
        fakeRepository = FakeChapterRepository()
        useCase = GetChapterUseCase(fakeRepository)
    }
    
    @Test
    fun `getChapter should return chapter from repository`() = runTest {
        // Arrange
        val expectedChapter = Chapter(id = 1, name = "Chapter 1")
        fakeRepository.chapters = listOf(expectedChapter)
        
        // Act
        val result = useCase(chapterId = 1)
        
        // Assert
        assertEquals(expectedChapter, result)
    }
}
```

### 3. UI Tests (Compose) - SKIP FOR NOW
**DO NOT write Compose component tests** - they are too complex and require:
- Full Compose test framework setup
- Android instrumentation tests
- Complex mocking of Compose state
- Significant maintenance overhead

Instead:
- Test business logic separately (unit tests)
- Test ViewModels with few dependencies (integration tests)
- Use manual testing for UI verification

**Example of what NOT to do:**
```kotlin
// ❌ DON'T DO THIS - Too complex, not worth the effort
class ReaderTextTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `paragraph should display with first-line indent`() {
        // This requires full Compose setup, mocking, etc.
        // Not practical for TDD workflow
    }
}
```

## 📐 Test Structure (AAA Pattern)

Always structure tests using **Arrange-Act-Assert**:

```kotlin
@Test
fun `descriptive test name in backticks`() {
    // Arrange - Set up test data and dependencies
    val input = "test data"
    val expected = "expected result"
    
    // Act - Execute the code under test
    val actual = functionUnderTest(input)
    
    // Assert - Verify the results
    assertEquals(expected, actual)
}
```

## 🎯 TDD Best Practices

### 1. Write Tests Before Code
- ✅ Write test first (RED)
- ✅ Write minimal implementation (GREEN)
- ✅ Refactor (REFACTOR)
- ❌ Don't write code without a failing test first

### 2. Know What to Test
**DO test:**
- Pure functions and business logic
- Use cases and domain logic
- Data transformations
- Algorithms and calculations
- Simple ViewModels (≤5 dependencies)

**DON'T test:**
- Compose components (use manual testing)
- ViewModels with 10+ dependencies (too complex to mock)
- Platform-specific UI code
- Third-party library behavior

### 3. Pragmatic TDD
TDD is about confidence, not dogma:
- If a component has 20+ dependencies, skip the test
- If mocking is more complex than the code, skip the test
- If the test doesn't add confidence, skip it
- Focus on business logic, not UI glue code

## 🚨 Common Rationalizations - STOP and Start Over

| Excuse | Reality |
|--------|---------|
| "Too simple to test" | Simple code breaks. Test takes 30 seconds. |
| "I'll test after" | Tests passing immediately prove nothing. |
| "Tests after achieve same goals" | Tests-after = "what does this do?" Tests-first = "what should this do?" |
| "Already manually tested" | Ad-hoc ≠ systematic. No record, can't re-run. |
| "Deleting X hours is wasteful" | Sunk cost fallacy. Keeping unverified code is technical debt. |
| "Keep as reference, write tests first" | You'll adapt it. That's testing after. Delete means delete. |
| "Need to explore first" | Fine. Throw away exploration, start with TDD. |
| "Test hard = design unclear" | Listen to test. Hard to test = hard to use. |
| "TDD will slow me down" | TDD faster than debugging. Pragmatic = test-first. |
| "Manual test faster" | Manual doesn't prove edge cases. You'll re-test every change. |
| "Existing code has no tests" | You're improving it. Add tests for existing code. |

## 🚩 Red Flags - STOP and Start Over

If you catch yourself:
- Code before test
- Test after implementation
- Test passes immediately
- Can't explain why test failed
- Tests added "later"
- Rationalizing "just this once"
- "I already manually tested it"
- "Tests after achieve the same purpose"
- "It's about spirit not ritual"
- "Keep as reference" or "adapt existing code"
- "Already spent X hours, deleting is wasteful"
- "TDD is dogmatic, I'm being pragmatic"
- "This is different because..."

**All of these mean: Delete code. Start over with TDD.**

## 📐 Why Order Matters

**"I'll write tests after to verify it works"**

Tests written after code pass immediately. Passing immediately proves nothing:
- Might test wrong thing
- Might test implementation, not behavior
- Might miss edge cases you forgot
- You never saw it catch the bug

Test-first forces you to see the test fail, proving it actually tests something.

**"I already manually tested all the edge cases"**

Manual testing is ad-hoc. You think you tested everything but:
- No record of what you tested
- Can't re-run when code changes
- Easy to forget cases under pressure
- "It worked when I tried it" ≠ comprehensive

Automated tests are systematic. They run the same way every time.

**"Deleting X hours of work is wasteful"**

Sunk cost fallacy. The time is already gone. Your choice now:
- Delete and rewrite with TDD (X more hours, high confidence)
- Keep it and add tests after (30 min, low confidence, likely bugs)

The "waste" is keeping code you can't trust. Working code without real tests is technical debt.

**"TDD is dogmatic, being pragmatic means adapting"**

TDD IS pragmatic:
- Finds bugs before commit (faster than debugging after)
- Prevents regressions (tests catch breaks immediately)
- Documents behavior (tests show how to use code)
- Enables refactoring (change freely, tests catch breaks)

"Pragmatic" shortcuts = debugging in production = slower.

**"Tests after achieve the same goals - it's spirit not ritual"**

No. Tests-after answer "What does this do?" Tests-first answer "What should this do?"

Tests-after are biased by your implementation. You test what you built, not what's required. You verify remembered edge cases, not discovered ones.

Tests-first force edge case discovery before implementing. Tests-after verify you remembered everything (you didn't).

30 minutes of tests after ≠ TDD. You get coverage, lose proof tests work.

### 2. Test One Thing at a Time
```kotlin
// ✅ GOOD - Tests one specific behavior
@Test
fun `formatChapterTitle should capitalize first letter`() {
    val result = formatChapterTitle("chapter 1")
    assertEquals("Chapter 1", result)
}

// ❌ BAD - Tests multiple things
@Test
fun `formatChapterTitle should work correctly`() {
    // Tests capitalization, trimming, special chars all at once
}
```

### 3. Use Descriptive Test Names
```kotlin
// ✅ GOOD - Clear what is being tested
@Test
fun `setText with empty text should return empty string`()

@Test
fun `setText should trim leading whitespace from first paragraph`()

// ❌ BAD - Unclear test purpose
@Test
fun `test1()`

@Test
fun `testSetText()`
```

### 4. Keep Tests Independent
```kotlin
// ✅ GOOD - Each test is self-contained
@Test
fun `test A`() {
    val data = createTestData()
    // test logic
}

@Test
fun `test B`() {
    val data = createTestData()
    // test logic
}

// ❌ BAD - Tests depend on shared mutable state
private var sharedData = ""

@Test
fun `test A`() {
    sharedData = "modified"
}

@Test
fun `test B`() {
    // Depends on test A running first
    assertEquals("modified", sharedData)
}
```

### 5. Test Edge Cases
```kotlin
@Test
fun `setText with negative indent should not crash`() {
    val result = setText(text = "Hello", paragraphIndent = -5, /* ... */)
    assertNotNull(result)
}

@Test
fun `setText with very large indent should handle gracefully`() {
    val result = setText(text = "Hello", paragraphIndent = 1000, /* ... */)
    assertTrue(result.length < 10000) // Reasonable limit
}

@Test
fun `setText with null or empty text should return empty`() {
    assertEquals("", setText(text = "", paragraphIndent = 8, /* ... */))
}
```

## 🔄 TDD Cycle for Bug Fixes

### Step 1: Write a Failing Test that Reproduces the Bug
```kotlin
@Test
fun `paragraph indent should only affect first line not all lines`() {
    // This test would fail with the old implementation
    val text = "Line 1\nLine 2\nLine 3"
    val result = setText(text = text, paragraphIndent = 8, /* ... */)
    
    // Only first line should have indent
    val lines = result.split("\n")
    assertTrue(lines[0].startsWith("    ")) // First line indented
    assertFalse(lines[1].startsWith("    ")) // Second line NOT indented
    assertFalse(lines[2].startsWith("    ")) // Third line NOT indented
}
```

### Step 2: Fix the Bug
Make the test pass by fixing the implementation.

### Step 3: Verify All Tests Pass
Run the entire test suite to ensure no regressions.

## 📊 Test Coverage Goals

- **Unit Tests**: Aim for 80%+ coverage of business logic
- **Integration Tests**: Cover critical user flows
- **UI Tests**: Cover main user interactions

## 🛠️ Testing Tools

### Kotlin Multiplatform Testing
```kotlin
// In commonTest
expect class PlatformTest {
    @Test
    fun platformSpecificTest()
}

// In androidTest
actual class PlatformTest {
    @Test
    actual fun platformSpecificTest() {
        // Android-specific test
    }
}
```

### Coroutine Testing
```kotlin
@Test
fun `suspend function test`() = runTest {
    // Arrange
    val repository = FakeRepository()
    
    // Act
    val result = repository.fetchData()
    advanceUntilIdle()
    
    // Assert
    assertEquals(expected, result)
}
```

### Compose Testing
```kotlin
@Test
fun `composable renders correctly`() {
    composeTestRule.setContent {
        MyComposable(data = testData)
    }
    
    composeTestRule
        .onNodeWithText("Expected Text")
        .assertIsDisplayed()
}
```

## 🚀 Running Tests

### Run All Tests
```bash
# All modules
.\gradlew.bat test

# Specific module
.\gradlew.bat :presentation:testDebugUnitTest
.\gradlew.bat :domain:test
```

### Run Tests with Coverage
```bash
.\gradlew.bat testDebugUnitTestCoverage
```

### Run Specific Test Class
```bash
.\gradlew.bat :presentation:testDebugUnitTest --tests "TextFormattingTest"
```

## 📝 TDD Checklist

Before committing code, ensure:

- [ ] Test written first (RED phase)
- [ ] Test fails initially
- [ ] Minimal code written to pass test (GREEN phase)
- [ ] Test passes
- [ ] Code refactored for quality (REFACTOR phase)
- [ ] All tests still pass after refactoring
- [ ] Edge cases tested
- [ ] Test names are descriptive
- [ ] Tests are independent
- [ ] No commented-out test code

## 🎓 TDD Benefits

1. **Better Design**: Writing tests first forces you to think about API design
2. **Documentation**: Tests serve as living documentation
3. **Confidence**: Refactor fearlessly knowing tests will catch regressions
4. **Faster Debugging**: Failing tests pinpoint exact issues
5. **Less Debugging Time**: Catch bugs early in development

## 🔗 Related Documents

- `#[[file:.kiro/steering/compose-best-practices.md]]` - Compose testing patterns
- `#[[file:.kiro/steering/kmp-guidelines.md]]` - KMP testing considerations

## 📚 Example: Complete TDD Flow

### Feature: Add paragraph indent setting

#### 1. Write Test (RED)
```kotlin
@Test
fun `updateParagraphIndent should update preference value`() {
    val viewModel = ReaderSettingsViewModel(mockPreferences)
    
    viewModel.updateParagraphIndent(8)
    
    assertEquals(8, viewModel.paragraphsIndent.value)
}
```

**Result**: ❌ Test fails - method doesn't exist

#### 2. Implement (GREEN)
```kotlin
fun updateParagraphIndent(indent: Int) {
    readerPreferences.paragraphIndent().set(indent)
}
```

**Result**: ✅ Test passes

#### 3. Refactor (REFACTOR)
```kotlin
fun updateParagraphIndent(indent: Int) {
    require(indent >= 0) { "Indent must be non-negative" }
    preferencesController.dispatch(
        PreferenceCommand.SetParagraphIndent(indent.coerceIn(0, 100))
    )
}
```

**Result**: ✅ All tests still pass

#### 4. Add Edge Case Tests
```kotlin
@Test
fun `updateParagraphIndent should reject negative values`() {
    assertThrows<IllegalArgumentException> {
        viewModel.updateParagraphIndent(-5)
    }
}

@Test
fun `updateParagraphIndent should clamp to maximum value`() {
    viewModel.updateParagraphIndent(200)
    assertEquals(100, viewModel.paragraphsIndent.value)
}
```

## 🎯 Remember

> "Code without tests is broken by design." - Jacob Kaplan-Moss

Always follow TDD for:
- ✅ New features
- ✅ Bug fixes
- ✅ Refactoring
- ✅ Performance optimizations

The time invested in writing tests pays off exponentially in maintenance and debugging time saved.
