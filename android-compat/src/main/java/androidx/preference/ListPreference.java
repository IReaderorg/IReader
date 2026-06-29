package androidx.preference;
import android.content.Context;
public class ListPreference extends Preference {
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    public ListPreference(Context context) { super(context); }
    public void setEntries(CharSequence[] entries) { this.entries = entries; }
    public void setEntryValues(CharSequence[] entryValues) { this.entryValues = entryValues; }
    public CharSequence getValue() { return null; }
    public void setValue(String value) {}
}
