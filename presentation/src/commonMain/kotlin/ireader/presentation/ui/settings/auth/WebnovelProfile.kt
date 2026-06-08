package ireader.presentation.ui.settings.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.gamification.AchievementView
import ireader.domain.models.gamification.OwnedTitle
import ireader.domain.models.gamification.ProfileComment
import ireader.domain.models.gamification.ReadingActivityItem
import ireader.domain.models.remote.Badge
import ireader.presentation.ui.core.ui.AsyncImage

/** Rarity accents — the only non-theme colors (Material has no rarity palette). */
private fun rarityTint(r: String): Color = when (r.uppercase()) {
    "LEGENDARY" -> Color(0xFFFFB300)
    "PLATINUM" -> Color(0xFF00BCD4)
    "EPIC", "GOLD" -> Color(0xFF9C27B0)
    "RARE", "SILVER" -> Color(0xFF2196F3)
    else -> Color(0xFF8D6E63)
}

private fun fmtHours(min: Long): String {
    val h = min / 60; val m = min % 60
    return if (h > 0) "${h}h" else "${m}m"
}

/** Fully custom Webnovel-style profile, themed via MaterialTheme.colorScheme. */
@Composable
fun WebnovelProfileScreen(
    state: ProfileState,
    onBack: () -> Unit,
    onEditName: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onSignIn: () -> Unit,
    onCheckIn: () -> Unit,
    onActivateTitle: (String?) -> Unit,
    onOpenDiscord: () -> Unit,
    onPostComment: (String) -> Unit,
    onOpenBook: (Long) -> Unit,
) {
    val signedIn = state.currentUser != null
    val username = state.currentUser?.username ?: "Guest Reader"
    val ownedBadges = remember(state.featuredBadges, state.achievementBadges) {
        (state.featuredBadges + state.achievementBadges).distinctBy { it.id }
    }
    val activeTitle = state.ownedTitles.firstOrNull { it.isActive }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ProfileHeader(
                    username = username,
                    level = state.level,
                    levelTitle = state.levelTitle,
                    spiritStones = state.spiritStones,
                    checkinStreak = state.checkinStreak,
                    discordLinked = state.discordLinked,
                    discordUsername = state.discordUsername,
                    discordOnline = state.discordOnline,
                    leaderboardRank = state.leaderboardRank,
                    avatarUrl = state.avatarUrl,
                    coverUrl = state.coverUrl,
                    bio = state.bio,
                    joinedAt = state.joinedAt,
                    followers = state.followers,
                    following = state.following,
                    signedIn = signedIn,
                    onBack = onBack,
                    onEditName = onEditName,
                    onEditProfile = onEditProfile,
                    onChangePassword = onChangePassword,
                    onLogout = onLogout,
                    onSignIn = onSignIn,
                )
            }
            item {
                StatStrip(
                    readingMinutes = state.readingTimeMinutes,
                    chapters = state.chaptersRead,
                    books = state.booksCompleted,
                    streak = state.readingStreak,
                )
            }
            item { XpPanel(level = state.level, progress = state.levelProgress, xp = state.xp, rank = state.leaderboardRank) }
            if (signedIn) item { CheckinPanel(streak = state.checkinStreak, onCheckIn = onCheckIn) }
            if (activeTitle != null) item { ActiveTitlePanel(activeTitle) }
            if (ownedBadges.isNotEmpty()) item { BadgesShowcase(ownedBadges) }
            if (state.achievements.isNotEmpty()) item { AchievementsShowcase(state.achievements) }
            if (state.ownedTitles.isNotEmpty()) item { TitlesPanel(state.ownedTitles, onActivate = onActivateTitle) }
            if (state.favoriteBooks.isNotEmpty()) item { FavoriteBooksPanel(state.favoriteBooks, onOpenBook) }
            if (state.recentActivity.isNotEmpty()) item { ActivityPanel(state.recentActivity) }
            if (signedIn) item { CommentsPanel(state.comments, onPostComment) }
            item { DiscordPanel(onOpenDiscord) }
        }
    }
}

@Composable
private fun ProfileHeader(
    username: String,
    level: Int,
    levelTitle: String,
    spiritStones: Long,
    checkinStreak: Int,
    discordLinked: Boolean,
    discordUsername: String?,
    discordOnline: Int?,
    leaderboardRank: Int,
    avatarUrl: String?,
    coverUrl: String?,
    bio: String,
    joinedAt: String?,
    followers: Int,
    following: Int,
    signedIn: Boolean,
    onBack: () -> Unit,
    onEditName: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onSignIn: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val onHeader = cs.onPrimaryContainer
    Box(Modifier.fillMaxWidth()) {
        Box(Modifier.matchParentSize()) {
            if (coverUrl != null) {
                AsyncImage(model = coverUrl, contentDescription = "Cover",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(cs.primary, cs.primaryContainer))))
            }
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(cs.primaryContainer.copy(alpha = 0.35f), cs.primaryContainer.copy(alpha = 0.96f)))))
        }

        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconCircle(Icons.AutoMirrored.Filled.ArrowBack, "Back", onHeader, onBack)
                Spacer(Modifier.weight(1f))
                Box {
                    var menu by remember { mutableStateOf(false) }
                    IconCircle(Icons.Filled.MoreVert, "Menu", onHeader) { menu = true }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        if (signedIn) {
                            DropdownMenuItem(text = { Text("Edit profile") }, onClick = { menu = false; onEditProfile() })
                            DropdownMenuItem(text = { Text("Edit name") }, onClick = { menu = false; onEditName() })
                            DropdownMenuItem(text = { Text("Change password") }, onClick = { menu = false; onChangePassword() })
                            DropdownMenuItem(text = { Text("Log out") }, onClick = { menu = false; onLogout() })
                        } else {
                            DropdownMenuItem(text = { Text("Sign in") }, onClick = { menu = false; onSignIn() })
                        }
                    }
                }
            }

            Spacer(Modifier.height(70.dp))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier.size(96.dp).clip(CircleShape).border(3.dp, cs.primary, CircleShape)
                            .background(cs.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (avatarUrl != null) {
                            AsyncImage(model = avatarUrl, contentDescription = "Avatar",
                                modifier = Modifier.size(96.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Text(username.take(1).uppercase(), color = onHeader, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(Modifier.padding(bottom = 2.dp).clip(RoundedCornerShape(8.dp)).background(cs.primary)
                        .padding(horizontal = 9.dp, vertical = 1.dp)) {
                        Text("Lv $level", color = cs.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(username, color = onHeader, fontSize = 23.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(levelTitle, color = onHeader.copy(alpha = 0.8f), fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

            Spacer(Modifier.height(6.dp))
            Text(
                bio.ifBlank { if (signedIn) "Tap ⋮ → Edit profile to add a bio" else "" },
                color = onHeader.copy(alpha = if (bio.isBlank()) 0.55f else 0.9f),
                fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
            )

            formatJoined(joinedAt)?.let { joined ->
                Spacer(Modifier.height(6.dp))
                Text("📅 $joined", color = onHeader.copy(alpha = 0.7f), fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                HeaderPill("💎 $spiritStones", onHeader)
                if (leaderboardRank > 0) { Spacer(Modifier.width(8.dp)); HeaderPill("🏆 #$leaderboardRank", onHeader) }
                if (checkinStreak > 0) { Spacer(Modifier.width(8.dp)); HeaderPill("🔥 $checkinStreak d", onHeader) }
            }
            if (discordLinked || discordOnline != null) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    if (discordLinked) HeaderPill("🎮 ${discordUsername ?: "Linked"} ✓", onHeader)
                    if (discordOnline != null) { Spacer(Modifier.width(8.dp)); HeaderPill("🟢 $discordOnline online", onHeader) }
                }
            }

            Spacer(Modifier.height(14.dp))
            if (signedIn) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CountCol(following, "Following", onHeader)
                    Box(Modifier.padding(horizontal = 26.dp).size(1.dp, 28.dp).background(onHeader.copy(alpha = 0.3f)))
                    CountCol(followers, "Followers", onHeader)
                }
            } else {
                Box(Modifier.fillMaxWidth().padding(horizontal = 40.dp).clip(RoundedCornerShape(24.dp))
                    .background(cs.primary).clickable(onClick = onSignIn).padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center) {
                    Text("Sign in to join the community", color = cs.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

private val MonthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private fun formatJoined(iso: String?): String? {
    if (iso.isNullOrBlank() || iso.length < 7) return null
    val year = iso.take(4)
    val month = iso.substring(5, 7).toIntOrNull() ?: return null
    if (month !in 1..12) return null
    return "Joined ${MonthNames[month - 1]} $year"
}

@Composable
private fun IconCircle(icon: ImageVector, cd: String, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, cd, tint = tint)
    }
}

@Composable
private fun HeaderPill(text: String, onHeader: Color) {
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(onHeader.copy(alpha = 0.12f))
        .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text, color = onHeader, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CountCol(value: Int, label: String, onHeader: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", color = onHeader, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = onHeader.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

@Composable
private fun PanelBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(18.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) { content() }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun StatStrip(readingMinutes: Long, chapters: Int, books: Int, streak: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCell("⏱", fmtHours(readingMinutes), "Time", Modifier.weight(1f))
        StatCell("📚", "$books", "Books", Modifier.weight(1f))
        StatCell("🔥", "$streak", "Streak", Modifier.weight(1f))
        StatCell("📖", "$chapters", "Chapters", Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(cs.surfaceVariant).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = cs.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = cs.onSurfaceVariant, fontSize = 11.sp)
    }
}

@Composable
private fun XpPanel(level: Int, progress: Float, xp: Long, rank: Int) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Level $level", color = cs.onSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (rank > 0) {
                    Text("🏆 Rank #$rank", color = cs.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(10.dp))
                }
                Text("$xp XP", color = cs.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(cs.onSurface.copy(alpha = 0.12f))) {
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(10.dp).clip(RoundedCornerShape(5.dp))
                    .background(Brush.horizontalGradient(listOf(cs.primary, cs.tertiary))))
            }
            Spacer(Modifier.height(6.dp))
            Text("Next: Level ${level + 1}", color = cs.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CheckinPanel(streak: Int, onCheckIn: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(18.dp))
        .background(cs.tertiaryContainer).clickable(onClick = onCheckIn).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔥", fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Daily Check-in", color = cs.onTertiaryContainer, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(if (streak > 0) "$streak-day streak · tap to claim" else "Start your streak & earn 💎",
                    color = cs.onTertiaryContainer.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(cs.tertiary).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Claim", color = cs.onTertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Prominent active-title banner with rarity glow. */
@Composable
private fun ActiveTitlePanel(title: OwnedTitle) {
    val cs = MaterialTheme.colorScheme
    val tint = rarityTint(title.rarity)
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(18.dp))
        .background(Brush.horizontalGradient(listOf(tint.copy(alpha = 0.28f), cs.surfaceVariant)))
        .border(1.dp, tint.copy(alpha = 0.6f), RoundedCornerShape(18.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(tint.copy(alpha = 0.25f))
                .border(2.dp, tint, CircleShape), contentAlignment = Alignment.Center) { Text("📛", fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Active Title", color = cs.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(title.titleName, color = cs.onSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(14.dp)).background(tint).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(title.rarity.lowercase().replaceFirstChar { it.uppercase() }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Earned badges rendered from their Supabase image URLs, with rarity glow rings. */
@Composable
private fun BadgesShowcase(badges: List<Badge>) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            SectionTitle("Badges")
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(badges) { b ->
                    val tint = rarityTint(b.rarity)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(74.dp)) {
                        Box(Modifier.size(66.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.matchParentSize().clip(CircleShape)
                                .background(Brush.radialGradient(listOf(tint.copy(alpha = 0.45f), Color.Transparent))))
                            Box(Modifier.size(56.dp).clip(CircleShape).border(2.dp, tint, CircleShape)
                                .background(cs.surface), contentAlignment = Alignment.Center) {
                                AsyncImage(model = b.imageUrl, contentDescription = b.name,
                                    modifier = Modifier.size(56.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(b.name, color = cs.onSurface, fontSize = 10.sp, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

/** Premium achievement medallions: rarity radial glow + ring, not flat circles. */
@Composable
private fun AchievementsShowcase(achievements: List<AchievementView>) {
    val cs = MaterialTheme.colorScheme
    val sorted = achievements.sortedWith(compareByDescending<AchievementView> { it.isCompleted }.thenByDescending { it.fraction })
    PanelBox {
        Column {
            SectionTitle("Achievements")
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(sorted) { a ->
                    val tint = rarityTint(a.def.tier)
                    val on = a.isCompleted
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(74.dp)) {
                        Box(Modifier.size(66.dp), contentAlignment = Alignment.Center) {
                            if (on) Box(Modifier.matchParentSize().clip(CircleShape)
                                .background(Brush.radialGradient(listOf(tint.copy(alpha = 0.5f), Color.Transparent))))
                            Box(Modifier.size(56.dp).clip(CircleShape)
                                .background(if (on) Brush.verticalGradient(listOf(tint.copy(alpha = 0.4f), tint.copy(alpha = 0.15f)))
                                            else Brush.verticalGradient(listOf(cs.surface, cs.surface)))
                                .border(2.dp, if (on) tint else cs.onSurface.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center) {
                                Text(a.def.icon, fontSize = 26.sp)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        // Fixed 2-line name slot so every column is the same height.
                        Box(Modifier.height(28.dp), contentAlignment = Alignment.TopCenter) {
                            Text(a.def.name, color = if (on) cs.onSurface else cs.onSurfaceVariant, fontSize = 10.sp,
                                maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(4.dp))
                        // Always render the strip → bars align across completed & locked items.
                        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(cs.onSurface.copy(alpha = 0.12f))) {
                            Box(Modifier.fillMaxWidth(if (on) 1f else a.fraction).height(4.dp)
                                .clip(RoundedCornerShape(2.dp)).background(tint))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TitlesPanel(titles: List<OwnedTitle>, onActivate: (String?) -> Unit) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            SectionTitle("Titles")
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(titles) { t ->
                    val active = t.isActive
                    val tint = rarityTint(t.rarity)
                    Box(Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (active) tint.copy(alpha = 0.25f) else cs.surface)
                        .border(1.dp, tint, RoundedCornerShape(20.dp))
                        .clickable { onActivate(if (active) null else t.titleId) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text("📛 ${t.titleName}", color = cs.onSurface, fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteBooksPanel(books: List<FavoriteBook>, onOpen: (Long) -> Unit) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            SectionTitle("Favorite Books")
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(books) { b ->
                    Column(Modifier.width(96.dp).clickable { onOpen(b.id) }) {
                        Box(Modifier.fillMaxWidth().aspectRatio(0.7f).clip(RoundedCornerShape(12.dp)).background(cs.surface)) {
                            if (b.cover.isNotBlank()) {
                                AsyncImage(model = b.cover, contentDescription = b.title,
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(b.title, color = cs.onSurface, fontSize = 11.sp, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private data class ActivityStyle(val emoji: String, val accent: Color, val verb: String)
private fun activityStyle(type: String): ActivityStyle = when (type) {
    "ACHIEVEMENT" -> ActivityStyle("🏅", Color(0xFFFFB300), "Unlocked")
    "REVIEW" -> ActivityStyle("⭐", Color(0xFFFF8F00), "Reviewed")
    "VOTE" -> ActivityStyle("🗳", Color(0xFF7E57C2), "Voted for")
    else -> ActivityStyle("📖", Color(0xFF42A5F5), "Read")
}

/** Rich activity feed — colored medallion + accent stripe + book chip. */
@Composable
private fun ActivityPanel(activity: List<ReadingActivityItem>) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            SectionTitle("Recent Activity")
            Spacer(Modifier.height(12.dp))
            activity.take(12).forEachIndexed { i, a ->
                val s = activityStyle(a.type)
                if (i > 0) Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surface),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(4.dp).height(52.dp).background(s.accent))
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.size(40.dp).clip(CircleShape).background(s.accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center) { Text(s.emoji, fontSize = 20.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                        Text(a.description.ifBlank { "${s.verb} ${a.bookTitle ?: ""}".trim() },
                            color = cs.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val sub = when {
                            a.chapterNumber != null && a.bookTitle != null -> "${a.bookTitle} · Ch ${a.chapterNumber}"
                            a.bookTitle != null -> a.bookTitle!!
                            else -> s.verb
                        }
                        Text(sub, color = cs.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.width(10.dp))
                }
            }
        }
    }
}

@Composable
private fun CommentsPanel(comments: List<ProfileComment>, onPost: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            SectionTitle("Comments")
            Spacer(Modifier.height(10.dp))
            if (comments.isEmpty()) {
                Text("No comments yet — be the first.", color = cs.onSurfaceVariant, fontSize = 13.sp)
            } else {
                comments.take(20).forEach { c ->
                    Row(Modifier.padding(vertical = 6.dp)) {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(cs.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center) {
                            Text(c.commenterName.take(1).uppercase(), color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.commenterName, color = cs.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(c.text, color = cs.onSurfaceVariant, fontSize = 13.sp)
                        }
                        if (c.likes > 0) Text("👍 ${c.likes}", color = cs.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            var draft by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(cs.surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (draft.isEmpty()) Text("Write a public comment…", color = cs.onSurfaceVariant, fontSize = 13.sp)
                    BasicTextField(
                        value = draft, onValueChange = { draft = it }, singleLine = true,
                        textStyle = TextStyle(color = cs.onSurface, fontSize = 13.sp),
                        cursorBrush = SolidColor(cs.primary), modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(40.dp).clip(CircleShape).background(cs.primary)
                    .clickable(enabled = draft.isNotBlank()) { onPost(draft.trim()); draft = "" },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = cs.onPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DiscordPanel(onOpen: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(18.dp))
        .background(cs.secondaryContainer).clickable(onClick = onOpen).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("💬", fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Join the conversation", color = cs.onSecondaryContainer, fontWeight = FontWeight.Bold)
                Text("Chat, clubs & events live on Discord", color = cs.onSecondaryContainer.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            Text("Open →", color = cs.onSecondaryContainer, fontWeight = FontWeight.Bold)
        }
    }
}
