package org.ireader.domain.feature_services.updater_service

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
import org.ireader.domain.feature_services.notification.Notifications.CHANNEL_APP_UPDATE
import org.ireader.domain.feature_services.notification.Notifications.ID_APP_UPDATER
import org.ireader.domain.feature_services.updater_service.models.Release
import org.ireader.domain.feature_services.updater_service.models.Version
import org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import org.ireader.infinity.feature_services.flags
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val preferences: PreferencesUseCase,
    private val api: UpdateApi,
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {

        if (preferences.readLastUpdateTime() < preferences.readLastUpdateTime() + TimeUnit.DAYS.toMillis(
                1)
        ) {
            return Result.success()
        }

        val release = api.checkRelease()

        val version = Version.create(release.tagName)

        BuildConfig.LIBRARY_PACKAGE_NAME
        val versionCode: String =
            try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "1.0"
            }
        val current = Version.create(versionCode)

        if (Version.isNewVersion(release.tagName, versionCode)) {
            preferences.setLastUpdateTime(Date().time)
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
        Intent(Intent.ACTION_VIEW, release.htmlUrl.toUri()),
        flags
    )

    private val Version.simpleText: String
        get() = "v${version}"

}