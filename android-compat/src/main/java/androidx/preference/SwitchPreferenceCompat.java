package androidx.preference;
import android.content.Context;
public class SwitchPreferenceCompat extends Preference {
    public SwitchPreferenceCompat(Context context) { super(context); }
    public boolean isChecked() { return false; }
    public void setChecked(boolean checked) {}
}
