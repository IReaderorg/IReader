package ireader.data.sync

import ireader.data.sync.datasource.KtorTransferDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * Manual test to verify Ktor server starts and listens on port.
 * 
 * Run this test, then in PowerShell run:
 * Test-NetConnection -ComputerName localhost -Port 8963
 * 
 * Expected: TcpTestSucceeded : True
 */
fun main() = runBlocking {
    println("Starting Ktor server test...")
    
    val dataSource = KtorTransferDataSource()
    
    println("Starting server on port 8963...")
    val result = dataSource.startServer(8963)
    
    if (result.isSuccess) {
        println("✓ Server started successfully on port ${result.getOrNull()}")
        println("Server should now be listening on 0.0.0.0:8963")
        println("Test from PowerShell: Test-NetConnection -ComputerName localhost -Port 8963")
        println("Press Ctrl+C to stop...")
        
        // Keep server running
        while (true) {
            delay(1000)
        }
    } else {
        println("✗ Server failed to start: ${result.exceptionOrNull()?.message}")
        result.exceptionOrNull()?.printStackTrace()
    }
}
