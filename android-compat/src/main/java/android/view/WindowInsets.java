package android.view;

public class WindowInsets {
    public int getSystemWindowInsetTop() { return 0; }
    public int getSystemWindowInsetBottom() { return 0; }
    public int getSystemWindowInsetLeft() { return 0; }
    public int getSystemWindowInsetRight() { return 0; }
    public boolean hasSystemWindowInsets() { return false; }
    public boolean isRound() { return false; }
    public WindowInsets replaceSystemWindowInsets(int left, int top, int right, int bottom) { return this; }
    public WindowInsets consumeSystemWindowInsets() { return this; }
}
