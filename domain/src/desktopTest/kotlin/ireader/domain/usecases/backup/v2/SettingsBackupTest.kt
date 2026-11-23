package ireader.domain.usecases.backup.v2

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsBackupTest {

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        private inline fun <reified T : Any> pref(key: String, default: T): Preference<T> =
            object : Preference<T> {
                override fun key(): String = key
                override fun get(): T = values[key] as? T ?: default
                override fun set(value: T) { values[key] = value }
                override fun isSet(): Boolean = values.containsKey(key)
                override fun delete() { values.remove(key) }
                override fun defaultValue(): T = default
                override fun changes(): Flow<T> = MutableStateFlow(get())
                override fun stateIn(scope: CoroutineScope): StateFlow<T> = MutableStateFlow(get())
            }

        override fun getString(key: String, defaultValue: String): Preference<String> =
            pref(key, defaultValue)

        override fun getLong(key: String, defaultValue: Long): Preference<Long> =
            pref(key, defaultValue)

        override fun getInt(key: String, defaultValue: Int): Preference<Int> =
            pref(key, defaultValue)

        override fun getFloat(key: String, defaultValue: Float): Preference<Float> =
            pref(key, defaultValue)

        override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> =
            pref(key, defaultValue)

        override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> =
            pref(key, defaultValue)

        override fun <T> getObject(
            key: String,
            defaultValue: T,
            serializer: (T) -> String,
            deserializer: (String) -> T,
        ): Preference<T> = object : Preference<T> {
            override fun key(): String = key
            override fun get(): T = (values[key] as? String)?.let(deserializer) ?: defaultValue
            override fun set(value: T) { values[key] = serializer(value) }
            override fun isSet(): Boolean = values.containsKey(key)
            override fun delete() { values.remove(key) }
            override fun defaultValue(): T = defaultValue
            override fun changes(): Flow<T> = MutableStateFlow(get())
            override fun stateIn(scope: CoroutineScope): StateFlow<T> = MutableStateFlow(get())
        }

        override fun <T> getJsonObject(
            key: String,
            defaultValue: T,
            serializer: KSerializer<T>,
            serializersModule: SerializersModule,
        ): Preference<T> = throw UnsupportedOperationException()
    }

    @Test
    fun `round trips settings from every group`() {
        val store = InMemoryPreferenceStore()
        store.getInt("reader_font_size", 18).set(24)
        store.getBoolean("dynamic_color_mode", false).set(true)
        store.getString("language", "").set("ja")
        store.getBoolean("download_only_on_wifi", false).set(true)
        store.getFloat("tts_speed", 1f).set(1.5f)
        store.getString("translator_target_language", "en").set("fr")

        val snapshot = SettingsBackup(store).collect()
        assertTrue(snapshot.any { it.key == "font_size" && it.value == "24" })
        assertTrue(snapshot.any { it.key == "dynamic_color_mode" && it.value == "true" })
        assertTrue(snapshot.any { it.key == "language" && it.value == "ja" })

        store.getInt("reader_font_size", 18).delete()
        store.getBoolean("dynamic_color_mode", false).delete()
        store.getString("language", "").delete()
        store.getBoolean("download_only_on_wifi", false).delete()
        store.getFloat("tts_speed", 1f).delete()
        store.getString("translator_target_language", "en").delete()

        val restored = SettingsBackup(store).restore(snapshot)
        assertEquals(6, restored)
        assertEquals(24, store.getInt("reader_font_size", 18).get())
        assertTrue(store.getBoolean("dynamic_color_mode", false).get())
        assertEquals("ja", store.getString("language", "").get())
        assertTrue(store.getBoolean("download_only_on_wifi", false).get())
        assertEquals(1.5f, store.getFloat("tts_speed", 1f).get())
        assertEquals("fr", store.getString("translator_target_language", "en").get())
    }

    @Test
    fun `legacy backup keys map to current preference keys`() {
        val store = InMemoryPreferenceStore()
        val backup = SettingsBackup(store)
        val count = backup.restore(
            listOf(
                SettingSnapshot("theme_mode", "Dark"),
                SettingSnapshot("font_size", "30"),
                SettingSnapshot("line_height", "40"),
                SettingSnapshot("auto_backup", "Weekly"),
                SettingSnapshot("max_auto_backup", "7"),
                SettingSnapshot("max_auto_backup_age_days", "30"),
                SettingSnapshot("max_auto_backup_size_mb", "100"),
            )
        )

        assertEquals(7, count)
        assertEquals("Dark", store.getString("theme_mode", "System").get())
        assertEquals(30, store.getInt("reader_font_size", 18).get())
        assertEquals(40, store.getInt("font_height", 28).get())
        assertEquals("Weekly", store.getString("automatic_backup_time", "Off").get())
        assertEquals(7, store.getInt("max_automatic_backup_size", 5).get())
        assertEquals(30, store.getInt("max_automatic_backup_age_days", 7).get())
        assertEquals(100, store.getInt("max_automatic_backup_size_mb", 50).get())
    }

    @Test
    fun `invalid and unknown settings are skipped and not counted`() {
        val store = InMemoryPreferenceStore()
        val count = SettingsBackup(store).restore(
            listOf(
                SettingSnapshot("font_size", "not-an-int"),
                SettingSnapshot("dynamic_color_mode", "yes-please"),
                SettingSnapshot("does_not_exist", "1"),
                SettingSnapshot("language", "ko"),
            )
        )

        assertEquals(1, count)
        assertEquals("ko", store.getString("language", "").get())
        assertFalse(store.getString("does_not_exist", "").isSet())
    }

    @Test
    fun `registry has unique backup and preference keys`() {
        val backupKeys = SettingsBackupRegistry.entries.map { it.backupKey }
        val preferenceKeys = SettingsBackupRegistry.entries.map { it.preferenceKey }
        assertEquals(backupKeys.size, backupKeys.distinct().size, "duplicate backup key")
        assertEquals(preferenceKeys.size, preferenceKeys.distinct().size, "duplicate preference key")
        assertTrue(SettingsBackupRegistry.entries.size > 200)
    }

    @Test
    fun `registry excludes secrets and transient state`() {
        val keys = SettingsBackupRegistry.entries.map { it.preferenceKey }
        val forbidden = listOf(
            "api_key", "token", "password", "cookie", "secret", "credential", "app_lock_pin",
        )
        forbidden.forEach { fragment ->
            assertFalse(
                keys.any { it.contains(fragment, ignoreCase = true) },
                "registry must not include secret-like key containing '$fragment'",
            )
        }
        listOf(
            "last_sync_timestamp", "last_app_unlock", "last_app_closed",
            "current_session_start_time", "last_reading_break_prompt_time",
            "last_automatic_backup_time", "last_update_check", "source_errors",
            "has_completed_onboarding", "database_version",
        ).forEach { key ->
            assertFalse(key in keys, "registry must not include transient key '$key'")
        }
    }
}
