package ireader.presentation.ui.book.components

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.resources.MR
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

private val whitespaceLineRegex = Regex("[\\r\\n]{2,}", setOf(RegexOption.MULTILINE))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookSummary(
    onClickToggle: () -> Unit,
    description: String,
    genres: List<String>,
    expandedSummary: Boolean,
    onCopy:(summary:String) -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    Column {
        val (expanded, onExpanded) = rememberSaveable {
            mutableStateOf(false)
        }
        val desc =
            description.takeIf { it.isNotBlank() }
                ?: localizeHelper.localize(MR.strings.description_placeholder)
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
                .padding(top = 12.dp, bottom = 8.dp)
                .padding(horizontal = 16.dp)
                .clickableNoIndication(
                    onLongClick = { onCopy(desc) },
                    onClick = { onExpanded(!expanded) },
                ),
        )
        if (genres.isNotEmpty()) {
            if (expanded) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genres.filter { it.isNotBlank() }.forEach { genre ->
                        TagsChip(genre) {}
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres.filter { it.isNotBlank() }) { genre ->
                        TagsChip(genre) {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun TagsChip(
    text: String,
    onClick: () -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        SuggestionChip(
            onClick = onClick,
            label = { 
                Text(
                    text = text, 
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                ) 
            },
            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                borderWidth = 1.dp),
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}
