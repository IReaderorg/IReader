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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class UpdateService constructor(
    private val context: Context,
    params: WorkerParameters,

    ) : CoroutineWorker(context, params), KoinComponent {
    private val appPreferences: AppPreferences by inject()
    private val api: UpdateApi by inject()
    private val notificationManager: NotificationManager by inject()
    override suspend fun doWork(): Result {
        val lastCheck = Instant.fromEpochMilliseconds(appPreferences.lastUpdateCheck().get())
        val now = Clock.System.now()

        if ((!ireader.i18n.BuildKonfig.DEBUG || !ireader.i18n.BuildKonfig.PREVIEW) && now - lastCheck < minTimeUpdateCheck) {
            return Result.success()
        }

        val release = api.checkRelease()

        val version = Version.create(release.tag_name)


        val versionCode: String = ireader.i18n.BuildKonfig.VERSION_NAME
        val current = Version.create(versionCode)

        if (Version.isNewVersion(release.tag_name, versionCode)) {
            appPreferences.lastUpdateCheck().set(Clock.System.now().toEpochMilliseconds())
            notificationManager.show(ID_APP_UPDATER, createNotification(current, version, createIntent(release)))

        }

        return Result.success()
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
        val minTimeUpdateCheck = 1.toDuration(DurationUnit.HOURS)
    }
}
