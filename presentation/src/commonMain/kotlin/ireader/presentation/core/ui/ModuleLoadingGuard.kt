package ireader.presentation.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.di.ModuleInitializationState

/**
 * A composable that guards content until all Koin modules are fully initialized.
 * Shows a loading indicator while background modules are being loaded.
 * 
 * Use this for screens that depend on background-loaded modules like:
 * - Reader screen (depends on UseCasesInject, DomainServices)
 * - Settings screens (depends on various services)
 * - Any screen that uses remote/sync functionality
 * 
 * @param content The content to show once modules are initialized
 */
@Composable
fun ModuleLoadingGuard(
    loadingMessage: String = "Loading...",
    content: @Composable () -> Unit
) {
    val isInitialized by ModuleInitializationState.isFullyInitialized.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Show loading state while modules are loading
        AnimatedVisibility(
            visible = !isInitialized,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = loadingMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        
        // Show content when initialized
        AnimatedVisibility(
            visible = isInitialized,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            content()
        }
    }
}

/**
 * Check if modules are ready without composable context.
 * Useful for navigation guards.
 */
fun areModulesReady(): Boolean = ModuleInitializationState.isReady
