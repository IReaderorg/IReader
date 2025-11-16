package org.ireader.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Composable that handles double back press to exit the app
 * Shows a toast message on first back press, exits on second press within 2 seconds
 */
@Composable
fun ConfirmExit() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }
    
    BackHandler(enabled = true) {
        if (backPressedOnce) {
            // Second press - exit the app
            (context as? MainActivity)?.finish()
        } else {
            // First press - show toast and set flag
            backPressedOnce = true
            Toast.makeText(
                context,
                "Press back again to exit",
                Toast.LENGTH_SHORT
            ).show()
            
            // Reset the flag after 2 seconds
            scope.launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }
}
