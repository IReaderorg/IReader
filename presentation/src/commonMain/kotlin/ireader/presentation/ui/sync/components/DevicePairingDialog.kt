package ireader.presentation.ui.sync.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.sync.DeviceInfo
import ireader.presentation.ui.component.components.IAlertDialog

/**
 * Dialog for device pairing with PIN code verification.
 * 
 * Displays a 6-digit PIN code prominently for user verification, along with
 * device information. Users must verify that the PIN matches on both devices
 * before confirming the pairing.
 * 
 * Follows Material Design 3 dialog patterns with proper accessibility.
 * 
 * **Validates: Requirements FR6.1, FR6.2**
 * 
 * @param device The device to pair with
 * @param pinCode The 6-digit PIN code for verification
 * @param onConfirm Callback when user confirms pairing after PIN verification
 * @param onDismiss Callback when user dismisses the dialog
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun DevicePairingDialog(
    device: DeviceInfo,
    pinCode: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    IAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Device Pairing",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Instructions
                Text(
                    text = "Verify that this PIN matches the one displayed on",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Device name
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // PIN code display - prominently formatted
                PinCodeDisplay(pinCode = pinCode)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Device information
                DeviceInfoSection(device = device)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Security notice
                Text(
                    text = "Only confirm if the PIN matches exactly on both devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

/**
 * Displays the PIN code prominently with formatting for readability.
 * Formats as "XXX XXX" for better visual parsing.
 */
@Composable
private fun PinCodeDisplay(
    pinCode: String,
    modifier: Modifier = Modifier
) {
    // Format PIN as "XXX XXX" for readability
    val formattedPin = if (pinCode.length == 6) {
        "${pinCode.substring(0, 3)} ${pinCode.substring(3, 6)}"
    } else {
        pinCode
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Text(
            text = formattedPin,
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        )
    }
}

/**
 * Displays device information in a compact format.
 */
@Composable
private fun DeviceInfoSection(
    device: DeviceInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        DeviceInfoRow(
            label = "Device Type",
            value = device.deviceType.name
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DeviceInfoRow(
            label = "IP Address",
            value = "${device.ipAddress}:${device.port}"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DeviceInfoRow(
            label = "App Version",
            value = device.appVersion
        )
    }
}

/**
 * Displays a label-value pair for device information.
 */
@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
