package ireader.presentation.ui.settings.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WebnovelGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF7B2FF7), Color(0xFF5B2AE0), Color(0xFF3A1C9E)),
)
private val LevelRing = Color(0xFFFFC73C)

private fun formatHours(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h" else "${m}m"
}

/**
 * Immersive Webnovel-style profile header: full-bleed gradient, avatar with level ring,
 * username + level title, currency/streak chips, follower counts.
 */
@Composable
fun WebnovelProfileHeader(
    username: String,
    levelTitle: String,
    level: Int,
    spiritStones: Long,
    checkinStreak: Int,
    discordUsername: String?,
    followers: Int,
    following: Int,
    signedIn: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSignIn: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(WebnovelGradient)
            .padding(bottom = 20.dp),
    ) {
        Column(Modifier.statusBarsPadding()) {
            // Top bar (back + edit), overlaid on the gradient
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onEdit),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Edit, "Edit", tint = Color.White) }
            }

            Spacer(Modifier.height(4.dp))

            // Avatar with level ring + level badge
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier.size(92.dp).clip(CircleShape)
                            .border(3.dp, LevelRing, CircleShape)
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            username.take(1).uppercase(),
                            color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        Modifier.padding(bottom = 2.dp).clip(RoundedCornerShape(8.dp))
                            .background(LevelRing).padding(horizontal = 8.dp, vertical = 1.dp),
                    ) {
                        Text("Lv $level", color = Color(0xFF3A1C9E), fontSize = 11.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                username, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                levelTitle, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            // Chips row: stones, streak, discord
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                HeaderChip(Icons.Filled.Diamond, "$spiritStones", Color(0xFF6CE0FF))
                if (checkinStreak > 0) {
                    Spacer(Modifier.width(8.dp))
                    HeaderChip(Icons.Filled.LocalFireDepartment, "$checkinStreak d", Color(0xFFFFB04A))
                }
                if (discordUsername != null) {
                    Spacer(Modifier.width(8.dp))
                    HeaderChip(Icons.Filled.Whatshot, "Discord ✓", Color(0xFF8C9EFF))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Followers / following  OR sign-in nudge
            if (signedIn) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CountBlock(following, "Following")
                    Box(Modifier.padding(horizontal = 24.dp).size(1.dp, 28.dp)
                        .background(Color.White.copy(alpha = 0.3f)))
                    CountBlock(followers, "Followers")
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .clickable(onClick = onSignIn)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Sign in to join the community", color = Color(0xFF5B2AE0),
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(icon: ImageVector, label: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CountBlock(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

/** Webnovel-style 4-up stat tiles that overlap the header bottom (card pulled up). */
@Composable
fun WebnovelStatTiles(readingMinutes: Long, books: Int, chapters: Int, streak: Int) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatTile(Icons.Filled.Schedule, formatHours(readingMinutes), "Time", Modifier.weight(1f))
        StatTile(Icons.Filled.MenuBook, "$chapters", "Chapters", Modifier.weight(1f))
        StatTile(Icons.Filled.LibraryBooks, "$books", "Books", Modifier.weight(1f))
        StatTile(Icons.Filled.LocalFireDepartment, "$streak", "Streak", Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Prominent daily check-in banner (Webnovel surfaces this front-and-center). */
@Composable
fun WebnovelCheckinBanner(checkinStreak: Int, lastReward: Int, signedIn: Boolean, onCheckIn: () -> Unit) {
    if (!signedIn) return
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFF9D2F), Color(0xFFFF6F3C))))
            .clickable(onClick = onCheckIn)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Daily Check-in", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    if (checkinStreak > 0) "$checkinStreak-day streak · tap to claim today"
                    else "Tap to start your streak & earn Spirit Stones",
                    color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp,
                )
            }
            Box(
                Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) { Text("Claim", color = Color(0xFFFF6F3C), fontWeight = FontWeight.Bold) }
        }
    }
}
