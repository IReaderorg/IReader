package ireader.domain.services.update_service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.domain.models.update_service_models.Release
import ireader.domain.models.update_service_models.Version
import ireader.domain.notification.NotificationsIds.CHANNEL_APP_UPDATE
import ireader.domain.notification.NotificationsIds.ID_APP_UPDATER
import ireader.domain.notification.legacyFlags
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.utils.NotificationManager
import ireader.i18n.R
import kotlin.time.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class UpdateService constructor(
    private val context: Context,
    params: WorkerParameters,

    ) : CoroutineWorker(context, params), KoinComponent {
    private val appPreferences: AppPreferences by inject()
    private val api: UpdateApi by inject()
    private val notificationManager: NotificationManager by inject()
    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        return try {
            val lastCheck = Instant.fromEpochMilliseconds(appPreferences.lastUpdateCheck().get())
            val now = kotlin.time.Clock.System.now()

            // Skip check if not enough time has passed (except in debug/preview builds)
            val isDebugOrPreview = ireader.i18n.BuildKonfig.DEBUG || ireader.i18n.BuildKonfig.PREVIEW
            if (!isDebugOrPreview && now - lastCheck < minTimeUpdateCheck) {
                return Result.success()
            }

            val release = api.checkRelease()

            // Skip if tag_name is null or empty
            if (release.tag_name.isNullOrBlank()) {
                return Result.success()
            }

            val versionCode: String = ireader.i18n.BuildKonfig.VERSION_NAME
            
            // Check if new version is available
            if (Version.isNewVersion(release.tag_name, versionCode)) {
                val current = Version.create(versionCode)
                val newVersion = Version.create(release.tag_name)
                
                // Update last check time before showing notification
                appPreferences.lastUpdateCheck().set(now.toEpochMilliseconds())
                
                notificationManager.show(
                    ID_APP_UPDATER, 
                    createNotification(current, newVersion, createIntent(release))
                )
            } else {
                // Still update last check time even if no update available
                appPreferences.lastUpdateCheck().set(now.toEpochMilliseconds())
            }

            Result.success()
        } catch (e: Exception) {
            // Log error and retry later
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun createNotification(old: Version, new: Version, intent: PendingIntent) =
        NotificationCompat.Builder(context, CHANNEL_APP_UPDATE)
            .setSmallIcon(R.drawable.ic_infinity)
            .setContentTitle("Update available - ${new.simpleText}")
            .setContentText("Download new version to update from ${old.simpleText}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .build()

    private fun createIntent(release: Release) = PendingIntent.getActivity(
        context.applicationContext,
        release.hashCode(),
        Intent(Intent.ACTION_VIEW, release.html_url.toUri()),
        legacyFlags
    )

    private val Version.simpleText: String
        get() = "v$version"

    internal companion object {
        val minTimeUpdateCheck = 6.toDuration(DurationUnit.HOURS)
    }
}
