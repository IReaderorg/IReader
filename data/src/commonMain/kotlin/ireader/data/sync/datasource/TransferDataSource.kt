package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.SyncData
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for transferring sync data between devices.
 * 
 * This interface defines the contract for establishing connections and transferring
 * data between devices using WebSocket connections (Ktor).
 * 
 * The transfer protocol:
 * 1. Server device calls [startServer] to listen for connections
 * 2. Client device calls [connectToDevice] to establish connection
 * 3. Devices exchange data using [sendData] and [receiveData]
 * 4. Progress can be monitored via [observeTransferProgress]
 * 5. Connection is closed with [closeConnection] or [disconnectFromDevice]
 */
interface TransferDataSource {
    
    /**
     * Start a server to accept incoming sync connections.
     * 
     * The server will listen on the specified port for WebSocket connections
     * from other IReader devices. Only one server can run at a time.
     * 
     * @param port Port number to listen on (typically 8080-8090)
     * @return Result containing the actual port number if successful
     */
    suspend fun startServer(port: Int): Result<Int>
    
    /**
     * Stop the server and close all active connections.
     * 
     * @return Result indicating success or failure
     */
    suspend fun stopServer(): Result<Unit>
    
    /**
     * Connect to a remote device as a client.
     * 
     * Establishes a WebSocket connection to the specified device.
     * The device must be running a server (via [startServer]).
     * 
     * @param deviceInfo Information about the device to connect to
     * @return Result indicating success or failure
     */
    suspend fun connectToDevice(deviceInfo: DeviceInfo): Result<Unit>
    
    /**
     * Disconnect from the remote device.
     * 
     * Closes the client connection gracefully.
     * 
     * @return Result indicating success or failure
     */
    suspend fun disconnectFromDevice(): Result<Unit>
    
    /**
     * Send sync data to the connected device.
     * 
     * The data is serialized to JSON and sent over the WebSocket connection.
     * Progress updates are emitted via [observeTransferProgress].
     * 
     * @param data The sync data to send
     * @return Result indicating success or failure
     */
    suspend fun sendData(data: SyncData): Result<Unit>
    
    /**
     * Receive sync data from the connected device.
     * 
     * Waits for incoming data on the WebSocket connection and deserializes it.
     * This is a suspending function that will wait until data arrives or timeout occurs.
     * 
     * @return Result containing the received sync data or error
     */
    suspend fun receiveData(): Result<SyncData>
    
    /**
     * Observe the progress of data transfer operations.
     * 
     * Emits progress values from 0.0 (0%) to 1.0 (100%) during send/receive operations.
     * Progress is calculated based on bytes transferred vs total size.
     * 
     * @return Flow emitting progress values between 0.0 and 1.0
     */
    fun observeTransferProgress(): Flow<Float>
    
    /**
     * Close the current connection (client or server).
     * 
     * This is a general-purpose method that closes any active connection,
     * whether acting as client or server.
     * 
     * @return Result indicating success or failure
     */
    suspend fun closeConnection(): Result<Unit>
    
    /**
     * Check if there is an active connection (client or server).
     * 
     * Returns true if either a client session or server session is active
     * and ready to send/receive data.
     * 
     * @return true if there is an active connection, false otherwise
     */
    suspend fun hasActiveConnection(): Boolean
}
