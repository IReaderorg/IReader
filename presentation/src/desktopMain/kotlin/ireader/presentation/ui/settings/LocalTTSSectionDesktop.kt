package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.core.log.Log
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.tts_service.TTSEngineCallback
import ireader.domain.services.tts_service.local.LocalTTSApiFormat
import ireader.domain.services.tts_service.local.LocalTTSConfig
import ireader.domain.services.tts_service.local.LocalTTSHealthResponse
import ireader.domain.services.tts_service.local.LocalTTSManager
import ireader.domain.services.tts_service.local.LocalTTSVoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Desktop settings card for Local Server TTS (e.g. Thomcles/Chatterbox-TTS-Persian-Farsi).
 */
@Composable
fun LocalTTSSectionDesktop(
    appPrefs: AppPreferences,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val localTTSManager: LocalTTSManager = koinInject()
    var useLocalTTS by remember { mutableStateOf(false) }
    var config by remember { mutableStateOf(localTTSManager.config.value) }

    var serverUrl by remember { mutableStateOf(config.serverUrl) }
    var selectedVoice by remember { mutableStateOf(config.voice) }
    var selectedFormat by remember { mutableStateOf(config.apiFormat) }
    var speed by remember { mutableStateOf(config.speed) }

    var isCheckingConnection by remember { mutableStateOf(false) }
    var connectionHealth by remember { mutableStateOf<LocalTTSHealthResponse?>(null) }
    var connectionError by remember { mutableStateOf<String?>(null) }

    var voices by remember { mutableStateOf(LocalTTSManager.DEFAULT_VOICES) }
    var isPlayingTest by remember { mutableStateOf(false) }
    var testEngine by remember { mutableStateOf<ireader.domain.services.tts_service.local.LocalTTSEngine?>(null) }

    LaunchedEffect(Unit) {
        useLocalTTS = appPrefs.useLocalTTS().get()
        config = localTTSManager.config.value
        serverUrl = config.serverUrl
        selectedVoice = config.voice
        selectedFormat = config.apiFormat
        speed = config.speed

        if (useLocalTTS) {
            // Fetch voices in background
            scope.launch {
                voices = localTTSManager.fetchVoices(serverUrl)
            }
        }
    }

    fun saveConfig() {
        val newConfig = config.copy(
            serverUrl = serverUrl,
            voice = selectedVoice,
            apiFormat = selectedFormat,
            speed = speed,
            enabled = useLocalTTS
        )
        config = newConfig
        localTTSManager.updateConfig(newConfig)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (useLocalTTS)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        Text(
                            text = "Local Server TTS (Chatterbox Persian)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connect to Thomcles/Chatterbox-TTS-Persian-Farsi or local network server",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = useLocalTTS,
                    onCheckedChange = { enabled ->
                        useLocalTTS = enabled
                        scope.launch {
                            appPrefs.useLocalTTS().set(enabled)
                            saveConfig()
                            if (enabled) {
                                voices = localTTSManager.fetchVoices(serverUrl)
                            }
                        }
                    }
                )
            }

            if (useLocalTTS) {
                HorizontalDivider()

                // Server URL input + Test Button
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Server Address",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = {
                                serverUrl = it
                                saveConfig()
                            },
                            label = { Text("Server URL (e.g. http://192.168.1.50:8000)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                isCheckingConnection = true
                                connectionHealth = null
                                connectionError = null
                                scope.launch {
                                    try {
                                        val result = localTTSManager.testConnection(serverUrl)
                                        if (result.isSuccess) {
                                            connectionHealth = result.getOrNull()
                                            voices = localTTSManager.fetchVoices(serverUrl)
                                        } else {
                                            connectionError = result.exceptionOrNull()?.message ?: "Connection failed"
                                        }
                                    } catch (e: Exception) {
                                        connectionError = e.message ?: "Unknown error"
                                    } finally {
                                        isCheckingConnection = false
                                    }
                                }
                            },
                            enabled = !isCheckingConnection && serverUrl.isNotBlank()
                        ) {
                            if (isCheckingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Connection")
                            }
                        }
                    }

                    // Connection status badge
                    if (connectionHealth != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Connected: ${connectionHealth?.model?.ifEmpty { "Chatterbox TTS" }} (${connectionHealth?.device?.uppercase()}) - Sample rate: ${connectionHealth?.sample_rate}Hz",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else if (connectionError != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Column {
                                    Text(
                                        text = "Connection Failed: $connectionError",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Make sure chatterbox-tts-server is running (run run.bat or python server.py).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Voice selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Persian Voice Model",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        voices.forEach { voice ->
                            val isSelected = selectedVoice == voice.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedVoice = voice.id
                                    saveConfig()
                                },
                                label = { Text(voice.name) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                HorizontalDivider()

                // API Format
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "API Protocol",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedFormat == LocalTTSApiFormat.SIMPLE_REST,
                                onClick = {
                                    selectedFormat = LocalTTSApiFormat.SIMPLE_REST
                                    saveConfig()
                                }
                            )
                            Text("Simple REST (/api/tts)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedFormat == LocalTTSApiFormat.OPENAI_SPEECH,
                                onClick = {
                                    selectedFormat = LocalTTSApiFormat.OPENAI_SPEECH
                                    saveConfig()
                                }
                            )
                            Text("OpenAI Audio Speech (/v1/audio/speech)")
                        }
                    }
                }

                HorizontalDivider()

                // Speed Slider
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speech Speed",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(speed * 10).toInt() / 10f}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = speed,
                        onValueChange = {
                            speed = it
                            saveConfig()
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 15
                    )
                }

                HorizontalDivider()

                // Test Persian Speech Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPlayingTest) {
                        Button(
                            onClick = {
                                testEngine?.stop()
                                isPlayingTest = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop Playback")
                        }
                    } else {
                        Button(
                            onClick = {
                                saveConfig()
                                scope.launch {
                                    try {
                                        isPlayingTest = true
                                        val engine = localTTSManager.createEngine(config)
                                        testEngine = engine
                                        engine.setCallback(object : TTSEngineCallback {
                                            override fun onStart(utteranceId: String) {}
                                            override fun onDone(utteranceId: String) {
                                                isPlayingTest = false
                                            }
                                            override fun onError(utteranceId: String, error: String) {
                                                Log.error { "Test speech error: $error" }
                                                isPlayingTest = false
                                            }
                                        })
                                        engine.speak(
                                            "درود! این یک آزمایش برای موتور صوتی فارسی آی‌ریدر است.",
                                            "test_utterance"
                                        )
                                    } catch (e: Exception) {
                                        Log.error { "Failed to test speech: ${e.message}" }
                                        isPlayingTest = false
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Persian Speech")
                        }
                    }
                }
            }
        }
    }
}
