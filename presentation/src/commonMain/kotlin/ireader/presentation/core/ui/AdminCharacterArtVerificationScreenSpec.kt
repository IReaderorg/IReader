package ireader.presentation.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.domain.models.characterart.CharacterArt
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.characterart.CharacterArtViewModel

/**
 * Admin screen for verifying character art submissions
 */
class AdminCharacterArtVerificationScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: CharacterArtViewModel = getIViewModel()
        val state by vm.state.collectAsState()
        
        var selectedArt by remember { mutableStateOf<CharacterArt?>(null) }
        var showRejectDialog by remember { mutableStateOf(false) }
        var rejectReason by remember { mutableStateOf("") }
        
        // Refresh pending art when screen is shown
        LaunchedEffect(Unit) {
            vm.refresh()
        }
        
        // Reject dialog
        if (showRejectDialog && selectedArt != null) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Reject Art") },
                text = {
                    Column {
                        Text("Reason for rejection:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectReason,
                            onValueChange = { rejectReason = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter reason...") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedArt?.let { vm.rejectArt(it.id, rejectReason) }
                            showRejectDialog = false
                            rejectReason = ""
                            selectedArt = null
                        }
                    ) {
                        Text("Reject", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRejectDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Character Art Verification") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            if (state.pendingArt.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No pending submissions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "All caught up!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "${state.pendingArt.size} pending submissions",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    items(state.pendingArt, key = { it.id }) { art ->
                        PendingArtCard(
                            art = art,
                            onApprove = { featured ->
                                vm.approveArt(art.id, featured)
                            },
                            onReject = {
                                selectedArt = art
                                showRejectDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingArtCard(
    art: CharacterArt,
    onApprove: (featured: Boolean) -> Unit,
    onReject: () -> Unit
) {
    var showFeaturedOption by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (art.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = art.imageUrl,
                        contentDescription = "${art.characterName} from ${art.bookTitle}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Info
            Text(
                text = art.characterName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "from ${art.bookTitle}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (art.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = art.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Submitter info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "by ${art.submitterUsername.ifBlank { "Anonymous" }}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (art.aiModel.isNotBlank()) {
                    Spacer(Modifier.width(16.dp))
                    Text("🤖 ${art.aiModel}", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reject button
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject")
                }
                
                // Approve button
                Button(
                    onClick = { showFeaturedOption = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Approve")
                }
            }
            
            // Featured option
            if (showFeaturedOption) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onApprove(false)
                            showFeaturedOption = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Normal")
                    }
                    Button(
                        onClick = {
                            onApprove(true)
                            showFeaturedOption = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("⭐ Featured")
                    }
                }
            }
        }
    }
}


