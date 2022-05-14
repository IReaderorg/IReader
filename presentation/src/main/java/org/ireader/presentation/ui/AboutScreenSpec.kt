package org.ireader.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.ireader.about.AboutSettingScreen
import org.ireader.common_extensions.toDateTimestampString
import org.ireader.common_resources.BuildConfig
import org.ireader.components.components.TitleToolbar
import org.ireader.ui_settings.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object AboutScreenSpec : ScreenSpec {

    override val navHostRoute: String = "about_screen_route"
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState
    ) {
        TitleToolbar(
            title = stringResource(R.string.about),
            navController = navController
        )
    }



    @OptIn(
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding:PaddingValues,
        sheetState: ModalBottomSheetState
    ) {
        AboutSettingScreen(
            modifier = Modifier.padding(scaffoldPadding),
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


