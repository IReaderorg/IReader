package ireader.domain.usecases.backup.v2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratedBackupSettingsTest {

    @Test
    fun `KSP generates registry entries for annotated preference getters`() {
        val keys = GeneratedBackupSettings.entries.map { it.preferenceKey }

        assertTrue("browse_concurrent_searches" in keys)
        assertTrue("browse_selected_languages" in keys)
        assertTrue("browse_search_timeout" in keys)
        assertTrue("browse_max_results" in keys)
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `generated settings are available to the backup registry`() {
        val byKey = SettingsBackupRegistry.entries.associateBy { it.preferenceKey }

        assertEquals(SettingType.INT, byKey["browse_concurrent_searches"]?.type)
        assertEquals(SettingType.STRING_SET, byKey["browse_selected_languages"]?.type)
        assertEquals(SettingType.LONG, byKey["browse_search_timeout"]?.type)
    }

    @Test
    fun `unannotated preference getters are discovered automatically`() {
        val keys = GeneratedBackupSettings.entries.map { it.preferenceKey }

        assertTrue(GeneratedBackupSettings.entries.size > 150)
        assertTrue("dynamic_color_mode" in keys)
        assertTrue("reader_font_size" in keys)
        assertTrue("tts_speed" in keys)
        assertTrue("language" in keys)
    }

    @Test
    fun `secrets and explicitly excluded getters are not generated`() {
        val keys = GeneratedBackupSettings.entries.map { it.preferenceKey }

        assertTrue("active_gradio_api_key" !in keys)
        assertTrue("mangaupdates_session_token" !in keys)
        assertTrue("saved_local_catalog_location" !in keys)
    }
}
