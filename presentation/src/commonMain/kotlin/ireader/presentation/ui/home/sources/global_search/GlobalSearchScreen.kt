package ireader.presentation.ui.home.sources.global_search

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.domain.models.entities.Book
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.list.layouts.BookImage
import ireader.presentation.ui.component.loading.DotsFlashing
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.SmallTextComposable
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import ireader.presentation.ui.home.sources.global_search.viewmodel.SearchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
        vm: GlobalSearchViewModel,
        onPopBackStack: () -> Unit,
        onSearch: (query: String) -> Unit,
        onBook: (Book) -> Unit,
        onGoToExplore: (SearchItem) -> Unit,
) {
    // Use the modern implementation
    GlobalSearchScreenModern(
        vm = vm,
        onPopBackStack = onPopBackStack,
        onSearch = onSearch,
        onBook = onBook,
        onGoToExplore = onGoToExplore
    )
}

private enum class Types {
    WithResult,
    NoResult,
    InProgress;
}


private fun Source.key(numberOfTries: Int, type: Types) :String {
   return when(type) {
        Types.InProgress -> "in_progress-${this.id}-${this.name}-${numberOfTries}"
        Types.NoResult -> "no_result-${this.id}-${this.name}-${numberOfTries}"
        Types.WithResult -> "with_result-${this.id}-${this.name}-${numberOfTries}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernGlobalSearchCard(
        book: SearchItem,
        onBook: (Book) -> Unit,
        goToExplore: () -> Unit,
        loading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with source info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = book.source.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = book.source.lang.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = goToExplore,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = localize(Res.string.open_explore),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Results section
            when {
                loading -> {
                    // Skeleton loading state
                    SkeletonLoadingRow()
                }
                book.items.isEmpty() -> {
                    // No results state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    // Results row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(book.items.size) { index ->
                            BookImage(
                                modifier = Modifier
                                    .height(200.dp)
                                    .aspectRatio(3f / 4f)
                                    .clip(RoundedCornerShape(8.dp)),
                                onClick = {
                                    onBook(book.items[index])
                                },
                                book = book.items[index]
                            ) {
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonLoadingRow() {
    val infiniteTransition = rememberInfiniteTransition()
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(shimmerTranslate - 1000f, shimmerTranslate - 1000f),
        end = Offset(shimmerTranslate, shimmerTranslate)
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(3) {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlobalSearchBookInfo(
        book: SearchItem,
        onBook: (Book) -> Unit,
        goToExplore: () -> Unit,
        loading:Boolean
) {
    val modifier = when (book.items.isNotEmpty()) {
        true ->
            Modifier
                .fillMaxWidth()

        else -> Modifier
    }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                MidSizeTextComposable(text = book.source.name, fontWeight = FontWeight.Bold)
                SmallTextComposable(text = book.source.lang.uppercase())
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotsFlashing(loading)
                AppIconButton(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = localize(Res.string.open_explore),
                    onClick = goToExplore
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(modifier = Modifier) {
            items(book.items.size) { index ->
                BookImage(
                    modifier = Modifier
                        .height(250.dp)
                        .aspectRatio(3f / 4f),
                    onClick = {
                        onBook(book.items[index])
                    },
                    book = book.items[index]
                ) {
                }
            }
        }
    }
}
