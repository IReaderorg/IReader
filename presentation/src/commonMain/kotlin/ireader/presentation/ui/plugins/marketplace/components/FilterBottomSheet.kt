package ireader.presentation.ui.plugins.marketplace.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.IBackHandler
import ireader.presentation.ui.plugins.marketplace.PriceFilter
import ireader.presentation.ui.plugins.marketplace.SortOrder

/**
 * Bottom sheet for filtering and sorting plugins
 * Requirements: 16.3, 16.4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentSortOrder: SortOrder,
    currentPriceFilter: PriceFilter,
    currentMinRating: Float,
    onSortOrderChange: (SortOrder) -> Unit,
    onPriceFilterChange: (PriceFilter) -> Unit,
    onMinRatingChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle back button to dismiss the sheet
    IBackHandler(enabled = true, onBack = onDismiss)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth().fillMaxHeight(0.5f),
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filter & Sort",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Sort section
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            SortOrder.entries.forEach { sortOrder ->
                FilterOption(
                    text = getSortOrderLabel(sortOrder),
                    isSelected = currentSortOrder == sortOrder,
                    onClick = {
                        onSortOrderChange(sortOrder)
                        onDismiss()
                    }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Price filter section
            Text(
                text = "Price",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            PriceFilter.entries.forEach { priceFilter ->
                FilterOption(
                    text = getPriceFilterLabel(priceFilter),
                    isSelected = currentPriceFilter == priceFilter,
                    onClick = {
                        onPriceFilterChange(priceFilter)
                    }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Rating filter section
            Text(
                text = "Minimum Rating",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column {
                Text(
                    text = if (currentMinRating > 0) "${currentMinRating.toInt()} stars and above" else "All ratings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Slider(
                    value = currentMinRating,
                    onValueChange = onMinRatingChange,
                    valueRange = 0f..5f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Apply button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Single filter option row
 */
@Composable
private fun FilterOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Get label for sort order
 */
private fun getSortOrderLabel(sortOrder: SortOrder): String {
    return when (sortOrder) {
        SortOrder.POPULARITY -> "Most Popular"
        SortOrder.RATING -> "Highest Rated"
        SortOrder.DATE_ADDED -> "Recently Added"
        SortOrder.PRICE_LOW_TO_HIGH -> "Price: Low to High"
        SortOrder.PRICE_HIGH_TO_LOW -> "Price: High to Low"
        SortOrder.NAME -> "Name (A-Z)"
    }
}

/**
 * Get label for price filter
 */
private fun getPriceFilterLabel(priceFilter: PriceFilter): String {
    return when (priceFilter) {
        PriceFilter.ALL -> "All Plugins"
        PriceFilter.FREE -> "Free Only"
        PriceFilter.PAID -> "Paid Only"
        PriceFilter.FREEMIUM -> "Freemium Only"
    }
}
