package ireader.presentation.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.web.WebPageScreen
import ireader.presentation.ui.web.WebPageTopBar
import ireader.presentation.ui.web.WebViewPageModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class
)
actual data class WebViewScreenSpec actual constructor(
    val url: String?,
    val sourceId: Long?,
    val bookId: Long?,
    val chapterId: Long?,
    val enableBookFetch: Boolean,
    val enableChapterFetch: Boolean,
    val enableChaptersFetch: Boolean,
) : VoyagerScreen() {

    @Composable
    override fun Content() {
        val vm: WebViewPageModel = getIViewModel(parameters =
        { parametersOf(WebViewPageModel.Param(url,bookId,sourceId,chapterId,enableChapterFetch,enableChaptersFetch,enableBookFetch))}
        )
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val host = SnackBarListener(vm)
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        
        // UI state
        var showUrlBar by remember { mutableStateOf(false) }
        var showActionMenu by remember { mutableStateOf(false) }
        var showFetchFeedback by remember { mutableStateOf(false) }
        var fetchFeedbackMessage by remember { mutableStateOf("") }
        val urlFocusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val webView = vm.webViewManager.webView

        // URL state
        val url by remember { derivedStateOf { vm.webUrl } }
        val source = vm.source
        val isLoading = vm.isLoading

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                ModernWebViewTopBar(
                    scrollBehavior = scrollBehavior,
                    currentUrl = url ?: vm.url,
                    isLoading = isLoading,
                    showUrlBar = showUrlBar,
                    onShowUrlBarChange = { showUrlBar = it },
                    onUrlChange = { vm.webUrl = it },
                    onGoToUrl = {
                        vm.webViewState?.content = WebContent.Url(vm.webUrl)
                        vm.updateWebUrl(vm.url)
                        showUrlBar = false
                        focusManager.clearFocus()
                    },
                    onRefresh = { webView?.reload() },
                    onBack = { navigator.pop() },
                    urlFocusRequester = urlFocusRequester,
                    canGoBack = webView?.canGoBack() == true,
                    canGoForward = webView?.canGoForward() == true,
                    onGoBack = { webView?.goBack() },
                    onGoForward = { webView?.goForward() }
                )
            },
            floatingActionButton = {
                // Only show FAB if at least one of the fetch options is enabled
                if (enableBookFetch || enableChapterFetch || enableChaptersFetch) {
                    FloatingActionButton(
                        onClick = { showActionMenu = !showActionMenu },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 16.dp, end = 16.dp).navigationBarsPadding()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkAdd,
                            contentDescription = "Fetch Actions",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            snackbarHost = { 
                SnackbarHost(hostState = host)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Main WebView content
                WebPageScreen(
                    viewModel = vm,
                    source = vm.source,
                    snackBarHostState = host,
                    scaffoldPadding = paddingValues,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Action Menu
                AnimatedVisibility(
                    visible = showActionMenu,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                ) {
                    ActionMenu(
                        onFetchBook = {
                            webView?.let {
                                val book = vm.stateBook
                                if (source != null) {
                                    showFetchFeedback = true
                                    fetchFeedbackMessage = "Fetching book details..."
                                    scope.launch {
                                        vm.getDetails(book = book, webView = it)
                                        delay(500) // Brief delay to ensure feedback is shown
                                        showFetchFeedback = false
                                    }
                                } else {
                                    scope.launch {
                                        host.showSnackbar("Source not available")
                                    }
                                }
                            }
                            showActionMenu = false
                        },
                        onFetchChapter = {
                            webView?.let {
                                val chapter = vm.stateChapter
                                if (chapter != null && source != null) {
                                    showFetchFeedback = true
                                    fetchFeedbackMessage = "Fetching chapter content..."
                                    scope.launch {
                                        vm.getContentFromWebView(chapter = chapter, webView = it)
                                        delay(500) // Brief delay to ensure feedback is shown
                                        showFetchFeedback = false
                                    }
                                } else {
                                    scope.launch {
                                        host.showSnackbar("Chapter not available")
                                    }
                                }
                            }
                            showActionMenu = false
                        },
                        onFetchChapters = {
                            webView?.let {
                                val book = vm.stateBook
                                val source = vm.source
                                if (book != null && source != null) {
                                    showFetchFeedback = true
                                    fetchFeedbackMessage = "Fetching all chapters..."
                                    scope.launch {
                                        vm.getChapters(book = book, webView = it)
                                        delay(500) // Brief delay to ensure feedback is shown
                                        showFetchFeedback = false
                                    }
                                } else {
                                    scope.launch {
                                        host.showSnackbar("Book not available")
                                    }
                                }
                            }
                            showActionMenu = false
                        },
                        enableBookFetch = enableBookFetch,
                        enableChapterFetch = enableChapterFetch,
                        enableChaptersFetch = enableChaptersFetch,
                        onDismiss = { showActionMenu = false }
                    )
                }
                
                // Loading overlay for fetch operations
                AnimatedVisibility(
                    visible = showFetchFeedback,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(32.dp)
                                .shadow(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = fetchFeedbackMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                
                // Loading overlay for page loading
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.3f)
                            .background(MaterialTheme.colorScheme.background)
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernWebViewTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    currentUrl: String,
    isLoading: Boolean,
    showUrlBar: Boolean,
    onShowUrlBarChange: (Boolean) -> Unit,
    onUrlChange: (String) -> Unit,
    onGoToUrl: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    urlFocusRequester: FocusRequester,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(androidx.compose.foundation.layout.WindowInsetsSides.Top))
            .shadow(elevation = 4.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top row with back button, URL bar, and refresh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button - fixed width
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // URL bar - flexible width with weight
                if (showUrlBar) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        TextField(
                            value = currentUrl,
                            onValueChange = onUrlChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(urlFocusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            placeholder = { 
                                Text(
                                    "Enter URL", 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = onGoToUrl,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Go",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .clickable { onShowUrlBarChange(true) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = currentUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Refresh button - fixed width
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                        contentDescription = if (isLoading) "Stop" else "Refresh",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Bottom navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Navigation buttons in a Row
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Back navigation button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (canGoBack) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .size(36.dp)
                            .clickable(enabled = canGoBack) { onGoBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (canGoBack) MaterialTheme.colorScheme.onSecondary
                                  else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Forward navigation button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (canGoForward) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .size(36.dp)
                            .clickable(enabled = canGoForward) { onGoForward() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (canGoForward) MaterialTheme.colorScheme.onSecondary
                                  else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Spacer to push the loading indicator to the right
                Spacer(modifier = Modifier.weight(1f))
                
                // Loading indicator (visible only when loading)
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        }
    }
}

@Composable
private fun ActionMenu(
    onFetchBook: () -> Unit,
    onFetchChapter: () -> Unit,
    onFetchChapters: () -> Unit,
    enableBookFetch: Boolean,
    enableChapterFetch: Boolean,
    enableChaptersFetch: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(elevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Fetch Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (enableBookFetch) {
                ActionMenuItem(
                    icon = Icons.Outlined.Book,
                    title = "Fetch Book Details",
                    description = "Download book information from this page",
                    onClick = onFetchBook
                )
            }
            
            if (enableChapterFetch) {
                ActionMenuItem(
                    icon = Icons.Outlined.AutoStories,
                    title = "Fetch Current Chapter",
                    description = "Download this chapter's content",
                    onClick = onFetchChapter
                )
            }
            
            if (enableChaptersFetch) {
                ActionMenuItem(
                    icon = Icons.Outlined.BookmarkAdd,
                    title = "Fetch All Chapters",
                    description = "Find and download all book chapters",
                    onClick = onFetchChapters
                )
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Cancel button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
