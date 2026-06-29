package android.view.inputmethod;

import android.view.InputEvent;

public interface InputConnection {
    CharSequence getTextBeforeCursor(int n, int flags);
    CharSequence getTextAfterCursor(int n, int flags);
    CharSequence getSelectedText(int flags);
    int getCursorCapsMode(int reqModes);
    int getExtractedText(ExtractedText text, int flags);
    boolean deleteSurroundingText(int beforeLength, int afterLength);
    boolean setComposingText(CharSequence text, int newCursorPosition);
    boolean setComposingRegion(int start, int end);
    boolean finishComposingText();
    boolean commitText(CharSequence text, int newCursorPosition);
    boolean commitCorrection(CorrectionInfo correctionInfo);
    boolean performEditorAction(int actionCode);
    boolean performContextMenuAction(int id);
    boolean beginBatchEdit();
    boolean endBatchEdit();
    boolean sendKeyEvent(InputEvent event);
    boolean clearMetaKeyStates(int states);
    boolean reportFullscreenMode(boolean enabled);
    boolean performAutofillAction(int action);
    boolean commitContent(InputContentInfo inputContentInfo, int flags, android.os.Bundle opts);

    class ExtractedText {
        public CharSequence text;
        public int startOffset;
        public int selectionStart;
        public int selectionEnd;
    }

    class CorrectionInfo {
        public CorrectionInfo(int offset, CharSequence originalText, CharSequence correctedText) {}
    }
}
