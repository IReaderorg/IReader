package ireader.presentation.ui.component.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.dismiss
import ireader.i18n.resources.sign_in_again
import ireader.i18n.resources.try_again
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Sealed class representing user-related errors with beautiful UI display
 */
sealed class UserError {
    abstract val title: String
    abstract val message: String
    abstract val icon: ImageVector
    
    data class NotFound(
        override val title: String = "Account Not Found",
        override val message: String = "We couldn't find your account. Please sign in again to continue."
    ) : UserError() {
        override val icon: ImageVector = Icons.Default.AccountCircle
    }
    
    data class NotAuthenticated(
        override val title: String = "Not Signed In",
        override val message: String = "You need to sign in to access this feature."
    ) : UserError() {
        override val icon: ImageVector = Icons.Default.Lock
    }
    
    data class SessionExpired(
        override val title: String = "Session Expired",
        override val message: String = "Your session has expired. Please sign in again."
    ) : UserError() {
        override val icon: ImageVector = Icons.Default.Warning
    }
    
    data class NetworkError(
        override val title: String = "Connection Error",
        override val message: String = "Unable to connect. Please check your internet connection and try again."
    ) : UserError() {
        override val icon: ImageVector = Icons.Default.CloudOff
    }
    
    data class ServerError(
        override val title: String = "Server Error",
        override val message: String = "Something went wrong on our end. Please try again later."
    ) : UserError() {
        override val icon: ImageVector = Icons.Default.Error
    }
    
    data class Unknown(
        override val title: String = "Error",
        override val message: String
    ) : UserError() {
        override val icon: ImageVector = Icons.Default.Error
    }
    
    data class ServiceUnavailable(
        override val title: String = "Service Unavailable",
        override val message: String = "This service is temporarily unavailable. Please try again later."
    ) : UserError() {
        override val icon: ImageVector = Icons.Default.CloudOff
    }
    
    companion object {
        /**
         * Parse an error message string into a UserError type
         */
        fun fromMessage(message: String?): UserError {
            val lowerMessage = message?.lowercase() ?: ""
            return when {
                // User not found errors
                lowerMessage.contains("user not found") ||
                lowerMessage.contains("account not found") ||
                lowerMessage.contains("no user") ||
                lowerMessage.contains("user does not exist") -> NotFound()
                
                // Not authenticated errors
                lowerMessage.contains("not authenticated") ||
                lowerMessage.contains("unauthenticated") ||
                lowerMessage.contains("unauthorized") ||
                lowerMessage.contains("401") ||
                lowerMessage.contains("sign in required") ||
                lowerMessage.contains("login required") ||
                lowerMessage.contains("please sign in") -> NotAuthenticated()
                
                // Session expired errors
                lowerMessage.contains("session expired") ||
                lowerMessage.contains("token expired") ||
                lowerMessage.contains("jwt expired") ||
                lowerMessage.contains("refresh token") -> SessionExpired()
                
                // Network errors
                lowerMessage.contains("network") ||
                lowerMessage.contains("connection") ||
                lowerMessage.contains("timeout") ||
                lowerMessage.contains("unreachable") ||
                lowerMessage.contains("failed to connect") ||
                lowerMessage.contains("no internet") ||
                lowerMessage.contains("offline") -> NetworkError()
                
                // Service unavailable
                lowerMessage.contains("service") && lowerMessage.contains("unavailable") ||
                lowerMessage.contains("temporarily unavailable") ||
                lowerMessage.contains("maintenance") -> ServiceUnavailable()
                
                // Server errors
                lowerMessage.contains("server error") ||
                lowerMessage.contains("internal error") ||
                lowerMessage.contains("500") ||
                lowerMessage.contains("502") ||
                lowerMessage.contains("503") -> ServerError()
                
                // Default - show the actual error for debugging
                else -> Unknown(
                    title = "Something Went Wrong",
                    message = message ?: "An unexpected error occurred. Please try again."
                )
            }
        }
    }
}


/**
 * A beautiful error card component for displaying user-related errors
 * with appropriate icons, colors, and action buttons.
 */
@Composable
fun UserErrorCard(
    error: UserError,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onSignIn: (() -> Unit)? = null
) {
    val localizeHelper = LocalLocalizeHelper.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = error.icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = error.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localizeHelper?.localize(Res.string.dismiss),
                            tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
            )
            
            // Show action buttons based on error type
            val showSignIn = error is UserError.NotFound || 
                            error is UserError.NotAuthenticated || 
                            error is UserError.SessionExpired
            val showRetry = error is UserError.NetworkError || 
                           error is UserError.ServerError ||
                           error is UserError.ServiceUnavailable ||
                           error is UserError.Unknown
            
            if ((showSignIn && onSignIn != null) || (showRetry && onRetry != null)) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDismiss != null) {
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(localizeHelper?.localize(Res.string.dismiss) ?: "Dismiss")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    if (showSignIn && onSignIn != null) {
                        Button(
                            onClick = onSignIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(localizeHelper?.localize(Res.string.sign_in_again) ?: "Sign In Again")
                        }
                    } else if (showRetry && onRetry != null) {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(localizeHelper?.localize(Res.string.try_again) ?: "Try Again")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated version of UserErrorCard that smoothly appears/disappears
 */
@Composable
fun AnimatedUserErrorCard(
    error: UserError?,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onSignIn: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        error?.let {
            UserErrorCard(
                error = it,
                modifier = modifier,
                onDismiss = onDismiss,
                onRetry = onRetry,
                onSignIn = onSignIn
            )
        }
    }
}

/**
 * Simple error card for displaying string error messages
 * Automatically parses the message to determine the error type
 */
@Composable
fun SimpleUserErrorCard(
    errorMessage: String?,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onSignIn: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        errorMessage?.let {
            val error = UserError.fromMessage(it)
            UserErrorCard(
                error = error,
                modifier = modifier,
                onDismiss = onDismiss,
                onRetry = onRetry,
                onSignIn = onSignIn
            )
        }
    }
}
