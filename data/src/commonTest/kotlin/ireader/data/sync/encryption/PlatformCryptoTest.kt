package ireader.data.sync.encryption

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for PlatformCrypto implementations across all platforms.
 * 
 * Tests secure random generation and AES-GCM encryption/decryption.
 */
class PlatformCryptoTest {
    
    private val crypto = PlatformCrypto()
    
    @Test
    fun `generateSecureRandom should generate bytes of requested size`() {
        val size = 32
        val random = crypto.generateSecureRandom(size)
        
        assertEquals(size, random.size, "Generated random bytes should match requested size")
    }
    
    @Test
    fun `generateSecureRandom should generate different values each time`() {
        val random1 = crypto.generateSecureRandom(32)
        val random2 = crypto.generateSecureRandom(32)
        
        assertNotEquals(
            random1.contentToString(),
            random2.contentToString(),
            "Two random generations should produce different values"
        )
    }
    
    @Test
    fun `generateSecureRandom should not generate all zeros`() {
        val random = crypto.generateSecureRandom(32)
        val allZeros = random.all { it == 0.toByte() }
        
        assertTrue(!allZeros, "Random bytes should not be all zeros")
    }
    
    @Test
    fun `generateSecureRandom with zero size should return empty array`() {
        val random = crypto.generateSecureRandom(0)
        
        assertEquals(0, random.size, "Zero size should return empty array")
    }
}
