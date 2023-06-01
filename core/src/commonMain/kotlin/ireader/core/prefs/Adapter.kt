package ireader.core.prefs

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

interface Adapter<T> {
    fun get(key: String, preferences: ObservableSettings): T

    fun set(key: String, value: T, editor: ObservableSettings)

    fun isSet(keys: Set<String>, key: String): Boolean = key in keys

    fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener
}

internal object StringAdapter : Adapter<String> {
    override fun get(key: String, preferences: ObservableSettings): String {
        return preferences.getString(key, "") // Not called unless key is present.
    }

    override fun set(key: String, value: String, editor: ObservableSettings) {
        editor.putString(key, value)
    }

    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        return preferences.addStringOrNullListener(key) { callback() }
    }
}

internal object LongAdapter : Adapter<Long> {
    override fun get(key: String, preferences: ObservableSettings): Long {
        return preferences.getLong(key, 0)
    }

    override fun set(key: String, value: Long, editor: ObservableSettings) {
        editor.putLong(key, value)
    }

    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        return preferences.addLongOrNullListener(key) { callback() }
    }
}

internal object IntAdapter : Adapter<Int> {
    override fun get(key: String, preferences: ObservableSettings): Int {
        return preferences.getInt(key, 0)
    }

    override fun set(key: String, value: Int, editor: ObservableSettings) {
        editor.putInt(key, value)
    }

    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        return preferences.addIntOrNullListener(key) { callback() }
    }
}

internal object FloatAdapter : Adapter<Float> {
    override fun get(key: String, preferences: ObservableSettings): Float {
        return preferences.getFloat(key, 0f)
    }

    override fun set(key: String, value: Float, editor: ObservableSettings) {
        editor.putFloat(key, value)
    }

    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        return preferences.addFloatOrNullListener(key) { callback() }
    }
}

internal object BooleanAdapter : Adapter<Boolean> {
    override fun get(key: String, preferences: ObservableSettings): Boolean {
        return preferences.getBoolean(key, false)
    }

    override fun set(key: String, value: Boolean, editor: ObservableSettings) {
        editor.putBoolean(key, value)
    }

    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        return preferences.addBooleanOrNullListener(key) { callback() }
    }
}

internal object StringSetAdapter : Adapter<Set<String>> {
    private val serializer = SetSerializer(String.serializer())

    override fun get(key: String, preferences: ObservableSettings): Set<String> {
        return preferences.decodeValue(
            serializer,
            key,
            emptySet()
        ) // Not called unless key is present.
    }

    override fun set(key: String, value: Set<String>, editor: ObservableSettings) {
        editor.encodeValue(serializer, key, value)
    }

    /**
     *  Encoding a string set makes a list of keys and a size key, such as key.size and key.0-size
     */
    override fun isSet(keys: Set<String>, key: String): Boolean {
        return keys.contains("$key.size")
    }

    /**
     * Watching the regular key doesn't produce updates for a string set for some reason
     * TODO make better, doesn't produce updates when you add something and remove something
     */
    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        return preferences.addIntOrNullListener("$key.size") { callback() }
    }
}

internal class ObjectAdapter<T>(
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
) : Adapter<T> {

    override fun get(key: String, preferences: ObservableSettings): T {
        return deserializer(preferences.getString(key, "")) // Not called unless key is present.
    }

    override fun set(key: String, value: T, editor: ObservableSettings) {
        editor.putString(key, serializer(value))
    }

    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        return preferences.addStringOrNullListener(key) { callback() }
    }
}

internal class JsonObjectAdapter<T>(
    private val defaultValue: T,
    private val serializer: KSerializer<T>,
    private val serializersModule: SerializersModule = EmptySerializersModule()
) : Adapter<T> {

    override fun get(key: String, preferences: ObservableSettings): T {
        return preferences.decodeValue(
            serializer,
            key,
            defaultValue,
            serializersModule
        ) // Not called unless key is present.
    }

    override fun set(key: String, value: T, editor: ObservableSettings) {
        editor.encodeValue(serializer, key, value, serializersModule)
    }

    /**
     *  Encoding a structure makes keys start with the [key] and adds extensions for values,
     *  for a pair it would be like [key].first [key].second.
     */
    override fun isSet(keys: Set<String>, key: String): Boolean {
        return keys.any { it.startsWith(key) }
    }

    /**
     * Todo doesn't work
     */
    override fun addListener(
        key: String,
        preferences: ObservableSettings,
        callback: () -> Unit
    ): SettingsListener {
        @Suppress("DEPRECATION") // Because we don't care about the type, and it crashes with any other listener
        return preferences.addStringOrNullListener(key) { callback() }
    }
}
