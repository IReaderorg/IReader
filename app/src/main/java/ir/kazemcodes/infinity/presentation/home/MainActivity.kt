package ir.kazemcodes.infinity.presentation.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackcomposeintegration.core.BackstackProvider
import com.zhuinden.simplestackcomposeintegration.core.ComposeStateChanger
import com.zhuinden.simplestackextensions.navigatorktx.androidContentFrame
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import ir.kazemcodes.infinity.api_feature.network.InfinityInstance
import ir.kazemcodes.infinity.api_feature.network.NetworkHelper
import ir.kazemcodes.infinity.base_feature.navigation.MainScreenKey
import ir.kazemcodes.infinity.presentation.theme.InfinityTheme


@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity() {


    private val composeStateChanger = ComposeStateChanger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backstack = Navigator.configure()
            .setScopedServices(DefaultServiceProvider())
            .setStateChanger(AsyncStateChanger(composeStateChanger))
            .install(this, androidContentFrame, History.of(MainScreenKey()))

        InfinityInstance.networkHelper = NetworkHelper(this)

        setContent {
            BackstackProvider(backstack) {
                InfinityTheme {
                    composeStateChanger.RenderScreen()
                }

            }
        }
    }

    override fun onBackPressed() {
        if (!Navigator.onBackPressed(this)) {
            super.onBackPressed()
        }
    }




}
