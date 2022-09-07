package ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.ui.component.Controller
import ireader.ui.about.AboutSettingScreen
import ireader.common.extensions.toDateTimestampString
import ireader.common.resources.BuildConfig
import ireader.ui.component.components.TitleToolbar
import ireader.ui.settings.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object AboutSettingSpec : ScreenSpec {

    override val navHostRoute: String = "about_screen_route"
    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.about),
            navController = controller.navController,
            scrollBehavior = controller.scrollBehavior
        )
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        AboutSettingScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            getFormattedBuildTime = this::getFormattedBuildTime,
        )
    }
    private fun getFormattedBuildTime(): String {
        return try {
            val inputDf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)
            inputDf.timeZone = TimeZone.getTimeZone("UTC")
            val buildTime = inputDf.parse(BuildConfig.BUILD_TIME)

            val outputDf = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT,
                Locale.getDefault(),
            )
            outputDf.timeZone = TimeZone.getDefault()

            buildTime!!.toDateTimestampString(DateFormat.getDateInstance())
        } catch (e: Exception) {
            BuildConfig.BUILD_TIME
        }
    }
}
