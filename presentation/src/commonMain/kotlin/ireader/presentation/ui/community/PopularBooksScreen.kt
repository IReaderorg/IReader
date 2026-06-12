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
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.core.ui.AsyncImage
import ireader.presentation.ui.popular.SourceInstallDialog

private val Gold = Color(0xFFFFC73C)
private val Silver = Color(0xFFC6CCD6)
private val Bronze = Color(0xFFD08B4B)
private fun rankColor(rank: Int): Color = when (rank) { 1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> Color(0xFF8C7BAE) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularBooksScreen(
    vm: PopularBooksViewModel,
    onBackPressed: () -> Unit,
    onNavigateToBook: (Long) -> Unit = {},
    onNavigateToGlobalSearch: (String) -> Unit = {},
    onOpenExternalUrl: (String) -> Unit = {},
    onAddSources: () -> Unit = {},
    onNavigateToExtensions: () -> Unit = {},
) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()

    val groups = state.groups

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            last != null && last.index >= groups.size - 3 && !state.isLoadingMore && state.hasMore
        }
    }
    androidx.compose.runtime.LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) vm.loadMore() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { DiscoverHeader(onBack = onBackPressed, onRefresh = { vm.refresh() }, discordOnline = state.discordOnline) }

            when {
                state.isInitialLoading && groups.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                groups.isEmpty() -> item {
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
                    groups.firstOrNull()?.let { hero ->
                        item { FeaturedHero(hero, onOpen = { vm.openBookDetail(it) }) }
                    }
                    if (groups.size > 1) {
                        item { SectionTitle("🔥 Trending Now") }
                        item { TrendingRail(groups.take(10), onOpen = { vm.openBookDetail(it) }) }
                    }
                    item { SectionTitle("🏆 Top Most Read") }
                    itemsIndexed(groups, key = { _, g -> g.key }) { index, g ->
                        RankedBookRow(
                            rank = index + 1,
                            group = g,
                            voted = state.votedBookIds.contains(g.primary.bookId),
                            onOpen = { vm.openBookDetail(g) },
                            onVote = { vm.vote(g.primary.bookId) },
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

    // Book detail sheet: pick a source variant, see how popular each is.
    state.selectedBook?.let { group ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { vm.dismissBookDetail() }, sheetState = sheetState) {
            BookDetailSheet(
                group = group,
                resolving = state.resolvingSourceFor == group.key,
                voted = state.votedBookIds.contains(group.primary.bookId),
                onVote = { vm.vote(group.primary.bookId) },
                onPickSource = { variant ->
                    vm.openSource(group, variant) { action ->
                        when (action) {
                            is BookNavigationAction.OpenLocalBook -> { vm.dismissBookDetail(); onNavigateToBook(action.bookId) }
                            is BookNavigationAction.OpenGlobalSearch -> { vm.dismissBookDetail(); onNavigateToGlobalSearch(action.query) }
                            is BookNavigationAction.OpenExternalUrl -> onOpenExternalUrl(action.url)
                            is BookNavigationAction.SourceMissing -> { vm.dismissBookDetail(); onAddSources() }
                        }
                    }
                },
            )
        }
    }

    // Source install dialog
    if (state.showSourceInstallDialog) {
        SourceInstallDialog(
            sourceName = state.pendingInstallSourceName,
            sourceGroup = state.pendingInstallSourceGroup,
            onDismiss = { vm.dismissSourceInstallDialog() },
            onInstall = {
                vm.dismissSourceInstallDialog()
                onNavigateToExtensions()
            }
        )
    }
}

@Composable
private fun DiscoverHeader(onBack: () -> Unit, onRefresh: () -> Unit, discordOnline: Int?) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth()
        .background(Brush.verticalGradient(listOf(cs.primary, cs.primaryContainer)))
        .padding(bottom = 14.dp)) {
        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(50)).clickable(onClick = onBack),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = cs.onPrimary)
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(50)).clickable(onClick = onRefresh),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Refresh, "Refresh", tint = cs.onPrimary)
                }
            }
            Text("Discover", color = cs.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp))
            Text("What the community is reading", color = cs.onPrimary.copy(alpha = 0.8f), fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            val uriHandler = LocalUriHandler.current
            Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(RoundedCornerShape(20.dp)).background(cs.onPrimary.copy(alpha = 0.16f))
                    .clickable { uriHandler.openUri(ireader.i18n.discord) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("💬  Join the community on Discord", color = cs.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                if (discordOnline != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(cs.onPrimary.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("🟢 $discordOnline online", color = cs.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityNewsCard(ann: ireader.domain.models.gamification.CommunityAnnouncement) {
    val cs = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(16.dp))
        .background(cs.secondaryContainer)
        .clickable(enabled = ann.discordUrl != null) { ann.discordUrl?.let { uriHandler.openUri(it) } }
        .padding(16.dp)) {
        Column {
            Text("📣  Community News", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSecondaryContainer)
            Spacer(Modifier.height(4.dp))
            ann.title?.let { Text(it, fontWeight = FontWeight.SemiBold, color = cs.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            ann.body?.let { Text(it, fontSize = 13.sp, color = cs.onSecondaryContainer.copy(alpha = 0.85f), maxLines = 2, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun FeaturedHero(group: PopularBookGroup, onOpen: (PopularBookGroup) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(190.dp)
        .clip(RoundedCornerShape(20.dp)).background(cs.surfaceVariant).clickable { onOpen(group) }) {
        group.coverUrl?.let {
            AsyncImage(model = it, contentDescription = group.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))))
        Row(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalAlignment = Alignment.Bottom) {
            group.coverUrl?.let {
                AsyncImage(model = it, contentDescription = null,
                    modifier = Modifier.width(72.dp).height(104.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Gold).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("🔥 #1 this week", color = Color(0xFF3A1C9E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(group.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${formatCount(group.totalReaders)} reading · ${group.sourceCount} source${if (group.sourceCount > 1) "s" else ""}",
                    color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TrendingRail(groups: List<PopularBookGroup>, onOpen: (PopularBookGroup) -> Unit) {
    val cs = MaterialTheme.colorScheme
    LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(groups, key = { _, g -> g.key }) { index, g ->
            Column(Modifier.width(108.dp).clickable { onOpen(g) }) {
                Box(Modifier.fillMaxWidth().aspectRatio(0.7f).clip(RoundedCornerShape(12.dp)).background(cs.surfaceVariant)) {
                    g.coverUrl?.let { AsyncImage(model = it, contentDescription = g.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                    Box(Modifier.padding(6.dp).size(22.dp).clip(RoundedCornerShape(50)).background(rankColor(index + 1)),
                        contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(g.title, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF7043), modifier = Modifier.size(12.dp))
                    Text(" ${formatCount(g.totalReaders)}", fontSize = 11.sp, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RankedBookRow(rank: Int, group: PopularBookGroup, voted: Boolean, onOpen: () -> Unit, onVote: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable { onOpen() }, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(34.dp), contentAlignment = Alignment.Center) {
            Text("$rank", fontWeight = FontWeight.Bold, fontSize = if (rank <= 3) 22.sp else 16.sp,
                color = if (rank <= 3) rankColor(rank) else cs.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.width(54.dp).height(78.dp).clip(RoundedCornerShape(8.dp)).background(cs.surfaceVariant)) {
            group.coverUrl?.let { AsyncImage(model = it, contentDescription = group.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(group.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, color = cs.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("${group.sourceCount} source${if (group.sourceCount > 1) "s" else ""} · top: ${group.primary.sourceName}",
                fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF7043), modifier = Modifier.size(13.dp))
                Text(" ${formatCount(group.totalReaders)} reading", fontSize = 12.sp, color = cs.primary, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.width(8.dp))
        PowerStoneVoteButton(voted = voted, onVote = onVote)
    }
}

@Composable
private fun PowerStoneVoteButton(voted: Boolean, onVote: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg = if (voted) Gold.copy(alpha = 0.18f) else cs.primary.copy(alpha = 0.12f)
    val tint = if (voted) Gold else cs.primary
    Column(Modifier.clip(RoundedCornerShape(12.dp)).background(bg).clickable(enabled = !voted, onClick = onVote)
        .padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (voted) "💎" else "⚡", fontSize = 16.sp)
        Text(if (voted) "Voted" else "Vote", fontSize = 10.sp, color = tint, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BookDetailSheet(
    group: PopularBookGroup,
    resolving: Boolean,
    voted: Boolean,
    onVote: () -> Unit,
    onPickSource: (BookSourceVariant) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 28.dp)) {
        Row {
            Box(Modifier.width(92.dp).height(132.dp).clip(RoundedCornerShape(12.dp)).background(cs.surfaceVariant)) {
                group.coverUrl?.let { AsyncImage(model = it, contentDescription = group.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(group.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = cs.onSurface, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF7043), modifier = Modifier.size(14.dp))
                    Text(" ${formatCount(group.totalReaders)} reading", fontSize = 13.sp, color = cs.primary, fontWeight = FontWeight.SemiBold)
                }
                Text("Available on ${group.sourceCount} source${if (group.sourceCount > 1) "s" else ""}", fontSize = 12.sp, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                PowerStoneVoteButton(voted = voted, onVote = onVote)
            }
        }

        group.description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, fontSize = 13.sp, color = cs.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
        }

        Spacer(Modifier.height(18.dp))
        Text("Read on", fontWeight = FontWeight.Bold, color = cs.onSurface)
        Text("Sources ranked by how many readers use them", fontSize = 12.sp, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))

        group.sources.forEachIndexed { i, v ->
            if (i > 0) Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceVariant)
                .clickable(enabled = !resolving) { onPickSource(v) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(cs.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center) {
                    if (i == 0) Text("👑", fontSize = 16.sp) else Icon(Icons.Filled.Extension, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(v.sourceName, fontWeight = FontWeight.SemiBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${formatCount(v.readers)} readers" + if (i == 0) " · most popular" else "", fontSize = 12.sp, color = cs.onSurfaceVariant)
                }
                if (resolving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Open →", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
