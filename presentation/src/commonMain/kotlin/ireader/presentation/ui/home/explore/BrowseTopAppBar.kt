package ireader.presentation.ui.home.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ireader.core.source.HttpSource
import ireader.core.source.LocalSource
import ireader.core.source.model.Filter
import ireader.domain.models.DisplayMode
import ireader.domain.models.getLayoutName
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.*
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.component.text_related.RadioButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseTopAppBar(
        state: ExploreViewModel,
        source: ireader.core.source.CatalogSource?,
        onValueChange: (String) -> Unit,
        onSearch: () -> Unit,
        onSearchDisable: () -> Unit,
        onSearchEnable: () -> Unit,
        onWebView: () -> Unit,
        onPop: () -> Unit,
        onLayoutTypeSelect: (DisplayMode) -> Unit,
        currentLayout: DisplayMode,
        scrollBehavior: TopAppBarScrollBehavior?,
        onOpenLocalFolder: (() -> Unit)? = null,
        searchQuery: String = state.searchQuery ?: "",
        isSearchMode: Boolean = state.isSearchModeEnable
) {
    var topMenu by remember {
        mutableStateOf(false)
    }
    val layouts = remember {
        listOf(DisplayMode.ComfortableGrid, DisplayMode.CompactGrid, DisplayMode.List, DisplayMode.OnlyCover)
    }
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Animated content transition between normal and search mode
    AnimatedContent(
        targetState = isSearchMode,
        transitionSpec = {
            if (targetState) {
                // Entering search mode - slide in from right with fade
                (slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { fullWidth -> fullWidth / 3 } + fadeIn(
                    animationSpec = tween(200)
                )).togetherWith(
                    slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) { fullWidth -> -fullWidth / 3 } + fadeOut(
                        animationSpec = tween(150)
                    )
                )
            } else {
                // Exiting search mode - slide out to right with fade
                (slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { fullWidth -> -fullWidth / 3 } + fadeIn(
                    animationSpec = tween(200)
                )).togetherWith(
                    slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) { fullWidth -> fullWidth / 3 } + fadeOut(
                        animationSpec = tween(150)
                    )
                )
            }
        },
        label = localizeHelper.localize(Res.string.searchmodetransition)
    ) { inSearchMode ->
        if (inSearchMode) {
            // Full-width search toolbar
            FullWidthSearchToolbar(
                scrollBehavior = scrollBehavior,
                searchQuery = searchQuery,
                onValueChange = onValueChange,
                onSearch = onSearch,
                onClose = onSearchDisable
            )
        } else {
            // Normal toolbar
            NormalBrowseToolbar(
                scrollBehavior = scrollBehavior,
                source = source,
                onSearchEnable = onSearchEnable,
                onWebView = onWebView,
                onPop = onPop,
                onLayoutTypeSelect = onLayoutTypeSelect,
                currentLayout = currentLayout,
                onOpenLocalFolder = onOpenLocalFolder,
                topMenu = topMenu,
                onTopMenuChange = { topMenu = it },
                layouts = layouts,
                localizeHelper = localizeHelper
            )
        }
    }
}

/**
 * Full-width search toolbar that takes over the entire app bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullWidthSearchToolbar(
    scrollBehavior: TopAppBarScrollBehavior?,
    searchQuery: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClose: () -> Unit
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            ExpandingSearchField(
                query = searchQuery,
                onValueChange = onValueChange,
                onSearch = onSearch,
                placeholder = localize(Res.string.search_hint)
            )
        },
        navigationIcon = {
            // Animated close/back button
            Surface(
                modifier = Modifier.padding(start = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = CircleShape
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = localize(Res.string.close),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            // Clear text button when there's text
            AnimatedVisibility(
                visible = searchQuery.isNotEmpty(),
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = CircleShape
                ) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localize(Res.string.clear),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    )
}

/**
 * Normal browse toolbar with all action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalBrowseToolbar(
    scrollBehavior: TopAppBarScrollBehavior?,
    source: ireader.core.source.CatalogSource?,
    onSearchEnable: () -> Unit,
    onWebView: () -> Unit,
    onPop: () -> Unit,
    onLayoutTypeSelect: (DisplayMode) -> Unit,
    currentLayout: DisplayMode,
    onOpenLocalFolder: (() -> Unit)?,
    topMenu: Boolean,
    onTopMenuChange: (Boolean) -> Unit,
    layouts: List<DisplayMode>,
    localizeHelper: ireader.i18n.LocalizeHelper
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            BigSizeTextComposable(text = source?.name ?: "")
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                // Search button
                AnimatedVisibility(
                    visible = source?.getFilters()?.find { it is Filter.Title } != null,
                    enter = fadeIn() + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AppIconButton(
                            imageVector = Icons.Default.Search,
                            contentDescription = localize(Res.string.search),
                            onClick = onSearchEnable,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // WebView button
                if (source is HttpSource) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AppIconButton(
                            imageVector = Icons.Default.Public,
                            contentDescription = localize(Res.string.webView),
                            onClick = onWebView,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                // Local Source - Open Folder Button
                if (source?.id == LocalSource.SOURCE_ID && onOpenLocalFolder != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AppIconButton(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = localizeHelper.localize(Res.string.open_local_folder),
                            onClick = onOpenLocalFolder,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                // Layout selector
                Spacer(modifier = Modifier.width(4.dp))
                Box {
                    Surface(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = if (topMenu) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AppIconButton(
                            imageVector = Icons.Default.GridView,
                            contentDescription = localize(Res.string.layout),
                            onClick = { onTopMenuChange(true) },
                            tint = if (topMenu) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IDropdownMenu(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .clip(RoundedCornerShape(12.dp)),
                        expanded = topMenu,
                        onDismissRequest = { onTopMenuChange(false) }
                    ) {
                        layouts.forEach { layout ->
                            IDropdownMenuItem(onClick = {
                                onLayoutTypeSelect(layout)
                                onTopMenuChange(false)
                            }, text = {
                                val layoutName = layout.getLayoutName(localizeHelper)
                                val description = when (layout) {
                                    DisplayMode.ComfortableGrid -> localize(Res.string.comfortable_grid_layout_description)
                                    DisplayMode.CompactGrid -> localize(Res.string.compact_grid_layout_description)
                                    DisplayMode.List -> localize(Res.string.list_layout_description) 
                                    DisplayMode.OnlyCover -> localize(Res.string.cover_only_layout_description)
                                }
                                
                                RadioButton(
                                    text = layoutName,
                                    description = description,
                                    selected = currentLayout == layout,
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        onLayoutTypeSelect(layout)
                                        onTopMenuChange(false)
                                    }
                                )
                            })
                        }
                    }
                }
            }
        },
        navigationIcon = {
            TopAppBarBackButton { onPop() }
        }
    )
}

/**
 * Expanding search field with beautiful animations that takes the full toolbar width
 */
@Composable
private fun ExpandingSearchField(
    query: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val focusRequester = remember { FocusRequester() }
    
    // Animate the search field expansion
    var isExpanded by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = localizeHelper.localize(Res.string.searchfieldalpha)
    )
    
    // Auto-focus and trigger expansion animation
    LaunchedEffect(Unit) {
        isExpanded = true
        focusRequester.requestFocus()
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .graphicsLayer { alpha = animatedAlpha },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated search icon
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                // Placeholder text
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                
                BasicTextField(
                    value = query,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearch() }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
