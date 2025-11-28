package org.ireader.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Baseline Profile Generator for IReader.
 * 
 * This generates a baseline profile by exercising critical user journeys.
 * The generated profile helps ART pre-compile hot code paths for faster startup.
 * 
 * To generate the baseline profile:
 * 1. Connect a physical device (or use an emulator with API 28+)
 * 2. Build and install the app: ./gradlew :android:installStandardDebug
 * 3. Run: ./gradlew :benchmark:connectedStandardBenchmarkAndroidTest -P android.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
 * 4. The profile will be generated at: benchmark/build/outputs/connected_android_test_additional_output/
 * 5. Copy the generated baseline-prof.txt to android/src/main/baseline-prof.txt
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * Generate baseline profile for app startup.
     * This covers the critical path from launch to first frame.
     */
    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
            profileBlock = {
                // Start the app
                startActivityAndWait()
                
                // Wait for the main screen to load
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            }
        )
    }

    /**
     * Generate baseline profile for library browsing.
     * This covers scrolling through the book library.
     */
    @Test
    fun generateLibraryProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                
                // Wait for content to load
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(2000) // Allow initial data to load
                
                // Scroll through library content
                val scrollable = device.findObject(By.scrollable(true))
                scrollable?.let {
                    repeat(3) {
                        scrollable.scroll(Direction.DOWN, 0.8f)
                        Thread.sleep(500)
                    }
                    repeat(2) {
                        scrollable.scroll(Direction.UP, 0.8f)
                        Thread.sleep(500)
                    }
                }
            }
        )
    }

    /**
     * Generate baseline profile for catalog/explore browsing.
     * This covers navigating to and browsing the catalog.
     */
    @Test
    fun generateExploreProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                
                // Wait for content to load
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(2000)
                
                // Try to find and click on Explore/Browse tab
                val exploreTab = device.findObject(By.text("Explore")) 
                    ?: device.findObject(By.text("Browse"))
                    ?: device.findObject(By.desc("Explore"))
                    ?: device.findObject(By.desc("Browse"))
                
                exploreTab?.click()
                Thread.sleep(2000)
                
                // Scroll through explore content
                val scrollable = device.findObject(By.scrollable(true))
                scrollable?.let {
                    repeat(2) {
                        scrollable.scroll(Direction.DOWN, 0.7f)
                        Thread.sleep(500)
                    }
                }
            }
        )
    }

    /**
     * Generate baseline profile for settings navigation.
     * This covers opening and browsing settings.
     */
    @Test
    fun generateSettingsProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                
                // Wait for content to load
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(2000)
                
                // Try to find and click on More/Settings tab
                val moreTab = device.findObject(By.text("More")) 
                    ?: device.findObject(By.text("Settings"))
                    ?: device.findObject(By.desc("More"))
                    ?: device.findObject(By.desc("Settings"))
                
                moreTab?.click()
                Thread.sleep(1500)
                
                // Scroll through settings
                val scrollable = device.findObject(By.scrollable(true))
                scrollable?.let {
                    scrollable.scroll(Direction.DOWN, 0.5f)
                    Thread.sleep(500)
                }
            }
        )
    }

    companion object {
        // Use debug package name for testing
        private const val PACKAGE_NAME = "org.ireader.app.debug"
        private const val TIMEOUT = 10_000L
    }
}
