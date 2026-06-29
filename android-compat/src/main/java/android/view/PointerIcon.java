package android.view;

import android.content.Context;

public class PointerIcon {
    public static final int TYPE_DEFAULT = 1000;
    public static final int TYPE_ARROW = 1000;
    public static final int TYPE_HAND = 1004;
    public static final int TYPE_TEXT = 1008;
    public static final int TYPE_CROSSHAIR = 1001;
    public static final int TYPE_WAIT = 1016;

    public static PointerIcon getSystemIcon(Context context, int type) {
        return new PointerIcon();
    }

    public static PointerIcon load(Context context, int resId) {
        return new PointerIcon();
    }

    public int getType() { return TYPE_DEFAULT; }
}
