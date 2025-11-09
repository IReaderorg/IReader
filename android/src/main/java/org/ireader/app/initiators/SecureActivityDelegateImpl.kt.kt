package org.ireader.app.initiators

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.AuthenticatorUtil.isAuthenticationSupported
import ireader.domain.utils.extensions.setSecureScreen
import ireader.presentation.core.ui.UnlockActivity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ireader.app.SecureActivityDelegate
import kotlin.time.ExperimentalTime


class SecureActivityDelegateImpl : SecureActivityDelegate, DefaultLifecycleObserver {

    private lateinit var activity: ComponentActivity
    private lateinit var preferences: UiPreferences

    override fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences,initializers: AppInitializers) {
        this.activity = activity
        this.preferences = preferences
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        setSecureScreen()
    }

    override fun onResume(owner: LifecycleOwner) {
        setAppLock()
    }

    private fun setSecureScreen() {
        val secureScreenFlow = preferences.secureScreen().changes()
        val incognitoModeFlow = preferences.incognitoMode().changes()
        val secureScreenEnabledFlow = preferences.secureScreenEnabled().changes()
        combine(secureScreenFlow, incognitoModeFlow, secureScreenEnabledFlow) { secureScreen, incognitoMode, secureScreenEnabled ->
            secureScreenEnabled ||
            secureScreen == PreferenceValues.SecureScreenMode.ALWAYS ||
            secureScreen == PreferenceValues.SecureScreenMode.INCOGNITO && incognitoMode
        }
            .onEach { activity.window.setSecureScreen(it) }
            .launchIn(activity.lifecycleScope)
    }

    private fun setAppLock() {
        if (!preferences.useAuthenticator().get()) return
        if (activity.isAuthenticationSupported()) {
            if (!isAppLocked()) return
            activity.startActivity(Intent(activity, UnlockActivity::class.java))
            activity.overridePendingTransition(0, 0)
        } else {
            preferences.useAuthenticator().set(false)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun isAppLocked(): Boolean {
        if (!preferences.isAppLocked) return false
        return (preferences.lockAppAfter().get() <= 0) ||
            kotlin.time.Clock.System.now().toEpochMilliseconds() >= (preferences.lastAppUnlock().get() + 60 * 1000 * preferences.lockAppAfter().get())
    }
}
