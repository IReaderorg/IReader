package android.view.inputmethod;

import android.net.Uri;

public class InputContentInfo {
    public InputContentInfo(Uri contentUri, android.content.ClipDescription description, Uri linkUri) {}
    public InputContentInfo(Uri contentUri, android.content.ClipDescription description, Uri linkUri, Bundle opts) {}
    public Uri getContentUri() { return null; }
    public android.content.ClipDescription getDescription() { return null; }
    public Uri getLinkUri() { return null; }
    public void requestPermission() {}
}
