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
import java.text.SimpleDateFormat
import java.util.*

class CrashActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        
        setContent {
            CrashScreen(crashReport = crashReport)
        }
    }
}

@Composable
fun CrashScreen(crashReport: CrashReport) {
    val context = LocalContext.current
    var showFullStackTrace by remember { mutableStateOf(false) }
    
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
