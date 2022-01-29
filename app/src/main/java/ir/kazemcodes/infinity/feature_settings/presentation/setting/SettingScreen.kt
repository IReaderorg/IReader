package ir.kazemcodes.infinity.feature_settings.presentation.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.feature_activity.presentation.Screen

@Composable
fun SettingScreen(modifier: Modifier = Modifier, navController: NavController = rememberNavController()) {
    val settingItems = listOf(
        //SettingItems.Downloads,
        //SettingItems.ExtensionCreator,
        SettingItems.Appearance,
        SettingItems.DnsOverHttp,
        SettingItems.About,
    )
    Box(modifier
        .fillMaxSize()
        .padding(bottom = 50.dp)) {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    TopAppBarTitle(title = "Setting")
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
            )
        }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
            ) {

                settingItems.forEach { item ->
                    SettingsItem(title = item.title,
                        imageVector = item.icon,
                        destinationScreenRoute = item.route,
                        navController = navController

                    )
                }

            }

        }
    }

}

sealed class SettingItems(
    val title: String,
    val icon: ImageVector,
    val route: String,
) {
    object Downloads : SettingItems("Downloads", Icons.Default.Download, Screen.Downloader.route)
    object ExtensionCreator :
        SettingItems("ExtensionCreator", Icons.Default.Extension, Screen.ExtensionCreator.route)

    object Appearance :
        SettingItems("Appearance", Icons.Default.Palette, Screen.AppearanceSetting.route)

    object DnsOverHttp :
        SettingItems("DnsOverHttp", Icons.Default.Dns, Screen.DnsOverHttpSetting.route)

    object About : SettingItems("About", Icons.Default.Info, Screen.AboutSetting.route)
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    imageVector: ImageVector,
    navController: NavController = rememberNavController(),
    destinationScreenRoute: String,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
            .clickable(interactionSource = interactionSource,
                indication = null) { navController.navigate(destinationScreenRoute) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = imageVector,
            contentDescription = "$title icon",
            tint = MaterialTheme.colors.primary)
        Spacer(modifier = modifier.width(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )


    }

}