package ir.kazemcodes.infinity.base_feature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.kazemcodes.infinity.base_feature.navigation.SetupNavigation
import ir.kazemcodes.infinity.base_feature.theme.InfinityTheme
import ir.kazemcodes.infinity.explore_feature.domain.repository.SharedViewModel


@AndroidEntryPoint
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var navController: NavHostController

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InfinityTheme {
                navController = rememberAnimatedNavController()
                SetupNavigation(
                    navController = navController
                )
            }
        }
    }
}
