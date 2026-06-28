package android.view.autofill;

public class AutofillValue {
    public static AutofillValue forText(CharSequence textValue) { return new AutofillValue(); }
    public static AutofillValue forToggle(boolean toggleValue) { return new AutofillValue(); }
    public static AutofillValue forDate(long dateValue) { return new AutofillValue(); }
    public static AutofillValue forList(int listValue) { return new AutofillValue(); }
    public CharSequence getTextValue() { return null; }
    public boolean getToggleValue() { return false; }
    public long getDateValue() { return 0; }
    public int getListValue() { return 0; }
    public boolean isText() { return false; }
    public boolean isToggle() { return false; }
    public boolean isDate() { return false; }
    public boolean isList() { return false; }
}
