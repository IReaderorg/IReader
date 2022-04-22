package org.ireader.domain.services.update_service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.ireader.core.BuildConfig
import org.ireader.domain.R
import org.ireader.domain.models.update_service_models.Release
import org.ireader.domain.models.update_service_models.Version
import org.ireader.domain.notification.Notifications.CHANNEL_APP_UPDATE
import org.ireader.domain.notification.Notifications.ID_APP_UPDATER
import org.ireader.domain.notification.flags
import org.ireader.domain.use_cases.preferences.services.LastUpdateTime
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltWorker
class UpdateService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val lastUpdateTime: LastUpdateTime,
    private val api: UpdateApi,
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {
        val last = lastUpdateTime.read()
        val time = lastUpdateTime.read() + 1.toDuration(DurationUnit.HOURS).inWholeMilliseconds

        if (last < time) {
            return Result.success()
        }

        val release = api.checkRelease()

        val version = Version.create(release.tag_name)

        BuildConfig.LIBRARY_PACKAGE_NAME
        val versionCode: String =
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Throwable) {
                "1.0"
            }
        val current = Version.create(versionCode)

        if (Version.isNewVersion(release.tag_name, versionCode)) {
            lastUpdateTime.save(Calendar.getInstance().timeInMillis)
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(ID_APP_UPDATER, createNotification(current, version, createIntent(release)))
            }
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
        flags
    )

    private val Version.simpleText: String
        get() = "v${version}"

}