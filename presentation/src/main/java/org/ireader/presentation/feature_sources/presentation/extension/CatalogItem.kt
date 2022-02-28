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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import org.ireader.domain.catalog.model.InstallStep
import org.ireader.domain.models.entities.Catalog
import org.ireader.domain.models.entities.CatalogBundled
import org.ireader.domain.models.entities.CatalogInstalled
import org.ireader.domain.models.entities.CatalogRemote
import org.ireader.presentation.feature_sources.presentation.extension.composables.LetterIcon
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
    }?.let { Language(it).toEmoji() }

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
                text = "",
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
            Image(
                painter = rememberImagePainter(LocalContext.current.packageManager.getApplicationIcon(
                    catalog.pkgName)),
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
                IconButton(onClick = onInstall) {
                    Icon(
                        imageVector = Filled.GetApp,
                        contentDescription = null
                    )
                }
            }
            if (catalog !is CatalogRemote) {
//        CatalogMenuButton(
//          catalog = catalog,
//          onPinToggle = onPinToggle,
//          onUninstall = onUninstall
//        )
            }
        }
    }
}
