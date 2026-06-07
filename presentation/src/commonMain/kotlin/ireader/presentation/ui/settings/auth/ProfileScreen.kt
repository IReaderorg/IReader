package ireader.presentation.ui.settings.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import org.koin.compose.koinInject

/**
 * Profile entry point. The visual screen is fully custom (WebnovelProfileScreen);
 * this just wires the ViewModel + the edit/auth dialogs.
 */
class ProfileScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ProfileViewModel = koinInject()
        val state by viewModel.state.collectAsState()
        val uriHandler = LocalUriHandler.current

        WebnovelProfileScreen(
            state = state,
            onBack = { navController.safePopBackStack() },
            onEditName = { viewModel.showUsernameDialog() },
            onEditProfile = { viewModel.showEditProfile() },
            onChangePassword = { viewModel.showPasswordDialog() },
            onLogout = { viewModel.signOut() },
            onSignIn = { navController.navigate(NavigationRoutes.auth) },
            onCheckIn = { viewModel.checkIn() },
            onActivateTitle = { viewModel.setActiveTitle(it) },
            onOpenDiscord = { uriHandler.openUri(ireader.i18n.discord) },
            onPostComment = { viewModel.postComment(it) },
        )

        AchievementUnlockDialog(
            unlocks = state.newlyUnlocked,
            shareEnabled = viewModel.discordShareEnabled,
            onShare = { name, tier -> viewModel.shareUnlock(name, tier) },
            onDismiss = { viewModel.consumeUnlocks() },
        )

        if (state.showEditProfileDialog) {
            EditProfileDialog(
                initialBio = state.bio,
                initialAvatar = state.avatarUrl ?: "",
                initialCover = state.coverUrl ?: "",
                onDismiss = { viewModel.hideEditProfile() },
                onSave = { bio, avatar, cover -> viewModel.saveProfile(bio, avatar, cover) },
            )
        }

        if (state.showUsernameDialog) {
            TextEntryDialog(
                title = "Edit name",
                label = "Username",
                initial = state.currentUser?.username ?: "",
                onDismiss = { viewModel.hideUsernameDialog() },
                onConfirm = { viewModel.updateUsername(it) },
            )
        }

        if (state.showPasswordDialog) {
            TextEntryDialog(
                title = "Change password",
                label = "New password",
                initial = "",
                isSecret = true,
                onDismiss = { viewModel.hidePasswordDialog() },
                onConfirm = { viewModel.updatePassword(it) },
            )
        }

        if (state.passwordUpdateSuccess) {
            AlertDialog(
                onDismissRequest = { viewModel.clearPasswordUpdateSuccess() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearPasswordUpdateSuccess() }) { Text("OK") }
                },
                icon = { Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp)) },
                title = { Text("Password updated", fontWeight = FontWeight.Bold) },
                text = { Text("Your password was changed successfully.") },
            )
        }

        if (state.error != null) {
            val needsSignIn = state.requiresSignIn ||
                state.error!!.contains("not found", true) ||
                state.error!!.contains("unauthorized", true) ||
                state.error!!.contains("session", true)
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                confirmButton = {
                    if (needsSignIn) {
                        Button(onClick = {
                            viewModel.clearError(); viewModel.signOut()
                            navController.navigate(NavigationRoutes.auth)
                        }) {
                            Icon(Icons.Filled.Login, null, modifier = Modifier.size(18.dp))
                            Text("  Sign in again")
                        }
                    } else {
                        TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss") }
                    }
                },
                title = { Text("Something went wrong", fontWeight = FontWeight.Bold) },
                text = { Text(state.error ?: "") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    initialBio: String,
    initialAvatar: String,
    initialCover: String,
    onDismiss: () -> Unit,
    onSave: (bio: String, avatar: String, cover: String) -> Unit,
) {
    var bio by remember { mutableStateOf(initialBio) }
    var avatar by remember { mutableStateOf(initialAvatar) }
    var cover by remember { mutableStateOf(initialCover) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onSave(bio, avatar, cover) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Edit profile", fontWeight = FontWeight.Bold) },
        text = {
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(value = bio, onValueChange = { bio = it.take(200) },
                    label = { Text("Bio") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = avatar, onValueChange = { avatar = it },
                    label = { Text("Avatar image URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = cover, onValueChange = { cover = it },
                    label = { Text("Cover image URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextEntryDialog(
    title: String,
    label: String,
    initial: String,
    isSecret: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { if (value.isNotBlank()) onConfirm(value) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                visualTransformation = if (isSecret) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}
