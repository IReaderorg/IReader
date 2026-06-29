package android.content;
public class ClipData {
    public static ClipData newPlainText(CharSequence label, CharSequence text) { return new ClipData(); }
    public static ClipData newUri(android.content.ContentResolver resolver, CharSequence label, android.net.Uri uri) { return new ClipData(); }
    public int getItemCount() { return 0; }
}
