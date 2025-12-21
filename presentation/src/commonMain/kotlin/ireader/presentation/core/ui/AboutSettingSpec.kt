package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.utils.extensions.formatDateTime
import ireader.i18n.BuildKonfig
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.about
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.about.AboutSettingScreen
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

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
                        navController.safePopBackStack()
                    }
                )
            }
        ) { padding ->
            AboutSettingScreen(
                modifier = Modifier.padding(padding),
                onPopBackStack = {
                    navController.safePopBackStack()
                },
                getFormattedBuildTime = this::getFormattedBuildTime,
                onNavigateToChangelog = {
                    navController.navigate(NavigationRoutes.changelog)
                }
            )
        }

    }
    
    @OptIn(ExperimentalTime::class)
    private fun getFormattedBuildTime(): String {
        return try {
            // Parse ISO 8601 format: yyyy-MM-dd'T'HH:mm'Z'
            val buildTimeStr = BuildKonfig.BUILD_TIME
                .replace("Z", "")
                .replace("z", "")
            
            val dateTime = LocalDateTime.parse(buildTimeStr)
            val instant = dateTime.toInstant(TimeZone.UTC)
            instant.toEpochMilliseconds().formatDateTime()
        } catch (e: Exception) {
            BuildKonfig.BUILD_TIME
        }
    }
}
