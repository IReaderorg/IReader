package android.content;
import android.net.Uri;
import android.os.Bundle;
public class Intent implements android.os.Parcelable {
    public static final String ACTION_MAIN = "android.intent.action.MAIN";
    public static final String ACTION_VIEW = "android.intent.action.VIEW";
    public static final String ACTION_SEND = "android.intent.action.SEND";
    public static final String CATEGORY_DEFAULT = "android.intent.category.DEFAULT";
    public static final String EXTRA_TEXT = "android.intent.extra.TEXT";
    public static final String EXTRA_STREAM = "android.intent.extra.STREAM";
    public static final String EXTRA_SUBJECT = "android.intent.extra.SUBJECT";
    public Intent() {}
    public Intent(String action) {}
    public Intent(Context context, Class<?> cls) {}
    public Intent(String action, Uri uri) {}
    public Intent setAction(String action) { return this; }
    public String getAction() { return null; }
    public Intent setData(Uri data) { return this; }
    public Uri getData() { return null; }
    public Intent setType(String type) { return this; }
    public String getType() { return null; }
    public Intent setDataAndType(Uri data, String type) { return this; }
    public Intent setClassName(String packageName, String className) { return this; }
    public Intent setClassName(Context context, String className) { return this; }
    public Intent setComponent(ComponentName component) { return this; }
    public Intent putExtra(String name, String value) { return this; }
    public Intent putExtra(String name, int value) { return this; }
    public Intent putExtra(String name, long value) { return this; }
    public Intent putExtra(String name, boolean value) { return this; }
    public Intent putExtra(String name, Bundle value) { return this; }
    public Intent putExtra(String name, java.io.Serializable value) { return this; }
    public Intent putExtra(String name, android.os.Parcelable value) { return this; }
    public String getStringExtra(String name) { return null; }
    public int getIntExtra(String name, int defaultValue) { return defaultValue; }
    public long getLongExtra(String name, long defaultValue) { return defaultValue; }
    public boolean getBooleanExtra(String name, boolean defaultValue) { return defaultValue; }
    public Bundle getExtras() { return null; }
    public Uri getDataString() { return null; }
    public Intent setPackage(String packageName) { return this; }
    public String getPackage() { return null; }
    public Intent setFlags(int flags) { return this; }
    public int getFlags() { return 0; }
    public Intent addFlags(int flags) { return this; }
    public Intent addCategory(String category) { return this; }
    public Intent setClipData(ClipData data) { return this; }
    public ClipData getClipData() { return null; }
    public Intent setSelector(Intent intent) { return this; }
    public Intent setSourceBounds(android.graphics.Rect sourceBounds) { return this; }
    public ComponentName getComponent() { return null; }
    public IntentSender getSender() { return null; }
    public Intent setIdentifier(String identifier) { return this; }
    public String getIdentifier() { return null; }
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(android.os.Parcel dest, int flags) {}
    public static final Creator<Intent> CREATOR = new Creator<Intent>() {
        @Override public Intent createFromParcel(android.os.Parcel in) { return new Intent(); }
        @Override public Intent[] newArray(int size) { return new Intent[size]; }
    };
}
