package com.gargsoft.gargnote.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class EditTextEx extends androidx.appcompat.widget.AppCompatEditText {
    private boolean noEnterAction = false;

    public boolean isNoEnterAction() {
        return noEnterAction;
    }

    public void setNoEnterAction(boolean noEnterAction) {
        this.noEnterAction = noEnterAction;
    }

    public EditTextEx(Context context)
    {
        super(context);
    }

    public EditTextEx(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public EditTextEx(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        InputConnection conn = super.onCreateInputConnection(outAttrs);

        if (isNoEnterAction())
            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;

        return conn;
    }
}
