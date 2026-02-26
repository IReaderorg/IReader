package ireader.data.sync.datasource

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import ireader.core.log.Log
import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.SyncData
import ireader.domain.services.sync.CertificateService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.GzipSink
import okio.GzipSource
import okio.buffer
import okio.use
import kotlin.time.Duration.Companion.seconds

/**
 * TCP-based implementation of TransferDataSource using raw sockets.
 * 
 * Uses plain TCP sockets instead of WebSockets for better firewall compatibility.
 * No HTTP upgrade needed - direct TCP connection.
 * 
 * Protocol:
 * - 4 bytes: Message length (Int, big-endian)
 * - N bytes: Message data (JSON)
 * 
 * @property certificateService Service for certificate operations (optional, for TLS)
 */
class TcpTransferDataSource(
    private val certificateService: CertificateService? = null
) : TransferDataSource {
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var serverJob: Job? = null
    private var clientJob: Job? = null
    
    private val transferProgressFlow = MutableStateFlow(0f)
    private val stateMutex = Mutex()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val selectorManager = SelectorManager(Dispatchers.IO)
    
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }
    
    // Channels for communication
    private var sendChannel = kotlinx.coroutines.channels.Channel<SendRequest>(kotlinx.coroutines.channels.Channel.UNLIMITED)
    private var receiveChannel = kotlinx.coroutines.channels.Channel<SyncData>(kotlinx.coroutines.channels.Channel.UNLIMITED)
    private var manifestSendChannel = kotlinx.coroutines.channels.Channel<ManifestSendRequest>(kotlinx.coroutines.channels.Channel.UNLIMITED)
    private var manifestReceiveChannel = kotlinx.coroutines.channels.Channel<ireader.domain.models.sync.SyncManifest>(kotlinx.coroutines.channels.Channel.UNLIMITED)
    
    private data class SendRequest(
        val data: SyncData,
        val completion: CompletableDeferred<Result<Unit>>
    )
    
    private data class ManifestSendRequest(
        val manifest: ireader.domain.models.sync.SyncManifest,
        val completion: CompletableDeferred<Result<Unit>>
    )
    
    companion object {
        private const val MANIFEST_TYPE = 1
        private const val DATA_TYPE = 2
    }
    
    override suspend fun startServer(port: Int): Result<Int> {
        return try {
            Log.debug { "[TcpTransferDataSource] Starting TCP server on port $port" }
            
            stateMutex.withLock {
                if (serverSocket != null) {
                    Log.debug { "[TcpTransferDataSource] Server already running, stopping it first" }
                    serverSocket?.close()
                    serverJob?.cancel()
                    delay(200)
                }
                
                recreateChannels()
            }
            
            val socket = aSocket(selectorManager).tcp().bind("0.0.0.0", port)
            
            stateMutex.withLock {
                serverSocket = socket
            }
            
            Log.info { "[TcpTransferDataSource] TCP server started on 0.0.0.0:$port" }
            
            // Accept client connection in background
            serverJob = scope.launch {
                try {
                    Log.debug { "[TcpTransferDataSource] Server: Waiting for client connection..." }
                    val clientSocket = socket.accept()
                    Log.info { "[TcpTransferDataSource] Server: Client connected from ${clientSocket.remoteAddress}" }
                    
                    handleConnection(clientSocket, isServer = true)
                } catch (e: Exception) {
                    Log.error(e, "[TcpTransferDataSource] Server error: ${e.message}")
                }
            }
            
            Result.success(port)
        } catch (e: Exception) {
            Log.error(e, "[TcpTransferDataSource] Failed to start server: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun stopServer(): Result<Unit> {
        return try {
            Log.debug { "[TcpTransferDataSource] Stopping TCP server..." }
            
            stateMutex.withLock {
                closeChannels()
                serverSocket?.close()
                serverSocket = null
                serverJob?.cancel()
                serverJob = null
            }
            
            Log.info { "[TcpTransferDataSource] TCP server stopped" }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error(e, "[TcpTransferDataSource] Error stopping server: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun connectToDevice(deviceInfo: DeviceInfo): Result<Unit> {
        return try {
            Log.debug { "[TcpTransferDataSource] Connecting to ${deviceInfo.ipAddress}:${deviceInfo.port}" }
            
            stateMutex.withLock {
                if (clientSocket != null) {
                    clientSocket?.close()
                    clientJob?.cancel()
                    delay(100)
                }
                
                recreateChannels()
            }
            
            val socket = aSocket(selectorManager).tcp().connect(deviceInfo.ipAddress, deviceInfo.port)
            
            stateMutex.withLock {
                clientSocket = socket
            }
            
            Log.info { "[TcpTransferDataSource] Connected to ${deviceInfo.ipAddress}:${deviceInfo.port}" }
            
            // Handle connection in background
            clientJob = scope.launch {
                try {
                    handleConnection(socket, isServer = false)
                } catch (e: Exception) {
                    Log.error(e, "[TcpTransferDataSource] Client error: ${e.message}")
                }
            }
            
            // Give connection time to establish
            delay(100)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error(e, "[TcpTransferDataSource] Failed to connect: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun handleConnection(socket: Socket, isServer: Boolean) {
        val role = if (isServer) "Server" else "Client"
        Log.debug { "[TcpTransferDataSource] $role: Starting message handler" }
        
        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)
        
        coroutineScope {
            // Sender job
            launch {
                try {
                    for (request in sendChannel) {
                        try {
                            Log.debug { "[TcpTransferDataSource] $role: Sending data..." }
                            val jsonData = json.encodeToString(request.data)
                            val compressed = compressData(jsonData)
                            
                            sendMessage(output, DATA_TYPE, compressed)
                            
                            Log.debug { "[TcpTransferDataSource] $role: Data sent (${compressed.size} bytes)" }
                            request.completion.complete(Result.success(Unit))
                        } catch (e: Exception) {
                            Log.error(e, "[TcpTransferDataSource] $role: Send failed: ${e.message}")
                            request.completion.complete(Result.failure(e))
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    Log.debug { "[TcpTransferDataSource] $role: Sender cancelled" }
                    throw e
                }
            }
            
            // Manifest sender job
            launch {
                try {
                    for (request in manifestSendChannel) {
                        try {
                            Log.debug { "[TcpTransferDataSource] $role: Sending manifest..." }
                            val jsonData = json.encodeToString(request.manifest)
                            val bytes = jsonData.encodeToByteArray()
                            
                            sendMessage(output, MANIFEST_TYPE, bytes)
                            
                            Log.debug { "[TcpTransferDataSource] $role: Manifest sent (${bytes.size} bytes)" }
                            request.completion.complete(Result.success(Unit))
                        } catch (e: Exception) {
                            Log.error(e, "[TcpTransferDataSource] $role: Manifest send failed: ${e.message}")
                            request.completion.complete(Result.failure(e))
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    Log.debug { "[TcpTransferDataSource] $role: Manifest sender cancelled" }
                    throw e
                }
            }
            
            // Receiver job
            launch {
                try {
                    while (isActive) {
                        val (type, data) = receiveMessage(input)
                        
                        when (type) {
                            MANIFEST_TYPE -> {
                                Log.debug { "[TcpTransferDataSource] $role: Received manifest (${data.size} bytes)" }
                                val jsonData = data.decodeToString()
                                val manifest = json.decodeFromString<ireader.domain.models.sync.SyncManifest>(jsonData)
                                manifestReceiveChannel.send(manifest)
                            }
                            DATA_TYPE -> {
                                Log.debug { "[TcpTransferDataSource] $role: Received data (${data.size} bytes)" }
                                val jsonData = decompressData(data)
                                val syncData = json.decodeFromString<SyncData>(jsonData)
                                receiveChannel.send(syncData)
                            }
                            else -> {
                                Log.warn { "[TcpTransferDataSource] $role: Unknown message type: $type" }
                            }
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    Log.debug { "[TcpTransferDataSource] $role: Receiver cancelled" }
                    throw e
                } catch (e: Exception) {
                    Log.error(e, "[TcpTransferDataSource] $role: Receiver error: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun sendMessage(output: ByteWriteChannel, type: Int, data: ByteArray) {
        // Write type (1 byte)
        output.writeByte(type.toByte())
        
        // Write length (4 bytes, big-endian)
        output.writeInt(data.size)
        
        // Write data
        output.writeFully(data, 0, data.size)
        
        output.flush()
    }
    
    private suspend fun receiveMessage(input: ByteReadChannel): Pair<Int, ByteArray> {
        // Read type (1 byte)
        val type = input.readByte().toInt()
        
        // Read length (4 bytes, big-endian)
        val length = input.readInt()
        
        // Read data
        val data = ByteArray(length)
        input.readFully(data, 0, length)
        
        return Pair(type, data)
    }
    
    override suspend fun disconnectFromDevice(): Result<Unit> {
        return try {
            stateMutex.withLock {
                closeChannels()
                clientSocket?.close()
                clientSocket = null
                clientJob?.cancel()
                clientJob = null
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendData(data: SyncData): Result<Unit> {
        return try {
            if (!hasActiveConnection()) {
                return Result.failure(Exception("No active connection"))
            }
            
            val completion = CompletableDeferred<Result<Unit>>()
            val request = SendRequest(data, completion)
            sendChannel.send(request)
            completion.await()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun receiveData(): Result<SyncData> {
        return try {
            if (!hasActiveConnection()) {
                return Result.failure(Exception("No active connection"))
            }
            
            val data = withTimeout(30.seconds) {
                receiveChannel.receive()
            }
            
            Result.success(data)
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Receive timeout"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeTransferProgress(): Flow<Float> {
        return transferProgressFlow
    }
    
    override suspend fun closeConnection(): Result<Unit> {
        return try {
            stateMutex.withLock {
                closeChannels()
                clientSocket?.close()
                clientSocket = null
                clientJob?.cancel()
                clientJob = null
                transferProgressFlow.value = 0f
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun hasActiveConnection(): Boolean {
        return stateMutex.withLock {
            clientSocket != null || serverSocket != null
        }
    }
    
    override suspend fun sendManifest(manifest: ireader.domain.models.sync.SyncManifest): Result<Unit> {
        return try {
            Log.debug { "[TcpTransferDataSource] sendManifest() called with ${manifest.items.size} items" }
            
            if (!hasActiveConnection()) {
                Log.error { "[TcpTransferDataSource] ERROR: No active connection" }
                return Result.failure(Exception("No active connection"))
            }
            
            val completion = CompletableDeferred<Result<Unit>>()
            val request = ManifestSendRequest(manifest, completion)
            manifestSendChannel.send(request)
            completion.await()
        } catch (e: Exception) {
            Log.error(e, "[TcpTransferDataSource] Failed to send manifest: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun receiveManifest(): Result<ireader.domain.models.sync.SyncManifest> {
        return try {
            Log.debug { "[TcpTransferDataSource] Waiting to receive manifest..." }
            
            if (!hasActiveConnection()) {
                return Result.failure(Exception("No active connection"))
            }
            
            val manifest = withTimeout(30.seconds) {
                manifestReceiveChannel.receive()
            }
            
            Log.debug { "[TcpTransferDataSource] Manifest received: ${manifest.items.size} items" }
            Result.success(manifest)
        } catch (e: TimeoutCancellationException) {
            Log.error { "[TcpTransferDataSource] Manifest receive timeout" }
            Result.failure(Exception("Manifest receive timeout"))
        } catch (e: Exception) {
            Log.error(e, "[TcpTransferDataSource] Failed to receive manifest: ${e.message}")
            Result.failure(e)
        }
    }
    
    private fun recreateChannels() {
        sendChannel = kotlinx.coroutines.channels.Channel(kotlinx.coroutines.channels.Channel.UNLIMITED)
        receiveChannel = kotlinx.coroutines.channels.Channel(kotlinx.coroutines.channels.Channel.UNLIMITED)
        manifestSendChannel = kotlinx.coroutines.channels.Channel(kotlinx.coroutines.channels.Channel.UNLIMITED)
        manifestReceiveChannel = kotlinx.coroutines.channels.Channel(kotlinx.coroutines.channels.Channel.UNLIMITED)
    }
    
    private fun closeChannels() {
        try {
            sendChannel.close()
            receiveChannel.close()
            manifestSendChannel.close()
            manifestReceiveChannel.close()
        } catch (e: Exception) {
            // Channels might already be closed
        }
    }
    
    private fun compressData(data: String): ByteArray {
        val buffer = Buffer()
        GzipSink(buffer).buffer().use { sink ->
            sink.writeString(data, Charsets.UTF_8)
        }
        return buffer.readByteArray()
    }
    
    private fun decompressData(compressedData: ByteArray): String {
        val source = Buffer().write(compressedData)
        return GzipSource(source).buffer().readString(Charsets.UTF_8)
    }
}
