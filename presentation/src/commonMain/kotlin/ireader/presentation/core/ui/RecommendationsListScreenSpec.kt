package ireader.presentation.core.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.domain.models.entities.Recommendation
import ireader.i18n.resources.Res
import ireader.i18n.resources.recommendations_section_title
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
 * screen. Resolves the SAME BookDetailViewModel instance (same Param → same
 * NavBackStackEntry-scoped ViewModelStore entry), so no data has to travel
 * through navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
data class RecommendationsListScreenSpec(val bookId: Long) {

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
        val isLoading = state is BookDetailState.Loading

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
                title = stringResource(Res.string.recommendations_section_title),
                popBackStack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        if (isLoading && recommendations.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = recommendation.cover.takeIf { it.isNotBlank() },
                    contentDescription = recommendation.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (recommendation.sourceName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendation.sourceName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(shimmerBrush())
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(14.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .background(shimmerBrush())
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(14.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .background(shimmerBrush())
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(12.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .background(shimmerBrush())
                )
            }
        }
    }
}
