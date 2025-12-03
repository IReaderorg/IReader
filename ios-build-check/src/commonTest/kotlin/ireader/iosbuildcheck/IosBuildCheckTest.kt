package ireader.iosbuildcheck

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class IosBuildCheckTest {
    
    @Test
    fun testGetMessage() {
        val check = IosBuildCheck()
        val message = check.getMessage()
        assertNotNull(message)
        assertTrue(message.contains("iOS Build Check"))
    }
    
    @Test
    fun testGreet() {
        val greeting = greet()
        assertNotNull(greeting)
        assertTrue(greeting.startsWith("Hello from"))
    }
}
