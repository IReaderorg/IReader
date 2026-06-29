package android.view;

public class DragEvent {
    public static final int ACTION_DRAG_STARTED = 1;
    public static final int ACTION_DRAG_ENTERED = 2;
    public static final int ACTION_DRAG_LOCATION = 3;
    public static final int ACTION_DRAG_EXITED = 4;
    public static final int ACTION_DROP = 5;
    public static final int ACTION_DRAG_ENDED = 6;

    public int getAction() { return 0; }
    public float getX() { return 0; }
    public float getY() { return 0; }
    public int getFlags() { return 0; }
}
