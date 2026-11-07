package ireader.presentation.core.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalNetworkPermissionTest {

    @Test
    fun private192168_isLocal() {
        assertTrue(isLocalNetworkUrl("http://192.168.1.100:1234/v1"))
    }

    @Test
    fun private10_isLocal() {
        assertTrue(isLocalNetworkUrl("http://10.0.0.5:8080"))
    }

    @Test
    fun private172_isLocal() {
        assertTrue(isLocalNetworkUrl("http://172.16.0.1:11434"))
        assertTrue(isLocalNetworkUrl("http://172.31.255.255"))
    }

    @Test
    fun public172_isNotLocal() {
        assertFalse(isLocalNetworkUrl("http://172.32.0.1"))
    }

    @Test
    fun loopback_isLocal() {
        assertTrue(isLocalNetworkUrl("http://localhost:11434"))
        assertTrue(isLocalNetworkUrl("http://127.0.0.1:1234"))
    }

    @Test
    fun mDNS_isLocal() {
        assertTrue(isLocalNetworkUrl("http://myserver.local:1234/v1"))
    }

    @Test
    fun linkLocal_isLocal() {
        assertTrue(isLocalNetworkUrl("http://169.254.1.2:8080"))
    }

    @Test
    fun ipv6ULA_isLocal() {
        assertTrue(isLocalNetworkUrl("http://[fd00::1]:1234"))
        assertTrue(isLocalNetworkUrl("http://[::1]:1234"))
    }

    @Test
    fun publicHost_isNotLocal() {
        assertFalse(isLocalNetworkUrl("https://api.openai.com/v1"))
        assertFalse(isLocalNetworkUrl("https://8.8.8.8/v1"))
    }

    @Test
    fun blank_isNotLocal() {
        assertFalse(isLocalNetworkUrl(""))
        assertFalse(isLocalNetworkUrl("   "))
    }
}
