package android.content;
public class ComponentName implements android.os.Parcelable {
    public ComponentName(String pkg, String cls) {}
    public ComponentName(Context pkg, String cls) {}
    public ComponentName(Context pkg, Class<?> cls) {}
    public String getClassName() { return ""; }
    public String getPackageName() { return ""; }
    public ComponentName flattenToString() { return this; }
    public static ComponentName unflattenFromString(String str) { return new ComponentName("", ""); }
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(android.os.Parcel dest, int flags) {}
}
