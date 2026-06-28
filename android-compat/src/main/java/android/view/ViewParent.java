package android.view;

public interface ViewParent {
    ViewParent getParent();
    Object findViewById(int id);
    void requestLayout();
    boolean isLayoutRequested();
    void requestDisallowInterceptTouchEvent(boolean disallowIntercept);
}
