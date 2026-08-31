package ireader.domain.services.tts_service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GradioTTSPresetsTest {

    @Test
    fun testOnlyCoquiTTSIsBuiltInPreset() {
        val presets = GradioTTSPresets.getAllPresets()
        assertEquals(1, presets.size)
        assertEquals("coqui_ireader", presets[0].id)
        assertEquals("Coqui TTS (IReader)", presets[0].name)
    }

    @Test
    fun testGetPresetById() {
        val coqui = GradioTTSPresets.getPresetById("coqui_ireader")
        assertNotNull(coqui)
        assertEquals("coqui_ireader", coqui.id)

        val nonExistent = GradioTTSPresets.getPresetById("edge_tts_cloud")
        assertNull(nonExistent)
    }
}
