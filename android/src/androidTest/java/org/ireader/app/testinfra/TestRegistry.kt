package org.ireader.app.testinfra

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Test Registry - Tracks all test coverage and prevents duplicates.
 * 
 * Each test must register with a unique ID that describes what it tests.
 * If a duplicate is detected, the test fails with a clear error message.
 * 
 * Usage:
 * ```kotlin
 * @Test
 * fun libraryScreenDisplaysBooks() {
 *     TestRegistry.register("library.screen.displays_books") {
 *         // test logic
 *     }
 * }
 * ```
 */
object TestRegistry {
    private const val TAG = "TestRegistry"
    
    // Map of test ID -> (test class, test method, timestamp)
    private val registeredTests = ConcurrentHashMap<String, TestRegistration>()
    
    // Map of feature area -> list of test IDs
    private val featureCoverage = ConcurrentHashMap<String, MutableSet<String>>()
    
    data class TestRegistration(
        val testClass: String,
        val testMethod: String,
        val timestamp: Long,
        val featureArea: String
    )
    
    /**
     * Register and run a test. Fails if duplicate detected.
     * 
     * @param testId Unique identifier for what this test covers (e.g., "library.screen.displays_books")
     * @param featureArea Feature area for coverage tracking (e.g., "library", "settings", "reader")
     * @param testBody The actual test logic
     */
    fun register(
        testId: String,
        featureArea: String = extractFeatureArea(testId),
        testBody: () -> Unit
    ) {
        val caller = getCallerInfo()
        
        // Check for duplicate
        val existing = registeredTests[testId]
        if (existing != null) {
            val errorMsg = buildString {
                appendLine("DUPLICATE TEST DETECTED!")
                appendLine("Test ID: $testId")
                appendLine("First registration: ${existing.testClass}.${existing.testMethod}")
                appendLine("Current test: ${caller.className}.${caller.methodName}")
                appendLine("")
                appendLine("FIX: Remove the duplicate test or rename it to test different behavior.")
                appendLine("Each test should verify ONE unique behavior.")
            }
            Log.e(TAG, errorMsg)
            throw AssertionError(errorMsg)
        }
        
        // Register
        registeredTests[testId] = TestRegistration(
            testClass = caller.className,
            testMethod = caller.methodName,
            timestamp = System.currentTimeMillis(),
            featureArea = featureArea
        )
        
        // Track coverage
        featureCoverage.getOrPut(featureArea) { mutableSetOf() }.add(testId)
        
        Log.d(TAG, "Registered test: $testId in $featureArea")
        
        // Run the test
        testBody()
    }
    
    /**
     * Get coverage report for a feature area.
     */
    fun getCoverageReport(featureArea: String): Set<String> {
        return featureCoverage[featureArea] ?: emptySet()
    }
    
    /**
     * Get all registered test IDs.
     */
    fun getAllTests(): Map<String, TestRegistration> {
        return registeredTests.toMap()
    }
    
    /**
     * Check for duplicates without running tests (for test discovery).
     */
    fun checkForDuplicates(testIds: List<String>): List<String> {
        val duplicates = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        
        for (id in testIds) {
            if (!seen.add(id)) {
                duplicates.add(id)
            }
        }
        
        return duplicates
    }
    
    /**
     * Generate coverage summary.
     */
    fun generateSummary(): String {
        return buildString {
            appendLine("=== Test Coverage Summary ===")
            appendLine("Total tests registered: ${registeredTests.size}")
            appendLine("")
            
            for ((area, tests) in featureCoverage.toSortedMap()) {
                appendLine("$area: ${tests.size} tests")
                for (test in tests.sorted()) {
                    appendLine("  - $test")
                }
            }
        }
    }
    
    /**
     * Clear registry (for test suite restart).
     */
    fun clear() {
        registeredTests.clear()
        featureCoverage.clear()
    }
    
    private fun extractFeatureArea(testId: String): String {
        val parts = testId.split(".")
        return if (parts.size >= 2) parts[0] else "general"
    }
    
    private fun getCallerInfo(): CallerInfo {
        val stackTrace = Throwable().stackTrace
        // Find the test method (skipping test framework internals)
        for (element in stackTrace) {
            if (element.className.startsWith("org.ireader.app") && 
                !element.className.contains("TestRegistry")) {
                return CallerInfo(
                    className = element.className.substringAfterLast('.'),
                    methodName = element.methodName
                )
            }
        }
        return CallerInfo("Unknown", "unknown")
    }
    
    data class CallerInfo(val className: String, val methodName: String)
}

/**
 * Extension function for cleaner test registration.
 * 
 * Usage:
 * ```kotlin
 * "library.screen.displays_books".test {
 *     // test logic
 * }
 * ```
 */
fun String.test(featureArea: String = "", testBody: () -> Unit) {
    TestRegistry.register(
        testId = this,
        featureArea = featureArea.ifEmpty { TestRegistry.extractFeatureArea(this) },
        testBody = testBody
    )
}
