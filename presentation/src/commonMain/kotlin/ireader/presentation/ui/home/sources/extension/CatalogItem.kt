package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.core.os.InstallStep
import ireader.domain.models.entities.*
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.ContentAlpha
import ireader.presentation.ui.home.sources.extension.composables.LetterIcon
import java.util.*

@Composable
fun CatalogItem(
    modifier: Modifier = Modifier,
    catalog: Catalog,
    installStep: InstallStep? = null,
    onClick: (() -> Unit)? = null,
    onInstall: (() -> Unit)? = null,
    onUninstall: (() -> Unit)? = null,
    onPinToggle: (() -> Unit)? = null,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
) {
    val title = buildAnnotatedString {
        append("${catalog.name} ")
    }
    val lang = when (catalog) {
        is CatalogBundled -> null
        is CatalogInstalled -> catalog.source?.lang
        is CatalogRemote -> catalog.lang
    }?.let { Language(it) }

    Row(
        modifier = onClick?.let { modifier.clickable(onClick = it) } ?: modifier
    ) {
        CatalogPic(
            catalog = catalog,
            modifier = Modifier
                .padding(12.dp)
                .size(48.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
                .fillMaxHeight()
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 12.dp)
            )

            Text(
                text = lang?.code?.uppercase(Locale.getDefault()) ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(bottom = 12.dp, end = 12.dp)
            )
        }

        CatalogButtons(
            catalog = catalog,
            installStep = installStep,
            onInstall = onInstall,
            onUninstall = onUninstall,
            onPinToggle = onPinToggle,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterVertically)
                .padding(end = 4.dp),
            onCancelInstaller = onCancelInstaller,
        )
    }
}

@Composable
private fun CatalogPic(catalog: Catalog, modifier: Modifier = Modifier) {
    when(catalog) {
        is CatalogBundled -> {
            LetterIcon(catalog.name, modifier)
        }
        else -> {
            IImageLoader(
                model = catalog,
                contentDescription = null,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun CatalogButtons(
    catalog: Catalog,
    installStep: InstallStep?,
    onInstall: (() -> Unit)?,
    onUninstall: (() -> Unit)?,
    onPinToggle: (() -> Unit)?,
    onCancelInstaller: ((Catalog) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium()) {
            // Show either progress indicator or install button
            if (installStep != null && !installStep.isFinished()) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                    AppIconButton(
                        imageVector = Icons.Default.Close, onClick = {
                        if (onCancelInstaller != null) {
                            onCancelInstaller(catalog)
                        }
                    })
                }
            } else if (onInstall != null) {
                if (catalog is CatalogLocal) {
                    MidSizeTextComposable(
                        text = localize(MR.strings.update),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onInstall() }
                    )
                } else if (catalog is CatalogRemote) {
                    MidSizeTextComposable(
                        text = localize(MR.strings.install),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onInstall() }
                    )
                }
            }
            if (catalog !is CatalogRemote) {
                CatalogMenuButton(
                    catalog = catalog,
                    onPinToggle = onPinToggle,
                    onUninstall = onUninstall
                )
            }
        }
    }
}

@Composable
internal fun CatalogMenuButton(
    catalog: Catalog,
    onPinToggle: (() -> Unit)?,
    onUninstall: (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        if (onPinToggle != null && catalog is CatalogLocal && onUninstall == null) {
            if (catalog.isPinned) {
                AppIconButton(
                    imageVector = Icons.Filled.PushPin,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = localize(MR.strings.pin),
                    onClick = onPinToggle
                )
            } else {
                AppIconButton(
                    imageVector = Icons.Outlined.PushPin,
                    tint = MaterialTheme.colorScheme.onBackground.copy(
                        .5f
                    ),
                    contentDescription = localize(MR.strings.unpin),
                    onClick = onPinToggle
                )
            }
        } else {
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            if (onUninstall != null && catalog is CatalogLocal) {
                MidSizeTextComposable(
                    text = localize(MR.strings.uninstall),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onUninstall() }
                )
            }
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}