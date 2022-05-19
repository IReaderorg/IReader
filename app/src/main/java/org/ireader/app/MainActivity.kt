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
import org.ireader.domain.use_cases.backup.BackUpUseCases
import org.ireader.presentation.ScreenContent
import org.ireader.presentation.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var backUpUseCases: BackUpUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
