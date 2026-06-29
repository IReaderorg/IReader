package android.graphics;
public final class Insets {
    public static final Insets NONE = new Insets();
    public final int left, top, right, bottom;
    public Insets() { this(0, 0, 0, 0); }
    public Insets(int left, int top, int right, int bottom) { this.left = left; this.top = top; this.right = right; this.bottom = bottom; }
    public static Insets of(int left, int top, int right, int bottom) { return new Insets(left, top, right, bottom); }
}
