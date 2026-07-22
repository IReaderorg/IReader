package org.ireader.app.testinfra

import android.util.Log
import java.io.File

/**
 * Detects duplicate tests across the test suite.
 * 
 * Run this before executing tests to identify:
 * - Duplicate test IDs
 * - Tests that test the same UI element
 * - Missing test coverage
 * 
 * Usage in test:
 * ```kotlin
 * @Before
 * fun checkDuplicates() {
 *     DuplicateTestDetector.check()
 * }
 * ```
 */
object DuplicateTestDetector {
    private const val TAG = "DuplicateTestDetector"
    
    data class DuplicateInfo(
        val testId: String,
        val locations: List<String>,
        val suggestion: String
    )
    
    /**
     * Check for duplicates in registered tests.
     */
    fun check(): List<DuplicateInfo> {
        val allTests = TestRegistry.getAllTests()
        val duplicates = mutableListOf<DuplicateInfo>()
        
        // Group by test ID
        val grouped = allTests.entries.groupBy { it.key }
        
        for ((testId, registrations) in grouped) {
            if (registrations.size > 1) {
                val locations = registrations.map { 
                    "${it.value.testClass}.${it.value.testMethod}" 
                }
                
                duplicates.add(
                    DuplicateInfo(
                        testId = testId,
                        locations = locations,
                        suggestion = buildString {
                            appendLine("Test '$testId' is registered ${registrations.size} times.")
                            appendLine("Keep one and either:")
                            appendLine("1. Delete the duplicate")
                            appendLine("2. Rename to test different behavior")
                            appendLine("3. Use @Ignore if intentionally skipped")
                        }
                    )
                )
            }
        }
        
        if (duplicates.isNotEmpty()) {
            Log.e(TAG, "Found ${duplicates.size} duplicate tests:")
            for (dup in duplicates) {
                Log.e(TAG, dup.suggestion)
            }
        }
        
        return duplicates
    }
    
    /**
     * Analyze test files for potential duplicates based on UI interactions.
     */
    fun analyzeFiles(testDir: File): List<DuplicateInfo> {
        val duplicates = mutableListOf<DuplicateInfo>()
        val interactions = mutableMapOf<String, MutableList<String>>()
        
        testDir.walkTopDown()
            .filter { it.extension == "kt" }
            .forEach { file ->
                val content = file.readText()
                val testName = file.nameWithoutExtension
                
                // Find click interactions
                val clicks = Regex("""(?:clickOn|performClick|onNodeWithText\("([^"]+)"\)\.performClick)""")
                    .findAll(content)
                    .map { it.groupValues.getOrElse(1) { "unknown" } }
                    .toList()
                
                for (click in clicks) {
                    interactions.getOrPut(click) { mutableListOf() }.add(testName)
                }
            }
        
        // Find interactions tested by multiple files
        for ((interaction, files) in interactions) {
            if (files.size > 1) {
                duplicates.add(
                    DuplicateInfo(
                        testId = "interaction:$interaction",
                        locations = files,
                        suggestion = "UI element '$interaction' is tested in ${files.size} files. Consider consolidating."
                    )
                )
            }
        }
        
        return duplicates
    }
    
    /**
     * Generate test coverage report.
     */
    fun generateCoverageReport(): String {
        return buildString {
            appendLine("=== Test Coverage Report ===")
            appendLine(TestRegistry.generateSummary())
            appendLine("")
            
            val duplicates = check()
            if (duplicates.isNotEmpty()) {
                appendLine("=== Duplicates Found ===")
                for (dup in duplicates) {
                    appendLine(dup.suggestion)
                }
            } else {
                appendLine("No duplicates found.")
            }
        }
    }
}
