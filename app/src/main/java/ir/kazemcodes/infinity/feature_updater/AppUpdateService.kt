package ir.kazemcodes.infinity.feature_updater

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import ir.kazemcodes.infinity.BuildConfig

import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.core.data.network.utils.acquireWakeLock
import ir.kazemcodes.infinity.core.data.network.utils.isServiceRunning
import ir.kazemcodes.infinity.core.data.network.utils.launchIO
import ir.kazemcodes.infinity.core.utils.getUriCompat
import ir.kazemcodes.infinity.core.utils.saveTo
import ir.kazemcodes.infinity.di.newCallWithProgress
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications
import ir.kazemcodes.infinity.feature_sources.sources.utils.NetworkHelper
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import uy.kohesive.injekt.injectLazy
import java.io.File

class AppUpdateService : Service() {

    private val network: NetworkHelper by injectLazy()

    /**
     * Wake lock that will be held until the service is destroyed.
     */
    private lateinit var wakeLock: PowerManager.WakeLock

    private lateinit var notifier: AppUpdateNotifier

    override fun onCreate() {
        super.onCreate()

        notifier = AppUpdateNotifier(this)
        wakeLock = acquireWakeLock(javaClass.name)

        startForeground(Notifications.ID_APP_UPDATER, notifier.onDownloadStarted().build())
    }

    /**
     * This method needs to be implemented, but it's not used/needed.
     */
    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val url = intent.getStringExtra(EXTRA_DOWNLOAD_URL) ?: return START_NOT_STICKY
        val title = intent.getStringExtra(EXTRA_DOWNLOAD_TITLE) ?: getString(R.string.app_name)

        launchIO {
            downloadApk(title, url)
        }

        stopSelf(startId)
        return START_NOT_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        destroyJob()
        return super.stopService(name)
    }

    override fun onDestroy() {
        destroyJob()
        super.onDestroy()
    }

    private fun destroyJob() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    /**
     * Called to start downloading apk of new update
     *
     * @param url url location of file
     */
    private suspend fun downloadApk(title: String, url: String) {
        // Show notification download starting.
        notifier.onDownloadStarted(title)

        val progressListener = object : ProgressListener {
            // Progress of the download
            var savedProgress = 0

            // Keep track of the last notification sent to avoid posting too many.
            var lastTick = 0L

            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                val progress = (100 * (bytesRead.toFloat() / contentLength)).toInt()
                val currentTime = System.currentTimeMillis()
                if (progress > savedProgress && currentTime - 200 > lastTick) {
                    savedProgress = progress
                    lastTick = currentTime
                    notifier.onProgressChange(progress)
                }
            }
        }

        try {
            // Download the new update.
            val response = network.client.newCallWithProgress(GET(url), progressListener).await()

            // File where the apk will be saved.
            val apkFile = File(externalCacheDir, "update.apk")

            if (response.isSuccessful) {
                response.body!!.source().saveTo(apkFile)
            } else {
                response.close()
                throw Exception("Unsuccessful response")
            }
            notifier.onDownloadFinished(apkFile.getUriCompat(this))
        } catch (error: Exception) {
            Timber.e(error)
            notifier.onDownloadError(url)
        }
    }

    companion object {

        internal const val EXTRA_DOWNLOAD_URL = "${BuildConfig.APPLICATION_ID}.UpdaterService.DOWNLOAD_URL"
        internal const val EXTRA_DOWNLOAD_TITLE = "${BuildConfig.APPLICATION_ID}.UpdaterService.DOWNLOAD_TITLE"

        /**
         * Returns the status of the service.
         *
         * @param context the application context.
         * @return true if the service is running, false otherwise.
         */
        private fun isRunning(context: Context): Boolean =
            context.isServiceRunning(AppUpdateService::class.java)

        /**
         * Downloads a new update and let the user install the new version from a notification.
         *
         * @param context the application context.
         * @param url the url to the new update.
         */
        fun start(context: Context, url: String, title: String = context.getString(R.string.app_name)) {
            if (!isRunning(context)) {
                val intent = Intent(context, AppUpdateService::class.java).apply {
                    putExtra(EXTRA_DOWNLOAD_TITLE, title)
                    putExtra(EXTRA_DOWNLOAD_URL, url)
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }

        /**
         * Returns [PendingIntent] that starts a service which downloads the apk specified in url.
         *
         * @param url the url to the new update.
         * @return [PendingIntent]
         */
        internal fun downloadApkPendingService(context: Context, url: String): PendingIntent {
            val intent = Intent(context, AppUpdateService::class.java).apply {
                putExtra(EXTRA_DOWNLOAD_URL, url)
            }
            return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}
