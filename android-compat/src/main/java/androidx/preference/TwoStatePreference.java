package androidx.preference;
import android.content.Context;
public class TwoStatePreference extends Preference {
    public TwoStatePreference(Context context) { super(context); }
    public boolean isChecked() { return false; }
    public void setChecked(boolean checked) {}
}
