package androidx.preference;
import android.content.Context;
import java.util.Set;
import java.util.HashSet;
public class MultiSelectListPreference extends DialogPreference {
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    public MultiSelectListPreference(Context context) { super(context); }
    public void setEntries(CharSequence[] entries) { this.entries = entries; }
    public void setEntryValues(CharSequence[] entryValues) { this.entryValues = entryValues; }
    public Set<String> getValues() { return new HashSet<>(); }
    public void setValues(Set<String> values) {}
}
