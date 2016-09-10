package com.gao.jiefly.nubiaclock;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.TimerTask;

/**
 * Created by jiefly on 2016/9/8.
 * Email:jiefly1993@gmail.com
 * Fighting_jiiiiie
 */
public class Timer extends View {
    private static final String TAG = Timer.class.getSimpleName();
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final int DEFAULT_PASS_COLOR = Color.parseColor("#5BCFA6");
    private static final int DEFAULT_PASS_WIDTH = 2;
    private static final int DEFAULT_REMAIN_COLOR_1 = Color.parseColor("#02FFBA");
    private static final int DEFAULT_REMAIN_COLOR_2 = Color.parseColor("#9AFF9A");
    private static final int DEFAULT_REMAIN_COLOR_3 = Color.parseColor("#FFF886");
    private static final int DEFAULT_REMAIN_WIDTH = DEFAULT_PASS_WIDTH * 4;
    private static final int DEFAULT_CIRCLE_SPAN = DEFAULT_REMAIN_WIDTH * 2;
    private static final double DEFAULT_CIRCLE_ALPHA_DECREASE = 0.8;
    private static final int DEFAULT_TEXT_SIZE = 150;
    private static final int DEFAULT_BUNDLE_CIRCLE_RADIUS = DEFAULT_CIRCLE_SPAN;
    private static final int DEFAULT_BUNDLE_WIDTH = DEFAULT_REMAIN_WIDTH * 2 / 3;
    private static final int circleNum = 6;
    private static final int BUNDLE_STATU_NORMAL = 0x10;
    private static final int BUNDLE_STATU_END = 0x01;
    private static final int BUNDLE_STATU_CHANGING = 0x11;
    //    ms
    private static final int BUNDLE_ANIM_TIME = 400;
    private OnTimeUpListener mOnTimeUpListener;
    boolean sdkOk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    boolean isFirst = true;
    Paint backgroundPaint;
    Paint textPaint;
    Paint passCirclePaint;
    Paint remainCirclePaint;
    Paint bundlePaint;


    int bundleStatu = BUNDLE_STATU_NORMAL;

    float textSize;
    float textWidth;
    float textHeight;

    int passColor;
    int remainColor;

    int remainWidth;
    int passWidth;

    boolean isChangingTime = false;

    int width;
    int height;
    float radius;
    float maxTextWidth;
    int bundleLineHeight;
    //    int bundleLineWidth;
    float bundleCircleRadius;

    Point bundleStart;
    Point bundleEnd;
    Point bundleCircleCenter;

    GradientDrawable background;

    float currentAngle = 0;
    String time;
    int hour = 0;
    int minute = 0;
    float second = 0;

    int oldHour;
    int oldMinute;
    float oldSecond;

    long startTime;
    long endTime;
    long passTime;
    float timerValue;


    TimerTask mTimerTask;
    java.util.Timer mTimer;


    public Timer(Context context) {
        super(context);
    }

    public Timer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Timer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Timer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        remainColor = DEFAULT_REMAIN_COLOR_1;
        passColor = DEFAULT_PASS_COLOR;
        remainWidth = DEFAULT_REMAIN_WIDTH;
        passWidth = DEFAULT_PASS_WIDTH;
        textSize = DEFAULT_TEXT_SIZE;
        time = angle2Time(currentAngle);
        initPaint();
        initBundle();
        initSize();
    }

    private void initTimer() {
        mTimer = new java.util.Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
//               ms
                passTime = (System.currentTimeMillis() - startTime);
                timerValue = timerValue - 0.05f;
                timerValue2Time(timerValue);
                time = formateTime(hour, minute, second);
                currentAngle = time2Angle(hour, minute, second);
                postInvalidate();
//                Log.e(TAG, "passTime:" + passTime);
                if (timerValue <= 0) {
                    bundleStatu = BUNDLE_STATU_NORMAL;
                    mTimer.cancel();
                    setTime(oldHour, oldMinute, (int) oldSecond);
                    if (mOnTimeUpListener != null)
                        mOnTimeUpListener.onTimeUp();
                }
            }
        };
    }

    public void setOnTimeUpListener(OnTimeUpListener onTimeUpListener) {
        this.mOnTimeUpListener = onTimeUpListener;
    }

    public void startTimer() {
        if (second == 0 && minute == 0 && hour == 0)
            return;
        initTimer();
        savaTime();
        startTime = System.currentTimeMillis();
        timerValue = hour * 60 * 60 + minute * 60 + second;
        endTime = (long) (startTime + timerValue * 1000);
        mTimer.schedule(mTimerTask, 0, 50);
        bundleStatu = BUNDLE_STATU_CHANGING;

    }

    //    save time ,when time up restore time
    private void savaTime() {
        oldSecond = second;
        oldHour = hour;
        oldMinute = minute;
    }

    public void stopTimer() {
        if (mTimer != null)
            mTimer.cancel();
        bundleStatu = BUNDLE_STATU_NORMAL;
        postInvalidate();
    }

    public void setTime(int hour, int minute, int second) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        currentAngle = time2Angle(hour, minute, second);
        time = formateTime(hour, minute, second);
        postInvalidate();
    }

    private void timerValue2Time(float timerValue) {
        second = timerValue % 60;
        minute = (int) ((timerValue - second) % 3600) / 60;
        hour = (int) ((timerValue - second - 60 * minute) / 3600);
//        Log.e(TAG, "timerValud:" + timerValue + "\ntime:" + hour + ":" + minute + ":" + second);
    }

    private void initBundle() {
        bundleStart = new Point();
        bundleEnd = new Point();
        bundleCircleCenter = new Point();
    }

    private void initSize() {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        Log.e(TAG, "width:" + width + "\nheight:" + height);
        radius = Math.min(width / 2, height / 2) - Math.max(getPaddingBottom() + getPaddingTop(), getPaddingLeft() + getPaddingRight());
//        if text width is bigger than maxTextWidth,textSize will multiply by 0.9 until <= maxTextWidth
        maxTextWidth = 2 * (radius - circleNum * DEFAULT_CIRCLE_SPAN);
        bundleLineHeight = (circleNum - 1) * DEFAULT_CIRCLE_SPAN;
        bundleCircleRadius = DEFAULT_BUNDLE_CIRCLE_RADIUS;
    }

    private void initPaint() {
        textPaint = new Paint();
        textPaint.setColor(DEFAULT_TEXT_COLOR);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        passCirclePaint = new Paint();
        passCirclePaint.setColor(passColor);
        passCirclePaint.setStrokeWidth(passWidth);
        passCirclePaint.setStyle(Paint.Style.STROKE);
        passCirclePaint.setAntiAlias(true);

        remainCirclePaint = new Paint();
        remainCirclePaint.setColor(remainColor);
        remainCirclePaint.setStyle(Paint.Style.STROKE);
        remainCirclePaint.setStrokeWidth(remainWidth);
        remainCirclePaint.setAntiAlias(true);

        backgroundPaint = new Paint();

        bundlePaint = new Paint();
        bundlePaint.setStyle(Paint.Style.FILL);
        bundlePaint.setAntiAlias(true);
//        end of line is round
        bundlePaint.setStrokeCap(Paint.Cap.ROUND);
        bundlePaint.setStrokeWidth(DEFAULT_BUNDLE_WIDTH);
        bundlePaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isFirst)
            init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        translate canvas to center
        canvas.translate(width / 2, height / 2);
//        if is first draw view ,generate a background drawable and set view background
        if (isFirst)
            generateAndSetBackground();
//        set circle style
        changeCirclePaintConfig();
//        draw double circle
        drawCircle(canvas);
//        draw time
        drawText(canvas);
        changeBundlePaintConfig();
//        draw bundle
        drawBundle(canvas);
//        set flag
        isFirst = false;
    }

    private void changeBundlePaintConfig() {
        float angle = (float) (((currentAngle - 90) / 180) * Math.PI);
        switch (bundleStatu) {
            case BUNDLE_STATU_NORMAL:
                bundleCircleRadius = DEFAULT_BUNDLE_CIRCLE_RADIUS;
                bundleStart.x = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.cos(angle));
                bundleStart.y = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.sin(angle));
                bundleCircleCenter.x = (int) (radius * Math.cos(angle));
                bundleCircleCenter.y = (int) (radius * Math.sin(angle));
                bundleEnd.x = bundleCircleCenter.x;
                bundleEnd.y = bundleCircleCenter.y;

                break;
            case BUNDLE_STATU_CHANGING:
//                anim start in these time
                float time = System.currentTimeMillis() - startTime;
                if (time > 0 && time <= BUNDLE_ANIM_TIME / 5) {
                    bundleStart.x = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.cos(angle));
                    bundleStart.y = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.sin(angle));
                    bundleCircleCenter.x = (int) (radius * Math.cos(angle));
                    bundleCircleCenter.y = (int) (radius * Math.sin(angle));
                    bundleEnd.x = bundleCircleCenter.x;
                    bundleEnd.y = bundleCircleCenter.y;
                    bundleCircleRadius = DEFAULT_BUNDLE_CIRCLE_RADIUS / 2;
                }
//                 bundle anim
                if (passTime > BUNDLE_ANIM_TIME / 5 && passTime < BUNDLE_ANIM_TIME) {
                    float x = (passTime - BUNDLE_ANIM_TIME * 0.2f) / (BUNDLE_ANIM_TIME * 0.8f);
//                    Log.e(TAG, "x:" + x);
                    bundleCircleRadius = DEFAULT_BUNDLE_CIRCLE_RADIUS;
                    bundleStart.x = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.cos(angle));
                    bundleStart.y = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.sin(angle));
                    bundleEnd.x = (int) (bundleStart.x + (1.0f - x) * (radius * Math.cos(angle) - bundleStart.x));
                    bundleEnd.y = (int) (bundleStart.y + (1.0f - x) * (radius * Math.sin(angle) - bundleStart.y));
                    bundleCircleCenter.x = bundleEnd.x;
                    bundleCircleCenter.y = bundleEnd.y;
                    bundleCircleRadius = DEFAULT_BUNDLE_CIRCLE_RADIUS / 2;

                }
//                anim end in these time
                if (passTime >= 300) {
                    bundleStatu = BUNDLE_STATU_END;
                }
                postInvalidate();
                break;
            case BUNDLE_STATU_END:
                bundleCircleRadius = DEFAULT_BUNDLE_CIRCLE_RADIUS / 2;
                bundleCircleCenter.x = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.cos(angle));
                bundleCircleCenter.y = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.sin(angle));
                postInvalidate();
                break;
        }


    }

    private void changeCirclePaintConfig() {
        switch ((int) (currentAngle / 360)) {
            case 0:
//                only currentAngle < 360 ，pass circle width is small
                passColor = DEFAULT_PASS_COLOR;
                remainColor = DEFAULT_REMAIN_COLOR_1;
                passWidth = DEFAULT_PASS_WIDTH;
                resetPaint();
                break;
            case 1:
                passColor = DEFAULT_REMAIN_COLOR_1;
                remainColor = DEFAULT_REMAIN_COLOR_2;
                passWidth = DEFAULT_REMAIN_WIDTH;
                resetPaint();
                break;
            case 2:
                passColor = DEFAULT_REMAIN_COLOR_2;
                remainColor = DEFAULT_REMAIN_COLOR_3;
                passWidth = DEFAULT_REMAIN_WIDTH;
                resetPaint();
                break;
//            no pass
            default:
                remainColor = DEFAULT_REMAIN_COLOR_3;
                resetPaint();
        }
    }

    private void resetPaint() {
        passCirclePaint.setColor(passColor);
        passCirclePaint.setStrokeWidth(passWidth);
        remainCirclePaint.setColor(remainColor);
        remainCirclePaint.setStrokeWidth(remainWidth);
    }

    private float checkAndChangeTextSize(String in, int maxTextWidth, Paint textPaint) {
        Log.i(TAG, "maxTextWidth:" + maxTextWidth);
        float textWidth = textPaint.measureText(in);
        if (textWidth <= maxTextWidth)
            return textWidth;
        textSize = textPaint.getTextSize();
        while (textWidth > maxTextWidth) {
            textSize = textPaint.getTextSize();
            textSize *= 0.9;
            textPaint.setTextSize(textSize);
            textWidth = textPaint.measureText(in);
        }
//        Log.i(TAG, "textWidth:" + textWidth);
//        Log.i(TAG, "textPaintSize:" + this.textPaint.getTextSize());
        return textWidth;
    }

    private void drawCircle(Canvas canvas) {
        canvas.save();
        if (currentAngle > 360 * 3) {
            drawAllRemainCircle(canvas);
            return;
        }
        float radiusPass;
        float radiusRemain;
        int alpha;

        float angle = currentAngle % 360;
        for (int i = 0; i < circleNum; i++) {
            radiusPass = this.radius - i * DEFAULT_CIRCLE_SPAN - passCirclePaint.getStrokeWidth() / 2;
            radiusRemain = this.radius - i * DEFAULT_CIRCLE_SPAN - remainCirclePaint.getStrokeWidth() / 2;
            alpha = (int) (255 * Math.pow(DEFAULT_CIRCLE_ALPHA_DECREASE, i));
            passCirclePaint.setAlpha(alpha);
            remainCirclePaint.setAlpha(alpha);
            if (sdkOk) {

                canvas.drawArc(-radiusPass, -radiusPass, radiusPass, radiusPass, angle - 90, 360 - angle, false, passCirclePaint);
                canvas.drawArc(-radiusRemain, -radiusRemain, radiusRemain, radiusRemain, -90, angle, false, remainCirclePaint);
            } else {
                RectF rectP = new RectF(-radiusPass, -radiusPass, radiusPass, radiusPass);
                canvas.drawArc(rectP, angle - 90, 360 - angle, false, passCirclePaint);
                RectF rectR = new RectF(-radiusRemain, -radiusRemain, radiusRemain, radiusRemain);
                canvas.drawArc(rectR, angle - 90, 360 - angle, false, remainCirclePaint);
            }
        }
        canvas.restore();
    }

    private void drawBundle(Canvas canvas) {
        canvas.save();
        if (bundleStatu != BUNDLE_STATU_END)
            canvas.drawLine(bundleStart.x, bundleStart.y, bundleEnd.x, bundleEnd.y, bundlePaint);
        canvas.drawCircle(bundleCircleCenter.x, bundleCircleCenter.y, bundleCircleRadius, bundlePaint);
        canvas.restore();
    }

    private void generateAndSetBackground() {
        if (background == null) {
            background = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xff4FCCC1, 0xff299E88});
            background.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        }
        this.setBackground(background);
    }

    private void drawAllRemainCircle(Canvas canvas) {
        canvas.save();
        for (int i = 0; i < circleNum; i++) {
            int radius = (int) (this.radius - i * DEFAULT_CIRCLE_SPAN - remainCirclePaint.getStrokeWidth() / 2);
//            canvas.drawCircle(0,0,radius,remainCirclePaint);
            int alpha = (int) (255 * Math.pow(DEFAULT_CIRCLE_ALPHA_DECREASE, i));
            remainCirclePaint.setAlpha(alpha);
            canvas.drawCircle(0, 0, radius, remainCirclePaint);
        }
        canvas.restore();
    }

    private void drawText(Canvas canvas) {
        if (isFirst) {
            textWidth = checkAndChangeTextSize(time, (int) maxTextWidth, textPaint);
            textHeight = textPaint.ascent() + textPaint.descent();
            textPaint.setTextSize(textSize);
        }
        Log.e(TAG, "textSize:" + textPaint.getTextSize());
        canvas.drawText(time, -textWidth / 2, -textHeight / 2, textPaint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                if (checkPositionIsInBundleArea(x, y)) {
                    isChangingTime = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isChangingTime) {
                    Point pressed = new Point((int) event.getX(), (int) event.getY());
                    pressed = resetCoordinateSystem(pressed);
                    currentAngle = calCurrentAngle(calAngle(pressed));
                    time = angle2Time(currentAngle);
                    postInvalidate();
//                    Log.e(TAG, "currentAngle:" + currentAngle);
                }
                break;
            case MotionEvent.ACTION_UP:
                isChangingTime = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private float calCurrentAngle(float v) {
//        Log.e(TAG, "calCurrentAngle:" + currentAngle);
        float angle = currentAngle % 360;
        if (angle > 350 && v < 180)
            return currentAngle - angle + v + 360;
        if (currentAngle < 10 && currentAngle >= 0 && v > 270)
            return 0;
        if (angle <= 90 && v >= 270) {
            return currentAngle - angle + v - 360;
        }
        if (currentAngle < 0)
            return 0;
        return Math.max(currentAngle - angle + v, 0);
    }

    private float calAngle(Point pressed) {
//        avoid divide by zero
        double x = 0.0001;
        double y = 0.0001;
        if (pressed.x != 0)
            x = pressed.x;
        if (pressed.y != 0)
            y = pressed.y;
        if (x > 0)
            return (float) Math.toDegrees(Math.atan(y / x)) + 90;
        else
            return 180 + ((float) Math.toDegrees(Math.atan(y / x)) + 90);
    }

    private boolean checkPositionIsInBundleArea(float x, float y) {
        Point pressed = new Point((int) x, (int) y);
//        Log.e(TAG,"before:"+pressed);
        pressed = resetCoordinateSystem(pressed);
//        Log.e(TAG,"after:"+pressed);
        int maxX = Math.abs(bundleEnd.x) + DEFAULT_BUNDLE_CIRCLE_RADIUS * 2;
        int maxY = Math.abs(bundleEnd.y) + DEFAULT_BUNDLE_CIRCLE_RADIUS * 2;
        int minX = Math.abs(bundleStart.x);
        int minY = Math.abs(bundleStart.y);
        if (Math.abs(bundleEnd.x - bundleStart.x) < DEFAULT_BUNDLE_CIRCLE_RADIUS * 2) {
            maxX = Math.abs(bundleEnd.x) + DEFAULT_BUNDLE_CIRCLE_RADIUS * 4;
            minX = Math.abs(bundleStart.x) - DEFAULT_BUNDLE_CIRCLE_RADIUS * 2;
        }
        if (Math.abs(bundleEnd.y - bundleStart.y) < DEFAULT_BUNDLE_CIRCLE_RADIUS * 2) {
            maxY = Math.abs(bundleEnd.y) + DEFAULT_BUNDLE_CIRCLE_RADIUS * 4;
            minY = Math.abs(bundleStart.y) - DEFAULT_BUNDLE_CIRCLE_RADIUS * 2;
        }
        return !(Math.abs(pressed.x) > maxX || Math.abs(pressed.x) < minX) && !(Math.abs(pressed.y) > maxY || Math.abs(pressed.y) < minY);
    }

    private Point resetCoordinateSystem(Point src) {
        src.offset(-width / 2, -height / 2);
        return src;
    }

    private String formateTime(int hour, int minute, float second) {
        StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour).append(":");
        if (minute < 10)
            sb.append(0);
        sb.append(minute).append(":");
        if (second < 10)
            sb.append(0);
        sb.append((int) second);
        return sb.toString();
    }


    private String angle2Time(float currentAngle) {

        switch ((int) (currentAngle / 90)) {
            // 0<=currentAngle<90  1.5'/s
            case 0:
                hour = 0;
                minute = 0;
                second = (int) (currentAngle * 60 / 90);
                break;
            // 90<=currentAngle<180 9'/m
            case 1:
                hour = 0;
                minute = (int) ((currentAngle - 90) / 9) + 1;
                second = 0;
                break;
            // 180<=currentAngle<270 4.5'/m
            case 2:
                second = 0;
                hour = 0;
                minute = (int) ((currentAngle - 180) / 4.5) + 10;
                break;
            // 270<=currentAngle<360 3'/m
            case 3:
                second = 0;
                hour = 0;
                minute = (int) ((currentAngle - 270) / 3) + 30;
                break;
            // 360<=currentAngle 6'/m
            default:
                second = 0;
                hour = 1;
                minute = (int) ((currentAngle - 360) / 6);
                break;
        }
        hour += minute / 60;
        minute = minute % 60;
        return formateTime(hour, minute, second);
    }

    private float time2Angle(int hour, int minute, float second) {
        minute += hour * 60;

//        0~90
        if (minute == 0) {
            return second * 1.5f;
        }
        if (minute <= 10) {
            return (minute - 1) * 10 + 90 + second * 0.15f;
        }
        if (minute <= 30)
            return (minute - 10) * 4.5f + 180 + second * 0.075f;
        if (minute <= 60)
            return (minute - 30) * 3 + 270 + second * 0.05f;
        return (minute - 60) * 6 + 360 + second * 0.1f;
    }

    interface OnTimeUpListener {
        void onTimeUp();
    }

}
