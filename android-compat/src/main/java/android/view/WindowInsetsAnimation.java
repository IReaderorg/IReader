package android.view;
public class WindowInsetsAnimation {
    public static class Bounds {
        public static Bounds of(android.graphics.Rect insets) { return new Bounds(); }
        public android.graphics.Rect getUpperBound() { return new android.graphics.Rect(); }
        public android.graphics.Rect getLowerBound() { return new android.graphics.Rect(); }
    }
}
