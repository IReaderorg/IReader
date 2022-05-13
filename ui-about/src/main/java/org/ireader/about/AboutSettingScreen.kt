package org.ireader.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.ireader.common_resources.BuildConfig
import org.ireader.common_resources.UiText
import org.ireader.components.components.LogoHeader
import org.ireader.components.components.Toolbar
import org.ireader.components.components.component.LinkIcon
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.ui_about.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingScreen(
    modifier: Modifier = Modifier,
    getFormattedBuildTime:() -> String,
    onPopBackStack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Scaffold(modifier = modifier.fillMaxSize(), topBar = {
        Toolbar(
            modifier = Modifier,
            title = {
                BigSizeTextComposable(
                    text = UiText.StringResource(R.string.about),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            navigationIcon = {
                TopAppBarBackButton(onClick = {
                    onPopBackStack()
                })
            }
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
        ) {
            item {
                LogoHeader()
            }
            item {
                PreferenceRow(
                    title = stringResource(R.string.version),
                    subtitle = when {
                        BuildConfig.DEBUG -> {
                            "Debug ${BuildConfig.COMMIT_SHA} (${getFormattedBuildTime()})"
                        }
                        BuildConfig.PREVIEW -> {
                            "Preview r${BuildConfig.COMMIT_COUNT} (${BuildConfig.COMMIT_SHA}, ${getFormattedBuildTime()})"
                        }
                        else -> {
                            "Stable ${BuildConfig.VERSION_NAME} (${getFormattedBuildTime()})"
                        }
                    },
                )
            }
            item {
                PreferenceRow(
                    title = stringResource(R.string.check_the_update),
                    onClick = {
                        uriHandler.openUri("https://github.com/kazemcodes/Infinity/releases")
                    },
                )
            }
            item {
                PreferenceRow(
                    title = stringResource(R.string.whats_new),
                    onClick = { uriHandler.openUri("https://github.com/kazemcodes/IReader/releases/latest") },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LinkIcon(
                        label = stringResource(R.string.website),
                        painter = rememberVectorPainter(Icons.Outlined.Public),
                        url = "https://github.com/kazemcodes/IReader",
                    )
                    LinkIcon(
                        label = "Discord",
                        painter = painterResource(R.drawable.ic_discord_24dp),
                        url = "https://discord.gg/HBU6zD8c5v",
                    )
                    LinkIcon(
                        label = "GitHub",
                        painter = painterResource(R.drawable.ic_github_24dp),
                        url = "https://github.com/kazemcodes/IReader",
                    )
                }
            }
        }
    }
}

