package ireader.presentation.ui.settings.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.PaymentProofStatus
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBadgeVerificationScreen(
    viewModel: AdminBadgeVerificationViewModel,
    onNavigateBack: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show snackbar for verification results
    LaunchedEffect(state.verificationSuccess, state.verificationError) {
        when {
            state.verificationSuccess -> {
                snackbarHostState.showSnackbar(
                    message = "Payment proof verified successfully",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearVerificationStatus()
            }
            state.verificationError != null -> {
                snackbarHostState.showSnackbar(
                    message = state.verificationError ?: "Failed to verify payment proof",
                    duration = SnackbarDuration.Long
                )
                viewModel.clearVerificationStatus()
            }
        }
    }
    
    IScaffold(
        snackbarHostState = snackbarHostState,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localizeHelper.localize(Res.string.badge_verification),
                popBackStack = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPendingProofs() }) {
                            Text(localizeHelper.localize(Res.string.retry))
                        }
                    }
                }
                state.pendingProofs.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.no_pending_verifications),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.all_payment_proofs_have_been_reviewed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = localizeHelper.localize(Res.string.pending_verifications),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${state.pendingProofs.size} payment proof(s) awaiting review",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        
                        items(state.pendingProofs, key = { it.id }) { proof ->
                            PaymentProofCard(
                                proof = proof,
                                onApprove = { viewModel.verifyPaymentProof(proof.id, approve = true) },
                                onReject = { viewModel.verifyPaymentProof(proof.id, approve = false) },
                                isProcessing = state.processingProofId == proof.id
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentProofCard(
    proof: PaymentProof,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    isProcessing: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showConfirmDialog by remember { mutableStateOf<Boolean?>(null) }
    var showImageViewer by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = proof.username ?: proof.userEmail ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = proof.badgeName ?: proof.badgeId,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.pending),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Payment Details
            PaymentDetailRow(
                label = localizeHelper.localize(Res.string.transaction_id),
                value = proof.transactionId
            )
            PaymentDetailRow(
                label = localizeHelper.localize(Res.string.payment_method),
                value = proof.paymentMethod
            )
            PaymentDetailRow(
                label = localizeHelper.localize(Res.string.submitted),
                value = formatDate(proof.submittedAt)
            )
            
            if (proof.proofImageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showImageViewer = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.view_payment_proof))
                }
            }
    
    // Image Viewer Dialog
    if (showImageViewer && proof.proofImageUrl != null) {
        PaymentProofImageDialog(
            imageUrl = proof.proofImageUrl!!,
            onDismiss = { showImageViewer = false }
        )
    }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.reject))
                }
                
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.approve))
                }
            }
        }
    }
    
    // Confirmation Dialog
    if (showConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            icon = {
                Icon(
                    imageVector = if (showConfirmDialog == true) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (showConfirmDialog == true) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (showConfirmDialog == true) "Approve Payment?" else "Reject Payment?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (showConfirmDialog == true) {
                        "This will grant the badge to ${proof.username ?: "the user"} and mark the payment as verified."
                    } else {
                        "This will reject the payment proof. The user will not receive the badge."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (showConfirmDialog == true) onApprove() else onReject()
                        showConfirmDialog = null
                    },
                    colors = if (showConfirmDialog == true) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    }
                ) {
                    Text(if (showConfirmDialog == true) "Approve" else "Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PaymentDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // Parse ISO 8601 date string and format it nicely
        // Expected format: "2024-01-15T10:30:00Z" or "2024-01-15"
        val datePart = dateString.substringBefore("T").trim()
        if (datePart.length >= 10) {
            val parts = datePart.split("-")
            if (parts.size == 3) {
                val year = parts[0]
                val month = parts[1]
                val day = parts[2]
                
                val monthName = when (month) {
                    "01" -> "Jan"
                    "02" -> "Feb"
                    "03" -> "Mar"
                    "04" -> "Apr"
                    "05" -> "May"
                    "06" -> "Jun"
                    "07" -> "Jul"
                    "08" -> "Aug"
                    "09" -> "Sep"
                    "10" -> "Oct"
                    "11" -> "Nov"
                    "12" -> "Dec"
                    else -> month
                }
                
                "$monthName $day, $year"
            } else {
                datePart
            }
        } else {
            dateString.take(10)
        }
    } catch (e: Exception) {
        dateString.take(10)
    }
}

/**
 * Full screen dialog to view payment proof image
 */
@Composable
private fun PaymentProofImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(0.95f),
        title = {
            Text(
                text = localizeHelper.localize(Res.string.view_payment_proof),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val context = LocalPlatformContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .build(),
                    contentDescription = localizeHelper.localize(Res.string.view_payment_proof),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.close))
            }
        }
    )
}
