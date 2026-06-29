package android.content;

import java.io.File;

public abstract class Context {
    public static final int MODE_PRIVATE = 0;
    public abstract SharedPreferences getSharedPreferences(String name, int mode);
    public abstract String getPackageName();
    public File getCacheDir() { throw new RuntimeException("Stub!"); }
    public File getFilesDir() { throw new RuntimeException("Stub!"); }
    public File getNoBackupFilesDir() { throw new RuntimeException("Stub!"); }
    public File getExternalCacheDir() { throw new RuntimeException("Stub!"); }
    public File getExternalFilesDir(String type) { throw new RuntimeException("Stub!"); }
    public File[] getExternalCacheDirs() { throw new RuntimeException("Stub!"); }
    public File[] getExternalFilesDirs(String type) { throw new RuntimeException("Stub!"); }
    public File getDir(String name, int mode) { throw new RuntimeException("Stub!"); }
    public File getDataDir() { throw new RuntimeException("Stub!"); }
    public File getDatabasePath(String name) { throw new RuntimeException("Stub!"); }
    public File getObbDir() { throw new RuntimeException("Stub!"); }
    public File[] getObbDirs() { throw new RuntimeException("Stub!"); }
    public boolean deleteFile(String name) { throw new RuntimeException("Stub!"); }
    public String[] fileList() { throw new RuntimeException("Stub!"); }
    public File getFileStreamPath(String name) { throw new RuntimeException("Stub!"); }
    public java.io.FileInputStream openFileInput(String name) throws java.io.FileNotFoundException { throw new RuntimeException("Stub!"); }
    public java.io.FileOutputStream openFileOutput(String name, int mode) throws java.io.FileNotFoundException { throw new RuntimeException("Stub!"); }
    public Object getSystemService(String name) { throw new RuntimeException("Stub!"); }
    public Context getApplicationContext() { return this; }
    public Object getApplicationInfo() { throw new RuntimeException("Stub!"); }
    public void setTheme(int resid) {}
    public int getThemeResId() { return 0; }
    public Object getTheme() { throw new RuntimeException("Stub!"); }
    public Object getResources() { throw new RuntimeException("Stub!"); }
    public Object getAssets() { throw new RuntimeException("Stub!"); }
    public Object getContentResolver() { throw new RuntimeException("Stub!"); }
    public ClassLoader getClassLoader() { return getClass().getClassLoader(); }
}
