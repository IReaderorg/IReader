package ireader.presentation.ui.title

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.TitleRarity
import ireader.domain.models.entities.UserTitle
import ireader.presentation.ui.common.FeatureScreenScaffold

/**
 * User Title screen showing earned titles and active title.
 *
 * State is managed by [UserTitleViewModel]. The screen displays:
 * - List of all titles with rarity indicators
 * - Active title highlighted
 * - Ability to activate earned titles
 */
@Composable
fun UserTitleScreen(
    vm: UserTitleViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()

    FeatureScreenScaffold(
        title = "Titles",
        icon = Icons.Default.WorkspacePremium,
        iconTint = Color(0xFFFFD700),
        onBack = onBack,
        isLoading = state.isLoading,
        isEmpty = state.titles.isEmpty(),
        emptyMessage = "No titles yet",
        emptySubMessage = "Earn titles through achievements!"
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = state.titles,
                key = { it.id }
            ) { title ->
                TitleCard(
                    title = title,
                    isActive = state.activeTitleId == title.id,
                    onActivate = { vm.activateTitle(title) }
                )
            }
        }
    }
}

/**
 * Card displaying a user title with rarity, effect info, and activation status.
 */
@Composable
private fun TitleCard(
    title: UserTitle,
    isActive: Boolean,
    onActivate: () -> Unit
) {
    val rarityColor = getRarityColor(title.rarity)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) rarityColor.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                RarityBadge(rarity = title.rarity)
            }

            if (isActive) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = rarityColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = rarityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else if (title.isEarned) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Activate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Small badge showing the title rarity with appropriate color.
 */
@Composable
private fun RarityBadge(rarity: TitleRarity) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = getRarityColor(rarity).copy(alpha = 0.1f)
    ) {
        Text(
            text = rarity.name.lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = getRarityColor(rarity),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * Returns the display color for a given title rarity.
 */
private fun getRarityColor(rarity: TitleRarity): Color = when (rarity) {
    TitleRarity.COMMON -> Color(0xFF607D8B)
    TitleRarity.RARE -> Color(0xFF1976D2)
    TitleRarity.EPIC -> Color(0xFF7B1FA2)
    TitleRarity.LEGENDARY -> Color(0xFFFF8F00)
}
