package org.ireader.app

import org.ireader.core_api.prefs.Preference
import org.ireader.core_api.prefs.PreferenceStore

class FakePreferencesStore:  PreferenceStore  {
    override fun getString(key: String, defaultValue: String): Preference<String> {
        throw Exception()
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        throw Exception()
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        throw Exception()
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        throw Exception()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        throw Exception()
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        throw Exception()
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T> {
        throw Exception()
    }
}