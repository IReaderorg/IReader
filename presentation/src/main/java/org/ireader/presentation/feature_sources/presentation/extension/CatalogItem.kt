/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.presentation.feature_sources.presentation.extension

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import org.ireader.domain.catalog.model.InstallStep
import org.ireader.domain.models.entities.*
import org.ireader.presentation.feature_sources.presentation.extension.composables.LetterIcon
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import java.util.*
import kotlin.math.max

@Composable
fun CatalogItem(
    catalog: Catalog,
    installStep: InstallStep? = null,
    onClick: (() -> Unit)? = null,
    onInstall: (() -> Unit)? = null,
    onUninstall: (() -> Unit)? = null,
    onPinToggle: (() -> Unit)? = null,
) {
    val title = buildAnnotatedString {
        append("${catalog.name} ")
    }
    val lang = when (catalog) {
        is CatalogBundled -> null
        is CatalogInstalled -> catalog.source.lang
        is CatalogRemote -> catalog.lang
    }?.let { Language(it) }

    Layout(
        modifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier,
        content = {
            CatalogPic(
                catalog = catalog,
                modifier = Modifier
                    .layoutId("pic")
                    .padding(12.dp)
                    .size(48.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .layoutId("title")
                    .padding(top = 12.dp)
            )

            Text(
                text = lang?.code?.uppercase(Locale.getDefault()) ?: "",
                style = MaterialTheme.typography.body2,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .layoutId("desc")
                    .padding(bottom = 12.dp, end = 12.dp)
            )
            CatalogButtons(
                catalog = catalog,
                installStep = installStep,
                onInstall = onInstall,
                onUninstall = onUninstall,
                onPinToggle = onPinToggle,
                modifier = Modifier
                    .layoutId("icons")
                    .padding(end = 4.dp)
            )
        },
        measurePolicy = { measurables, fullConstraints ->
            val picPlaceable = measurables.first { it.layoutId == "pic" }.measure(fullConstraints)
            val langPlaceable = measurables.find { it.layoutId == "lang" }?.measure(fullConstraints)

            val constraints = fullConstraints.copy(
                maxWidth = fullConstraints.maxWidth - picPlaceable.width
            )

            val iconsPlaceable = measurables.first { it.layoutId == "icons" }.measure(constraints)
            val titlePlaceable = measurables.first { it.layoutId == "title" }
                .measure(constraints.copy(maxWidth = constraints.maxWidth - iconsPlaceable.width))
            val descPlaceable = measurables.first { it.layoutId == "desc" }.measure(constraints)

            val height = max(picPlaceable.height, titlePlaceable.height + descPlaceable.height)

            layout(fullConstraints.maxWidth, height) {
                picPlaceable.placeRelative(0, 0)
                langPlaceable?.placeRelative(
                    x = picPlaceable.width - langPlaceable.width,
                    y = picPlaceable.height - langPlaceable.height
                )
                titlePlaceable.placeRelative(picPlaceable.width, 0)
                descPlaceable.placeRelative(picPlaceable.width, titlePlaceable.height)
                iconsPlaceable.placeRelative(
                    x = constraints.maxWidth - iconsPlaceable.width + picPlaceable.width,
                    y = 0
                )
            }
        }
    )
}

@Composable
private fun CatalogPic(catalog: Catalog, modifier: Modifier = Modifier) {
    when (catalog) {
        is CatalogBundled -> {
            LetterIcon(catalog.name, modifier)
        }
        is CatalogInstalled -> {
            runCatching {
                Image(
                    painter = rememberImagePainter(LocalContext.current.packageManager.getApplicationIcon(
                        catalog.pkgName)),
                    contentDescription = null,
                    modifier = modifier
                )
            }.getOrElse {
                Image(
                    painter = rememberImagePainter(catalog),
                    contentDescription = null,
                    modifier = modifier
                )
            }

        }
        is CatalogRemote -> {
            Image(
                painter = rememberImagePainter(catalog.iconUrl),
                contentDescription = null,
                modifier = modifier
            )
        }
        else -> {
            Image(
                painter = rememberImagePainter(catalog),
                contentDescription = null,
                modifier = modifier
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
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            // Show either progress indicator or install button
            if (installStep != null && !installStep.isFinished()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp)
                )
            } else if (onInstall != null) {
                if (catalog is CatalogLocal) {
                    MidSizeTextComposable(text = "Update",
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.clickable { onInstall() })
                } else if (catalog is CatalogRemote) {
                    MidSizeTextComposable(text = "Install",
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.clickable { onInstall() })
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
                TopAppBarActionButton(
                    imageVector = Icons.Filled.PushPin,
                    tint = MaterialTheme.colors.primary,
                    title = "Pin",
                    onClick = onPinToggle)
            } else {
                TopAppBarActionButton(
                    imageVector = Icons.Outlined.PushPin,
                    tint = MaterialTheme.colors.onBackground.copy(.5f),
                    title = "UnPin",
                    onClick = onPinToggle)
            }
        } else {
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            if (onUninstall != null && catalog is CatalogLocal) {
                MidSizeTextComposable(text = "Uninstall",
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable { onUninstall() })
            }
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogItemPreview() {
    CatalogItem(
        catalog = CatalogRemote(
            name = "My Catalog",
            description = "Some description",
            sourceId = 0L,
            pkgName = "my.catalog",
            versionName = "1.0.0",
            versionCode = 1,
            lang = "en",
            pkgUrl = "",
            iconUrl = "",
            nsfw = false,
        ),
        onClick = {},
        onInstall = {},
        onUninstall = {},
        onPinToggle = {}
    )
}
