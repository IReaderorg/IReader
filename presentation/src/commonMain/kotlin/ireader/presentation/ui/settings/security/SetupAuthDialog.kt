package ireader.presentation.ui.settings.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ireader.domain.models.security.AuthMethod

@Composable
fun SetupAuthDialog(
    authType: AuthMethod,
    onConfirm: (AuthMethod) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null
) {
    var input by remember { mutableStateOf("") }
    var confirmInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    val isValid = when (authType) {
        is AuthMethod.PIN -> input.length in 4..6 && input.all { it.isDigit() } && input == confirmInput
        is AuthMethod.Password -> input.length >= 4 && input == confirmInput
        is AuthMethod.Biometric -> true
        is AuthMethod.None -> false
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when (authType) {
                    is AuthMethod.PIN -> Icons.Default.Pin
                    is AuthMethod.Password -> Icons.Default.Password
                    is AuthMethod.Biometric -> Icons.Default.Fingerprint
                    is AuthMethod.None -> Icons.Default.Lock
                },
                contentDescription = null
            )
        },
        title = {
            Text(
                text = when (authType) {
                    is AuthMethod.PIN -> "Set up PIN"
                    is AuthMethod.Password -> "Set up Password"
                    is AuthMethod.Biometric -> "Enable Biometric"
                    is AuthMethod.None -> "App Lock"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (authType) {
                    is AuthMethod.PIN -> {
                        Text(
                            text = "Enter a 4-6 digit PIN to secure your app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = input,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) input = it },
                            label = { Text("PIN") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Next
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showPassword) "Hide PIN" else "Show PIN"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = confirmInput,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) confirmInput = it },
                            label = { Text("Confirm PIN") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            isError = confirmInput.isNotEmpty() && input != confirmInput,
                            supportingText = if (confirmInput.isNotEmpty() && input != confirmInput) {
                                { Text("PINs do not match", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is AuthMethod.Password -> {
                        Text(
                            text = "Enter a password to secure your app (minimum 4 characters)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text("Password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = confirmInput,
                            onValueChange = { confirmInput = it },
                            label = { Text("Confirm Password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            isError = confirmInput.isNotEmpty() && input != confirmInput,
                            supportingText = if (confirmInput.isNotEmpty() && input != confirmInput) {
                                { Text("Passwords do not match", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is AuthMethod.Biometric -> {
                        Text(
                            text = "Use your fingerprint or face to unlock the app. You can still use your device PIN as a fallback.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is AuthMethod.None -> {
                        // Should not reach here
                    }
                }
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val method = when (authType) {
                        is AuthMethod.PIN -> AuthMethod.PIN(input)
                        is AuthMethod.Password -> AuthMethod.Password(input)
                        is AuthMethod.Biometric -> AuthMethod.Biometric
                        is AuthMethod.None -> AuthMethod.None
                    }
                    onConfirm(method)
                },
                enabled = isValid
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
