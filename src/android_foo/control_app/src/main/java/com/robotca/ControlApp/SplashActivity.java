package com.robotca.ControlApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.robotca.ControlApp.Core.RobotStorage;

/**
 * Simple splash screen.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, RobotChooser.class);
        try {
            Thread.sleep(3000, 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(intent);
        finish();
    }
}
