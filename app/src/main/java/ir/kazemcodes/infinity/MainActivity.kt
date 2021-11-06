package ir.kazemcodes.infinity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.kazemcodes.infinity.presentation.screen.home_screen.HomeScreen
import ir.kazemcodes.infinity.presentation.theme.InfinityTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
          InfinityTheme {
                HomeScreen(navController = navController)
            }
        }
    }
}
