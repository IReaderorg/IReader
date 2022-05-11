package org.ireader.settings.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.ui_settings.R

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    onItemClick: (destinationScreenRoute: String) -> Unit,
    itemsRoutes: List<String>
) {
    val settingItems = listOf(
        SettingItem(
            "Downloads",
            Icons.Default.Download,
            itemsRoutes[0]
        ),
        SettingItem(
            "Appearance",
            Icons.Default.Palette,
            itemsRoutes[1]
        ),
        SettingItem(
            "Advance Setting",
            Icons.Default.Settings,
            itemsRoutes[2]
        ),
        SettingItem(
            "About",
            Icons.Default.Info,
            itemsRoutes[3]
        ),
    )
    Box(
        modifier
            .fillMaxSize()
            .padding(bottom = 50.dp)
    ) {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            Toolbar(
                title = {
                    BigSizeTextComposable(text =  UiText.StringResource(R.string.settings),)
                },
            )
        }) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.Start,
            ) {

                settingItems.forEach { item ->
                    SettingsItem(
                        title = item.title,
                        imageVector = item.icon,
                        destinationScreenRoute = item.route,
                        onClick =onItemClick

                    )
                }
            }
        }
    }
}


data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)


@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    imageVector: ImageVector,
    destinationScreenRoute: String,
    onClick: (destinationScreenRoute: String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(30.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(destinationScreenRoute) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "$title icon",
            tint = MaterialTheme.colors.primary
        )
        Spacer(modifier = modifier.width(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )
    }
}
