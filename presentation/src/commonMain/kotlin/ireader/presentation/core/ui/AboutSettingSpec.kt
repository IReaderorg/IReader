package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.utils.extensions.toDateTimestampString
import ireader.i18n.BuildKonfig
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.about.AboutSettingScreen
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AboutSettingSpec {

    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localize(Res.string.about),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navController.popBackStack()
                    }
                )
            }
        ) { padding ->
            AboutSettingScreen(
                modifier = Modifier.padding(padding),
                onPopBackStack = {
                    navController.popBackStack()
                },
                getFormattedBuildTime = this::getFormattedBuildTime,
                onNavigateToChangelog = {
                    navController.navigate(NavigationRoutes.changelog)
                }
            )
        }

    }
    private fun getFormattedBuildTime(): String {
        return try {
            val inputDf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)
            inputDf.timeZone = TimeZone.getTimeZone("UTC")
            val buildTime = inputDf.parse(BuildKonfig.BUILD_TIME)

            val outputDf = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT,
                Locale.getDefault(),
            )
            outputDf.timeZone = TimeZone.getDefault()

            buildTime!!.toDateTimestampString(DateFormat.getDateInstance())
        } catch (e: Exception) {
            BuildKonfig.BUILD_TIME
        }
    }
}
