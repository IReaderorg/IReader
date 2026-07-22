package org.ireader.app.testinfra

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.ireader.app.MainActivity
import kotlin.system.measureTimeMillis

/**
 * Base class for registered Compose tests.
 * 
 * Automatically:
 * - Registers tests with the registry (fails on duplicates)
 * - Logs test lifecycle events
 * - Provides structured error output
 * 
 * Subclasses should use "feature.area.test_name" format for test IDs.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class RegisteredComposeTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    protected open val featureArea: String = "general"
    
    private var testStartTime: Long = 0L
    private var currentTestId: String = ""
    
    @Before
    open fun setUp() {
        TestLogger.testStarted(currentTestId, "Setup phase")
    }
    
    @After
    open fun tearDown() {
        val duration = System.currentTimeMillis() - testStartTime
        TestLogger.testPassed(currentTestId, duration)
    }
    
    /**
     * Run a registered test with duplicate detection.
     * 
     * @param testId Unique test identifier (e.g., "library.screen.displays_books")
     * @param description Human-readable description
     * @param testBody The test logic
     */
    protected fun runRegisteredTest(
        testId: String,
        description: String = "",
        testBody: AndroidComposeTestRule<MainActivity, *>.() -> Unit
    ) {
        currentTestId = testId
        testStartTime = System.currentTimeMillis()
        
        TestRegistry.register(testId, featureArea) {
            TestLogger.testStarted(testId, description)
            
            try {
                testBody(composeTestRule)
                TestLogger.testPassed(testId, System.currentTimeMillis() - testStartTime)
            } catch (e: AssertionError) {
                TestLogger.testFailed(
                    testId = testId,
                    error = e,
                    suggestion = "Check the assertion and fix the expected value or UI behavior.",
                    relevantFiles = listOf("android/src/androidTest/java/org/ireader/app/")
                )
                throw e
            } catch (e: Exception) {
                TestLogger.testFailed(
                    testId = testId,
                    error = e,
                    suggestion = "Unexpected error. Check the stack trace and fix the root cause.",
                    relevantFiles = listOf("android/src/androidTest/java/org/ireader/app/")
                )
                throw e
            }
        }
    }
    
    /**
     * Log a test step for tracing.
     */
    protected fun logStep(step: String, details: Map<String, Any> = emptyMap()) {
        TestLogger.testStep(currentTestId, step, details)
    }
    
    /**
     * Log UI state snapshot.
     */
    protected fun logSnapshot(vararg pairs: Pair<String, Any>) {
        TestLogger.uiSnapshot(currentTestId, pairs.toMap())
    }
    
    /**
     * Log screen transition.
     */
    protected fun logTransition(from: String, to: String) {
        TestLogger.screenTransition(currentTestId, from, to)
    }
}

/**
 * Extension to get composeTestRule from test class.
 */
val RegisteredComposeTest.rule: AndroidComposeTestRule<MainActivity, *>
    get() = composeTestRule
