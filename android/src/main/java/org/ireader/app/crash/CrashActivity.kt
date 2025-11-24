package org.ireader.app.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CrashActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show loading screen immediately to avoid black screen
        setContent {
            LoadingScreen()
        }
        
        val crashReport = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(CrashHandler.EXTRA_CRASH_REPORT, CrashReport::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(CrashHandler.EXTRA_CRASH_REPORT)
        }
        
        if (crashReport == null) {
            finish()
            return
        }
        
        // Replace loading screen with actual crash screen
        setContent {
            CrashScreenWithLoading(crashReport = crashReport)
        }
    }
}

@Composable
fun LoadingScreen() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            error = Color(0xFFCF6679)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // App icon or warning icon
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Preparing crash report...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Please wait a moment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun CrashScreenWithLoading(crashReport: CrashReport) {
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        // Simulate a brief loading time to ensure smooth transition
        kotlinx.coroutines.delay(300)
        isLoading = false
    }
    
    if (isLoading) {
        LoadingScreen()
    } else {
        CrashScreen(crashReport = crashReport)
    }
}

@Composable
fun CrashScreen(crashReport: CrashReport) {
    val context = LocalContext.current
    var showFullStackTrace by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Database migration state
    var showMigrationDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var migrationResult by remember { mutableStateOf<String?>(null) }
    var selectedTables by remember { mutableStateOf(crashReport.conflictingTables.toSet()) }
    
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            error = Color(0xFFCF6679)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "App Crashed",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "We're sorry for the inconvenience",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Exception Info
                InfoCard(
                    title = "Exception",
                    content = crashReport.exceptionType,
                    icon = Icons.Default.BugReport
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoCard(
                    title = "Message",
                    content = crashReport.exceptionMessage,
                    icon = Icons.Default.Info
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Device Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Device Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DeviceInfoRow("App Version", crashReport.appVersion)
                        DeviceInfoRow("Android", crashReport.androidVersion)
                        DeviceInfoRow("Device", crashReport.deviceModel)
                        DeviceInfoRow("Build", "${crashReport.buildTime} (${crashReport.commitSha})")
                        DeviceInfoRow("Time", formatTimestamp(crashReport.timestamp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stack Trace
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stack Trace",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { showFullStackTrace = !showFullStackTrace }) {
                                Text(if (showFullStackTrace) "Hide" else "Show")
                            }
                        }
                        
                        if (showFullStackTrace) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF0D0D0D),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = crashReport.stackTrace,
                                    modifier = Modifier.padding(12.dp),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE0E0E0),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Database Migration Section (if applicable)
                if (crashReport.isDatabaseMigrationError) {
                    DatabaseMigrationCard(
                        conflictingTables = crashReport.conflictingTables,
                        onFixDatabase = { showMigrationDialog = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Action Buttons
                Button(
                    onClick = {
                        createGitHubIssue(context, crashReport)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Issue on GitHub")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = {
                        copyToClipboard(context, crashReport)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Crash Details")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = {
                        restartApp(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart App")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = {
                        (context as? ComponentActivity)?.finishAffinity()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close App")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Database Migration Dialog
    if (showMigrationDialog) {
        DatabaseMigrationDialog(
            conflictingTables = crashReport.conflictingTables,
            selectedTables = selectedTables,
            onTableSelectionChange = { table, selected ->
                selectedTables = if (selected) {
                    selectedTables + table
                } else {
                    selectedTables - table
                }
            },
            isProcessing = isProcessing,
            migrationResult = migrationResult,
            onDismiss = { 
                showMigrationDialog = false
                migrationResult = null
            },
            onDropTables = {
                scope.launch {
                    isProcessing = true
                    migrationResult = null
                    
                    val result = DatabaseMigrationHelper.dropTables(
                        context,
                        "ireader.db",
                        selectedTables.toList()
                    )
                    
                    isProcessing = false
                    migrationResult = if (result.isSuccess) {
                        "Successfully dropped ${selectedTables.size} table(s). Please restart the app."
                    } else {
                        "Failed to drop tables: ${result.exceptionOrNull()?.message}"
                    }
                }
            },
            onIntelligentMigration = {
                scope.launch {
                    isProcessing = true
                    migrationResult = null
                    
                    val result = DatabaseMigrationHelper.intelligentMigration(
                        context,
                        "ireader.db",
                        selectedTables.toList()
                    )
                    
                    isProcessing = false
                    migrationResult = if (result.isSuccess) {
                        val report = result.getOrNull()
                        "Migration completed!\nProcessed: ${report?.tablesProcessed?.size ?: 0} tables\nBackup: ${report?.backupPath}\nPlease restart the app."
                    } else {
                        "Migration failed: ${result.exceptionOrNull()?.message}"
                    }
                }
            },
            onDeleteDatabase = {
                scope.launch {
                    isProcessing = true
                    migrationResult = null
                    
                    val result = DatabaseMigrationHelper.deleteDatabase(context, "ireader.db")
                    
                    isProcessing = false
                    migrationResult = if (result.isSuccess) {
                        "Database deleted successfully. All data will be reset. Please restart the app."
                    } else {
                        "Failed to delete database: ${result.exceptionOrNull()?.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun InfoCard(title: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun createGitHubIssue(context: Context, crashReport: CrashReport) {
    val repoUrl = "https://github.com/IReaderorg/IReader"
    val title = "[Crash] ${crashReport.exceptionType}: ${crashReport.exceptionMessage.take(50)}"
    val body = crashReport.toGitHubIssueBody()
    
    val url = "$repoUrl/issues/new?title=${Uri.encode(title)}&body=${Uri.encode(body)}"
    
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to open browser", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: Context, crashReport: CrashReport) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Crash Report", crashReport.toClipboardText())
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Crash details copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    (context as? ComponentActivity)?.finishAffinity()
}


@androidx.compose.ui.tooling.preview.Preview(
    name = "Crash Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CrashScreenPreview() {
    val sampleCrashReport = CrashReport(
        exceptionType = "NullPointerException",
        exceptionMessage = "Attempt to invoke virtual method 'java.lang.String.toString()' on a null object reference",
        stackTrace = """
            java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String.toString()' on a null object reference
                at org.ireader.app.MainActivity.onCreate(MainActivity.kt:42)
                at android.app.Activity.performCreate(Activity.java:8000)
                at android.app.Activity.performCreate(Activity.java:7984)
                at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1309)
                at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3422)
                at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3601)
                at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:85)
                at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135)
                at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95)
                at android.app.ActivityThread${"$"}H.handleMessage(ActivityThread.java:2066)
                at android.os.Handler.dispatchMessage(Handler.java:106)
                at android.os.Looper.loop(Looper.java:223)
                at android.app.ActivityThread.main(ActivityThread.java:7656)
                at java.lang.reflect.Method.invoke(Native Method)
                at com.android.internal.os.RuntimeInit${"$"}MethodAndArgsCaller.run(RuntimeInit.java:592)
                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:947)
        """.trimIndent(),
        threadName = "main",
        appVersion = "1.6.0 (160)",
        androidVersion = "14 (API 34)",
        deviceModel = "Samsung Galaxy A15",
        buildTime = "2024-11-23T10:30Z",
        commitSha = "a1b2c3d",
        timestamp = System.currentTimeMillis()
    )
    
    CrashScreen(crashReport = sampleCrashReport)
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Crash Screen - Long Message",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CrashScreenLongMessagePreview() {
    val sampleCrashReport = CrashReport(
        exceptionType = "IllegalStateException",
        exceptionMessage = "This is a very long error message that demonstrates how the crash screen handles lengthy exception messages. It should wrap properly and remain readable even with extensive text content that spans multiple lines.",
        stackTrace = """
            java.lang.IllegalStateException: This is a very long error message
                at org.ireader.presentation.ui.reader.ReaderScreen.loadChapter(ReaderScreen.kt:156)
                at org.ireader.presentation.ui.reader.ReaderViewModel.loadNextChapter(ReaderViewModel.kt:89)
                at org.ireader.domain.usecases.reader.LoadChapterUseCase.invoke(LoadChapterUseCase.kt:45)
        """.trimIndent(),
        threadName = "DefaultDispatcher-worker-1",
        appVersion = "1.6.0-dev (161)",
        androidVersion = "13 (API 33)",
        deviceModel = "Google Pixel 7 Pro",
        buildTime = "2024-11-23T15:45Z",
        commitSha = "dev-123",
        timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
    )
    
    CrashScreen(crashReport = sampleCrashReport)
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Crash Screen - OutOfMemoryError",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CrashScreenOOMPreview() {
    val sampleCrashReport = CrashReport(
        exceptionType = "OutOfMemoryError",
        exceptionMessage = "Failed to allocate a 524288000 byte allocation with 16777216 free bytes and 245MB until OOM",
        stackTrace = """
            java.lang.OutOfMemoryError: Failed to allocate a 524288000 byte allocation with 16777216 free bytes and 245MB until OOM
                at coil3.decode.BitmapFactoryDecoder.decode(BitmapFactoryDecoder.kt:78)
                at coil3.ImageLoader.execute(ImageLoader.kt:234)
                at ireader.presentation.ui.book.BookCoverImage(BookCoverImage.kt:67)
                at ireader.presentation.ui.library.LibraryScreen(LibraryScreen.kt:123)
        """.trimIndent(),
        threadName = "main",
        appVersion = "1.5.8 (158)",
        androidVersion = "12 (API 31)",
        deviceModel = "Samsung Galaxy A15",
        buildTime = "2024-11-20T08:15Z",
        commitSha = "f4e5d6c",
        timestamp = System.currentTimeMillis()
    )
    
    CrashScreen(crashReport = sampleCrashReport)
}


@Composable
fun DatabaseMigrationCard(
    conflictingTables: List<String>,
    onFixDatabase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D1B00)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFFFFB74D)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Database Migration Failed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D)
                    )
                    Text(
                        text = "We detected a database conflict",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFB74D).copy(alpha = 0.7f)
                    )
                }
            }
            
            if (conflictingTables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Conflicting Tables:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFB74D).copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                conflictingTables.take(5).forEach { table ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFB74D).copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = table,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFE0B2)
                        )
                    }
                }
                if (conflictingTables.size > 5) {
                    Text(
                        text = "... and ${conflictingTables.size - 5} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFB74D).copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onFixDatabase,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB74D),
                    contentColor = Color(0xFF1E1E1E)
                )
            ) {
                Icon(Icons.Default.Build, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fix Database Issues", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseMigrationDialog(
    conflictingTables: List<String>,
    selectedTables: Set<String>,
    onTableSelectionChange: (String, Boolean) -> Unit,
    isProcessing: Boolean,
    migrationResult: String?,
    onDismiss: () -> Unit,
    onDropTables: () -> Unit,
    onIntelligentMigration: () -> Unit,
    onDeleteDatabase: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Database Migration Tools",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Choose how to handle the database conflict:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Table Selection
                if (conflictingTables.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Select Tables to Process:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            conflictingTables.forEach { table ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedTables.contains(table),
                                        onCheckedChange = { checked ->
                                            onTableSelectionChange(table, checked)
                                        },
                                        enabled = !isProcessing
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = table,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Migration Result
                if (migrationResult != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (migrationResult.contains("Success") || migrationResult.contains("completed")) {
                                Color(0xFF1B5E20)
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (migrationResult.contains("Success") || migrationResult.contains("completed")) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.Error
                                },
                                contentDescription = null,
                                tint = if (migrationResult.contains("Success") || migrationResult.contains("completed")) {
                                    Color(0xFF4CAF50)
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = migrationResult,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Processing Indicator
                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Action Buttons
                MigrationOptionButton(
                    icon = Icons.Default.AutoFixHigh,
                    title = "Intelligent Migration",
                    description = "Backup and migrate data automatically",
                    color = Color(0xFF4CAF50),
                    enabled = !isProcessing && selectedTables.isNotEmpty(),
                    onClick = onIntelligentMigration
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                MigrationOptionButton(
                    icon = Icons.Default.Delete,
                    title = "Drop Selected Tables",
                    description = "Remove conflicting tables (data will be lost)",
                    color = Color(0xFFFF9800),
                    enabled = !isProcessing && selectedTables.isNotEmpty(),
                    onClick = onDropTables
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                MigrationOptionButton(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete Entire Database",
                    description = "Start fresh (all data will be lost)",
                    color = Color(0xFFF44336),
                    enabled = !isProcessing,
                    onClick = onDeleteDatabase
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun MigrationOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = if (enabled) 1f else 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(
    name = "Crash Screen - Database Migration Error",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CrashScreenDatabaseMigrationPreview() {
    val sampleCrashReport = CrashReport(
        exceptionType = "SQLiteException",
        exceptionMessage = "Migration failed: table 'Book' already exists. Cannot create table 'Chapter' due to schema conflict.",
        stackTrace = """
            android.database.sqlite.SQLiteException: table Book already exists (code 1 SQLITE_ERROR)
                at android.database.sqlite.SQLiteConnection.nativeExecute(Native Method)
                at android.database.sqlite.SQLiteConnection.execute(SQLiteConnection.java:555)
                at androidx.room.RoomDatabase.beginTransaction(RoomDatabase.kt:567)
                at androidx.room.migration.Migration.migrate(Migration.kt:45)
                at org.ireader.data.local.AppDatabase.migrate(AppDatabase.kt:123)
                at org.ireader.app.MainActivity.onCreate(MainActivity.kt:67)
        """.trimIndent(),
        threadName = "main",
        appVersion = "1.6.0 (160)",
        androidVersion = "14 (API 34)",
        deviceModel = "Samsung Galaxy A15",
        buildTime = "2024-11-23T10:30Z",
        commitSha = "a1b2c3d",
        timestamp = System.currentTimeMillis(),
        isDatabaseMigrationError = true,
        conflictingTables = listOf("Book", "Chapter", "Download", "History", "Catalog", "Category", "BookCategory")
    )
    
    CrashScreen(crashReport = sampleCrashReport)
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Database Migration Card",
    showBackground = true
)
@Composable
fun DatabaseMigrationCardPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            DatabaseMigrationCard(
                conflictingTables = listOf("Book", "Chapter", "Download", "History", "Catalog"),
                onFixDatabase = {}
            )
        }
    }
}
