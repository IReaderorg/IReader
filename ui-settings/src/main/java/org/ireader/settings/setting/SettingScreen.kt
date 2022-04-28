package org.ireader.settings.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.ireader.core_ui.ui_components.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.Toolbar

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    itemsRoutes: List<String>
) {
    val settingItems = listOf(
        SettingItems.Downloads(itemsRoutes[0]),
        SettingItems.Appearance(itemsRoutes[1]),
        SettingItems.DnsOverHttp(itemsRoutes[2]),
        SettingItems.About(itemsRoutes[3]),
    )
    val a by remember {
        mutableStateOf("")
    }
    Box(modifier
        .fillMaxSize()
        .padding(bottom = 50.dp)) {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            Toolbar(
                title = {
                    BigSizeTextComposable(text = "Setting")
                },
            )
        }) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
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
    data class Downloads( val navRoute: String) :
        SettingItems("Downloads", Icons.Default.Download, navRoute)

    data class Appearance(val navRoute: String) :
        SettingItems("Appearance", Icons.Default.Palette, navRoute)

    data class  DnsOverHttp( val navRoute: String) :
        SettingItems("Advance Setting", Icons.Default.Settings, navRoute)

    data class  About( val navRoute: String) : SettingItems("About", Icons.Default.Info, navRoute)
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    imageVector: ImageVector,
    navController: NavController,
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