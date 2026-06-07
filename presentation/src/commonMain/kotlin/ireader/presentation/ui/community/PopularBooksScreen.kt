package ireader.presentation.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.remote.PopularBook
import ireader.presentation.ui.core.ui.AsyncImage

private val DiscoverGradient = Brush.verticalGradient(listOf(Color(0xFF7B2FF7), Color(0xFF4A1FB0)))
private val Gold = Color(0xFFFFC73C)
private val Silver = Color(0xFFC6CCD6)
private val Bronze = Color(0xFFD08B4B)

private fun rankColor(rank: Int): Color = when (rank) { 1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> Color(0xFF8C7BAE) }

private fun formatReaders(n: Int): String = when {
    n >= 1_000_000 -> "${(n / 100_000) / 10.0}M"
    n >= 1_000 -> "${(n / 100) / 10.0}k"
    else -> "$n"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularBooksScreen(
    vm: PopularBooksViewModel,
    onBackPressed: () -> Unit,
    onNavigateToBook: (Long) -> Unit = {},
    onNavigateToGlobalSearch: (String) -> Unit = {},
    onOpenExternalUrl: (String) -> Unit = {},
) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()
    val uriHandler = LocalUriHandler.current

    val openBook: (PopularBook) -> Unit = { book ->
        vm.checkBookInLibrary(book.bookId, book.title, book.sourceId, book.sourceName) { action ->
            when (action) {
                is BookNavigationAction.OpenLocalBook -> onNavigateToBook(action.bookId)
                is BookNavigationAction.OpenGlobalSearch -> onNavigateToGlobalSearch(action.query)
                is BookNavigationAction.OpenExternalUrl -> uriHandler.openUri(action.url)
            }
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            last != null && last.index >= state.books.size - 3 && !state.isLoadingMore && state.hasMore
        }
    }
    androidx.compose.runtime.LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) vm.loadMore() }

    val ranked = remember(state.books) { state.books.sortedByDescending { it.readerCount } }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { DiscoverHeader(onBack = onBackPressed, onRefresh = { vm.refresh() }) }

            when {
                state.isInitialLoading && state.books.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.books.isEmpty() -> item {
                    Column(Modifier.fillMaxWidth().padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📚", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Nothing here yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    if (state.announcements.isNotEmpty()) {
                        item { CommunityNewsCard(state.announcements.first()) }
                    }
                    ranked.firstOrNull()?.let { hero ->
                        item { FeaturedHero(hero, onOpen = openBook) }
                    }
                    if (ranked.size > 1) {
                        item { SectionTitle("🔥 Trending Now") }
                        item { TrendingRail(ranked.take(10), onOpen = openBook) }
                    }
                    item { SectionTitle("🏆 Top Most Read") }
                    itemsIndexed(ranked, key = { _, b -> b.bookId }) { index, book ->
                        RankedBookRow(
                            rank = index + 1,
                            book = book,
                            voted = state.votedBookIds.contains(book.bookId),
                            onOpen = openBook,
                            onVote = { vm.vote(book.bookId) },
                        )
                    }
                    if (state.isLoadingMore) item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverHeader(onBack: () -> Unit, onRefresh: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(DiscoverGradient).padding(bottom = 14.dp)) {
        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(50)).clickable(onClick = onBack),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(50)).clickable(onClick = onRefresh),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = Color.White)
                }
            }
            Text("Discover", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp))
            Text("What the community is reading", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            val uriHandler = LocalUriHandler.current
            Box(
                Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.16f))
                    .clickable { uriHandler.openUri(ireader.i18n.discord) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("💬  Join the community on Discord", color = Color.White, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CommunityNewsCard(ann: ireader.domain.models.gamification.CommunityAnnouncement) {
    val uriHandler = LocalUriHandler.current
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(enabled = ann.discordUrl != null) { ann.discordUrl?.let { uriHandler.openUri(it) } }
            .padding(16.dp),
    ) {
        Column {
            Text("📣  Community News", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.height(4.dp))
            ann.title?.let {
                Text(it, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            ann.body?.let {
                Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun FeaturedHero(book: PopularBook, onOpen: (PopularBook) -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(190.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onOpen(book) },
    ) {
        if (book.coverUrl != null) {
            AsyncImage(model = book.coverUrl, contentDescription = book.title,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))))
        Row(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalAlignment = Alignment.Bottom) {
            if (book.coverUrl != null) {
                AsyncImage(model = book.coverUrl, contentDescription = null,
                    modifier = Modifier.width(72.dp).height(104.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop)
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Gold).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("🔥 #1 this week", color = Color(0xFF3A1C9E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(book.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${formatReaders(book.readerCount)} reading", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TrendingRail(books: List<PopularBook>, onOpen: (PopularBook) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(books, key = { _, b -> b.bookId }) { index, book ->
            Column(Modifier.width(108.dp).clickable { onOpen(book) }) {
                Box(Modifier.fillMaxWidth().aspectRatio(0.7f).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)) {
                    if (book.coverUrl != null) {
                        AsyncImage(model = book.coverUrl, contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Box(Modifier.padding(6.dp).size(22.dp).clip(RoundedCornerShape(50))
                        .background(rankColor(index + 1)), contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(book.title, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF7043), modifier = Modifier.size(12.dp))
                    Text(" ${formatReaders(book.readerCount)}", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RankedBookRow(
    rank: Int,
    book: PopularBook,
    voted: Boolean,
    onOpen: (PopularBook) -> Unit,
    onVote: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable { onOpen(book) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(34.dp), contentAlignment = Alignment.Center) {
            Text("$rank", fontWeight = FontWeight.Bold, fontSize = if (rank <= 3) 22.sp else 16.sp,
                color = if (rank <= 3) rankColor(rank) else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.width(54.dp).height(78.dp).clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (book.coverUrl != null) {
                AsyncImage(model = book.coverUrl, contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(book.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            if (book.sourceName.isNotBlank()) {
                Text(book.sourceName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF7043), modifier = Modifier.size(13.dp))
                Text(" ${formatReaders(book.readerCount)} reading", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.width(8.dp))
        PowerStoneVoteButton(voted = voted, onVote = onVote)
    }
}

/** Free daily Power-Stone vote — a "like with a budget" that drives the Trending list. */
@Composable
private fun PowerStoneVoteButton(voted: Boolean, onVote: () -> Unit) {
    val bg = if (voted) Gold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val tint = if (voted) Gold else MaterialTheme.colorScheme.primary
    Column(
        Modifier.clip(RoundedCornerShape(12.dp)).background(bg)
            .clickable(enabled = !voted, onClick = onVote)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(if (voted) "💎" else "⚡", fontSize = 16.sp)
        Text(if (voted) "Voted" else "Vote", fontSize = 10.sp, color = tint, fontWeight = FontWeight.Bold)
    }
}
