package ireader.data.sync.datasource

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO as ClientCIO
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.cio.CIO as ServerCIO
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.SyncData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Ktor-based implementation of TransferDataSource using WebSockets.
 * 
 * This implementation works on both Android and Desktop platforms.
 * Uses Ktor CIO engine for WebSocket connections.
 */
class KtorTransferDataSource : TransferDataSource {
    
    private var server: EmbeddedServer<*, *>? = null
    private var client: HttpClient? = null
    private var clientSession: DefaultClientWebSocketSession? = null
    private var serverSession: DefaultWebSocketServerSession? = null
    private var connectionJob: Job? = null
    
    private val transferProgressFlow = MutableStateFlow(0f)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }
    
    companion object {
        private const val WEBSOCKET_PATH = "/sync"
        private const val PING_INTERVAL_MS = 15000L
        private const val TIMEOUT_MS = 30000L
    }
    
    override suspend fun startServer(port: Int): Result<Int> {
        return try {
            if (server != null) {
                return Result.failure(Exception("Server already running"))
            }
            
            val serverEngine = embeddedServer(ServerCIO, port = port) {
                install(WebSockets) {
                    pingPeriod = PING_INTERVAL_MS.milliseconds
                    timeout = TIMEOUT_MS.milliseconds
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
                
                routing {
                    webSocket(WEBSOCKET_PATH) {
                        serverSession = this
                        
                        // Keep connection alive until closed
                        try {
                            for (frame in incoming) {
                                // Messages are handled via receiveData()
                            }
                        } catch (e: Exception) {
                            // Connection closed
                        } finally {
                            serverSession = null
                        }
                    }
                }
            }
            
            serverEngine.start(wait = false)
            server = serverEngine
            
            // Give server time to bind to port
            delay(100)
            
            Result.success(port)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun stopServer(): Result<Unit> {
        return try {
            serverSession?.close()
            serverSession = null
            
            server?.stop(1000, 2000)
            server = null
            
            // Give server time to fully stop
            delay(100)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun connectToDevice(deviceInfo: DeviceInfo): Result<Unit> {
        return try {
            if (client != null) {
                return Result.failure(Exception("Already connected"))
            }
            
            val httpClient = HttpClient(ClientCIO) {
                install(io.ktor.client.plugins.websocket.WebSockets) {
                    pingInterval = PING_INTERVAL_MS.milliseconds
                    maxFrameSize = Long.MAX_VALUE
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
            }
            
            client = httpClient
            
            // Use CompletableDeferred to wait for connection establishment
            val connectionEstablished = CompletableDeferred<Boolean>()
            
            // Launch WebSocket connection in background
            connectionJob = scope.launch {
                try {
                    httpClient.webSocket(
                        method = HttpMethod.Get,
                        host = deviceInfo.ipAddress,
                        port = deviceInfo.port,
                        path = WEBSOCKET_PATH
                    ) {
                        clientSession = this
                        connectionEstablished.complete(true)
                        
                        // Keep connection alive
                        try {
                            for (frame in incoming) {
                                // Messages are handled via receiveData()
                            }
                        } catch (e: Exception) {
                            // Connection closed
                        } finally {
                            clientSession = null
                        }
                    }
                } catch (e: Exception) {
                    clientSession = null
                    connectionEstablished.complete(false)
                }
            }
            
            // Wait for connection to establish with timeout
            val connected = withTimeoutOrNull(2.seconds) {
                connectionEstablished.await()
            } ?: false
            
            if (!connected) {
                client?.close()
                client = null
                connectionJob?.cancel()
                connectionJob = null
                return Result.failure(Exception("Failed to establish connection"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            client?.close()
            client = null
            connectionJob?.cancel()
            connectionJob = null
            Result.failure(e)
        }
    }
    
    override suspend fun disconnectFromDevice(): Result<Unit> {
        return try {
            clientSession?.close()
            clientSession = null
            
            connectionJob?.cancel()
            connectionJob = null
            
            client?.close()
            client = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendData(data: SyncData): Result<Unit> {
        return try {
            val session = clientSession ?: serverSession
                ?: return Result.failure(Exception("No active connection"))
            
            transferProgressFlow.value = 0f
            
            val jsonData = json.encodeToString(data)
            val totalBytes = jsonData.length.toLong()
            
            // Send data in chunks for progress tracking
            val chunkSize = 8192
            var bytesSent = 0L
            
            for (i in jsonData.indices step chunkSize) {
                val end = minOf(i + chunkSize, jsonData.length)
                val chunk = jsonData.substring(i, end)
                
                session.send(Frame.Text(chunk))
                
                bytesSent += chunk.length
                transferProgressFlow.value = bytesSent.toFloat() / totalBytes
            }
            
            // Send end marker
            session.send(Frame.Text("__END__"))
            transferProgressFlow.value = 1f
            
            Result.success(Unit)
        } catch (e: Exception) {
            transferProgressFlow.value = 0f
            Result.failure(e)
        }
    }
    
    override suspend fun receiveData(): Result<SyncData> {
        return try {
            val session = clientSession ?: serverSession
                ?: return Result.failure(Exception("No active connection"))
            
            transferProgressFlow.value = 0f
            
            val receivedData = StringBuilder()
            var totalBytesReceived = 0L
            
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    
                    if (text == "__END__") {
                        break
                    }
                    
                    receivedData.append(text)
                    totalBytesReceived += text.length
                    
                    // Update progress (estimate based on received bytes)
                    transferProgressFlow.value = minOf(0.99f, totalBytesReceived / 1_000_000f)
                }
            }
            
            val syncData = json.decodeFromString<SyncData>(receivedData.toString())
            transferProgressFlow.value = 1f
            
            Result.success(syncData)
        } catch (e: Exception) {
            transferProgressFlow.value = 0f
            Result.failure(e)
        }
    }
    
    override fun observeTransferProgress(): Flow<Float> {
        return transferProgressFlow
    }
    
    override suspend fun closeConnection(): Result<Unit> {
        return try {
            // Close client connection if exists
            clientSession?.close()
            clientSession = null
            
            connectionJob?.cancel()
            connectionJob = null
            
            client?.close()
            client = null
            
            // Close server session if exists
            serverSession?.close()
            serverSession = null
            
            transferProgressFlow.value = 0f
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
