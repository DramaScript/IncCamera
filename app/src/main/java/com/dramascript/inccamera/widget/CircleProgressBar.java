package com.dramascript.inccamera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.dramascript.inccamera.R;


/**
 * @CreadBy ：DramaScript
 * @date 2017/5/31
 */
public class CircleProgressBar extends View {
    private Context context;
    private Paint paint;
    private Paint bgPaint;
    private float mStrokeWidth;

    private int contentWidth = 100;
    private int contentHeight = 100;
    private int defaultBgColor = 0xffefefef;
    private int sweepAngle;
    private int animAngle;
    private ProgressBarAnimation anim;


    private int
            startColor = Color.parseColor("#ffffff"),
            endColor = Color.parseColor("#ffffff"),
            intermediateOne = Color.parseColor("#ffffff"),
            intermediateTwo = Color.parseColor("#ffffff"),
            intermediateThree = Color.parseColor("#ffffff");


    public CircleProgressBar(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
        mStrokeWidth = typedArray.getDimension(R.styleable.CircleProgressBar_circleStrokeWidth, 5);
        defaultBgColor = typedArray.getColor(R.styleable.CircleProgressBar_bgProgressBarColor, 0xffefefef);
        typedArray.recycle();
        anim = new ProgressBarAnimation();
    }


    private void initPaint() {
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        int[] mColors = new int[]{intermediateOne, intermediateTwo, intermediateThree, endColor, startColor, intermediateOne};
        float[] positions = new float[]{0f, 0.25f, 0.5f, 0.75f, 0.75f, 1f};
        Shader s = new SweepGradient(contentWidth / 2, contentHeight / 2, mColors, positions);
        paint.setShader(s);

        bgPaint = new Paint();
        bgPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStrokeWidth(mStrokeWidth);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setColor(defaultBgColor);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        contentWidth = getWidth();
        contentHeight = getHeight();
        initPaint();
        /**
         * left - The X coordinate of the left side of the rectagle
         * top - The Y coordinate of the top of the rectangle
         * right - The X coordinate of the right side of the rectagle
         * bottom - The Y coordinate of the bottom of the rectangle
         */
        RectF oval = new RectF(0 + mStrokeWidth / 2, 0 + mStrokeWidth / 2, 0 + contentWidth - mStrokeWidth / 2, 0 + contentHeight - mStrokeWidth / 2);
        canvas.drawArc(oval, 0, 360, false, bgPaint);
        canvas.drawArc(oval, -90, sweepAngle, false, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMeasuredWidth() / 2, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, getMeasuredHeight() / 2);
        }
    }

   /**
    * @CreadBy ：DramaScript
    * @date 2017/5/31
    */
    public class ProgressBarAnimation extends Animation {
        public ProgressBarAnimation() {

        }

        @Override
        protected void applyTransformation(float interpolatedTime,
                                           Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            sweepAngle = (int) (interpolatedTime * animAngle);
            postInvalidate();
        }
    }

    public void setProgress(float progress, int time) {
        animAngle = (int) (progress * 360f);
        anim.setDuration(time);
        this.startAnimation(anim);
    }

    public void stop(){
        anim.cancel();
    }
}
