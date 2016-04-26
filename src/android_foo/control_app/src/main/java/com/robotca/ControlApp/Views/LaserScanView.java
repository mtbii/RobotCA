package com.robotca.ControlApp.Views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Layers.LaserScanRenderer;

/**
 * Custom GLSurfaceView for rendering a LaserScanLayer.
 *
 * Created by Nathaniel Stone on 3/29/16.
 */
public class LaserScanView extends GLSurfaceView {

    private static final String TAG = "LaserScanView";

    // The Renderer for this View
    private LaserScanRenderer laserScanRenderer;


    /**
     * Required Constructor.
     * @param context The parent context
     */
    public LaserScanView(Context context) {
        super(context);

        if (!isInEditMode())
            setRenderer(laserScanRenderer = new LaserScanRenderer((ControlApp) getContext()));
    }

    /**
     * Standard View constructor.
     */
    public LaserScanView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode())
            setRenderer(laserScanRenderer = new LaserScanRenderer((ControlApp) getContext()));
    }

    /**
     * @return The renderer for this view
     */
    public LaserScanRenderer getLaserScanRenderer() {
        return laserScanRenderer;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        super.surfaceChanged(holder, format, w, h);

        Log.d(TAG, "surfaceChanged(" + format + ", " + w + ", " + h + ")");

        ((ControlApp)getContext()).getRobotController().addLaserScanListener(laserScanRenderer);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);

        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        Log.d(TAG, "surfaceCreated(" + holder + ")");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        Log.d(TAG, "surfaceDestroyed(" + holder + ")");

        ((ControlApp)getContext()).getRobotController().removeLaserScanListener(laserScanRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return laserScanRenderer.onTouchEvent(e);
    }
}
