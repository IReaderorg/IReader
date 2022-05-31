package org.ireader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import org.ireader.app.initiators.SecureActivityDelegateImpl
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.domain.use_cases.backup.BackUpUseCases
import org.ireader.presentation.ScreenContent
import org.ireader.presentation.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity(),SecureActivityDelegate by SecureActivityDelegateImpl() {

    @Inject
    lateinit var backUpUseCases: BackUpUseCases
    @Inject lateinit var uiPreferences: UiPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSecureActivity(this,uiPreferences)

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
    fun registerSecureActivity(activity: ComponentActivity,preferences:UiPreferences)

}





