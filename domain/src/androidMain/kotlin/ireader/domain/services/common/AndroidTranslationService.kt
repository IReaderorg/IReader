package ireader.domain.services.common

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ireader.core.log.Log
import ireader.domain.notification.NotificationsIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of TranslationService.
 * Uses WorkManager for background translation tasks and shows notifications.
 */
class AndroidTranslationService(
    private val context: Context
) : TranslationService, KoinComponent {
    
    // Lazy inject dependencies to avoid circular dependency issues
    private val translationServiceImpl: ireader.domain.services.translationService.TranslationServiceImpl by inject()
    
    // Delegate state to the impl
    override val state: StateFlow<ServiceState>
        get() = translationServiceImpl.state
    
    override val translationProgress: StateFlow<Map<Long, TranslationProgress>>
        get() = translationServiceImpl.translationProgress
    
    override val currentBookId: StateFlow<Long?>
        get() = translationServiceImpl.currentBookId
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var observerStarted = false
    
    companion object {
        const val ACTION_CANCEL_TRANSLATION = "ireader.action.CANCEL_TRANSLATION"
    }
    
    // Broadcast receiver for cancel action
    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CANCEL_TRANSLATION) {
                Log.info { "AndroidTranslationService: Cancel action received from notification" }
                serviceScope.launch {
                    cancelAll()
                }
            }
        }
    }
    
    private var receiverRegistered = false
    
    init {
        Log.info { "AndroidTranslationService: Created - instance: ${this.hashCode()}" }
        Log.info { "AndroidTranslationService: Will use TranslationServiceImpl: ${translationServiceImpl.hashCode()}" }
    }
    
    private fun registerCancelReceiver() {
        if (receiverRegistered) return
        try {
            val filter = IntentFilter(ACTION_CANCEL_TRANSLATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(cancelReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(cancelReceiver, filter)
            }
            receiverRegistered = true
            Log.info { "AndroidTranslationService: Cancel receiver registered" }
        } catch (e: Exception) {
            Log.error { "AndroidTranslationService: Failed to register cancel receiver: ${e.message}" }
        }
    }
    
    private fun unregisterCancelReceiver() {
        if (!receiverRegistered) return
        try {
            context.unregisterReceiver(cancelReceiver)
            receiverRegistered = false
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    private fun getCancelPendingIntent(): PendingIntent {
        val intent = Intent(ACTION_CANCEL_TRANSLATION).apply {
            setPackage(context.packageName)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
    
    private fun ensureObserverStarted() {
        if (observerStarted) return
        observerStarted = true
        
        Log.info { "AndroidTranslationService: Starting notification observer" }
        // Start observing translation progress for notifications
        serviceScope.launch {
            Log.info { "AndroidTranslationService: Started collecting translation progress" }
            translationServiceImpl.translationProgress.collectLatest { progressMap ->
                Log.info { "AndroidTranslationService: Progress update received, ${progressMap.size} items" }
                updateNotification(progressMap)
            }
        }
    }
    
    override suspend fun initialize() {
        translationServiceImpl.initialize()
    }
    
    private fun updateNotification(progressMap: Map<Long, TranslationProgress>) {
        if (progressMap.isEmpty()) {
            hideProgressNotification()
            unregisterCancelReceiver()
            return
        }
        
        val completed = progressMap.values.count { it.status == TranslationStatus.COMPLETED }
        val failed = progressMap.values.count { it.status == TranslationStatus.FAILED }
        val cancelled = progressMap.values.count { it.status == TranslationStatus.CANCELLED }
        val total = progressMap.size
        val current = progressMap.values.find { 
            it.status == TranslationStatus.TRANSLATING || 
            it.status == TranslationStatus.DOWNLOADING_CONTENT 
        }
        // For single chapter, also check QUEUED status to get chapter name
        val activeOrQueued = current ?: progressMap.values.find { 
            it.status == TranslationStatus.QUEUED 
        }
        val firstFailed = progressMap.values.find { it.status == TranslationStatus.FAILED }
        val isPaused = state.value == ServiceState.PAUSED
        
        // Get book title and chapter name from any progress item
        val bookTitle = progressMap.values.firstOrNull()?.bookName ?: "Unknown Book"
        val currentChapter = activeOrQueued?.chapterName ?: progressMap.values.firstOrNull()?.chapterName ?: ""
        
        // Check if all items are in terminal state (completed, failed, or cancelled)
        val allTerminal = progressMap.values.all { 
            it.status == TranslationStatus.COMPLETED || 
            it.status == TranslationStatus.FAILED ||
            it.status == TranslationStatus.CANCELLED
        }
        
        if (allTerminal) {
            // All done - hide progress notification
            hideProgressNotification()
            unregisterCancelReceiver()
            
            if (failed > 0) {
                showTranslationError(bookTitle, firstFailed?.errorMessage ?: "Translation failed for $failed chapters")
            } else if (completed > 0) {
                showTranslationCompleted(bookTitle, completed)
            }
        } else {
            // Still in progress - show progress notification with cancel button
            // Get chunk progress from current translating chapter
            val currentChunk = current?.currentChunk ?: 0
            val totalChunks = current?.totalChunks ?: 0
            val translatedParagraphs = current?.translatedParagraphs ?: 0
            val totalParagraphs = current?.totalParagraphs ?: 0
            
            showTranslationProgress(
                bookTitle = bookTitle,
                currentChapter = currentChapter,
                completed = completed,
                total = total,
                isPaused = isPaused,
                currentChunk = currentChunk,
                totalChunks = totalChunks,
                translatedParagraphs = translatedParagraphs,
                totalParagraphs = totalParagraphs
            )
        }
    }
    
    private fun showTranslationProgress(
        bookTitle: String,
        currentChapter: String,
        completed: Int,
        total: Int,
        isPaused: Boolean = false,
        currentChunk: Int = 0,
        totalChunks: Int = 0,
        translatedParagraphs: Int = 0,
        totalParagraphs: Int = 0
    ) {
        // Register cancel receiver if not already registered
        registerCancelReceiver()
        
        // Title format - put all info in title since some Android versions don't show subtitle
        // - Single chapter: "Translating 2/5" (chunk progress)
        // - Mass translation: "Translating 20/100 - ChapterName"
        val title = buildString {
            if (isPaused) {
                append("Paused")
            } else {
                append("Translating")
            }
            
            if (total > 1) {
                // Mass translation: show chapter progress and name
                append(" ${completed + 1}/$total")
                if (currentChapter.isNotEmpty()) {
                    append(" - $currentChapter")
                }
            } else {
                // Single chapter: show chunk progress in title
                if (totalChunks > 0) {
                    append(" $currentChunk/$totalChunks")
                }
            }
        }
        
        // Subtitle (contentText) - additional details
        val subtitle = buildString {
            if (total > 1) {
                // Mass translation: show book name and paragraph progress
                append(bookTitle)
                if (totalParagraphs > 0) {
                    append(" • $translatedParagraphs/$totalParagraphs paragraphs")
                }
            } else {
                // Single chapter: show paragraph progress
                if (totalParagraphs > 0) {
                    append("$translatedParagraphs/$totalParagraphs paragraphs")
                } else {
                    append("Processing...")
                }
            }
        }
        
        val notification = NotificationCompat.Builder(context, NotificationsIds.CHANNEL_TRANSLATION_PROGRESS)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText(if (total > 1) bookTitle else null)
            .setSmallIcon(ireader.i18n.R.drawable.ic_downloading)
            .setProgress(total, completed, false)
            .setOngoing(true)
            .setSilent(true)
            .setAutoCancel(false)
            .addAction(
                ireader.i18n.R.drawable.baseline_close_24,
                "Cancel",
                getCancelPendingIntent()
            )
            .build()
        
        try {
            notificationManager.notify(NotificationsIds.ID_TRANSLATION_PROGRESS, notification)
            Log.info { "AndroidTranslationService: Notification posted successfully" }
        } catch (e: SecurityException) {
            Log.error { "AndroidTranslationService: Failed to show notification - permission denied: ${e.message}" }
        }
    }
    
    private fun showTranslationCompleted(bookTitle: String, completedCount: Int) {
        val title = if (completedCount == 1) {
            "Translation Complete"
        } else {
            "Translation Complete - $completedCount chapters"
        }
        
        val notification = NotificationCompat.Builder(context, NotificationsIds.CHANNEL_TRANSLATION_COMPLETE)
            .setContentTitle(title)
            .setContentText(bookTitle)
            .setSmallIcon(ireader.i18n.R.drawable.ic_downloading)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(NotificationsIds.ID_TRANSLATION_COMPLETE, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
    
    private fun showTranslationError(bookTitle: String, errorMessage: String) {
        val notification = NotificationCompat.Builder(context, NotificationsIds.CHANNEL_TRANSLATION_ERROR)
            .setContentTitle("Translation Failed")
            .setContentText(bookTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$bookTitle\n$errorMessage"))
            .setSmallIcon(ireader.i18n.R.drawable.ic_downloading)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(NotificationsIds.ID_TRANSLATION_ERROR, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
    
    private fun hideProgressNotification() {
        notificationManager.cancel(NotificationsIds.ID_TRANSLATION_PROGRESS)
    }
    
    private fun hideAllNotifications() {
        notificationManager.cancel(NotificationsIds.ID_TRANSLATION_PROGRESS)
        notificationManager.cancel(NotificationsIds.ID_TRANSLATION_COMPLETE)
        notificationManager.cancel(NotificationsIds.ID_TRANSLATION_ERROR)
    }
    
    override suspend fun cleanup() {
        translationServiceImpl.cleanup()
        hideAllNotifications()
    }
    
    override suspend fun queueChapters(
        bookId: Long,
        chapterIds: List<Long>,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long,
        bypassWarning: Boolean,
        priority: Boolean
    ): ServiceResult<TranslationQueueResult> {
        Log.info { "AndroidTranslationService.queueChapters called - instance: ${this.hashCode()}" }
        
        // Ensure notification observer is started before queueing
        ensureObserverStarted()
        
        Log.info { "AndroidTranslationService: Queueing ${chapterIds.size} chapters for translation" }
        
        return translationServiceImpl.queueChapters(
            bookId = bookId,
            chapterIds = chapterIds,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            engineId = engineId,
            bypassWarning = bypassWarning,
            priority = priority
        )
    }
    
    override suspend fun pause() {
        translationServiceImpl.pause()
    }
    
    override suspend fun resume() {
        translationServiceImpl.resume()
    }
    
    override suspend fun cancelTranslation(chapterId: Long): ServiceResult<Unit> {
        return translationServiceImpl.cancelTranslation(chapterId)
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        Log.info { "AndroidTranslationService: Cancelling all translations" }
        val result = translationServiceImpl.cancelAll()
        hideProgressNotification()
        unregisterCancelReceiver()
        return result
    }
    
    override suspend fun retryTranslation(chapterId: Long): ServiceResult<Unit> {
        return translationServiceImpl.retryTranslation(chapterId)
    }
    
    override fun getTranslationStatus(chapterId: Long): TranslationStatus? {
        return translationServiceImpl.getTranslationStatus(chapterId)
    }
    
    override fun requiresRateLimiting(engineId: Long): Boolean {
        return translationServiceImpl.requiresRateLimiting(engineId)
    }
    
    override fun isOfflineEngine(engineId: Long): Boolean {
        return translationServiceImpl.isOfflineEngine(engineId)
    }
    
    override suspend fun start() {
        translationServiceImpl.start()
    }
    
    override suspend fun stop() {
        translationServiceImpl.stop()
    }
    
    override fun isRunning(): Boolean {
        return translationServiceImpl.isRunning()
    }
}
