package ireader.presentation.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ireader.domain.models.entities.Recommendation
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.similar_titles_section_title
import ireader.i18n.resources.view_all

@Composable
fun RecommendationsSection(
    recommendations: List<Recommendation>,
    onRecommendationClick: (Recommendation) -> Unit,
    onViewMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    if (recommendations.isEmpty()) {
        return
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📚", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.similar_titles_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            TextButton(onClick = onViewMore) {
                Text(
                    text = localizeHelper.localize(Res.string.view_all),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendations, key = { it.key }) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onClick = { onRecommendationClick(recommendation) }
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: Recommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (recommendation.cover.isNotBlank()) {
                    AsyncImage(
                        model = recommendation.cover,
                        contentDescription = recommendation.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.7f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = null,
                        contentDescription = recommendation.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.7f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (recommendation.sourceName.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = recommendation.sourceName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
