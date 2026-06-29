package androidx.preference;
import android.content.Context;
public class CheckBoxPreference extends Preference {
    public CheckBoxPreference(Context context) { super(context); }
    public boolean isChecked() { return false; }
    public void setChecked(boolean checked) {}
}
