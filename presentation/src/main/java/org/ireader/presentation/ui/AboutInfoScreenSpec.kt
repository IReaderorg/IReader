package org.ireader.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.ireader.about.AboutSettingScreen
import org.ireader.common_extensions.toDateTimestampString
import org.ireader.common_resources.BuildConfig
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object AboutInfoScreenSpec : ScreenSpec {

    override val navHostRoute: String = "about_screen_route"

    @OptIn(
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        AboutSettingScreen(
            onPopBackStack = {
                navController.popBackStack()
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


