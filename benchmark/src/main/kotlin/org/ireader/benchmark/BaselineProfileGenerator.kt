package org.ireader.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
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
 * Generates profiles by exercising critical user journeys.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1000)
            }
        )
    }

    @Test
    fun generateLibraryProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
                
                val scrollable = device.findObject(By.scrollable(true))
                scrollable?.scroll(Direction.DOWN, 0.5f)
                Thread.sleep(500)
            }
        )
    }

    @Test
    fun generateNavigationProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
                
                // Try to click on navigation tabs (More icon is MoreHoriz)
                // The tabs are: Library, Updates, History, Extensions, More
                val moreTab = device.findObject(By.desc("More"))
                    ?: device.findObject(By.text("More"))
                    ?: device.findObject(By.descContains("more"))
                
                moreTab?.click()
                Thread.sleep(1000)
                
                // Go back
                device.pressBack()
                Thread.sleep(500)
            }
        )
    }

    @Test
    fun generateCombinedProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
                
                val scrollable = device.findObject(By.scrollable(true))
                scrollable?.scroll(Direction.DOWN, 0.5f)
                Thread.sleep(300)
                scrollable?.scroll(Direction.UP, 0.5f)
                Thread.sleep(300)
            }
        )
    }

    @Test
    fun generateBookDetailProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
                
                // Click on a book to open detail screen
                val bookItem = device.findObject(By.clickable(true).hasDescendant(By.clazz("android.widget.ImageView")))
                if (bookItem != null) {
                    bookItem.click()
                    Thread.sleep(2000)
                    
                    // Scroll through chapters
                    val scrollable = device.findObject(By.scrollable(true))
                    scrollable?.scroll(Direction.DOWN, 0.5f)
                    Thread.sleep(300)
                    
                    // Go back
                    device.pressBack()
                    Thread.sleep(500)
                }
            }
        )
    }

    @Test
    fun generateAllTabsProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
                
                // Visit all tabs to generate profiles for each
                listOf("Extensions", "More", "History", "Updates", "Library").forEach { tabName ->
                    val tab = device.findObject(By.desc(tabName))
                        ?: device.findObject(By.text(tabName))
                        ?: device.findObject(By.descContains(tabName))
                    tab?.click()
                    Thread.sleep(800)
                }
            }
        )
    }

    @Test
    fun generateReaderProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
                
                // Open a book
                val bookItem = device.findObject(By.clickable(true).hasDescendant(By.clazz("android.widget.ImageView")))
                if (bookItem != null) {
                    bookItem.click()
                    Thread.sleep(2000)
                    
                    // Try to start reading
                    val fab = device.findObject(By.descContains("Start"))
                        ?: device.findObject(By.descContains("Resume"))
                        ?: device.findObject(By.clazz("androidx.compose.material3.FloatingActionButton"))
                    
                    if (fab != null) {
                        fab.click()
                        Thread.sleep(2000)
                        
                        // Scroll in reader
                        val scrollable = device.findObject(By.scrollable(true))
                        scrollable?.scroll(Direction.DOWN, 0.3f)
                        Thread.sleep(300)
                        
                        device.pressBack()
                        Thread.sleep(500)
                    }
                    
                    device.pressBack()
                    Thread.sleep(500)
                }
            }
        )
    }

    @Test
    fun generateSettingsProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            profileBlock = {
                startActivityAndWait()
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(1500)
                
                // Go to More tab
                val moreTab = device.findObject(By.desc("More"))
                    ?: device.findObject(By.text("More"))
                moreTab?.click()
                Thread.sleep(800)
                
                // Open Settings
                val settingsItem = device.findObject(By.text("Settings"))
                if (settingsItem != null) {
                    settingsItem.click()
                    Thread.sleep(1500)
                    
                    // Scroll through settings
                    val scrollable = device.findObject(By.scrollable(true))
                    scrollable?.scroll(Direction.DOWN, 0.5f)
                    Thread.sleep(300)
                    
                    device.pressBack()
                    Thread.sleep(500)
                }
            }
        )
    }
    
    /**
     * Dismiss any dialogs (permissions, app dialogs, etc.)
     */
    private fun MacrobenchmarkScope.dismissPermissionDialogs() {
        for (i in 0 until 5) {
            val dismissButton = device.findObject(By.text("Not now"))
                ?: device.findObject(By.text("NOT NOW"))
                ?: device.findObject(By.text("Not Now"))
                ?: device.findObject(By.text("Later"))
                ?: device.findObject(By.text("Skip"))
                ?: device.findObject(By.text("Cancel"))
                ?: device.findObject(By.text("No thanks"))
                ?: device.findObject(By.text("Dismiss"))
                ?: device.findObject(By.text("Close"))
                ?: device.findObject(By.text("OK"))
                ?: device.findObject(By.text("Got it"))
                ?: device.findObject(By.text("Allow"))
                ?: device.findObject(By.text("ALLOW"))
                ?: device.findObject(By.text("While using the app"))
                ?: device.findObject(By.text("Deny"))
                ?: device.findObject(By.text("Don't allow"))
                ?: device.findObject(By.res("com.android.permissioncontroller:id/permission_allow_button"))
                ?: device.findObject(By.res("com.android.permissioncontroller:id/permission_deny_button"))
            
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
