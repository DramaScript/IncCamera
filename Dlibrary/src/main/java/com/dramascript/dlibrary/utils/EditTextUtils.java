package com.dramascript.dlibrary.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

/*
 * Cread By DramaScript on 2019/3/27
 */
public class EditTextUtils {
    public IEditTextChangeListener mChangeListener;

    public void setChangeListener(IEditTextChangeListener changeListener) {
        mChangeListener = changeListener;
    }

    public interface IEditTextChangeListener {
        void buttonChange();
    }

    private EditText[] editTexts;

    public EditTextUtils addAllEditText(EditText... editTexts) {
        this.editTexts = editTexts;
        initEditListener();
        return this;
    }


    private void initEditListener() {
        for (EditText editText : editTexts) {
            editText.addTextChangedListener(new textChange());
        }
    }

    /**
     * edit输入的变化来改变按钮的是否点击
     */
    private class textChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (mChangeListener != null) {
                mChangeListener.buttonChange();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}