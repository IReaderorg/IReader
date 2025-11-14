package ireader.presentation.core.ui

import android.os.Bundle
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import ireader.core.log.Log
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.AuthenticatorUtil
import ireader.domain.utils.extensions.AuthenticatorUtil.startAuthentication
import ireader.i18n.LocalizeHelper
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.security.SecuritySettingsScreen
import ireader.presentation.ui.settings.security.SecuritySettingsViewModel
import org.koin.android.ext.android.get
import kotlin.time.ExperimentalTime

actual class SecuritySettingSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual fun Content() {
        val vm: SecuritySettingsViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }

        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localize(Res.string.security),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
        ) { padding ->
            SecuritySettingsScreen(
                vm = vm,
                padding = padding
            )
        }
    }
}

/**
 * Blank activity with a BiometricPrompt.
 */

class UnlockActivity : FragmentActivity() {

    val appPreferences: UiPreferences = get<UiPreferences>()
    val localizeHelper: LocalizeHelper = get<LocalizeHelper>()

    @OptIn(ExperimentalTime::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.extras?.getString(TITLE)
        val subtitle = intent.extras?.getString(SUBTITLE)
        this.startAuthentication(
            title = title ?: localizeHelper.localize(Res.string.unlock_app),
            subtitle = subtitle,
            confirmationRequired = false,
            callback = object : AuthenticatorUtil.AuthenticationCallback() {
                override fun onAuthenticationError(
                    activity: FragmentActivity?,
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    super.onAuthenticationError(activity, errorCode, errString)
                    Log.error { errString.toString() }
                    setResult(ERROR)
                    finishAffinity()
                }

                override fun onAuthenticationSucceeded(
                    activity: FragmentActivity?,
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(activity, result)
                    kotlin.runCatching {
                        appPreferences.isAppLocked = false
                        appPreferences.lastAppUnlock().set(kotlin.time.Clock.System.now().toEpochMilliseconds())
                    }
                    setResult(SUCCESS, intent)
                    finish()
                }
            },
        )
    }

    companion object {
        const val TITLE = "title"
        const val SUBTITLE = "subtitle"
        const val ERROR = 0
        const val SUCCESS = 1
        const val IDLE_AFTER = "idle_after"
        const val ENABLE_AUTH = "enable_auth"
    }
}


