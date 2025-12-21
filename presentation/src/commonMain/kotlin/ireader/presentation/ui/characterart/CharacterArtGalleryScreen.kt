package ireader.presentation.ui.characterart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import ireader.domain.models.characterart.ArtStyleFilter
import ireader.domain.models.characterart.CharacterArt
import ireader.domain.models.characterart.CharacterArtSort
import ireader.domain.models.characterart.GalleryViewMode
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*


/**
 * Main Character Art Gallery Screen
 * Modern UI optimized for both Android and Desktop
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterArtGalleryScreen(
    vm: CharacterArtViewModel,
    onBack: () -> Unit,
    onArtClick: (CharacterArt) -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isWideScreen = isTableUi()
    
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }
    
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccessMessage()
        }
    }
    
    Scaffold(
        modifier = modifier.padding(paddingValues),
        topBar = {
            GalleryTopBar(
                searchQuery = state.searchQuery,
                onSearchChange = { vm.setSearchQuery(it) },
                onBack = onBack,
                isWideScreen = isWideScreen
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !state.isLoading,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = onUploadClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(localizeHelper.localize(Res.string.upload_art)) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips row
            FilterChipsRow(
                selectedFilter = state.selectedFilter,
                onFilterChange = { vm.setFilter(it) },
                selectedSort = state.selectedSort,
                onSortChange = { vm.setSort(it) },
                viewMode = state.viewMode,
                onViewModeChange = { vm.setViewMode(it) },
                isWideScreen = isWideScreen
            )
            
            // Main content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        LoadingContent(isWideScreen)
                    }
                    state.artList.isEmpty() -> {
                        EmptyGalleryContent(onUploadClick)
                    }
                    else -> {
                        GalleryContent(
                            artList = state.artList,
                            viewMode = state.viewMode,
                            onArtClick = onArtClick,
                            onLikeClick = { vm.toggleLike(it) },
                            isWideScreen = isWideScreen
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    isWideScreen: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
            }
            
            AnimatedVisibility(
                visible = !isSearchExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🎨",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Column {
                        Text(
                            text = localizeHelper.localize(Res.string.character_gallery),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.ai_generated_character_art),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    placeholder = { Text(localizeHelper.localize(Res.string.search_characters_books)) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = localizeHelper.localize(Res.string.clear_1))
                            }
                        }
                    }
                )
            }
            
            IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                Icon(
                    if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchExpanded) "Close search" else "Search"
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: ArtStyleFilter,
    onFilterChange: (ArtStyleFilter) -> Unit,
    selectedSort: CharacterArtSort,
    onSortChange: (CharacterArtSort) -> Unit,
    viewMode: GalleryViewMode,
    onViewModeChange: (GalleryViewMode) -> Unit,
    isWideScreen: Boolean
) {
    var showSortMenu by remember { mutableStateOf(false) }
    
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column {
            // Style filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ArtStyleFilter.entries, key = { it.name }) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(filter.icon, fontSize = 14.sp)
                                Text(filter.displayName)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            
            // Sort and view mode row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort dropdown
                Box {
                    TextButton(onClick = { showSortMenu = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(selectedSort.displayName)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        CharacterArtSort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.displayName) },
                                onClick = {
                                    onSortChange(sort)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (selectedSort == sort) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                
                // View mode toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ViewModeButton(
                        icon = Icons.Default.GridView,
                        selected = viewMode == GalleryViewMode.GRID,
                        onClick = { onViewModeChange(GalleryViewMode.GRID) }
                    )
                    ViewModeButton(
                        icon = Icons.Default.ViewList,
                        selected = viewMode == GalleryViewMode.LIST,
                        onClick = { onViewModeChange(GalleryViewMode.LIST) }
                    )
                    if (isWideScreen) {
                        ViewModeButton(
                            icon = Icons.Default.Dashboard,
                            selected = viewMode == GalleryViewMode.MASONRY,
                            onClick = { onViewModeChange(GalleryViewMode.MASONRY) }
                        )
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ViewModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = localizeHelper.localize(Res.string.scale)
    )
    
    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            Color.Transparent,
        onClick = onClick
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.padding(8.dp),
            tint = if (selected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GalleryContent(
    artList: List<CharacterArt>,
    viewMode: GalleryViewMode,
    onArtClick: (CharacterArt) -> Unit,
    onLikeClick: (String) -> Unit,
    isWideScreen: Boolean
) {
    val gridState = rememberLazyGridState()
    val columns = when {
        viewMode == GalleryViewMode.LIST -> GridCells.Fixed(1)
        isWideScreen -> GridCells.Adaptive(minSize = 280.dp)
        else -> GridCells.Fixed(2)
    }
    
    LazyVerticalGrid(
        columns = columns,
        state = gridState,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = artList,
            key = { it.id }
        ) { art ->
            CharacterArtCard(
                art = art,
                viewMode = viewMode,
                onClick = { onArtClick(art) },
                onLikeClick = { onLikeClick(art.id) },
                isWideScreen = isWideScreen
            )
        }
    }
}


@Composable
private fun CharacterArtCard(
    art: CharacterArt,
    viewMode: GalleryViewMode,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    isWideScreen: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = localizeHelper.localize(Res.string.cardscale)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (viewMode == GalleryViewMode.LIST) 
                    Modifier.height(120.dp) 
                else 
                    Modifier.aspectRatio(0.75f)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        if (viewMode == GalleryViewMode.LIST) {
            ListViewCard(art, onLikeClick)
        } else {
            GridViewCard(art, onLikeClick)
        }
    }
}

@Composable
private fun GridViewCard(
    art: CharacterArt,
    onLikeClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Image with gradient background fallback
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            if (art.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = art.thumbnailUrl.ifBlank { art.imageUrl },
                    contentDescription = "${art.characterName} from ${art.bookTitle}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder icon when no image
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        
        // Featured badge
        if (art.isFeatured) {
            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Featured",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // Like button
        LikeButton(
            isLiked = art.isLikedByUser,
            likesCount = art.likesCount,
            onClick = onLikeClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )
        
        // Bottom info overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = art.characterName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = art.bookTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (art.aiModel.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖", fontSize = 10.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = art.aiModel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListViewCard(
    art: CharacterArt,
    onLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (art.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = art.thumbnailUrl.ifBlank { art.imageUrl },
                    contentDescription = "${art.characterName} from ${art.bookTitle}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            if (art.isFeatured) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        "⭐",
                        fontSize = 10.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        
        // Info
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = art.characterName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = art.bookTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (art.aiModel.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = art.aiModel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "by ${art.submitterUsername.ifBlank { "Anonymous" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Like button
        LikeButton(
            isLiked = art.isLikedByUser,
            likesCount = art.likesCount,
            onClick = onLikeClick,
            compact = true
        )
    }
}

@Composable
private fun LikeButton(
    isLiked: Boolean,
    likesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = localizeHelper.localize(Res.string.likescale)
    )
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 8.dp else 12.dp),
        color = if (isLiked)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
        else
            Color.Black.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 6.dp else 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                modifier = Modifier
                    .size(if (compact) 16.dp else 18.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                tint = if (isLiked)
                    MaterialTheme.colorScheme.error
                else
                    Color.White
            )
            Text(
                text = if (likesCount > 0) likesCount.toString() else "",
                style = MaterialTheme.typography.labelSmall,
                color = if (isLiked)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    Color.White
            )
        }
    }
}

@Composable
private fun LoadingContent(isWideScreen: Boolean) {
    val columns = if (isWideScreen) 4 else 2
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(8, key = { "shimmer-$it" }) {
            ShimmerCard()
        }
    }
}

@Composable
private fun ShimmerCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val infiniteTransition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.shimmer))
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = localizeHelper.localize(Res.string.shimmeralpha)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
                )
        )
    }
}

@Composable
private fun EmptyGalleryContent(onUploadClick: () -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🎨", fontSize = 64.sp)
            
            Text(
                text = localizeHelper.localize(Res.string.no_character_art_yet),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = localizeHelper.localize(Res.string.be_the_first_to_share),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(8.dp))
            
            Button(
                onClick = onUploadClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.upload_first_art))
            }
        }
    }
}
