package androidx.preference;
import android.content.Context;
public class EditTextPreference extends Preference {
    public EditTextPreference(Context context) { super(context); }
    public CharSequence getText() { return null; }
    public void setText(CharSequence text) {}
}
