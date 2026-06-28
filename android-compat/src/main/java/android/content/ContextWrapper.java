package android.content;

public class ContextWrapper extends Context {
    private Context mBase;
    public ContextWrapper(Context base) { mBase = base; }
    protected void attachBaseContext(Context base) { mBase = base; }
    @Override public SharedPreferences getSharedPreferences(String name, int mode) {
        if (mBase != null) return mBase.getSharedPreferences(name, mode);
        return new NoOpSharedPreferences();
    }
    @Override public String getPackageName() { return mBase != null ? mBase.getPackageName() : ""; }
}
