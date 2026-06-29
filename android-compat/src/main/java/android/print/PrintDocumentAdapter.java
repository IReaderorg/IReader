package android.print;

public abstract class PrintDocumentAdapter {
    public PrintDocumentAdapter(String documentName) {}
    public void start() {}
    public abstract void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, LayoutResultCallback callback, Bundle extras);
    public abstract void onWrite(PageRange[] pages, android.os.ParcelFileDescriptor destination, WriteResultCallback callback, Bundle extras);
    public void onFinish() {}
    public void onStart() {}
    public boolean isCancelled() { return false; }

    public interface LayoutResultCallback {
        void onLayoutFinished(PrintDocumentInfo info, boolean changed);
        void onLayoutFailed(CharSequence error);
    }

    public interface WriteResultCallback {
        void onWriteFinished(PageRange[] pages);
        void onWriteFailed(CharSequence error);
    }

    public static class PrintDocumentInfo {
        public int getPageCount() { return 0; }
        public String getName() { return null; }
    }

    public static class PageRange {
        public static final PageRange[] ALL_PAGES = new PageRange[0];
        public PageRange(int start, int end) {}
        public int getStart() { return 0; }
        public int getEnd() { return 0; }
    }

    public static class PrintAttributes {
        public static class MediaSize {
            public static final MediaSize UNKNOWN = new MediaSize();
        }
        public static class Resolution {
            public int getHorizontalDpi() { return 0; }
            public int getVerticalDpi() { return 0; }
        }
    }
}
