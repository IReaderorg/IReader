package ireader.presentation.ui.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import ireader.i18n.BuildConfig
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.R
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.component.LinkIcon
import ireader.presentation.ui.component.components.component.PreferenceRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingScreen(
    modifier: Modifier = Modifier,
    getFormattedBuildTime: () -> String,
    onPopBackStack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = modifier,
    ) {
        item {
            LogoHeader()
        }
        item {
            PreferenceRow(
                title = localize(MR.strings.version),
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
                title = localize(MR.strings.check_the_update),
                onClick = {
                    uriHandler.openUri("https://github.com/kazemcodes/Infinity/releases")
                },
            )
        }
        item {
            PreferenceRow(
                title = localize(MR.strings.whats_new),
                onClick = { uriHandler.openUri("https://github.com/kazemcodes/IReader/releases/latest") },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                LinkIcon(
                    label = localize(MR.strings.website),
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
