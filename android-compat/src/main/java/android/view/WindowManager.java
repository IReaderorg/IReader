package android.view;
public interface WindowManager extends ViewManager {
    class LayoutParams {
        public static final int FLAG_NOT_FOCUSABLE = 8;
        public static final int FLAG_NOT_TOUCHABLE = 16;
        public int type;
        public int flags;
        public int format;
        public int width;
        public int height;
    }
}
