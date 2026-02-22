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
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * Test to verify WebSocket session persistence fix.
 * 
 * This test simulates the exact scenario that was failing:
 * 1. Start a WebSocket server
 * 2. Connect a client
 * 3. Verify the session persists (doesn't close immediately)
 * 4. Verify hasActiveConnection() returns true
 */

class WebSocketSessionTest {
    private var server: EmbeddedServer<*, *>? = null
    private var client: HttpClient? = null
    private var clientSession: DefaultClientWebSocketSession? = null
    private var serverSession: DefaultWebSocketServerSession? = null
    
    private val stateMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    companion object {
        private const val WEBSOCKET_PATH = "/sync"
        private const val TEST_PORT = 8080
    }
    
    suspend fun startServer(): Boolean {
        return try {
            val serverEngine = embeddedServer(ServerCIO, port = TEST_PORT) {
                install(WebSockets) {
                    pingPeriod = 15.seconds
                    timeout = 30.seconds
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
                
                routing {
                    webSocket(WEBSOCKET_PATH) {
                        println("[TEST] Server: Client connected, setting up session...")
                        stateMutex.withLock {
                            serverSession = this
                        }
                        println("[TEST] Server: Session established successfully")
                        
                        // Keep connection alive - THE FIX
                        try {
                            while (isActive) {
                                try {
                                    val frame = withTimeoutOrNull(30.seconds) {
                                        incoming.receiveCatching().getOrNull()
                                    }
                                    
                                    if (frame == null) {
                                        println("[TEST] Server: Sending keep-alive ping")
                                        try {
                                            send(Frame.Ping(ByteArray(0)))
                                        } catch (e: Exception) {
                                            println("[TEST] Server: Failed to send ping: ${e.message}")
                                            break
                                        }
                                    } else {
                                        println("[TEST] Server: Received frame: ${frame.frameType}")
                                    }
                                } catch (e: Exception) {
                                    println("[TEST] Server: Error in WebSocket loop: ${e.message}")
                                    break
                                }
                            }
                        } finally {
                            println("[TEST] Server: Cleaning up server session")
                            stateMutex.withLock {
                                serverSession = null
                            }
                        }
                    }
                }
            }
            
            serverEngine.start(wait = false)
            stateMutex.withLock {
                server = serverEngine
            }
            
            delay(500) // Give server time to start
            println("[TEST] Server started on port $TEST_PORT")
            true
        } catch (e: Exception) {
            println("[TEST] Failed to start server: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun connectClient(): Boolean {
        return try {
            val httpClient = HttpClient(ClientCIO) {
                install(io.ktor.client.plugins.websocket.WebSockets) {
                    pingInterval = 15.seconds
                    maxFrameSize = Long.MAX_VALUE
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
            }
            
            stateMutex.withLock {
                client = httpClient
            }
            
            val connectionEstablished = CompletableDeferred<Boolean>()
            
            scope.launch {
                try {
                    println("[TEST] Client: Connecting to localhost:$TEST_PORT")
                    httpClient.webSocket(
                        method = HttpMethod.Get,
                        host = "localhost",
                        port = TEST_PORT,
                        path = WEBSOCKET_PATH
                    ) {
                        println("[TEST] Client: WebSocket connected, setting up session...")
                        stateMutex.withLock {
                            clientSession = this
                        }
                        connectionEstablished.complete(true)
                        println("[TEST] Client: Session established successfully")
                        
                        // Keep connection alive - THE FIX
                        try {
                            while (isActive) {
                                try {
                                    val frame = withTimeoutOrNull(30.seconds) {
                                        incoming.receiveCatching().getOrNull()
                                    }
                                    
                                    if (frame == null) {
                                        println("[TEST] Client: Sending keep-alive ping")
                                        try {
                                            send(Frame.Ping(ByteArray(0)))
                                        } catch (e: Exception) {
                                            println("[TEST] Client: Failed to send ping: ${e.message}")
                                            break
                                        }
                                    } else {
                                        println("[TEST] Client: Received frame: ${frame.frameType}")
                                    }
                                } catch (e: Exception) {
                                    println("[TEST] Client: Error in WebSocket loop: ${e.message}")
                                    break
                                }
                            }
                        } finally {
                            println("[TEST] Client: Cleaning up client session")
                            stateMutex.withLock {
                                clientSession = null
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("[TEST] Client: Failed to connect: ${e.message}")
                    e.printStackTrace()
                    stateMutex.withLock {
                        clientSession = null
                    }
                    connectionEstablished.complete(false)
                }
            }
            
            val connected = withTimeoutOrNull(5.seconds) {
                connectionEstablished.await()
            } ?: false
            
            if (connected) {
                println("[TEST] Client connected successfully")
            } else {
                println("[TEST] Client connection failed or timed out")
            }
            
            connected
        } catch (e: Exception) {
            println("[TEST] Failed to connect client: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    suspend fun hasActiveConnection(): Boolean {
        return stateMutex.withLock {
            val hasClient = clientSession != null
            val hasServer = serverSession != null
            println("[TEST] hasActiveConnection: client=$hasClient, server=$hasServer")
            hasClient || hasServer
        }
    }
    
    suspend fun cleanup() {
        println("[TEST] Cleaning up...")
        stateMutex.withLock {
            clientSession?.close()
            clientSession = null
            
            serverSession?.close()
            serverSession = null
            
            client?.close()
            client = null
            
            server?.stop(1000, 2000)
            server = null
        }
        scope.cancel()
        println("[TEST] Cleanup complete")
    }
}

suspend fun main() {
    println("=".repeat(80))
    println("WebSocket Session Persistence Test")
    println("=".repeat(80))
    println()
    
    val test = WebSocketSessionTest()
    
    try {
        // Step 1: Start server
        println("Step 1: Starting server...")
        val serverStarted = test.startServer()
        if (!serverStarted) {
            println("❌ FAILED: Server did not start")
            return
        }
        println("✅ Server started successfully")
        println()
        
        // Step 2: Connect client
        println("Step 2: Connecting client...")
        val clientConnected = test.connectClient()
        if (!clientConnected) {
            println("❌ FAILED: Client did not connect")
            test.cleanup()
            return
        }
        println("✅ Client connected successfully")
        println()
        
        // Step 3: Wait a moment for connection to stabilize
        println("Step 3: Waiting for connection to stabilize...")
        delay(1000)
        println("✅ Connection stabilized")
        println()
        
        // Step 4: Check if connection is active (THE CRITICAL TEST)
        println("Step 4: Checking if connection is active...")
        val isActive1 = test.hasActiveConnection()
        if (!isActive1) {
            println("❌ FAILED: Connection is not active after 1 second")
            test.cleanup()
            return
        }
        println("✅ Connection is active after 1 second")
        println()
        
        // Step 5: Wait longer and check again
        println("Step 5: Waiting 5 more seconds...")
        delay(5000)
        val isActive2 = test.hasActiveConnection()
        if (!isActive2) {
            println("❌ FAILED: Connection is not active after 6 seconds")
            test.cleanup()
            return
        }
        println("✅ Connection is still active after 6 seconds")
        println()
        
        // Step 6: Wait even longer to test keep-alive
        println("Step 6: Waiting 10 more seconds to test keep-alive...")
        delay(10000)
        val isActive3 = test.hasActiveConnection()
        if (!isActive3) {
            println("❌ FAILED: Connection is not active after 16 seconds")
            test.cleanup()
            return
        }
        println("✅ Connection is still active after 16 seconds")
        println()
        
        // Success!
        println("=".repeat(80))
        println("✅ TEST PASSED: WebSocket session persists correctly!")
        println("=".repeat(80))
        println()
        println("Summary:")
        println("- Server started successfully")
        println("- Client connected successfully")
        println("- Connection remained active for 16+ seconds")
        println("- Keep-alive mechanism is working")
        println()
        
    } catch (e: Exception) {
        println("❌ TEST FAILED with exception: ${e.message}")
        e.printStackTrace()
    } finally {
        test.cleanup()
    }
}
