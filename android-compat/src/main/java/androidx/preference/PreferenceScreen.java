package androidx.preference;
import android.content.Context;
import java.util.LinkedList;
import java.util.List;
public class PreferenceScreen extends Preference {
    private List<Preference> preferences = new LinkedList<>();
    public PreferenceScreen(Context context) { super(context); }
    public boolean addPreference(Preference preference) { preference.setSharedPreferences(getSharedPreferences()); preferences.add(preference); return true; }
    public List<Preference> getPreferences() { return preferences; }
}
