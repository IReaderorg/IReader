package androidx.preference;
import android.content.Context;
import android.content.SharedPreferences;
public class Preference {
    protected Context context;
    private boolean isVisible = true;
    private boolean isEnabled = true;
    private String key;
    private CharSequence title;
    private CharSequence summary;
    private Object defaultValue;
    private SharedPreferences sharedPreferences;
    public OnPreferenceChangeListener onChangeListener;
    public Preference(Context context) { this.context = context; }
    public Context getContext() { return context; }
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener l) { this.onChangeListener = l; }
    public void setOnPreferenceClickListener(OnPreferenceClickListener l) {}
    public CharSequence getTitle() { return title; }
    public void setTitle(CharSequence title) { this.title = title; }
    public CharSequence getSummary() { return summary; }
    public void setSummary(CharSequence summary) { this.summary = summary; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public boolean isEnabled() { return isEnabled; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
    public Object getDefaultValue() { return defaultValue; }
    public void setVisible(boolean visible) { isVisible = visible; }
    public boolean getVisible() { return isVisible; }
    public void setSharedPreferences(SharedPreferences sp) { this.sharedPreferences = sp; }
    public SharedPreferences getSharedPreferences() { return sharedPreferences; }
    public interface OnPreferenceChangeListener { boolean onPreferenceChange(Preference p, Object v); }
    public interface OnPreferenceClickListener { boolean onPreferenceClick(Preference p); }
}
