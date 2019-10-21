package com.example.iot_car_rc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.json.JSONException;

/**
 * Represents JoystickView.
 *
 * @author Rafał Dąbrowski
 */
public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float knobRadius;
    private boolean isEnabled = true;

    private JoystickListener joystickListener;

    /**
     * Constructor for JoystickView. It registers onTouch and joystick listeners.
     *
     * @param context the context
     */
    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if (context instanceof JoystickListener) {
            joystickListener = (JoystickListener) context;
        }
    }

    /**
     * Constructor for JoystickView. It registers onTouch and joystick listeners.
     *
     * @param context the context
     * @param attrs   A collection of attributes.
     */
    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if (context instanceof JoystickListener) {
            joystickListener = (JoystickListener) context;
        }
    }

    /**
     * Constructor for JoystickView. It registers onTouch and joystick listeners.
     *
     * @param context  the context
     * @param attrs    A collection of attributes.
     * @param defStyle the default style
     */
    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if (context instanceof JoystickListener) {
            joystickListener = (JoystickListener) context;
        }
    }

    /**
     * Interface for joystick events callbacks.
     *
     * @author Rafał Dąbrowski
     */
    public interface JoystickListener {
        /**
         * This method handles joystick movement event.
         *
         * @param xOffset the offset of the joystick knob in the X axis, ranges between <0, 100>.
         * @param yOffset the offset of the joystick knob in the Y axis, ranges between <0, 100>.
         * @return Nothing.
         * @throws JSONException if parameters are of the wrong type
         * @see JSONException
         */
        void onJoystickMoved(int xOffset, int yOffset) throws JSONException;
    }

    /**
     * Handler of surfaceCreated event.
     * It calls {@link com.example.iot_car_rc.JoystickView#setupDimensions()}
     * and {@link com.example.iot_car_rc.JoystickView#drawJoystick(float, float)} methods.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.setupDimensions();
        drawJoystick(centerX, centerY);
    }

    /**
     * Handler of surfaceChanged event. This handler is not used in this application.
     *
     * @param holder the SurfaceHolder whose surface has changed.
     * @param format the new PixelFormat of the surface.
     * @param width  the new width of the surface.
     * @param height the new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * Handler of surfaceDestroyed event. This handler is not used in this application.
     *
     * @param holder the SurfaceHolder whose surface is being destroyed.
     * @return
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * This method check if the screen was touched close to the joystick base.
     *
     * @param x the x coordinate of the touch event.
     * @param y the y coordinate of the touch event.
     * @return
     */
    private boolean wasTouchedNearBase(float x, float y) {
        float hypotenuse = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

        return hypotenuse <= baseRadius + 300;
    }

    /**
     * This method is a handler of the OnTouch event.
     * It draws the joystick at a new location and
     * calls onJoystickMoved callback with x and y displacement
     *
     * @param view        The view the touch event has been dispatched to.
     * @param motionEvent The MotionEvent object containing full information about the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (this.isEnabled && view.equals(this)) {
            int xDisplacement = 0;
            int yDisplacement = 0;

            if (motionEvent.getAction() != MotionEvent.ACTION_UP
                    && wasTouchedNearBase(motionEvent.getX(), motionEvent.getY())) {
                float eventX = motionEvent.getX();
                float eventY = motionEvent.getY();
                float displacement = (float) Math.sqrt(
                        Math.pow(eventX - centerX, 2) + Math.pow(eventY - centerY, 2)
                );

                if (displacement < baseRadius) {
                    xDisplacement = (int) (((eventX - centerX) / baseRadius) * 100);
                    yDisplacement = -(int) (((eventY - centerY) / baseRadius) * 100);

                    drawJoystick(eventX, eventY);
                } else {
                    float ratio = baseRadius / displacement;
                    float x = centerX + (eventX - centerX) * ratio;
                    float y = centerY + (eventY - centerY) * ratio;

                    xDisplacement = (int) (((x - centerX) / baseRadius) * 100);
                    yDisplacement = -(int) (((y - centerY) / baseRadius) * 100);

                    drawJoystick(x, y);
                }
            } else {
                drawJoystick(centerX, centerY);
            }

            try {
                joystickListener.onJoystickMoved(xDisplacement, yDisplacement);
            } catch (JSONException exception) {
                Log.e(exception.getMessage(), "JoystickView:onTouch");
            }
        }

        return true;
    }

    /**
     * This method sets up the dimensions of the joystick
     *
     * @return Nothing.
     */
    private void setupDimensions() {
        final int height = this.getHeight();
        final int width = this.getWidth();

        centerX = width / 2;
        centerY = height / 2;
        baseRadius = Math.min(width, height) / 4;
        knobRadius = Math.min(width, height) / 6;
    }

    /**
     * This method enables the joystick.
     *
     * @param newX new X coordinate at which the joystick will be drawn.
     * @param newY new Y coordinate at which the joystick will be drawn.
     * @return Nothing.
     */
    private void drawJoystick(float newX, float newY) {
        if (this.getHolder().getSurface().isValid()) {
            Canvas canvas = this.getHolder().lockCanvas();
            Paint paint = new Paint();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            final int shading = 5;


            float hypotenuse =
                    (float) Math.sqrt(Math.pow(newX - centerX, 2) + Math.pow(newY - centerY, 2));
            float sin = (newY - centerY) / hypotenuse;
            float cos = (newX - centerX) / hypotenuse;

            //  Draw background circle (the base of the joystick)
            paint.setARGB(255, 50, 50, 50);
            canvas.drawCircle(
                    centerX,
                    centerY,
                    this.baseRadius,
                    paint
            );

            //  Draw the stick
            for (int i = 1; i <= this.baseRadius / shading; i++) {
                paint.setARGB(150 / i, 0, 0, 0);
                canvas.drawCircle(
                        newX - cos * hypotenuse * shading / this.baseRadius * i,
                        newY - sin * hypotenuse * shading / this.baseRadius * i,
                        i * (knobRadius * shading / this.baseRadius),
                        paint
                );
            }

            //  Draw the knob
            for (int i = 1; i <= this.knobRadius / shading; i++) {
                paint.setARGB(
                        255,
                        (int) (i * (255 * shading / this.knobRadius)),
                        (int) (i * (255 * shading / this.knobRadius)),
                        (int) (i * (255 * shading / this.knobRadius))
                );

                canvas.drawCircle(
                        newX,
                        newY,
                        this.knobRadius - (float) i * (shading) / 2,
                        paint);
            }

            this.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    /**
     * This method disables the joystick.
     *
     * @return Nothing.
     */
    public void disableJoystick() {
        this.isEnabled = false;
    }

    /**
     * This method enables the joystick.
     *
     * @return Nothing.
     */
    public void enableJoystick() {
        this.isEnabled = true;
    }
}
