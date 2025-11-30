package org.ireader.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Startup Benchmark for IReader.
 * Measures app startup time under different startup modes.
 * 
 * Note: This benchmark uses CompilationMode.DEFAULT to avoid ProfileInstaller
 * broadcast issues on devices with aggressive battery optimization (e.g., Huawei/EMUI).
 * 
 * For accurate compilation mode comparisons, use a Pixel device or emulator.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    /**
     * Cold startup - app process is killed before each iteration.
     * This is the most important metric for user-perceived startup time.
     */
    @Test
    fun startupCold() {
        measureStartup(StartupMode.COLD)
    }

    /**
     * Warm startup - app process exists but activity is recreated.
     * Measures activity creation time without process initialization.
     */
    @Test
    fun startupWarm() {
        measureStartup(StartupMode.WARM)
    }
    
    /**
     * Hot startup - activity is brought to foreground from background.
     * Fastest startup scenario, measures resume time.
     */
    @Test
    fun startupHot() {
        measureStartup(StartupMode.HOT)
    }

    private fun measureStartup(startupMode: StartupMode) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            // Use DEFAULT to avoid ProfileInstaller broadcast issues on Huawei/EMUI devices
            // DEFAULT uses whatever compilation state the app is already in
            compilationMode = CompilationMode.DEFAULT,
            startupMode = startupMode,
            iterations = 5,
            setupBlock = {
                pressHome()
            },
            measureBlock = {
                startActivityAndWait()
                // Dismiss any permission dialogs
                dismissPermissionDialogs()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            }
        )
    }
    
    /**
     * Dismiss any dialogs (permissions, app dialogs, etc.)
     */
    private fun androidx.benchmark.macro.MacrobenchmarkScope.dismissPermissionDialogs() {
        for (i in 0 until 5) {
            val dismissButton = device.findObject(By.text("Not now"))
                ?: device.findObject(By.text("NOT NOW"))
                ?: device.findObject(By.text("Later"))
                ?: device.findObject(By.text("LATER"))
                ?: device.findObject(By.text("Skip"))
                ?: device.findObject(By.text("SKIP"))
                ?: device.findObject(By.text("Cancel"))
                ?: device.findObject(By.text("OK"))
                ?: device.findObject(By.text("Allow"))
                ?: device.findObject(By.text("ALLOW"))
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
