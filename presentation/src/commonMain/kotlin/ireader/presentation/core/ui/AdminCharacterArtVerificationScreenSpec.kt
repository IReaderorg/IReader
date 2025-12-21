package ireader.presentation.core.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.domain.models.characterart.CharacterArt
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.characterart.CharacterArtViewModel
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Admin screen for verifying character art submissions
 */
class AdminCharacterArtVerificationScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: CharacterArtViewModel = getIViewModel()
        val state by vm.state.collectAsState()
        
        var selectedArt by remember { mutableStateOf<CharacterArt?>(null) }
        var showRejectDialog by remember { mutableStateOf(false) }
        var rejectReason by remember { mutableStateOf("") }
        var showApproveAllDialog by remember { mutableStateOf(false) }
        
        // Load pending art when screen is shown
        // Use loadPendingArtForVerification to bypass admin check since
        // this screen is only accessible to admins anyway
        LaunchedEffect(Unit) {
            vm.loadPendingArtForVerification()
        }
        
        // Approve All dialog
        if (showApproveAllDialog) {
            AlertDialog(
                onDismissRequest = { showApproveAllDialog = false },
                title = { Text(localizeHelper.localize(Res.string.approve_all)) },
                text = {
                    Text("Are you sure you want to approve all ${state.pendingArt.size} pending submissions?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.approveAllPendingArt(featured = false)
                            showApproveAllDialog = false
                        }
                    ) {
                        Text("Approve All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApproveAllDialog = false }) {
                        Text(localizeHelper.localize(Res.string.cancel))
                    }
                }
            )
        }
        
        // Reject dialog
        if (showRejectDialog && selectedArt != null) {
            AlertDialog(
                onDismissRequest = { showRejectDialog = false },
                title = { Text(localizeHelper.localize(Res.string.reject_art)) },
                text = {
                    Column {
                        Text(localizeHelper.localize(Res.string.reason_for_rejection))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectReason,
                            onValueChange = { rejectReason = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(localizeHelper.localize(Res.string.enter_reason)) }
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
                        Text(localizeHelper.localize(Res.string.cancel))
                    }
                }
            )
        }
        
        // Show error snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(state.error) {
            state.error?.let {
                snackbarHostState.showSnackbar(it)
                vm.clearError()
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(localizeHelper.localize(Res.string.character_art_verification)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.safePopBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                        }
                    },
                    actions = {
                        // Auto-approve old pending art (7+ days)
                        IconButton(onClick = { vm.autoApproveOldPendingArt(7) }) {
                            Icon(Icons.Default.Schedule, contentDescription = localizeHelper.localize(Res.string.auto_approve_7_days_old))
                        }
                        // Approve All button
                        if (state.pendingArt.isNotEmpty()) {
                            IconButton(onClick = { showApproveAllDialog = true }) {
                                Icon(Icons.Default.DoneAll, contentDescription = localizeHelper.localize(Res.string.approve_all))
                            }
                        }
                        // Refresh button
                        IconButton(onClick = { vm.loadPendingArtForVerification() }) {
                            Icon(Icons.Default.Refresh, contentDescription = localizeHelper.localize(Res.string.refresh))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.pendingArt.isEmpty() -> {
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
                            Spacer(Modifier.height(16.dp))
                            TextButton(onClick = { vm.loadPendingArtForVerification() }) {
                                Text(localizeHelper.localize(Res.string.refresh))
                            }
                        }
                    }
                }
                else -> {
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
}

@Composable
private fun PendingArtCard(
    art: CharacterArt,
    onApprove: (featured: Boolean) -> Unit,
    onReject: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    Text(localizeHelper.localize(Res.string.reject))
                }
                
                // Approve button
                Button(
                    onClick = { showFeaturedOption = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(localizeHelper.localize(Res.string.approve))
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
                        Text(localizeHelper.localize(Res.string.normal))
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
                        Text(localizeHelper.localize(Res.string.featured))
                    }
                }
            }
        }
    }
}


