package ireader.presentation.ui.characterart

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import ireader.domain.models.characterart.CharacterArt
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.presentation.ui.component.isTableUi
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Detail screen for viewing character art
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterArtDetailScreen(
    art: CharacterArt,
    onBack: () -> Unit,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    onReportClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    isOwner: Boolean = false,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    val isWideScreen = isTableUi()
    val scrollState = rememberScrollState()
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    
    // Image zoom state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onReport = { reason ->
                onReportClick()
                showReportDialog = false
            }
        )
    }
    
    if (showDeleteDialog && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Art?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        modifier = modifier.padding(paddingValues),
        topBar = {
            Surface(
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    
                    // Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onShareClick,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.5f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share")
                        }
                        
                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.Black.copy(alpha = 0.5f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Report") },
                                    onClick = {
                                        showMoreMenu = false
                                        showReportDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Flag, contentDescription = null)
                                    }
                                )
                                if (isOwner && onDeleteClick != null) {
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMoreMenu = false
                                            showDeleteDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isWideScreen) {
            // Desktop: Side-by-side layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Image section
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ArtImage(
                        imageUrl = art.imageUrl,
                        characterName = art.characterName,
                        bookTitle = art.bookTitle,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            }
                    )
                    
                    // Zoom controls
                    ZoomControls(
                        scale = scale,
                        onZoomIn = { scale = (scale * 1.5f).coerceAtMost(4f) },
                        onZoomOut = { scale = (scale / 1.5f).coerceAtLeast(1f) },
                        onReset = { scale = 1f; offsetX = 0f; offsetY = 0f },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
                
                // Info section
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ArtInfoContent(art, onLikeClick)
                }
            }
        } else {
            // Mobile: Stacked layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    ArtImage(
                        imageUrl = art.imageUrl,
                        characterName = art.characterName,
                        bookTitle = art.bookTitle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Info
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ArtInfoContent(art, onLikeClick)
                }
            }
        }
    }
}

@Composable
private fun ArtImage(
    imageUrl: String,
    characterName: String,
    bookTitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        Color.Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "$characterName from $bookTitle",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // Placeholder when no image URL
            Icon(
                Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun ZoomControls(
    scale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onZoomOut, enabled = scale > 1f) {
                Icon(
                    Icons.Default.ZoomOut,
                    contentDescription = "Zoom out",
                    tint = Color.White
                )
            }
            
            Text(
                text = "${(scale * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 8.dp)
            )
            
            IconButton(onClick = onZoomIn, enabled = scale < 4f) {
                Icon(
                    Icons.Default.ZoomIn,
                    contentDescription = "Zoom in",
                    tint = Color.White
                )
            }
            
            if (scale != 1f) {
                IconButton(onClick = onReset) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset zoom",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtInfoContent(
    art: CharacterArt,
    onLikeClick: () -> Unit
) {
    // Character name and like button
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = art.characterName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "from ${art.bookTitle}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (art.bookAuthor.isNotBlank()) {
                Text(
                    text = "by ${art.bookAuthor}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        LikeButtonLarge(
            isLiked = art.isLikedByUser,
            likesCount = art.likesCount,
            onClick = onLikeClick
        )
    }
    
    // Featured badge
    if (art.isFeatured) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("?", fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Featured Art",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    
    // Description
    if (art.description.isNotBlank()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = art.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    
    // AI Info
    if (art.aiModel.isNotBlank() || art.prompt.isNotBlank()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("??", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "AI Generation Info",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (art.aiModel.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Model",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = art.aiModel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (art.prompt.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Prompt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = art.prompt,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
    
    // Tags
    if (art.tags.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            art.tags.forEach { tag ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
    
    // Submitter info
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = art.submitterUsername.firstOrNull()?.uppercase() ?: "?",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column {
            Text(
                text = art.submitterUsername.ifBlank { "Anonymous" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatDate(art.submittedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LikeButtonLarge(
    isLiked: Boolean,
    likesCount: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "likeScale"
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isLiked)
            MaterialTheme.colorScheme.errorContainer
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale),
                tint = if (isLiked)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = likesCount.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var customReason by remember { mutableStateOf("") }
    
    val reasons = listOf(
        "Not AI-generated",
        "Inappropriate content",
        "Wrong character/book",
        "Copyright violation",
        "Spam",
        "Other"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Art") },
        text = {
            Column {
                Text(
                    "Why are you reporting this art?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedReason = reason }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(reason)
                    }
                }
                
                if (selectedReason == "Other") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("Please specify") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val reason = if (selectedReason == "Other") customReason else selectedReason
                    if (!reason.isNullOrBlank()) {
                        onReport(reason)
                    }
                },
                enabled = selectedReason != null && 
                         (selectedReason != "Other" || customReason.isNotBlank())
            ) {
                Text("Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalTime::class)
private fun formatDate(timestamp: Long): String {
    val now = currentTimeToLong()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            timestamp.let { 
                val dt = kotlinx.datetime.Instant.fromEpochMilliseconds(it)
                    .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                "${monthNames[dt.monthNumber - 1]} ${dt.dayOfMonth}, ${dt.year}"
            }
        }
    }
}
