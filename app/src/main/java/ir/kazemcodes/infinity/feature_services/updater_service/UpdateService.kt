package ir.kazemcodes.infinity.feature_services.updater_service

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
import ir.kazemcodes.infinity.BuildConfig
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications.CHANNEL_APP_UPDATE
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications.ID_APP_UPDATER
import ir.kazemcodes.infinity.feature_services.flags
import ir.kazemcodes.infinity.feature_services.updater_service.models.Release
import ir.kazemcodes.infinity.feature_services.updater_service.models.Version
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val api: UpdateApi
) : CoroutineWorker(context, params),KoinComponent {

    private val preferences: PreferencesUseCase by inject()

    override suspend fun doWork(): Result {

        if (preferences.readLastUpdateTime() <  preferences.readLastUpdateTime() + TimeUnit.DAYS.toMillis(1)) {
            return Result.success()
        }

        val release = api.checkRelease()

        val version = Version.create(release.tagName)

        val current = Version.create(BuildConfig.VERSION_NAME)

        if (Version.isNewVersion(release.tagName)) {
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
        context,
        release.hashCode(),
        Intent(Intent.ACTION_VIEW, release.htmlUrl.toUri()),
        flags
    )

    private val Version.simpleText: String
        get() = "v${version}"

}