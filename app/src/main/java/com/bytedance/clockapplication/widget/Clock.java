package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;
import java.util.logging.LogRecord;


public class Clock extends View {

    private static boolean useThread = false;

    class MyThread extends Thread{
        @Override
        public void run() {
            super.run();
            Log.d(TAG, "thread:run: begin");
            while(!isInterrupted()){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "thread run: sleep interrupted");
                    this.interrupt();
                }
                Log.d(TAG, "thread run: once");
                postInvalidate();
            }
            Log.d(TAG, "thread run: end");
        }
    }

    private Thread thread = new MyThread();

    private int TIME_MSG=1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==TIME_MSG){
                Log.d(TAG, "handleMessage: TIME_MSG");
                invalidate();
                handler.sendMessageDelayed(Message.obtain(handler,TIME_MSG),1000);
            }
        }
    };

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;

    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        super.dispatchWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.d(TAG, "dispatchWindowFocusChanged: loss focus ");
            if(useThread) {
                thread.interrupt();
            }else{
                handler.removeMessages(TIME_MSG);
            }
        } else {
            Log.d(TAG, "dispatchWindowFocusChanged: get focus");
            if(useThread) {
                thread = new MyThread();
                thread.start();
            }else{
                handler.sendMessageDelayed(Message.obtain(handler,TIME_MSG),1000);
            }
        }
    }

//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        Log.d(TAG, "onDetachedFromWindow() called");
//        if(useThread) {
//            thread.interrupt();
//        }
//    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }
//        getHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                invalidate();
//            }
//        },1000);
    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterY - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterY - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        //最后PM或AM字体变小
        spannableString.setSpan(new RelativeSizeSpan(0.3f),
                spannableString.toString().length() - 2,
                spannableString.toString().length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint,
                canvas.getWidth(), Layout.Alignment.ALIGN_CENTER,
                1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f,
                mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(hoursValuesColor);
        paint.setTextSize(mWidth*0.1f);
        paint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics metrics = paint.getFontMetrics();


        int radium = mCenterX-(int)(mWidth*0.12f);
        float top = metrics.top;
        float btm = metrics.bottom;

        for (int i = 1; i <= 12; i += 1 /* Step */) {
            int alpha = 90-i*30;

            int centerX = (int) (mCenterX + radium * Math.cos(Math.toRadians(alpha)));
            int centerY = (int) (mCenterY - radium * Math.sin(Math.toRadians(alpha)));

            int baseLine = (int)(centerY-top/2-btm/2);

            canvas.drawText(String.format("%02d",i),centerX,baseLine, paint);
        }
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int radium = mCenterX-(int)(mWidth*0.2f);

        paint.setColor(secondsNeedleColor);
        drawNeedlesOnece(canvas,paint,90-second*6,radium);

        paint.setStrokeWidth(1.5f*mWidth*DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(minutesNeedleColor);
        radium = mCenterX-(int)(mWidth*0.3f);
        drawNeedlesOnece(canvas,paint,90-minute*6,radium);

        paint.setColor(hoursValuesColor);
        radium = mCenterX-(int)(mWidth*0.4f);
        drawNeedlesOnece(canvas,paint,90-hour%12*30,radium);
    }

    private void drawNeedlesOnece(final Canvas canvas,Paint paint,double angle,int radius){
        int stopX = (int) (mCenterX + radius * Math.cos(Math.toRadians(angle)));
        int stopY = (int) (mCenterX - radius * Math.sin(Math.toRadians(angle)));
        canvas.drawLine(mCenterX, mCenterY, stopX, stopY, paint);
    }

    /**
     * Draw Center Dot
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(centerInnerColor);
        canvas.drawCircle(mCenterX,mCenterY,mWidth*0.01f,paint);
        paint.setColor(centerOuterColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mWidth*0.005f);
        canvas.drawCircle(mCenterX,mCenterY,mWidth*0.012f,paint);

    }

    public void setShowAnalog(boolean showAnalog) {
        mShowAnalog = showAnalog;
        invalidate();
    }

    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}