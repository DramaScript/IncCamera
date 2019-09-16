package com.dramascript.inccamera.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/*
 * Cread By DramaScript on 2019/9/12
 */
@SuppressLint("AppCompatCustomView")
public class RecordButton extends TextView {


//    private OnRecordListener mListener;

    public RecordButton(Context context) {
        super(context);
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (mListener == null) {
//            return false;
//        }
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                setPressed(true);
//                mListener.onRecordStart();
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                setPressed(false);
//                mListener.onRecordStop();
//                break;
//        }
//        return true;
//    }
//
//
//    public void setOnRecordListener(OnRecordListener listener) {
//        mListener = listener;
//    }
//
//    public interface OnRecordListener {
//        void onRecordStart();
//
//        void onRecordStop();
//    }
}
