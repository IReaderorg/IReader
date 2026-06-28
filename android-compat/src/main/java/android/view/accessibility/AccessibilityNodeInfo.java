package android.view.accessibility;

import android.os.Bundle;

public class AccessibilityNodeInfo {
    public static final int ACTION_FOCUS = 1;
    public static final int ACTION_CLEAR_FOCUS = 2;
    public static final int ACTION_SELECT = 4;
    public static final int ACTION_CLEAR_SELECTION = 8;
    public static final int ACTION_CLICK = 16;
    public static final int ACTION_LONG_CLICK = 32;
    public static final int ACTION_ACCESSIBILITY_FOCUS = 128;
    public static final int ACTION_CLEAR_ACCESSIBILITY_FOCUS = 256;
    public static final int ACTION_NEXT_AT_MOVEMENT_GRANULARITY = 512;
    public static final int ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = 1024;
    public static final int ACTION_NEXT_HTML_ELEMENT = 2048;
    public static final int ACTION_PREVIOUS_HTML_ELEMENT = 4096;
    public static final int ACTION_SCROLL_FORWARD = 8192;
    public static final int ACTION_SCROLL_BACKWARD = 16384;
    public static final int ACTION_COPY = 16384;
    public static final int ACTION_PASTE = 32768;

    public static AccessibilityNodeInfo obtain() { return new AccessibilityNodeInfo(); }
    public static AccessibilityNodeInfo obtain(AccessibilityNodeInfo source) { return new AccessibilityNodeInfo(); }
    public void setSource(java.lang.Object source) {}
    public void setSource(java.lang.Object source, int virtualDescendantId) {}
    public void setClassName(CharSequence className) {}
    public CharSequence getClassName() { return null; }
    public void setContentDescription(CharSequence contentDescription) {}
    public CharSequence getContentDescription() { return null; }
    public void setPackageName(CharSequence packageName) {}
    public CharSequence getPackageName() { return null; }
    public void setText(CharSequence text) {}
    public CharSequence getText() { return null; }
    public void setEnabled(boolean enabled) {}
    public boolean isEnabled() { return true; }
    public void setFocusable(boolean focusable) {}
    public boolean isFocusable() { return false; }
    public void setFocused(boolean focused) {}
    public boolean isFocused() { return false; }
    public void setSelected(boolean selected) {}
    public boolean isSelected() { return false; }
    public void setClickable(boolean clickable) {}
    public boolean isClickable() { return false; }
    public void setLongClickable(boolean longClickable) {}
    public boolean isLongClickable() { return false; }
    public void setScrollable(boolean scrollable) {}
    public boolean isScrollable() { return false; }
    public void setCheckable(boolean checkable) {}
    public boolean isCheckable() { return false; }
    public void setChecked(boolean checked) {}
    public boolean isChecked() { return false; }
    public boolean performAction(int action) { return false; }
    public boolean performAction(int action, Bundle arguments) { return false; }
    public int getWindowId() { return 0; }
    public void setWindowId(int windowId) {}
    public int getActions() { return 0; }
    public void addAction(int action) {}
    public void addChild(java.lang.Object source) {}
    public void addChild(java.lang.Object source, int virtualDescendantId) {}
    public void recycle() {}
    public void setViewIdResourceName(String viewIdResourceName) {}
    public String getViewIdResourceName() { return null; }
}
