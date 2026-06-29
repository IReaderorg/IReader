package android.net;

public final class Uri implements android.os.Parcelable {
    public static Uri parse(String uriString) { return new Uri(); }
    public String toString() { return ""; }
    public String getScheme() { return null; }
    public String getPath() { return null; }
    public String getHost() { return null; }
    public String getLastPathSegment() { return null; }
    public int describeContents() { return 0; }
    public void writeToParcel(android.os.Parcel dest, int flags) {}
    public static final Creator<Uri> CREATOR = null;
}
