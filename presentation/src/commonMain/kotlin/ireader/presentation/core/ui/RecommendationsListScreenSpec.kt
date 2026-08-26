package ireader.presentation.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ireader.domain.models.entities.Recommendation
import ireader.i18n.resources.Res
import ireader.i18n.resources.similar_titles_section_title
import ireader.i18n.resources.similar_titles_empty
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.component.shimmerBrush
import ireader.presentation.ui.book.viewmodel.BookDetailEvent
import ireader.presentation.ui.book.viewmodel.BookDetailState
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Full-screen list of the similar titles already loaded by the book detail
 * screen for the given [bookId].
 *
 * It retrieves the cached [BookDetailViewModel] via Koin's parameter scope,
 * reading [BookDetailState.Success.sourceRecommendations] directly so no
 * duplicate network calls are performed.
 */
data class RecommendationsListScreenSpec(val bookId: Long) {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val vm: BookDetailViewModel = koinViewModel(
            parameters = { parametersOf(BookDetailViewModel.Param(bookId)) }
        )
        val state by vm.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val navController = requireNotNull(ireader.presentation.core.LocalNavigator.current) { "LocalNavigator not provided" }

        LaunchedEffect(vm) {
            vm.events.collect { event ->
                when (event) {
                    is BookDetailEvent.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                    is BookDetailEvent.NavigateToBookDetail -> {
                        navController.navigate(NavigationRoutes.bookDetail(event.bookId))
                    }
                    else -> {}
                }
            }
        }

        val recommendations: List<Recommendation> = (state as? BookDetailState.Success)?.sourceRecommendations ?: emptyList()
        val isLoading = (state as? BookDetailState.Success)?.isLoadingRecommendations ?: (state is BookDetailState.Loading)

        RecommendationsListScreen(
            recommendations = recommendations,
            isLoading = isLoading,
            snackbarHostState = snackbarHostState,
            onBack = { navController.safePopBackStack() },
            onRecommendationClick = { vm.openRecommendation(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationsListScreen(
    recommendations: List<Recommendation>,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRecommendationClick: (Recommendation) -> Unit
) {
    IScaffold(
        snackbarHostState = snackbarHostState,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = stringResource(Res.string.similar_titles_section_title),
                popBackStack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (isLoading && recommendations.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(6) {
                    RecommendationItemSkeleton()
                }
            }
        } else if (recommendations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.similar_titles_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = recommendations,
                    key = { index, item -> "${item.sourceId}_${item.key}_$index" }
                ) { _, recommendation ->
                    RecommendationItem(
                        recommendation = recommendation,
                        onClick = { onRecommendationClick(recommendation) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    recommendation: Recommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Novel Cover
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (recommendation.cover.isNotBlank()) {
                    AsyncImage(
                        model = recommendation.cover,
                        contentDescription = recommendation.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f)
                                    )
                                )
                            )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Title and Details (No source name)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }

            // Trailing Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .padding(end = 14.dp)
                    .size(14.dp)
            )
        }
    }
}

@Composable
private fun RecommendationItemSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(shimmerBrush())
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush())
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush())
                )
            }
        }
    }
}
