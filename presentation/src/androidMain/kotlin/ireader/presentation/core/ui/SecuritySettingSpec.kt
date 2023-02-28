package ireader.presentation.core.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.log.Log
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.AuthenticatorUtil
import ireader.domain.utils.extensions.AuthenticatorUtil.isAuthenticationSupported
import ireader.domain.utils.extensions.AuthenticatorUtil.startAuthentication
import ireader.i18n.R
import ireader.i18n.localize
import ireader.i18n.localizePlural
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.component.components.component.ChoicePreference
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

class SecuritySettingSpec : VoyagerScreen() {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: SecuritySettingViewModel = getIViewModel()
        val context = LocalContext.current
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        val navigator = LocalNavigator.currentOrThrow

        val onIdleAfter =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
                when (resultIntent.resultCode) {
                    UnlockActivity.ERROR -> {}
                    UnlockActivity.SUCCESS -> {
                        resultIntent.data?.extras?.getLong(UnlockActivity.IDLE_AFTER)?.let {
                            vm.lockAppAfter.value = it
                        }
                    }
                }
            }
        val onEnableAuthResult =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
                when (resultIntent.resultCode) {
                    UnlockActivity.ERROR -> {
                    }
                    UnlockActivity.SUCCESS -> {
                        resultIntent.data?.extras?.getBoolean(UnlockActivity.ENABLE_AUTH)?.let {
                            vm.useAuth.value = it
                        }
                    }
                }
            }
        val items = remember {
            listOf<Components>(
                Components.Switch(
                    preference = vm.useAuth,
                    title = localizeHelper.localize(MR.strings.use_auth),
                    visible = context.isAuthenticationSupported(),
                    onValue = {
                        onEnableAuthResult.launch(
                            Intent(
                                context,
                                UnlockActivity::class.java
                            )
                                .putExtra(UnlockActivity.ENABLE_AUTH, !vm.useAuth.value)
                                .putExtra(
                                    UnlockActivity.TITLE,
                                    localizeHelper.localize(MR.strings.authentication)
                                )
                                .putExtra(
                                    UnlockActivity.SUBTITLE,
                                    localizeHelper.localize(MR.strings.authenticate_to_confirm_change)
                                )
                        )
                    }
                ),
                Components.Dynamic {
                    if (vm.useAuth.value) {
                        val values = arrayOf(0L, 1L, 2L, 5L, 10L, -1L)
                        ChoicePreference<Long>(
                            preference = vm.lockAppAfter,
                            choices = values.associate { text ->
                                when (text) {
                                    -1L -> -1L to localizeHelper.localize(MR.strings.lock_never)
                                    0L -> 0L to localizeHelper.localize(MR.strings.lock_always)
                                    else -> text.toLong() to localizePlural(MR.plurals.lock_after_mins, text.toInt())
                                }
                            },
                            onValue = { newValue ->
                                if (vm.lockAppAfter.value == newValue) return@ChoicePreference
                                onIdleAfter.launch(
                                    Intent(
                                        context,
                                        UnlockActivity::class.java
                                    )
                                        .putExtra(UnlockActivity.IDLE_AFTER, newValue)
                                        .putExtra(
                                            UnlockActivity.TITLE,
                                            localizeHelper.localize(MR.strings.lock_when_idle)
                                        )
                                        .putExtra(
                                            UnlockActivity.SUBTITLE,
                                            localizeHelper.localize(MR.strings.authenticate_to_confirm_change)
                                        )
                                )
                            },
                            title = stringResource(
                                id = R.string.lock_when_idle
                            )
                        )
                    }
                },
                Components.Dynamic {
                    ChoicePreference<PreferenceValues.SecureScreenMode>(
                        preference = vm.secureScreen,
                        choices = mapOf(
                            PreferenceValues.SecureScreenMode.ALWAYS to stringResource(id = PreferenceValues.SecureScreenMode.ALWAYS.titleResId),
                            PreferenceValues.SecureScreenMode.INCOGNITO to stringResource(id = PreferenceValues.SecureScreenMode.INCOGNITO.titleResId),
                            PreferenceValues.SecureScreenMode.NEVER to stringResource(id = PreferenceValues.SecureScreenMode.NEVER.titleResId)
                        ),
                        title = localize(
                            MR.strings.secure_screen
                        )
                    )
                },

            )
        }
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localize(MR.strings.security),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        popBackStack(navigator)
                    }
                )
            }
        ) { padding ->
            SetupSettingComponents(
                scaffoldPadding = padding,
                items = items,
            )
        }

    }
}

/**
 * Blank activity with a BiometricPrompt.
 */

class UnlockActivity : FragmentActivity(),DIAware {

    override val di: DI by closestDI()

    val appPreferences: UiPreferences by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.extras?.getString(TITLE)
        val subtitle = intent.extras?.getString(SUBTITLE)
        this.startAuthentication(
            title = title ?: getString(R.string.unlock_app),
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
                        appPreferences.lastAppUnlock().set(Clock.System.now().toEpochMilliseconds())
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


class SecuritySettingViewModel(
    private val appPreferences: UiPreferences,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    var useAuth = appPreferences.useAuthenticator().asState()
    var secureScreen = appPreferences.secureScreen().asState()
    var lockAppAfter = appPreferences.lockAppAfter().asState()
}
