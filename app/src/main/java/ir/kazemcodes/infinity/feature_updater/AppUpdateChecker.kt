package ir.kazemcodes.infinity.feature_updater

import android.content.Context
import ir.kazemcodes.infinity.BuildConfig
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.core.data.network.utils.withIOContext
import ir.kazemcodes.infinity.core.data.repository.PreferencesHelper
import ir.kazemcodes.infinity.feature_sources.sources.utils.NetworkHelper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response
import ru.gildor.coroutines.okhttp.await
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.fullType
import uy.kohesive.injekt.injectLazy
import java.util.*
import java.util.concurrent.TimeUnit

class AppUpdateChecker {

    private val networkService: NetworkHelper by injectLazy()
    private val preferences: PreferencesHelper by injectLazy()

    suspend fun checkForUpdate(context: Context, isUserPrompt: Boolean = false): AppUpdateResult {
        // Limit checks to once a day at most
        if (isUserPrompt.not() && Date().time < preferences.lastUpdateCheck.get() + TimeUnit.DAYS.toMillis(1)) {
            return AppUpdateResult.NoNewUpdate
        }

        return withIOContext {
            val result = networkService.client
                .newCall(GET("https://api.github.com/repos/$GITHUB_REPO/releases/latest"))
                .await()
                .parseAs<GithubRelease>()
                .let {
                    preferences.lastUpdateCheck.set(Date().time)

                    // Check if latest version is different from current version
                    if (isNewVersion(it.version)) {
                        AppUpdateResult.NewUpdate(it)
                    } else {
                        AppUpdateResult.NoNewUpdate
                    }
                }

            if (result is AppUpdateResult.NewUpdate) {
                AppUpdateNotifier(context).promptUpdate(result.release)
            }

            result
        }
    }

    private fun isNewVersion(versionTag: String): Boolean {
        val newVersion = versionTag.replace("[^\\d.]".toRegex(), "")
            return newVersion != BuildConfig.VERSION_NAME
    }
}

val GITHUB_REPO: String by lazy {
        "kazemcodes/Infinity"
}

val RELEASE_TAG: String by lazy {
        "v${BuildConfig.VERSION_NAME}"
}

val RELEASE_URL = "https://github.com/$GITHUB_REPO/releases/tag/$RELEASE_TAG"

inline fun <reified T> Response.parseAs(): T {
    // Avoiding Injekt.get<Json>() due to compiler issues
    val json = Injekt.getInstance<Json>(fullType<Json>().type)
    this.use {
        val responseBody = it.body?.string().orEmpty()
        return json.decodeFromString(responseBody)
    }
}