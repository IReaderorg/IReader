package android.view.accessibility;

public interface AccessibilityEventSource {
    void sendAccessibilityEvent(int eventType);
    void sendAccessibilityEventUnchecked(AccessibilityEvent event);
}
