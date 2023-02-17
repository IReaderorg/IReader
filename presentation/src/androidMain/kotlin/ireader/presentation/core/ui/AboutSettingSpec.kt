package ireader.presentation.core.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.settings.about.AboutSettingScreen
import ireader.domain.utils.extensions.toDateTimestampString
import ireader.i18n.BuildConfig
import ireader.presentation.R
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object AboutSettingSpec : ScreenSpec {

    override val navHostRoute: String = "about_screen_route"

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = stringResource(R.string.about),
                    navController = controller.navController,
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            AboutSettingScreen(
                modifier = Modifier.padding(padding),
                onPopBackStack = {
                    controller.navController.popBackStack()
                },
                getFormattedBuildTime = this::getFormattedBuildTime,
            )
        }

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
