package ireader.presentation.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
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
        val state = vm.state.value as? ireader.presentation.ui.book.viewmodel.BookDetailState.Success
        val recommendations: List<Recommendation> = state?.sourceRecommendations ?: emptyList()
        val navController = requireNotNull(ireader.presentation.core.LocalNavigator.current) { "LocalNavigator not provided" }

        RecommendationsListScreen(
            recommendations = recommendations,
            onBack = { navController.safePopBackStack() },
            onRecommendationClick = { vm.openRecommendation(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecommendationsListScreen(
    recommendations: List<Recommendation>,
    onBack: () -> Unit,
    onRecommendationClick: (Recommendation) -> Unit
) {
    IScaffold(
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = org.jetbrains.compose.resources.stringResource(Res.string.recommendations_section_title),
                popBackStack = onBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ponytail: key falls back to position — source keys aren't globally unique,
            // so key-only identity would crash on cross-source duplicates.
            items(recommendations, key = { "${it.sourceId}_${it.key}" }) { recommendation ->
                RecommendationItem(
                    recommendation = recommendation,
                    onClick = { onRecommendationClick(recommendation) }
                )
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
            .padding(horizontal = 16.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
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
                    .padding(16.dp)
                    .weight(1f)
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
