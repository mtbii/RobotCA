package com.robotca.ControlApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.robotca.ControlApp.Core.RobotStorage;

/**
 * Simple splash screen.
 */
public class SplashActivity extends Activity {

    // Log tag String
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, RobotChooser.class);
        try {
            Thread.sleep(2000, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "", e);
        }
        startActivity(intent);
        finish();
    }
}
