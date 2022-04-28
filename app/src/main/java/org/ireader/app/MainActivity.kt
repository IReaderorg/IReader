package org.ireader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import org.ireader.core_api.log.Log
import org.ireader.presentation.ScreenContent
import org.ireader.presentation.ui.AppTheme


@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        Log.error { "CLass localClassName " + this.localClassName }

        setContent {
            AppTheme {
                Surface(
                    color = MaterialTheme.colors.background,
                ) {
                    ScreenContent()
                }
            }
        }
    }

}

