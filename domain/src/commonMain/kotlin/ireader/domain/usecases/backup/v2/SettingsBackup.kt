package ireader.domain.usecases.backup.v2

import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.usecases.backup.v2.SettingType.BOOLEAN
import ireader.domain.usecases.backup.v2.SettingType.FLOAT
import ireader.domain.usecases.backup.v2.SettingType.INT
import ireader.domain.usecases.backup.v2.SettingType.LONG
import ireader.domain.usecases.backup.v2.SettingType.STRING
import ireader.domain.usecases.backup.v2.SettingType.STRING_SET

/**
 * Wire format type of a backed-up preference. Enum preferences are stored as their [name] by
 * [ireader.core.prefs.getEnum], so they use [STRING].
 */
enum class SettingType {
    INT, LONG, FLOAT, BOOLEAN, STRING, STRING_SET,
}

/**
 * One entry in the settings backup allow-list.
 *
 * [backupKey] is the stable key written into the backup file; it may differ from the current
 * [preferenceKey] for legacy entries so old backups keep restoring correctly.
 */
data class SettingSpec(
    val backupKey: String,
    val preferenceKey: String,
    val type: SettingType,
    val allowedValues: Set<String>? = null,
)

/**
 * Allow-list of user preferences included in portable backups.
 *
 * Entries are generated at compile time from `PreferenceStore` getters across modules by
 * backup-ksp; [manualEntries] contains only legacy key mappings needed for old backups.
 * Secrets (API keys, tokens, passwords, cookies), transient state (timestamps, install flags)
 * and dynamic per-plugin/per-source keys are excluded by the generator.
 */
object SettingsBackupRegistry {

    /**
     * Legacy backup-key mappings only. New settings are discovered automatically by backup-ksp;
     * these entries exist solely so backups created by older versions still restore correctly.
     */
    private val manualEntries: List<SettingSpec> = buildList {
        fun spec(
            key: String,
            type: SettingType,
            preferenceKey: String = key,
            allowedValues: Set<String>? = null,
        ) = add(SettingSpec(key, preferenceKey, type, allowedValues))

        spec("theme_mode", STRING, allowedValues = PreferenceValues.ThemeMode.entries.map { it.name }.toSet())
        spec("font_size", INT, preferenceKey = "reader_font_size")
        spec("line_height", INT, preferenceKey = "font_height")
        spec("auto_backup", STRING, preferenceKey = "automatic_backup_time",
            allowedValues = PreferenceValues.AutomaticBackup.entries.map { it.name }.toSet())
        spec("max_auto_backup", INT, preferenceKey = "max_automatic_backup_size")
        spec("max_auto_backup_age_days", INT, preferenceKey = "max_automatic_backup_age_days")
        spec("max_auto_backup_size_mb", INT, preferenceKey = "max_automatic_backup_size_mb")
    }

    val entries: List<SettingSpec> = buildList {
        addAll(manualEntries)
        addAll(GeneratedBackupSettings.entries)
    }.distinctBy { it.preferenceKey }

    val byBackupKey: Map<String, SettingSpec> = entries.associateBy { it.backupKey }
}

private const val SET_SEPARATOR = "\u001F"

private fun encodeStringSet(values: Set<String>): String = values.joinToString(SET_SEPARATOR)

private fun decodeStringSet(value: String): Set<String> =
    value.split(SET_SEPARATOR).filter { it.isNotEmpty() }.toSet()

/**
 * Exports and imports the preferences listed in [SettingsBackupRegistry].
 *
 * Every setting is handled independently so one bad value cannot truncate the rest, and only
 * successfully applied settings are counted.
 */
class SettingsBackup(private val store: PreferenceStore) {

    fun collect(): List<SettingSnapshot> = SettingsBackupRegistry.entries.mapNotNull { spec ->
        try {
            val value = when (spec.type) {
                SettingType.INT -> store.getInt(spec.preferenceKey).takeIf { it.isSet() }?.get()?.toString()
                SettingType.LONG -> store.getLong(spec.preferenceKey).takeIf { it.isSet() }?.get()?.toString()
                SettingType.FLOAT -> store.getFloat(spec.preferenceKey).takeIf { it.isSet() }?.get()?.toString()
                SettingType.BOOLEAN -> store.getBoolean(spec.preferenceKey).takeIf { it.isSet() }?.get()?.toString()
                SettingType.STRING -> store.getString(spec.preferenceKey).takeIf { it.isSet() }?.get()
                SettingType.STRING_SET -> store.getStringSet(spec.preferenceKey).takeIf { it.isSet() }
                    ?.get()?.let(::encodeStringSet)
            }
            value?.let { SettingSnapshot(spec.backupKey, it) }
        } catch (e: Exception) {
            Log.warn(e, "Failed to collect setting ${spec.backupKey}")
            null
        }
    }

    fun restore(settings: List<SettingSnapshot>): Int {
        var restored = 0
        settings.forEach { snapshot ->
            val spec = SettingsBackupRegistry.byBackupKey[snapshot.key] ?: return@forEach
            try {
                if (apply(spec, snapshot.value)) restored++
            } catch (e: Exception) {
                Log.warn(e, "Failed to restore setting ${snapshot.key}")
            }
        }
        return restored
    }

    private fun apply(spec: SettingSpec, value: String): Boolean = when (spec.type) {
        SettingType.INT -> value.toIntOrNull()?.let {
            store.getInt(spec.preferenceKey).set(it)
            true
        } ?: false

        SettingType.LONG -> value.toLongOrNull()?.let {
            store.getLong(spec.preferenceKey).set(it)
            true
        } ?: false

        SettingType.FLOAT -> value.toFloatOrNull()?.let {
            store.getFloat(spec.preferenceKey).set(it)
            true
        } ?: false

        SettingType.BOOLEAN -> value.toBooleanStrictOrNull()?.let {
            store.getBoolean(spec.preferenceKey).set(it)
            true
        } ?: false

        SettingType.STRING -> {
            if (spec.allowedValues != null && value !in spec.allowedValues) {
                false
            } else {
                store.getString(spec.preferenceKey).set(value)
                true
            }
        }

        SettingType.STRING_SET -> {
            store.getStringSet(spec.preferenceKey).set(decodeStringSet(value))
            true
        }
    }
}
