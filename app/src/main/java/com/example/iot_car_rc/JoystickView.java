package com.example.iot_car_rc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float knobRadius;
    private final int shading = 5;
    private boolean isEnabled = true;

    private JoystickListener joystickListener;

    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if (context instanceof JoystickListener) {
            joystickListener = (JoystickListener) context;
        }
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if (context instanceof JoystickListener) {
            joystickListener = (JoystickListener) context;
        }
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if (context instanceof JoystickListener) {
            joystickListener = (JoystickListener) context;
        }
    }

    public interface JoystickListener {
        void onJoystickMoved(int xOffset, int yOffset);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.setupDimensions();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private boolean wasTouchedNearBase(float x, float y) {
        float hypotenuse = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

        return hypotenuse <= baseRadius + 300;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (this.isEnabled && v.equals(this)) {
            if (event.getAction() != MotionEvent.ACTION_UP && wasTouchedNearBase(event.getX(), event.getY())) {
                float displacement = (float) Math.sqrt(Math.pow(event.getX() - centerX, 2) + Math.pow(event.getY() - centerY, 2));

                if (displacement < baseRadius) {
                    drawJoystick(event.getX(), event.getY());

                    int xDisplacement = (int) (((event.getX() - centerX) / baseRadius) * 100);
                    int yDisplacement = -(int) (((event.getY() - centerY) / baseRadius) * 100);

                    joystickListener.onJoystickMoved(xDisplacement, yDisplacement);

                } else {
                    float ratio = baseRadius / displacement;
                    float x = centerX + (event.getX() - centerX) * ratio;
                    float y = centerY + (event.getY() - centerY) * ratio;

                    drawJoystick(x, y);

                    int xDisplacement = (int) (((x - centerX) / baseRadius) * 100);
                    int yDisplacement = -(int) (((y - centerY) / baseRadius) * 100);

                    joystickListener.onJoystickMoved(xDisplacement, yDisplacement);
                }
            } else {
                drawJoystick(centerX, centerY);
                joystickListener.onJoystickMoved(0, 0);
            }
        }

        return true;
    }

    private void setupDimensions() {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 4;
        knobRadius = Math.min(getWidth(), getHeight()) / 6;
    }

    private void drawJoystick(float newX, float newY) {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = this.getHolder().lockCanvas();
            Paint paint = new Paint();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            float hypotenuse = (float) Math.sqrt(Math.pow(newX - centerX, 2) + Math.pow(newY - centerY, 2));
            float sin = (newY - centerY) / hypotenuse;
            float cos = (newX - centerX) / hypotenuse;

            paint.setARGB(255, 50, 50, 50);
            canvas.drawCircle(
                    centerX,
                    centerY,
                    baseRadius,
                    paint
            );

            for (int i = 1; i <= baseRadius / shading; i++) {
                paint.setARGB(150 / i, 0, 0, 0);
                canvas.drawCircle(
                        newX - cos * hypotenuse * shading / baseRadius * i,
                        newY - sin * hypotenuse * shading / baseRadius * i,
                        i * (knobRadius * shading / baseRadius),
                        paint
                );
            }

            for (int i = 1; i <= knobRadius / shading; i++) {
                paint.setARGB(
                        255,
                        255,
                        (int) (i * (255 * shading / knobRadius)),
                        (int) (i * (255 * shading / knobRadius))
                );

                canvas.drawCircle(
                        newX,
                        newY,
                        knobRadius - (float) i * (shading) / 2,
                        paint);
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void disableJoystick() {
        this.isEnabled = false;
    }

    public void enableJoystick() {
        this.isEnabled = true;
    }
}
