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
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_ui.ui.string
import org.ireader.domain.utils.toast
import org.ireader.ui_about.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AboutSettingScreen(
    modifier: Modifier = Modifier,
    onPopBackStack:() -> Unit
) {

    val context = LocalContext.current
    val versionCode: String =
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Throwable) {
            throw Exception(string(id = R.string.unable_to_get_package_version))
        }
    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        Toolbar(
            modifier = Modifier,
            title = {
                BigSizeTextComposable(text = UiText.StringResource( R.string.about), style = MaterialTheme.typography.h6)
            },
            navigationIcon = {
                TopAppBarBackButton(onClick = {
                    onPopBackStack()
                })
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
                                context.toast(R.string.no_app_was_found_to_lauch)
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

sealed class AboutTile(val title: UiText, val subtitle: UiText, val intent: Intent) {

    data class Version(val version: String) : AboutTile(
        UiText.StringResource(R.string.version),
        UiText.DynamicString(version),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kazemcodes/Infinity/releases"))
    )

    object WhatsNew : AboutTile(
        UiText.StringResource(R.string.whats_new),
        UiText.StringResource(R.string.check_the_update),
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/kazemcodes/IReader/releases/latest")
        )
    )

    object Discord : AboutTile(
        UiText.StringResource(R.string.discord),
        UiText.DynamicString("https://discord.gg/HBU6zD8c5v"),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/HBU6zD8c5v"))
    )

    object Github : AboutTile(
        UiText.StringResource(R.string.github),
        UiText.DynamicString("https://github.com/kazemcodes/IReader"),
        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kazemcodes/Infinity"))
    )
}
