package android.view;

public interface ViewParent {
    ViewParent getParent();
    View findViewById(int id);
    View findViewWithTag(Object tag);
    void requestLayout();
    boolean isLayoutRequested();
    void invalidateChild(View child, Rect r);
    boolean getChildVisibleRect(View child, Rect r, android.graphics.Point offset);
    void requestDisallowInterceptTouchEvent(boolean disallowIntercept);
    boolean onInterceptTouchEvent(MotionEvent ev);
    boolean onTouchEvent(MotionEvent event);
    void bringChildToFront(View child);
    void clearChildFocus(View child);
    void childFocused(View child, Rect previouslyFocusedRect);
    void focusableViewAvailable(View v);
    void recomputeViewAttributes(View child);
}
