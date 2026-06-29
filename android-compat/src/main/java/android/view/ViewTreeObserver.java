package android.view;

public class ViewTreeObserver {
    public interface OnGlobalFocusChangeListener {
        void onGlobalFocusChanged(View oldFocus, View newFocus);
    }
    public interface OnGlobalLayoutListener {
        void onGlobalLayout();
    }
    public interface OnPreDrawListener {
        boolean onPreDraw();
    }
    public interface OnScrollChangedListener {
        void onScrollChanged();
    }
    public interface OnTouchModeChangeListener {
        void onTouchModeChanged(boolean isInTouchMode);
    }

    public void addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener listener) {}
    public void removeOnGlobalFocusChangeListener(OnGlobalFocusChangeListener listener) {}
    public void addOnGlobalLayoutListener(OnGlobalLayoutListener listener) {}
    public void removeOnGlobalLayoutListener(OnGlobalLayoutListener listener) {}
    public void addOnPreDrawListener(OnPreDrawListener listener) {}
    public void removeOnPreDrawListener(OnPreDrawListener listener) {}
    public void addOnScrollChangedListener(OnScrollChangedListener listener) {}
    public void removeOnScrollChangedListener(OnScrollChangedListener listener) {}
    public void addOnTouchModeChangeListener(OnTouchModeChangeListener listener) {}
    public void removeOnTouchModeChangeListener(OnTouchModeChangeListener listener) {}
    public boolean isAlive() { return true; }
}
