package org.ireader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.backup.AutomaticBackup
import ireader.presentation.ScreenContent
import ireader.presentation.theme.AppTheme
import org.ireader.app.initiators.AppInitializers
import org.ireader.app.initiators.SecureActivityDelegateImpl
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity(), SecureActivityDelegate by SecureActivityDelegateImpl() {

    private val uiPreferences: UiPreferences by inject()
    private val automaticBackup: AutomaticBackup = get(parameters = { org.koin.core.parameter.parametersOf(this@MainActivity) })
    val initializers: AppInitializers = get<AppInitializers>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSecureActivity(this, uiPreferences)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        setContent {
            AppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,

                    ) {
                    ScreenContent()
                }
            }
        }
    }
}

interface SecureActivityDelegate {
    fun registerSecureActivity(activity: ComponentActivity, preferences: UiPreferences)
}
