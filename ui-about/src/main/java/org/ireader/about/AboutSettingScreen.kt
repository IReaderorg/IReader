package org.ireader.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.domain.utils.toast

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AboutSettingScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
) {

    val context = LocalContext.current
    val versionCode: String =
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Throwable) {
            "Unable to get Package Version"
        }
    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        Toolbar(
            modifier = Modifier,
            title = {
                BigSizeTextComposable(text = "About", style = MaterialTheme.typography.h6)
            },
            navigationIcon = {
                TopAppBarBackButton(navController = navController)
            }
        )
    }) { padding ->
        val list = listOf<AboutTile>(
            AboutTile.Version(versionCode),
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
                            try {
                                context.startActivity(it.intent)
                            } catch (e: Throwable) {
                                context.toast("Something went wrong. you don't have the required app.")
                            }
                        },
                    singleLineSecondaryText = false,
                    secondaryText = {
                        MidSizeTextComposable(
                            modifier
                                .fillMaxWidth()
                                .align(Alignment.Start),
                            text = it.subtitle,
                            color = MaterialTheme.colors.onBackground
                        )
                    },
                    text = {
                        BigSizeTextComposable(
                            modifier = modifier
                                .fillMaxWidth()
                                .align(Alignment.Start),
                            text = it.title
                        )
                    },
                )
                Divider(
                    modifier = modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.onBackground.copy(alpha = .1f)
                )
            }
        }
    }
}

sealed class AboutTile(val title: String, val subtitle: String, val intent: Intent) {

    data class Version(val version: String) : AboutTile(
        "Version",
        version,
        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kazemcodes/Infinity/releases"))
    )

    object WhatsNew : AboutTile(
        "Whats New",
        "Check the Update",
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/kazemcodes/IReader/releases/latest")
        )
    )

    object Discord : AboutTile(
        "Discord",
        "https://discord.gg/HBU6zD8c5v",
        Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/HBU6zD8c5v"))
    )

    object Github : AboutTile(
        "Github",
        "https://github.com/kazemcodes/IReader",
        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kazemcodes/Infinity"))
    )
}
