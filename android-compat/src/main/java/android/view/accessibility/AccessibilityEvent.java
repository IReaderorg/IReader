package android.view.accessibility;

public class AccessibilityEvent {
    public static final int TYPE_VIEW_CLICKED = 1;
    public static final int TYPE_VIEW_LONG_CLICKED = 2;
    public static final int TYPE_VIEW_FOCUSED = 8;
    public static final int TYPE_VIEW_SELECTED = 4;
    public static final int TYPE_VIEW_TEXT_CHANGED = 16;
    public static final int TYPE_WINDOW_STATE_CHANGED = 32;
    public static final int TYPE_VIEW_SCROLLED = 4096;

    public static AccessibilityEvent obtain() { return new AccessibilityEvent(); }
    public static AccessibilityEvent obtain(AccessibilityEvent event) { return new AccessibilityEvent(); }
    public int getEventType() { return 0; }
    public void setEventType(int eventType) {}
    public CharSequence getText() { return null; }
    public void getText(java.util.List<CharSequence> text) {}
    public void setClassName(CharSequence className) {}
    public CharSequence getClassName() { return null; }
    public void setContentDescription(CharSequence contentDescription) {}
    public CharSequence getContentDescription() { return null; }
    public void setEnabled(boolean enabled) {}
    public boolean isEnabled() { return true; }
    public void setPackageName(CharSequence packageName) {}
    public CharSequence getPackageName() { return null; }
    public void setSource(java.lang.Object source) {}
    public void setFromIndex(int fromIndex) {}
    public void setItemCount(int itemCount) {}
    public int getFromIndex() { return 0; }
    public int getItemCount() { return 0; }
    public void recycle() {}
}
