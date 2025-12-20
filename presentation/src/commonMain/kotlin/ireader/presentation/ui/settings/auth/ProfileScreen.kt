package ireader.presentation.ui.settings.auth

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.ui.component.badges.ProfileBadgeDisplay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.components.SimpleUserErrorCard
import org.koin.compose.koinInject
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.presentation.core.safePopBackStack
class ProfileScreen  {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ProfileViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        
        Scaffold(
            topBar = {
                Toolbar(
                    title = { Text(localizeHelper.localize(Res.string.profile)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.safePopBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    state.isLoading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    state.currentUser != null -> {
                        item {
                            UserProfileCard(
                                user = state.currentUser!!,
                                onSignOut = { viewModel.signOut() },
                                onUpdateUsername = { viewModel.showUsernameDialog() },
                                onUpdateWallet = { viewModel.showWalletDialog() },
                                onUpdatePassword = { viewModel.showPasswordDialog() }
                            )
                        }
                        
                        item {
                            BadgesSection(
                                badges = state.featuredBadges,
                                isLoading = state.isBadgesLoading,
                                error = state.badgesError,
                                onRetry = { viewModel.retryLoadBadges() }
                            )
                        }
                        
                        item {
                            AchievementBadgesSection(
                                badges = state.achievementBadges,
                                isLoading = state.isBadgesLoading,
                                error = state.badgesError,
                                onRetry = { viewModel.retryLoadBadges() }
                            )
                        }
                        
                        item {
                            ReadingStatisticsSection(
                                chaptersRead = state.chaptersRead,
                                booksCompleted = state.booksCompleted,
                                reviewsWritten = state.reviewsWritten,
                                readingStreak = state.readingStreak,
                                isLoading = state.isStatsLoading
                            )
                        }
                        
                        item {
                            SyncStatusCard(
                                connectionStatus = state.connectionStatus,
                                lastSyncTime = state.lastSyncTime
                            )
                        }
                    }
                    
                    else -> {
                        item {
                            LoginPromptCard(
                                onLogin = { navController.navigate(NavigationRoutes.auth) }
                            )
                        }
                        
                        item {
                            BenefitsCard()
                        }
                    }
                }
            }
        }
        
        if (state.showUsernameDialog) {
            UsernameDialog(
                currentUsername = state.currentUser?.username,
                onDismiss = { viewModel.hideUsernameDialog() },
                onConfirm = { username -> viewModel.updateUsername(username) }
            )
        }
        
        if (state.showWalletDialog) {
            WalletDialog(
                currentWallet = state.currentUser?.ethWalletAddress,
                onDismiss = { viewModel.hideWalletDialog() },
                onConfirm = { wallet -> viewModel.updateWallet(wallet) }
            )
        }
        
        if (state.showPasswordDialog) {
            PasswordDialog(
                onDismiss = { viewModel.hidePasswordDialog() },
                onConfirm = { password -> viewModel.updatePassword(password) }
            )
        }
        
        if (state.passwordUpdateSuccess) {
            AlertDialog(
                onDismissRequest = { viewModel.clearPasswordUpdateSuccess() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearPasswordUpdateSuccess() }) {
                        Text(localizeHelper.localize(Res.string.ok))
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = localizeHelper.localize(Res.string.password_updated),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = localizeHelper.localize(Res.string.password_updated_successfully))
                }
            )
        }
        
        // Show beautiful error dialog for user errors
        if (state.error != null) {
            val userError = ireader.presentation.ui.component.components.UserError.fromMessage(state.error)
            val needsSignIn = userError is ireader.presentation.ui.component.components.UserError.NotFound ||
                             userError is ireader.presentation.ui.component.components.UserError.NotAuthenticated ||
                             userError is ireader.presentation.ui.component.components.UserError.SessionExpired ||
                             state.requiresSignIn
            
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                confirmButton = {
                    if (needsSignIn) {
                        Button(
                            onClick = {
                                viewModel.clearError()
                                viewModel.signOut()
                                navController.navigate(NavigationRoutes.auth)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(localizeHelper.localize(Res.string.sign_in_again))
                        }
                    } else {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(localizeHelper.localize(Res.string.dismiss))
                        }
                    }
                },
                dismissButton = if (needsSignIn) {
                    {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(localizeHelper.localize(Res.string.dismiss))
                        }
                    }
                } else null,
                icon = {
                    Icon(
                        imageVector = userError.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = userError.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = userError.message)
                }
            )
        }
    }
}

@Composable
private fun UserProfileCard(
    user: ireader.domain.models.remote.User,
    onSignOut: () -> Unit,
    onUpdateUsername: () -> Unit,
    onUpdateWallet: () -> Unit,
    onUpdatePassword: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.email),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                if (user.isSupporter) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.supporter),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.name),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.username ?: "Not set",
                        style = MaterialTheme.typography.bodyLarge,
                        color =MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                TextButton(
                    onClick = onUpdateUsername,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(localizeHelper.localize(Res.string.edit))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.eth_wallet_address_api_key),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.ethWalletAddress?.let { 
                            it.take(6) + "..." + it.takeLast(4) 
                        } ?: "Not set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                TextButton(
                    onClick = onUpdateWallet,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(localizeHelper.localize(Res.string.edit))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.password),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "••••••••",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                TextButton(
                    onClick = onUpdatePassword,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(localizeHelper.localize(Res.string.change))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.sign_out))
            }
        }
    }
}

@Composable
private fun LoginPromptCard(
    onLogin: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.sign_in_to_sync),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.sign_in_to_sync_your),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.sign_in_sign_up))
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    connectionStatus: ireader.domain.models.remote.ConnectionStatus,
    lastSyncTime: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (connectionStatus) {
                    ireader.domain.models.remote.ConnectionStatus.CONNECTED -> Icons.Default.CloudDone
                    ireader.domain.models.remote.ConnectionStatus.DISCONNECTED -> Icons.Default.CloudOff
                    else -> Icons.Default.CloudSync
                },
                contentDescription = null,
                tint = when (connectionStatus) {
                    ireader.domain.models.remote.ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
                    ireader.domain.models.remote.ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.tertiary
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = when (connectionStatus) {
                        ireader.domain.models.remote.ConnectionStatus.CONNECTED -> "Connected"
                        ireader.domain.models.remote.ConnectionStatus.DISCONNECTED -> "Disconnected"
                        else -> "Syncing..."
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                lastSyncTime?.let {
                    Text(
                        text = "Last synced: ${formatTime(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitsCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.benefits_of_signing_in),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BenefitItem(
                icon = Icons.Default.Sync,
                title = localizeHelper.localize(Res.string.cross_device_sync),
                description = "Your reading progress syncs automatically across all your devices"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            BenefitItem(
                icon = Icons.Default.Security,
                title = localizeHelper.localize(Res.string.secure_private),
                description = "Your data is secured with industry-standard encryption"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            BenefitItem(
                icon = Icons.Default.Cloud,
                title = localizeHelper.localize(Res.string.cloud_backup),
                description = "Never lose your reading progress again"
            )
        }
    }
}

@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UsernameDialog(
    currentUsername: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var username by remember { mutableStateOf(currentUsername ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.set_name)) },
        text = {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.choose_a_name_for_your_profile),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        error = null
                    },
                    label = { Text(localizeHelper.localize(Res.string.name)) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.length < 3) {
                        error = "Name must be at least 3 characters"
                    } else {
                        onConfirm(username)
                    }
                }
            ) {
                Text(localizeHelper.localize(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun WalletDialog(
    currentWallet: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var wallet by remember { mutableStateOf(currentWallet ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.set_eth_wallet_address)) },
        text = {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.enter_your_ethereum_wallet_address),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = wallet,
                    onValueChange = {
                        wallet = it
                        error = null
                    },
                    label = { Text(localizeHelper.localize(Res.string.wallet_address)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.wallet_placeholder)) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (wallet.isNotBlank() && !wallet.startsWith("0x")) {
                        error = "Invalid wallet address format"
                    } else if (wallet.isNotBlank() && wallet.length != 42) {
                        error = "Wallet address must be 42 characters"
                    } else {
                        onConfirm(wallet)
                    }
                }
            ) {
                Text(localizeHelper.localize(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun PasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.change_password)) },
        text = {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.enter_new_password),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        error = null
                    },
                    label = { Text(localizeHelper.localize(Res.string.new_password)) },
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (passwordVisible) 
                        androidx.compose.ui.text.input.VisualTransformation.None 
                    else 
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) 
                                    Icons.Default.Security 
                                else 
                                    Icons.Default.Security,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        error = null
                    },
                    label = { Text(localizeHelper.localize(Res.string.confirm_password)) },
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (passwordVisible) 
                        androidx.compose.ui.text.input.VisualTransformation.None 
                    else 
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    supportingText = error?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        newPassword.length < 6 -> {
                            error = "Password must be at least 6 characters"
                        }
                        newPassword != confirmPassword -> {
                            error = "Passwords do not match"
                        }
                        else -> {
                            onConfirm(newPassword)
                        }
                    }
                }
            ) {
                Text(localizeHelper.localize(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

@Composable
private fun BadgesSection(
    badges: List<ireader.domain.models.remote.Badge>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.badges),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                isLoading -> {
                    // Loading state with skeleton placeholders (3 circular shimmer boxes)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                }
                
                error != null -> {
                    // Error state with retry button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetry) {
                            Text(localizeHelper.localize(Res.string.retry))
                        }
                    }
                }
                
                badges.isEmpty() -> {
                    // Empty state
                    Text(
                        text = localizeHelper.localize(Res.string.no_badges_yet_earn_badges),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                else -> {
                    // Display badges using ProfileBadgeDisplay
                    ProfileBadgeDisplay(badges = badges)
                }
            }
        }
    }
}

@Composable
private fun AchievementBadgesSection(
    badges: List<ireader.domain.models.remote.Badge>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.achievement_badges),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.earned_through_reading_and_app_usage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                isLoading -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                }
                
                error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetry) {
                            Text(localizeHelper.localize(Res.string.retry))
                        }
                    }
                }
                
                badges.isEmpty() -> {
                    Text(
                        text = localizeHelper.localize(Res.string.no_achievement_badges_yet_keep),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                else -> {
                    ProfileBadgeDisplay(badges = badges)
                }
            }
        }
    }
}

@Composable
private fun ReadingStatisticsSection(
    chaptersRead: Int,
    booksCompleted: Int,
    reviewsWritten: Int,
    readingStreak: Int,
    isLoading: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.reading_statistics),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticItem(
                        icon = "??",
                        label = localizeHelper.localize(Res.string.chapters_read),
                        value = chaptersRead.toString()
                    )
                    
                    StatisticItem(
                        icon = "??",
                        label = localizeHelper.localize(Res.string.books_completed),
                        value = booksCompleted.toString()
                    )
                    
                    StatisticItem(
                        icon = "??",
                        label = localizeHelper.localize(Res.string.reviews_written),
                        value = reviewsWritten.toString()
                    )
                    
                    StatisticItem(
                        icon = "??",
                        label = localizeHelper.localize(Res.string.reading_streak),
                        value = "$readingStreak days"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val now = currentTimeToLong()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> "${diff / 86400_000} days ago"
    }
}
