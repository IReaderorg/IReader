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
) {
    val signedIn = state.currentUser != null
    val username = state.currentUser?.username ?: "Guest Reader"

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
            item { XpPanel(level = state.level, progress = state.levelProgress, xp = state.xp) }
            if (signedIn) item { CheckinPanel(streak = state.checkinStreak, onCheckIn = onCheckIn) }
            if (state.achievements.isNotEmpty()) item { AchievementsPanel(state.achievements) }
            if (state.ownedTitles.isNotEmpty()) item { TitlesPanel(state.ownedTitles, onActivate = onActivateTitle) }
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
        // Backdrop fills the whole header so every text sits on the scrim (readable contrast).
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

            // Avatar with level ring + level badge
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

            if (bio.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(bio, color = onHeader.copy(alpha = 0.9f), fontSize = 13.sp, maxLines = 3,
                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp))
            }

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
private fun StatStrip(readingMinutes: Long, chapters: Int, books: Int, streak: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCell("⏱", fmtHours(readingMinutes), "Time", Modifier.weight(1f))
        StatCell("📖", "$chapters", "Chapters", Modifier.weight(1f))
        StatCell("📚", "$books", "Books", Modifier.weight(1f))
        StatCell("🔥", "$streak", "Streak", Modifier.weight(1f))
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
private fun PanelBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(18.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) { content() }
}

@Composable
private fun XpPanel(level: Int, progress: Float, xp: Long) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Level $level", color = cs.onSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
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

@Composable
private fun AchievementsPanel(achievements: List<AchievementView>) {
    val cs = MaterialTheme.colorScheme
    val sorted = achievements.sortedWith(compareByDescending<AchievementView> { it.isCompleted }.thenByDescending { it.fraction })
    PanelBox {
        Column {
            Text("Achievements", color = cs.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sorted) { a ->
                    val tint = rarityTint(a.def.tier)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                        Box(Modifier.size(56.dp).clip(CircleShape)
                            .background(if (a.isCompleted) tint.copy(alpha = 0.22f) else cs.surface)
                            .border(2.dp, tint.copy(alpha = if (a.isCompleted) 1f else 0.3f), CircleShape),
                            contentAlignment = Alignment.Center) { Text(a.def.icon, fontSize = 24.sp) }
                        Spacer(Modifier.height(4.dp))
                        Text(a.def.name, color = cs.onSurface, fontSize = 10.sp, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                        if (!a.isCompleted) {
                            Spacer(Modifier.height(2.dp))
                            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(cs.onSurface.copy(alpha = 0.12f))) {
                                Box(Modifier.fillMaxWidth(a.fraction).height(4.dp).clip(RoundedCornerShape(2.dp)).background(tint))
                            }
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
            Text("Titles", color = cs.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
private fun ActivityPanel(activity: List<ReadingActivityItem>) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            Text("Recent Activity", color = cs.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            activity.take(10).forEach { a ->
                val emoji = when (a.type) { "ACHIEVEMENT" -> "🏅"; "REVIEW" -> "⭐"; "VOTE" -> "🗳"; else -> "📖" }
                Text("$emoji  ${a.description.ifBlank { a.bookTitle ?: a.type }}",
                    color = cs.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
            }
        }
    }
}

@Composable
private fun CommentsPanel(comments: List<ProfileComment>, onPost: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    PanelBox {
        Column {
            Text("Comments", color = cs.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            // Composer
            var draft by remember { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(cs.surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (draft.isEmpty()) Text("Write a public comment…", color = cs.onSurfaceVariant, fontSize = 13.sp)
                    BasicTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        singleLine = true,
                        textStyle = TextStyle(color = cs.onSurface, fontSize = 13.sp),
                        cursorBrush = SolidColor(cs.primary),
                        modifier = Modifier.fillMaxWidth(),
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
