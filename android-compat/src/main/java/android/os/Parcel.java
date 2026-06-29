package android.os;

public class Parcel {
    public static Parcel obtain() { return new Parcel(); }
    public void recycle() {}
}
