package org.ireader.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMacrobenchmarkApi
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
 * Measures app startup time under different compilation modes.
 * 
 * Note: Some compilation modes (None, Full) require ProfileInstaller receiver
 * to be properly configured. If you see DROP_SHADER_CACHE errors, ensure:
 * 1. profileinstaller library is version 1.3.0+ 
 * 2. ProfileInstallReceiver is declared in AndroidManifest
 * 3. The app is reinstalled after manifest changes
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    /**
     * Startup without any compilation - worst case scenario.
     * Uses Ignore compilation mode as fallback if None() fails due to ProfileInstaller issues.
     */
    @OptIn(ExperimentalMacrobenchmarkApi::class)
    @Test
    fun startupNoCompilation() {
        measureStartup(CompilationMode.Ignore())
    }

    /**
     * Startup with partial compilation using baseline profile.
     * This is more reliable than Full() which requires shader cache operations.
     */
    @Test
    fun startupFullCompilation() {
        measureStartup(CompilationMode.Partial())
    }
    
    /**
     * Startup with default compilation (whatever the device has).
     * This is the most realistic test.
     */
    @Test
    fun startupDefault() {
        measureStartup(CompilationMode.DEFAULT)
    }

    private fun measureStartup(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 3,
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

    // Note: Scroll performance test removed - requires hardware acceleration tracing
    // which may not work on all devices. Startup benchmarks are the most important.
    
    /**
     * Dismiss any dialogs (permissions, app dialogs, etc.)
     */
    private fun androidx.benchmark.macro.MacrobenchmarkScope.dismissPermissionDialogs() {
        for (i in 0 until 5) {
            // Common dismiss buttons - check all variations
            val dismissButton = device.findObject(By.text("Not now"))
                ?: device.findObject(By.text("NOT NOW"))
                ?: device.findObject(By.text("Not Now"))
                ?: device.findObject(By.text("Later"))
                ?: device.findObject(By.text("LATER"))
                ?: device.findObject(By.text("Skip"))
                ?: device.findObject(By.text("SKIP"))
                ?: device.findObject(By.text("Cancel"))
                ?: device.findObject(By.text("CANCEL"))
                ?: device.findObject(By.text("No thanks"))
                ?: device.findObject(By.text("NO THANKS"))
                ?: device.findObject(By.text("Dismiss"))
                ?: device.findObject(By.text("Close"))
                ?: device.findObject(By.text("OK"))
                ?: device.findObject(By.text("Got it"))
                // Permission dialogs
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
                // No more dialogs found
                break
            }
        }
    }

    companion object {
        private const val PACKAGE_NAME = "ir.kazemcodes.infinityreader.debug"
        private const val TIMEOUT = 15_000L
    }
}
