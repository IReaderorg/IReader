package ireader.presentation.ui.home.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import ireader.presentation.ui.home.explore.components.JSPluginFilterIntegration
import ireader.presentation.ui.home.explore.components.DynamicFilterUI
import ireader.presentation.ui.home.explore.components.rememberJSPluginFilterState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreFilterModalSheet(
    filters: List<Filter<*>>,
    onDismissRequest: () -> Unit,
    onApplyFilter: () -> Unit,
    onReset: () -> Unit,
    value: String?,
    onValueChange: (String?) -> Unit,
    onListing: (Listing?) -> Unit,
    listing: Listing?,
    catalogs: List<Listing>,
    onModifyFilter: (Filter<*>) -> Unit,
    vm: ExploreViewModel
) {
    // Check if this is a JS plugin source and load its filters
    val source = vm.catalog
    val filterStateManager = org.koin.compose.koinInject<ireader.domain.filters.FilterStateManager>()
    val jsPluginFilterState = if (source != null && source.source != null && source.source is ireader.core.source.CatalogSource) {
        rememberJSPluginFilterState(source.source as ireader.core.source.CatalogSource, filterStateManager)
    } else {
        null
    }
    val helper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        modifier = Modifier.fillMaxHeight(0.9f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = localize(Res.string.filter),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onDismissRequest() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        if (jsPluginFilterState?.isJSPluginSource == true) {
                            jsPluginFilterState.resetFilters()
                        } else {
                            onReset()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset filters"
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    // Search field
                    OutlinedTextField(
                        value = value ?: "",
                        onValueChange = { onValueChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        label = { Text(localize(Res.string.search)) },
                        placeholder = { Text(localize(Res.string.search)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
                
                // Listings section if available
                if (catalogs.isNotEmpty()) {
                    item {
                        Text(
                            text = localize(Res.string.source_browse_latest),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        ListingChips(
                            listings = catalogs,
                            selectedListing = listing,
                            onListingSelected = onListing
                        )
                    }
                }
                
                // JS Plugin Filters section (if this is a JS plugin source)
                if (jsPluginFilterState?.isJSPluginSource == true && jsPluginFilterState.filterDefinitions != null) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localize(Res.string.filter),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dynamic JS Plugin Filters
                        DynamicFilterUI(
                            filterDefinitions = jsPluginFilterState.filterDefinitions!!,
                            filterValues = jsPluginFilterState.filterValues,
                            onFilterChange = { filterId, value ->
                                jsPluginFilterState.updateFilterValue(filterId, value)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                // Standard Filters section (for non-JS plugin sources)
                else if (filters.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localize(Res.string.filter),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    items(filters) { filter ->
                        FilterItem(
                            filter = filter,
                            onModifyFilter = onModifyFilter
                        )
                    }
                }
            }
            
            // Action button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        // If this is a JS plugin source, apply JS plugin filters
                        if (jsPluginFilterState?.isJSPluginSource == true) {
                            val convertedFilters = jsPluginFilterState.getConvertedFilters()
                            vm.loadItems(reset = true, jsPluginFilters = convertedFilters)
                        } else {
                            onApplyFilter()
                        }
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = localize(Res.string.filter))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListingChips(
    listings: List<Listing>,
    selectedListing: Listing?,
    onListingSelected: (Listing?) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedListing == null,
            onClick = { onListingSelected(null) },
            label = { Text(localize(Res.string.all)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        listings.forEach { listing ->
            FilterChip(
                selected = listing == selectedListing,
                onClick = { onListingSelected(listing) },
                label = { Text(listing.name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun FilterItem(
    filter: Filter<*>,
    onModifyFilter: (Filter<*>) -> Unit
) {
    when (filter) {
        is Filter.Title -> {
            // Title filter - already handled with search field
        }
        is Filter.Select -> {
            SelectFilter(filter = filter, onModifyFilter = onModifyFilter)
        }
        is Filter.Text -> {
            TextFilter(filter = filter, onModifyFilter = onModifyFilter)
        }
        is Filter.Group -> {
            GroupFilter(filter = filter, onModifyFilter = onModifyFilter)
        }
        is Filter.Sort -> {
            SortFilter(filter = filter, onModifyFilter = onModifyFilter)
        }
        else -> {
            Text(
                text = filter.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFilter(
    filter: Filter.Select,
    onModifyFilter: (Filter<*>) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = filter.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        var expanded by remember { mutableStateOf(false) }
        val currentValue = filter.value ?: 0
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = filter.options[currentValue],
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filter.options.forEachIndexed { index, value ->
                    DropdownMenuItem(
                        text = { Text(value) },
                        onClick = {
                            filter.value = index
                            onModifyFilter(filter)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TextFilter(
    filter: Filter.Text,
    onModifyFilter: (Filter<*>) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = filter.value ?: "",
            onValueChange = { 
                filter.value = it
                onModifyFilter(filter)
            },
            label = { Text(filter.name) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupFilter(
    filter: Filter.Group,
    onModifyFilter: (Filter<*>) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = filter.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filter.filters.forEach { subFilter ->
                when (subFilter) {
                    is Filter.Check -> {
                        val checked = subFilter.value ?: false
                        FilterChip(
                            selected = checked,
                            onClick = {
                                subFilter.value = !checked
                                onModifyFilter(filter)
                            },
                            label = { Text(subFilter.name) }
                        )
                    }
                    else -> {
                        // Handle other filter types if needed
                        Text(
                            text = subFilter.name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilter(
    filter: Filter.Sort,
    onModifyFilter: (Filter<*>) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = filter.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Selection for sort type
            var expanded by remember { mutableStateOf(false) }
            val currentSelection = filter.value?.index ?: 0
            val isAscending = filter.value?.ascending ?: true
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = filter.options[currentSelection],
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filter.options.forEachIndexed { index, value ->
                        DropdownMenuItem(
                            text = { Text(value) },
                            onClick = {
                                filter.value = Filter.Sort.Selection(index, isAscending)
                                onModifyFilter(filter)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Toggle button for ascending/descending
            IconButton(
                onClick = {
                    filter.value = Filter.Sort.Selection(currentSelection, !isAscending)
                    onModifyFilter(filter)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Sort direction",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
} 