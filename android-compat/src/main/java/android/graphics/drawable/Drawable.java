package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;

public abstract class Drawable {
    public interface Callback {
        void invalidateDrawable(Drawable who);
        void scheduleDrawable(Drawable who, Runnable what, long when);
        void unscheduleDrawable(Drawable who, Runnable what);
    }

    public abstract void draw(Canvas canvas);
    public void setBounds(int left, int top, int right, int bottom) {}
    public void setBounds(Rect bounds) {}
    public void setAlpha(int alpha) {}
    public void setColorFilter(ColorFilter colorFilter) {}
    public int getIntrinsicWidth() { return -1; }
    public int getIntrinsicHeight() { return -1; }
    public boolean isVisible() { return true; }
    public void setCallback(Callback cb) {}
}
