package android.view.inputmethod;

public class EditorInfo {
    public static final int IME_ACTION_UNSPECIFIED = 0;
    public static final int IME_ACTION_NONE = 1;
    public static final int IME_ACTION_GO = 2;
    public static final int IME_ACTION_SEARCH = 3;
    public static final int IME_ACTION_SEND = 4;
    public static final int IME_ACTION_NEXT = 5;
    public static final int IME_ACTION_DONE = 6;

    public int inputType = 0;
    public int imeOptions = 0;
    public String packageName = "";
    public CharSequence label = "";
    public int fieldId = 0;
    public String fieldName = "";
    public CharSequence hintText = "";
}
