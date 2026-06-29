package android.content.res;

import android.graphics.drawable.Drawable;

public class Resources {
    public static class Theme {
        public void applyStyle(int resId, boolean force) {}
        public void setTo(Theme other) {}
    }
    public DisplayMetrics getDisplayMetrics() { return new DisplayMetrics(); }
    public Configuration getConfiguration() { return new Configuration(); }
    public int getIdentifier(String name, String defType, String defPackage) { return 0; }
    public String getResourceName(int resId) { return ""; }
    public Drawable getDrawable(int id) { return null; }
}
