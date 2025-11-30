package org.ireader.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Scroll Performance Benchmark for IReader.
 * Measures frame timing during list scrolling to identify jank.
 * 
 * Target metrics for smooth scrolling:
 * - P50 frame time: < 16ms (60 FPS)
 * - P90 frame time: < 32ms
 * - P99 frame time: < 48ms
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ScrollBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    /**
     * Measures library screen scroll performance.
     * This is the most common user interaction.
     */
    @Test
    fun scrollLibrary() {
        measureScrollPerformance("library") {
            // Wait for library to load
            device.wait(Until.hasObject(By.scrollable(true)), TIMEOUT)
            Thread.sleep(1000)
            
            // Perform scroll gestures
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                repeat(3) {
                    scrollable.scroll(Direction.DOWN, 0.8f)
                    Thread.sleep(300)
                }
                repeat(3) {
                    scrollable.scroll(Direction.UP, 0.8f)
                    Thread.sleep(300)
                }
            }
        }
    }

    /**
     * Measures fast fling scroll performance.
     * Tests worst-case scenario for older devices.
     */
    @Test
    fun flingScrollLibrary() {
        measureScrollPerformance("fling") {
            device.wait(Until.hasObject(By.scrollable(true)), TIMEOUT)
            Thread.sleep(1000)
            
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                // Fast fling down
                scrollable.fling(Direction.DOWN)
                Thread.sleep(500)
                
                // Fast fling up
                scrollable.fling(Direction.UP)
                Thread.sleep(500)
            }
        }
    }

    private fun measureScrollPerformance(
        testName: String,
        scrollBlock: MacrobenchmarkScope.() -> Unit
    ) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 3,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
            },
            measureBlock = scrollBlock
        )
    }

    private fun MacrobenchmarkScope.dismissPermissionDialogs() {
        for (i in 0 until 5) {
            val dismissButton = device.findObject(By.text("Not now"))
                ?: device.findObject(By.text("NOT NOW"))
                ?: device.findObject(By.text("Later"))
                ?: device.findObject(By.text("Skip"))
                ?: device.findObject(By.text("Cancel"))
                ?: device.findObject(By.text("OK"))
                ?: device.findObject(By.text("Allow"))
                ?: device.findObject(By.text("Deny"))
            
            if (dismissButton != null) {
                dismissButton.click()
                Thread.sleep(400)
            } else {
                break
            }
        }
    }

    companion object {
        private const val PACKAGE_NAME = "ir.kazemcodes.infinityreader.debug"
        private const val TIMEOUT = 15_000L
    }
}
