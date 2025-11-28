package org.ireader.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
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
 * 
 * Measures app startup time under different compilation modes:
 * - None: No AOT compilation (worst case, like first install)
 * - Partial: With baseline profile (typical case after profile installation)
 * - Full: Fully AOT compiled (best case, after background optimization)
 * 
 * To run benchmarks:
 * ./gradlew :benchmark:connectedBenchmarkAndroidTest
 * 
 * Results will be in: benchmark/build/outputs/connected_android_test_additional_output/
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    /**
     * Measure cold startup time without any compilation.
     * This represents the worst-case scenario (first install).
     */
    @Test
    fun startupNoCompilation() {
        measureStartup(CompilationMode.None())
    }

    /**
     * Measure cold startup time with baseline profile.
     * This represents the typical case after baseline profile installation.
     */
    @Test
    fun startupWithBaselineProfile() {
        measureStartup(CompilationMode.Partial())
    }

    /**
     * Measure cold startup time with full AOT compilation.
     * This represents the best-case scenario after background optimization.
     */
    @Test
    fun startupFullCompilation() {
        measureStartup(CompilationMode.Full())
    }

    private fun measureStartup(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 5,
            setupBlock = {
                // Kill the app before each iteration
                pressHome()
            },
            measureBlock = {
                startActivityAndWait()
                
                // Wait for the main content to be visible
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
            }
        )
    }

    /**
     * Measure frame timing during library scrolling.
     * This helps identify jank during common user interactions.
     */
    @Test
    fun scrollPerformance() {
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.WARM,
            iterations = 3,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT)
                Thread.sleep(2000) // Wait for data to load
            },
            measureBlock = {
                val scrollable = device.findObject(By.scrollable(true))
                scrollable?.let {
                    repeat(5) {
                        scrollable.fling(androidx.test.uiautomator.Direction.DOWN)
                        Thread.sleep(300)
                    }
                }
            }
        )
    }

    companion object {
        private const val PACKAGE_NAME = "org.ireader.app.debug"
        private const val TIMEOUT = 10_000L
    }
}
