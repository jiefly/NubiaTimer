package com.gao.jiefly.nubiaclock;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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
    Paint backgroundPaint;
    Paint textPaint;
    Paint passCirclePaint;
    Paint remainCirclePaint;
    Paint bundlePaint;

    int passColor;
    int remainColor;

    int remainWidth;
    int passWidth;

    boolean isChangingTime = false;

    int width;
    int height;
    int radius;
    int maxTextWidth;
    int bundleLineHeight;
    //    int bundleLineWidth;
    int bundleCircleRadius;

    Point bundleStart;
    Point bundleEnd;
    Point bundleCircleCenter;

    float currentAngle = 30;

    public Timer(Context context) {
        super(context);
        init();
    }

    public Timer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Timer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Timer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        remainColor = DEFAULT_REMAIN_COLOR_1;
        passColor = DEFAULT_PASS_COLOR;
        remainWidth = DEFAULT_REMAIN_WIDTH;
        passWidth = DEFAULT_PASS_WIDTH;
        initPaint();
        initBundle();
    }

    private void initBundle() {
        bundleStart = new Point();
        bundleEnd = new Point();
        bundleCircleCenter = new Point();
    }

    private void initSize() {
        width = getWidth();
        height = getHeight();
        radius = Math.min(width / 2, height / 2) - Math.max(getPaddingBottom() + getPaddingTop(), getPaddingLeft() + getPaddingRight());
//        if text width is bigger than maxTextWidth,textSize will multiply by 0.9 until <= maxTextWidth
        maxTextWidth = 2 * (radius - circleNum * DEFAULT_CIRCLE_SPAN);
        bundleLineHeight = (circleNum - 1) * DEFAULT_CIRCLE_SPAN;
        bundleCircleRadius = DEFAULT_BUNDLE_CIRCLE_RADIUS;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initSize();
    }

    private void initPaint() {
        textPaint = new Paint();
        textPaint.setColor(DEFAULT_TEXT_COLOR);
        textPaint.setTextSize(DEFAULT_TEXT_SIZE);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        translate canvas to center
        canvas.translate(width / 2, height / 2);
        drawBackground(canvas);
//        drawPassCircle(canvas);
//        drawRemainCircle(canvas);
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
        if (currentAngle < 0)
            currentAngle = 0;
        drawCircle(canvas);
        drawText(canvas);
        drawBundle(canvas);
    }

    private void resetPaint() {
        passCirclePaint.setColor(passColor);
        passCirclePaint.setStrokeWidth(passWidth);
        remainCirclePaint.setColor(remainColor);
        remainCirclePaint.setStrokeWidth(remainWidth);
    }

    private void drawText(Canvas canvas) {
        String time = "00:12:12";
        float textWidth = checkAndChangeTextSize(time, maxTextWidth, textPaint);
        float textHeight = textPaint.ascent() + textPaint.descent();
        canvas.drawText(time, -textWidth / 2, -textHeight / 2, textPaint);
    }

    private float checkAndChangeTextSize(String in, int maxTextWidth, Paint textPaint) {
        Log.i(TAG, "maxTextWidth:" + maxTextWidth);
        float textWidth = textPaint.measureText(in);
        if (textWidth <= maxTextWidth)
            return textWidth;
        float textSize = textPaint.getTextSize();
        while (textWidth > maxTextWidth) {
            textSize *= 0.9;
            textPaint.setTextSize(textSize);
            textWidth = textPaint.measureText(in);
        }
        Log.i(TAG, "textWidth:" + textWidth);
        return textWidth;
    }

    private void drawCircle(Canvas canvas) {
        canvas.save();
        if (currentAngle > 360 * 3) {
            drawAllRemainCircle(canvas);
            return;
        }
        int radiusPass;
        int radiusRemain;
        int alpha;
        boolean sdkOk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        float angle = currentAngle % 360;
        for (int i = 0; i < circleNum; i++) {
            radiusPass = (int) (this.radius - i * DEFAULT_CIRCLE_SPAN - passCirclePaint.getStrokeWidth() / 2);
            radiusRemain = (int) (this.radius - i * DEFAULT_CIRCLE_SPAN - remainCirclePaint.getStrokeWidth() / 2);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float x = event.getX();
                float y = event.getY();
                if (checkPositionIsInBundleArea(x, y)) {
                    Log.e(TAG, "x:" + event.getX() + "\ny:" + event.getY());
                    isChangingTime = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isChangingTime) {
                    Point pressed = new Point((int) event.getX(), (int) event.getY());
                    pressed = resetCoordinateSystem(pressed);
                    currentAngle = calCurrentAngle(calAngle(pressed));
                    postInvalidate();
                    Log.e(TAG, "currentAngle:" + currentAngle);
                }
                break;
            case MotionEvent.ACTION_UP:
                isChangingTime = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private float calCurrentAngle(float v) {
        float angle = currentAngle % 360;
        if (angle > 350 && v < 180)
            return currentAngle - angle + v + 360;
        if (angle <= 10 && v >= 350)
            return currentAngle - angle + v - 360;
        if (currentAngle < 10 && v > 180)
            return currentAngle;
        return currentAngle - angle + v;
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


    private void drawBundle(Canvas canvas) {
        canvas.save();
        float angle = (float) (((currentAngle - 90) / 180) * Math.PI);
        Log.i(TAG, "cos(angle):" + (currentAngle / 180));
        Log.i(TAG, "cos(angle):" + Math.cos((currentAngle / 180) * Math.PI));
        bundleStart.x = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.cos(angle));
        bundleStart.y = (int) ((maxTextWidth / 2 - DEFAULT_REMAIN_WIDTH + DEFAULT_CIRCLE_SPAN) * Math.sin(angle));
        bundleEnd.x = (int) ((radius - bundleCircleRadius) * Math.cos(angle));
        bundleEnd.y = (int) ((radius - bundleCircleRadius) * Math.sin(angle));
        canvas.drawLine(bundleStart.x, bundleStart.y, bundleEnd.x, bundleEnd.y, bundlePaint);
        bundleCircleCenter.x = (int) (radius * Math.cos(angle));
        bundleCircleCenter.y = (int) (radius * Math.sin(angle));
        canvas.drawCircle(bundleCircleCenter.x, bundleCircleCenter.y, bundleCircleRadius, bundlePaint);
        canvas.restore();
    }


    private void drawBackground(Canvas canvas) {
        Shader shader = new LinearGradient(-width, -height, width, height, Color.parseColor("#4FCCC1"), Color.parseColor("#299E88"), Shader.TileMode.CLAMP);
        backgroundPaint.setShader(shader);
        canvas.drawRect(-width, -height, width, height, backgroundPaint);
    }
}
