package android.content;

import java.io.File;

public class ContextWrapper extends Context {
    private Context mBase;
    public ContextWrapper(Context base) { mBase = base; }
    protected void attachBaseContext(Context base) { mBase = base; }

    @Override public SharedPreferences getSharedPreferences(String name, int mode) {
        if (mBase != null) return mBase.getSharedPreferences(name, mode);
        return new NoOpSharedPreferences();
    }
    @Override public String getPackageName() { return mBase != null ? mBase.getPackageName() : ""; }
    @Override public File getCacheDir() { return mBase != null ? mBase.getCacheDir() : new File(System.getProperty("java.io.tmpdir"), "ireader-cache"); }
    @Override public File getFilesDir() { return mBase != null ? mBase.getFilesDir() : new File(System.getProperty("java.io.tmpdir"), "ireader-files"); }
    @Override public File getNoBackupFilesDir() { return mBase != null ? mBase.getNoBackupFilesDir() : new File(System.getProperty("java.io.tmpdir"), "ireader-nobackup"); }
    @Override public File getExternalCacheDir() { return mBase != null ? mBase.getExternalCacheDir() : new File(System.getProperty("java.io.tmpdir"), "ireader-ext-cache"); }
    @Override public File getExternalFilesDir(String type) { return mBase != null ? mBase.getExternalFilesDir(type) : new File(System.getProperty("java.io.tmpdir"), "ireader-ext-files"); }
    @Override public File getObbDir() { return mBase != null ? mBase.getObbDir() : new File(System.getProperty("java.io.tmpdir"), "ireader-obb"); }
    @Override public File getDataDir() { return mBase != null ? mBase.getDataDir() : new File(System.getProperty("java.io.tmpdir"), "ireader-data"); }
    @Override public File getDatabasePath(String name) { return mBase != null ? mBase.getDatabasePath(name) : new File(getCacheDir(), name); }
    @Override public File getFileStreamPath(String name) { return mBase != null ? mBase.getFileStreamPath(name) : new File(getFilesDir(), name); }
    @Override public File getDir(String name, int mode) { return mBase != null ? mBase.getDir(name, mode) : new File(getFilesDir(), name); }
    @Override public ClassLoader getClassLoader() { return mBase != null ? mBase.getClassLoader() : getClass().getClassLoader(); }
    @Override public Context getApplicationContext() { return mBase != null ? mBase.getApplicationContext() : this; }
    @Override public Object getSystemService(String name) { return mBase != null ? mBase.getSystemService(name) : null; }
}
