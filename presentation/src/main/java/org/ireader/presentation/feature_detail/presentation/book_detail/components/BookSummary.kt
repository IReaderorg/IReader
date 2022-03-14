package org.ireader.presentation.feature_detail.presentation.book_detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable

@Composable
fun BookSummary(
    onClickToggle: () -> Unit,
    description: String,
    genres: List<String>,
    expandedSummary: Boolean,
) {
    var isExpandable by remember {
        mutableStateOf<Boolean?>(null)
    }


    val modifier = when (isExpandable) {
        true -> Modifier.animateContentSize()
        else -> Modifier
    }
    Column(modifier = modifier) {
        Text(
            text = "Synopsis", fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.h6,
        )
        BookSummaryDescription(
            description,
            isExpandable,
            setIsExpandable = {
                isExpandable = it
            },
            expandedSummary,
            onClickToggle
        )
        if (expandedSummary || isExpandable != true) {
            FlowRow(
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp),
                mainAxisSpacing = 4.dp,
                crossAxisSpacing = 6.dp
            ) {
                genres.filter { it.isNotBlank() }.forEach { genre ->
                    GenreChip(genre)
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(genres.filter { it.isNotBlank() }) { genre ->
                    GenreChip(genre)
                }
            }
        }
    }
}

@Composable
private fun GenreChip(genre: String) {
    Surface(
        modifier = Modifier.padding(horizontal = 2.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground.copy(alpha = .5f)),
        color = MaterialTheme.colors.background
    ) {
        MidSizeTextComposable(text = genre,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}
