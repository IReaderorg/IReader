package ireader.core.system

import ireader.core.deeplink.DeepLinkHandler
import ireader.core.deeplink.UrlResolver
import ireader.core.error.GlobalExceptionHandler
import ireader.core.log.IReaderLog
import ireader.core.prefs.PrivacyPreferences
import ireader.core.telemetry.TelemetrySystem
import ireader.core.update.AppUpdateChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central system manager that coordinates all advanced system features
 */
class SystemManager(
    private val deepLinkHandler: DeepLinkHandler,
    private val urlResolver: UrlResolver,
    private val appUpdateChecker: AppUpdateChecker,
    private val telemetrySystem: TelemetrySystem,
    private val privacyPreferences: PrivacyPreferences,
    private val systemHealthMonitor: SystemHealthMonitor,
    private val scope: CoroutineScope
) {
    
    private val _crashReports = MutableStateFlow<List<CrashReport>>(emptyList())
    val crashReports: StateFlow<List<CrashReport>> = _crashReports.asStateFlow()
    
    private val maxCrashReports = 10
    
    fun initialize() {
        IReaderLog.info("Initializing SystemManager", tag = "SystemManager")
        
        GlobalExceptionHandler.initialize(
            onCrash = { throwable ->
                handleCrash(throwable)
            }
        )
        
        startHealthMonitoring()
        
        scope.launch {
            appUpdateChecker.checkForUpdates(force = false)
        }
        
        IReaderLog.info("SystemManager initialized", tag = "SystemManager")
    }
    
    private fun handleCrash(throwable: Throwable) {
        val crashReport = CrashReport.create(
            exception = throwable,
            context = "Application"
        )
        
        val currentReports = _crashReports.value.toMutableList()
        currentReports.add(crashReport)
        
        while (currentReports.size > maxCrashReports) {
            currentReports.removeAt(0)
        }
        
        _crashReports.value = currentReports
        systemHealthMonitor.recordCrash(throwable)
        telemetrySystem.trackError(throwable, context = "Crash", properties = mapOf("fatal" to true))
        
        IReaderLog.error(
            message = "Crash handled: ${throwable.message}",
            throwable = throwable,
            tag = "SystemManager"
        )
    }
    
    fun handleDeepLink(url: String): Boolean {
        IReaderLog.info("Handling deep link: $url", tag = "SystemManager")
        
        val handled = deepLinkHandler.handleDeepLink(url)
        
        if (handled) {
            telemetrySystem.trackEvent(
                eventName = "deep_link_handled",
                properties = mapOf("url" to url)
            )
        } else {
            telemetrySystem.trackEvent(
                eventName = "deep_link_failed",
                properties = mapOf("url" to url)
            )
        }
        
        return handled
    }
    
    suspend fun resolveUrl(url: String) = urlResolver.resolve(url)
    
    suspend fun checkForUpdates(force: Boolean = false) {
        IReaderLog.info("Checking for updates (force: $force)", tag = "SystemManager")
        appUpdateChecker.checkForUpdates(force)
    }
    
    fun trackScreenView(screenName: String) {
        telemetrySystem.trackScreenView(screenName)
    }
    
    fun trackUserAction(action: String, properties: Map<String, Any> = emptyMap()) {
        telemetrySystem.trackUserAction(action, properties)
    }
    
    fun trackPerformance(operation: String, durationMs: Long) {
        systemHealthMonitor.recordPerformanceMetric(
            operationName = operation,
            durationMs = durationMs,
            category = "Performance"
        )
        
        telemetrySystem.trackMetric(
            metricName = "${operation}_duration",
            value = durationMs.toDouble()
        )
    }
    
    fun getSystemDiagnostics(): String {
        return DiagnosticTools.generateDiagnosticReport(
            healthMonitor = systemHealthMonitor
        )
    }
    
    fun exportDiagnosticData(): String {
        return DiagnosticTools.exportDiagnosticData(
            healthMonitor = systemHealthMonitor
        )
    }
    
    fun runHealthCheck() = DiagnosticTools.performHealthCheck(
        healthMonitor = systemHealthMonitor
    )
    
    fun getSystemInfo() = DiagnosticTools.collectSystemInfo()
    
    fun clearCrashReports() {
        _crashReports.value = emptyList()
        IReaderLog.info("Crash reports cleared", tag = "SystemManager")
    }
    
    suspend fun flushTelemetry() {
        // Telemetry events are stored in memory, no flush needed
        IReaderLog.info("Telemetry flushed", tag = "SystemManager")
    }
    
    suspend fun enablePrivacyMode() {
        privacyPreferences.enablePrivacyMode()
        telemetrySystem.clearEvents()
        IReaderLog.info("Privacy mode enabled", tag = "SystemManager")
    }
    
    suspend fun disablePrivacyMode() {
        privacyPreferences.disablePrivacyMode()
        IReaderLog.info("Privacy mode disabled", tag = "SystemManager")
    }
    
    private fun startHealthMonitoring() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000)
                systemHealthMonitor.recordMemoryUsage()
            }
        }
    }
    
    fun shutdown() {
        IReaderLog.info("Shutting down SystemManager", tag = "SystemManager")
        systemHealthMonitor.stopMonitoring()
    }
}
