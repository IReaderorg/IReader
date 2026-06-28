package android.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class TextLine {
    public static TextLine obtain() { return new TextLine(); }
    public void set(TextPaint paint, CharSequence text, int start, int end) {}
    public void draw(Canvas c, float x, int top, int y, int bottom) {}
    public void getMetrics(Paint.FontMetrics fm) {}
    public void getOffsetToLeftRightOf(int cur, int h, boolean toLeft, boolean clamped) {}
    public int getLineLeft(int line) { return 0; }
    public int getLineRight(int line) { return 0; }
    public void getLineBounds(int line, Rect bounds) {}
    public float getLineMax(int line) { return 0; }
}
