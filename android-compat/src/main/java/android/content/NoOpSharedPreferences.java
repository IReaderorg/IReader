package android.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple file-backed SharedPreferences implementation for desktop.
 * Used as fallback when no Context is available (e.g., Application() created with null base).
 */
public class NoOpSharedPreferences implements SharedPreferences {
    private final Map<String, Object> store = new HashMap<>();

    @Override public String getString(String key, String defValue) { return store.containsKey(key) ? (String) store.get(key) : defValue; }
    @Override public int getInt(String key, int defValue) { return store.containsKey(key) ? (int) store.get(key) : defValue; }
    @Override public long getLong(String key, long defValue) { return store.containsKey(key) ? (long) store.get(key) : defValue; }
    @Override public float getFloat(String key, float defValue) { return store.containsKey(key) ? (float) store.get(key) : defValue; }
    @Override public boolean getBoolean(String key, boolean defValue) { return store.containsKey(key) ? (boolean) store.get(key) : defValue; }
    @Override public boolean contains(String key) { return store.containsKey(key); }
    @Override public Set<String> getStringSet(String key, Set<String> defValues) { return store.containsKey(key) ? (Set<String>) store.get(key) : defValues; }
    @Override public Map<String, ?> getAll() { return new HashMap<>(store); }
    @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
    @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
    @Override public Editor edit() { return new NoOpEditor(); }

    private class NoOpEditor implements Editor {
        private final Map<String, Object> pending = new HashMap<>();
        private final Set<String> removals = new HashSet<>();

        @Override public Editor putString(String key, String value) { pending.put(key, value); return this; }
        @Override public Editor putInt(String key, int value) { pending.put(key, value); return this; }
        @Override public Editor putLong(String key, long value) { pending.put(key, value); return this; }
        @Override public Editor putFloat(String key, float value) { pending.put(key, value); return this; }
        @Override public Editor putBoolean(String key, boolean value) { pending.put(key, value); return this; }
        @Override public Editor putStringSet(String key, Set<String> values) { pending.put(key, values); return this; }
        @Override public Editor remove(String key) { removals.add(key); return this; }
        @Override public Editor clear() { store.clear(); return this; }
        @Override public boolean commit() { apply(); return true; }
        @Override public void apply() {
            store.putAll(pending);
            removals.forEach(store::remove);
            pending.clear();
            removals.clear();
        }
    }
}
