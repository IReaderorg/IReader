//package ireader.js.runtime
//
//import ireader.core.http.HttpClients
//import ireader.core.http.HttpClientsInterface
//import ireader.core.prefs.PreferenceStore
//import ireader.core.source.Dependencies
//
///**
// * JavaScript implementation of Dependencies for iOS runtime.
// *
// * This provides the necessary dependencies for sources to run in JavaScriptCore.
// */
//class JsDependencies {
//
//    val httpClients: HttpClientsInterface by lazy {
//        HttpClients()
//    }
//
//    val preferences: PreferenceStore by lazy {
//        JsPreferenceStore()
//    }
//
//    /**
//     * Convert to Dependencies for source construction.
//     */
//    fun toDependencies(): Dependencies {
//        return Dependencies(httpClients, preferences)
//    }
//}
//
//
//
///**
// * JavaScript preference store implementation using localStorage.
// */
//class JsPreferenceStore : PreferenceStore {
//
//    private val storage = mutableMapOf<String, String>()
//
//    override fun getString(key: String, defaultValue: String): String {
//        return try {
//            js("localStorage.getItem(key)") as? String ?: storage[key] ?: defaultValue
//        } catch (e: Exception) {
//            storage[key] ?: defaultValue
//        }
//    }
//
//    override fun putString(key: String, value: String) {
//        try {
//            js("localStorage.setItem(key, value)")
//        } catch (e: Exception) {
//            storage[key] = value
//        }
//    }
//
//    override fun getInt(key: String, defaultValue: Int): Int {
//        return getString(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
//    }
//
//    override fun putInt(key: String, value: Int) {
//        putString(key, value.toString())
//    }
//
//    override fun getLong(key: String, defaultValue: Long): Long {
//        return getString(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
//    }
//
//    override fun putLong(key: String, value: Long) {
//        putString(key, value.toString())
//    }
//
//    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
//        return getString(key, defaultValue.toString()).toBooleanStrictOrNull() ?: defaultValue
//    }
//
//    override fun putBoolean(key: String, value: Boolean) {
//        putString(key, value.toString())
//    }
//
//    override fun getFloat(key: String, defaultValue: Float): Float {
//        return getString(key, defaultValue.toString()).toFloatOrNull() ?: defaultValue
//    }
//
//    override fun putFloat(key: String, value: Float) {
//        putString(key, value.toString())
//    }
//
//    override fun remove(key: String) {
//        try {
//            js("localStorage.removeItem(key)")
//        } catch (e: Exception) {
//            storage.remove(key)
//        }
//    }
//
//    override fun clear() {
//        try {
//            js("localStorage.clear()")
//        } catch (e: Exception) {
//            storage.clear()
//        }
//    }
//
//    override fun contains(key: String): Boolean {
//        return try {
//            js("localStorage.getItem(key)") != null
//        } catch (e: Exception) {
//            storage.containsKey(key)
//        }
//    }
//}
