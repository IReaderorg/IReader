package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.utils.extensions.toDateTimestampString
import ireader.i18n.BuildKonfig
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.about.AboutSettingScreen
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AboutSettingSpec : VoyagerScreen() {

    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localize(MR.strings.about),
                    scrollBehavior = scrollBehavior,
                    popBackStack = {
                        navigator.pop()
                    }
                )
            }
        ) { padding ->
            AboutSettingScreen(
                modifier = Modifier.padding(padding),
                onPopBackStack = {
                    navigator.pop()
                },
                getFormattedBuildTime = this::getFormattedBuildTime,
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
