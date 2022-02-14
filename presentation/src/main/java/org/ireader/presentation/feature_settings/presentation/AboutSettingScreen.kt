package org.ireader.presentation.feature_settings.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.ireader.core.ProjectConfig
import org.ireader.core.utils.Constants
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AboutSettingScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
) {

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
    val context = LocalContext.current
    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        TopAppBar(
            title = {
                TopAppBarTitle(title = "About", style = MaterialTheme.typography.h6)
            },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground,
            elevation = Constants.DEFAULT_ELEVATION,
            navigationIcon = {
                TopAppBarBackButton(navController = navController)
            }
        )
    }) {
        val list = listOf<AboutTile>(
            AboutTile.Version,
            AboutTile.WhatsNew,
            AboutTile.Discord,
            AboutTile.Github,
        )


        Column(
            modifier = modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            list.forEach {
                ListItem(
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                        .clickable(role = Role.Button) {
                            context.startActivity(it.intent)
                        },
                    singleLineSecondaryText = false,
                    secondaryText = {
                        MidSizeTextComposable(modifier
                            .fillMaxWidth()
                            .align(Alignment.Start), text = it.subtitle,
                            color = MaterialTheme.colors.onBackground)
                    },
                    text = {
                        TopAppBarTitle(modifier = modifier
                            .fillMaxWidth()
                            .align(Alignment.Start), title = it.title)
                    },
                )
                Divider(modifier = modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.onBackground.copy(alpha = .1f))
            }

        }
    }
}

sealed class AboutTile(val title: String, val subtitle: String, val intent: Intent) {
    object Version : AboutTile("Version",
        ProjectConfig.versionName + " Beta",
        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kazemcodes/Infinity/releases")))

    object WhatsNew : AboutTile("Whats New",
        "Check the Update",
        Intent(Intent.ACTION_VIEW,
            Uri.parse("https://github.com/kazemcodes/IReader/releases/latest")))

    object Discord : AboutTile("Discord",
        "https://discord.gg/HBU6zD8c5v",
        Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/HBU6zD8c5v")))

    object Github : AboutTile("Github",
        "https://github.com/kazemcodes/IReader",
        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kazemcodes/Infinity")))
}