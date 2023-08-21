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
import ireader.i18n.BuildKonfig
import ireader.i18n.Images.discord
import ireader.i18n.Images.github
import ireader.i18n.localize

import ireader.presentation.ui.component.components.LinkIcon
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.PreferenceRow

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
                title = localize { xml -> xml.version },
                subtitle = when {
                    BuildKonfig.DEBUG -> {
                        "Debug ${BuildKonfig.COMMIT_SHA} (${getFormattedBuildTime()})"
                    }

                    BuildKonfig.PREVIEW -> {
                        "Preview r${BuildKonfig.COMMIT_COUNT} (${BuildKonfig.COMMIT_SHA}, ${getFormattedBuildTime()})"
                    }

                    else -> {
                        "Stable ${BuildKonfig.VERSION_NAME} (${getFormattedBuildTime()})"
                    }
                },
            )
        }
        item {
            PreferenceRow(
                title = localize { xml -> xml.checkTheUpdate },
                onClick = {
                    uriHandler.openUri("https://github.com/kazemcodes/Infinity/releases")
                },
            )
        }
        item {
            PreferenceRow(
                title = localize { xml -> xml.whatsNew },
                onClick = { uriHandler.openUri("https://github.com/kazemcodes/IReader/releases/latest") },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                LinkIcon(
                    label = localize { xml -> xml.website },
                    painter = rememberVectorPainter(Icons.Outlined.Public),
                    url = "https://github.com/kazemcodes/IReader",
                )
                LinkIcon(
                    label = "Discord",
                    icon = discord(),
                    url = "https://discord.gg/HBU6zD8c5v",
                )
                LinkIcon(
                    label = "GitHub",
                    icon = github(),
                    url = "https://github.com/kazemcodes/IReader",
                )
            }
        }
    }
}
