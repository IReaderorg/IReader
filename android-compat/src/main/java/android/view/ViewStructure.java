package android.view;

public class ViewStructure {
    public void setClassName(String className) {}
    public void setTypeName(String typeName) {}
    public void setChildCount(int num) {}
    public ViewStructure newChild(int index) { return new ViewStructure(); }
    public int getChildCount() { return 0; }
    public void setId(int id, String packageName, String typeName, String entryName) {}
    public void setId(int id) {}
    public void setDimens(int left, int top, int width, int height) {}
    public void setText(CharSequence text) {}
    public CharSequence getText() { return null; }
    public void setHint(CharSequence hint) {}
    public void setFocused(boolean focused) {}
    public void setScrollable(boolean horizontal, boolean vertical) {}
    public void setContentDescription(CharSequence contentDescription) {}
    public void setHtmlInfo(String tag, String htmlInfo) {}
    public void setWebDescription(CharSequence description) {}
    public void setMinWidth(int minw) {}
    public void setMaxWidth(int maxw) {}
    public void setMinHeight(int minh) {}
    public void setMaxHeight(int maxh) {}
    public void setAccessibilityFocused(boolean focused) {}
    public void setSelected(boolean selected) {}
    public void setActivated(boolean activated) {}
    public void setEnabled(boolean enabled) {}
    public void setClickable(boolean clickable) {}
    public void setLongClickable(boolean longClickable) {}
    public void setContextClickable(boolean contextClickable) {}
    public void setCheckable(boolean isCheckable) {}
    public void setChecked(boolean isChecked) {}
    public void setFocusable(boolean isFocusable) {}
    public void setLongFocusable(boolean isLongFocusable) {}
    public void setAccessibilityData(String packageName, CharSequence cls, CharSequence contentDescription) {}
}
