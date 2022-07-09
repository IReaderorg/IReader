package org.ireader.bookDetails.components

/**
 * Copyright 2015 Javier Tomás

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
Code taken from tachiyomi
available over here:https://github.com/tachiyomiorg/tachiyomi
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import org.ireader.common_extensions.copyToClipboard
import org.ireader.core_ui.modifier.clickableNoIndication
import org.ireader.ui_book_details.R

private val whitespaceLineRegex = Regex("[\\r\\n]{2,}", setOf(RegexOption.MULTILINE))

@Composable
fun BookSummary(
    onClickToggle: () -> Unit,
    description: String,
    genres: List<String>,
    expandedSummary: Boolean,
) {
    val context = LocalContext.current
    Column {
        val (expanded, onExpanded) = rememberSaveable {
            mutableStateOf(false)
        }
        val desc =
            description.takeIf { it.isNotBlank() }
                ?: stringResource(id = R.string.description_placeholder)
        val trimmedDescription = remember(desc) {
            desc
                .replace(whitespaceLineRegex, "\n")
                .trimEnd()
        }
        BookSummaryDescription(
            description,
            shrunkDescription = trimmedDescription,
            expanded = expanded,
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .clickableNoIndication(
                    onLongClick = { context.copyToClipboard(desc, desc) },
                    onClick = { onExpanded(!expanded) },
                ),
        )
        if (genres.isNotEmpty()) {
            if (expanded) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    mainAxisSpacing = 4.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    genres.filter { it.isNotBlank() }.forEach { genre ->
                        TagsChip(genre) {}
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(genres.filter { it.isNotBlank() }) { genre ->
                        TagsChip(genre) {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsChip(
    text: String,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        SuggestionChip(
            onClick = onClick,
            label = { Text(text = text, style = MaterialTheme.typography.bodySmall) },
            border = null,
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                labelColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

