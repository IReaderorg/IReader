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
 * Navigation Performance Benchmark for IReader.
 * Measures frame timing during screen transitions to identify jank.
 * 
 * Tests critical navigation paths:
 * - Tab switching (Library, Updates, History, Extensions, More)
 * - Book detail screen navigation
 * - Reader screen navigation
 * - Settings navigation
 * 
 * Target metrics:
 * - P50 frame time: < 16ms (60 FPS)
 * - P90 frame time: < 32ms
 * - P99 frame time: < 48ms
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    /**
     * Measures tab switching performance between all main tabs.
     */
    @Test
    fun tabSwitchingPerformance() {
        measureNavigationPerformance("tab_switching") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1000)
            
            // Switch to Extensions tab
            clickTab("Extensions") ?: clickTab("Source") ?: clickTab("Browse")
            Thread.sleep(500)
            
            // Switch to More tab
            clickTab("More")
            Thread.sleep(500)
            
            // Switch to History tab
            clickTab("History")
            Thread.sleep(500)
            
            // Switch to Updates tab
            clickTab("Updates")
            Thread.sleep(500)
            
            // Switch back to Library tab
            clickTab("Library")
            Thread.sleep(500)
        }
    }

    /**
     * Measures rapid tab switching (stress test).
     */
    @Test
    fun rapidTabSwitching() {
        measureNavigationPerformance("rapid_tab_switching") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1000)
            
            // Rapid switching between tabs
            repeat(3) {
                clickTab("Extensions") ?: clickTab("Source")
                Thread.sleep(200)
                clickTab("Library")
                Thread.sleep(200)
                clickTab("More")
                Thread.sleep(200)
                clickTab("Library")
                Thread.sleep(200)
            }
        }
    }

    /**
     * Measures book detail screen navigation performance.
     */
    @Test
    fun bookDetailNavigation() {
        measureNavigationPerformance("book_detail") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1500)
            
            // Try to click on a book in the library
            val bookItem = device.findObject(By.clickable(true).hasDescendant(By.clazz("android.widget.ImageView")))
                ?: device.findObject(By.res("$PACKAGE_NAME:id/book_cover"))
                ?: device.findObject(By.descContains("book"))
            
            if (bookItem != null) {
                bookItem.click()
                Thread.sleep(2000) // Wait for detail screen to load
                
                // Navigate back
                device.pressBack()
                Thread.sleep(1000)
            }
        }
    }

    /**
     * Measures settings screen navigation performance.
     */
    @Test
    fun settingsNavigation() {
        measureNavigationPerformance("settings") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1000)
            
            // Go to More tab
            clickTab("More")
            Thread.sleep(500)
            
            // Click on Settings
            val settingsItem = device.findObject(By.text("Settings"))
                ?: device.findObject(By.descContains("Settings"))
                ?: device.findObject(By.textContains("Setting"))
            
            if (settingsItem != null) {
                settingsItem.click()
                Thread.sleep(1500)
                
                // Navigate back
                device.pressBack()
                Thread.sleep(500)
            }
            
            // Return to Library
            clickTab("Library")
            Thread.sleep(500)
        }
    }

    /**
     * Measures extensions/sources screen navigation and scrolling.
     */
    @Test
    fun extensionsScreenPerformance() {
        measureNavigationPerformance("extensions") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1000)
            
            // Go to Extensions tab
            clickTab("Extensions") ?: clickTab("Source") ?: clickTab("Browse")
            Thread.sleep(1500)
            
            // Scroll through extensions list
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                scrollable.scroll(Direction.DOWN, 0.5f)
                Thread.sleep(300)
                scrollable.scroll(Direction.DOWN, 0.5f)
                Thread.sleep(300)
                scrollable.scroll(Direction.UP, 0.8f)
                Thread.sleep(300)
            }
            
            // Return to Library
            clickTab("Library")
            Thread.sleep(500)
        }
    }

    /**
     * Measures history screen navigation and scrolling.
     */
    @Test
    fun historyScreenPerformance() {
        measureNavigationPerformance("history") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1000)
            
            // Go to History tab
            clickTab("History")
            Thread.sleep(1500)
            
            // Scroll through history list
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                scrollable.scroll(Direction.DOWN, 0.5f)
                Thread.sleep(300)
                scrollable.scroll(Direction.UP, 0.5f)
                Thread.sleep(300)
            }
            
            // Return to Library
            clickTab("Library")
            Thread.sleep(500)
        }
    }

    /**
     * Measures updates screen navigation and scrolling.
     */
    @Test
    fun updatesScreenPerformance() {
        measureNavigationPerformance("updates") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1000)
            
            // Go to Updates tab
            clickTab("Updates")
            Thread.sleep(1500)
            
            // Scroll through updates list
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                scrollable.scroll(Direction.DOWN, 0.5f)
                Thread.sleep(300)
                scrollable.scroll(Direction.UP, 0.5f)
                Thread.sleep(300)
            }
            
            // Return to Library
            clickTab("Library")
            Thread.sleep(500)
        }
    }

    /**
     * Measures complete user journey: Library -> Book Detail -> Reader -> Back.
     */
    @Test
    fun completeReadingJourney() {
        measureNavigationPerformance("reading_journey") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1500)
            
            // Click on first book
            val bookItem = device.findObject(By.clickable(true).hasDescendant(By.clazz("android.widget.ImageView")))
            if (bookItem != null) {
                bookItem.click()
                Thread.sleep(2000) // Book detail loads
                
                // Try to click on a chapter or the FAB to start reading
                val fab = device.findObject(By.descContains("Start"))
                    ?: device.findObject(By.descContains("Resume"))
                    ?: device.findObject(By.descContains("Play"))
                    ?: device.findObject(By.clazz("androidx.compose.material3.FloatingActionButton"))
                
                if (fab != null) {
                    fab.click()
                    Thread.sleep(2000) // Reader loads
                    
                    // Navigate back from reader
                    device.pressBack()
                    Thread.sleep(1000)
                }
                
                // Navigate back from book detail
                device.pressBack()
                Thread.sleep(1000)
            }
        }
    }

    /**
     * Measures back navigation performance from various screens.
     */
    @Test
    fun backNavigationPerformance() {
        measureNavigationPerformance("back_navigation") {
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            Thread.sleep(1000)
            
            // Navigate to More -> Settings -> Back -> Back
            clickTab("More")
            Thread.sleep(500)
            
            val settingsItem = device.findObject(By.text("Settings"))
            if (settingsItem != null) {
                settingsItem.click()
                Thread.sleep(1000)
                
                // Back to More
                device.pressBack()
                Thread.sleep(500)
            }
            
            // Navigate to Extensions
            clickTab("Extensions") ?: clickTab("Source")
            Thread.sleep(500)
            
            // Back to Library using tab
            clickTab("Library")
            Thread.sleep(500)
        }
    }

    private fun measureNavigationPerformance(
        testName: String,
        navigationBlock: MacrobenchmarkScope.() -> Unit
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
            measureBlock = navigationBlock
        )
    }

    private fun MacrobenchmarkScope.clickTab(tabName: String): Boolean {
        val tab = device.findObject(By.desc(tabName))
            ?: device.findObject(By.text(tabName))
            ?: device.findObject(By.descContains(tabName))
            ?: device.findObject(By.textContains(tabName))
        
        return if (tab != null) {
            tab.click()
            true
        } else {
            false
        }
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
                ?: device.findObject(By.text("ALLOW"))
                ?: device.findObject(By.text("Deny"))
                ?: device.findObject(By.text("Don't allow"))
                ?: device.findObject(By.res("com.android.permissioncontroller:id/permission_allow_button"))
            
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
