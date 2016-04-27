package com.robotca.ControlApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/**
 * Simple splash screen.
 */
public class SplashActivity extends Activity {

    // Log tag String
    private static final String TAG = "SplashActivity";

    // Request code for Overlay permissions
    private static final int OVERLAY_REQUEST_CODE = 0xABCD1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkDrawOverlayPermission();
    }

    /**
     * Checks for draw overlay permissions. Required on higher levels of Android.
     */
    public void checkDrawOverlayPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
            } else {
                goToNextActivity(2000L);
            }
        }
        else {
            goToNextActivity(2000L);
        }
    }

    /**
     * Callback for when the user has to grant overlay permissions. This only happens on high android version.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {

        if (requestCode == OVERLAY_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // If this permission is not granted, the app will crash because of something
                // ROS implements so we'll just cleanly close the app here
                if (!Settings.canDrawOverlays(this)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permission Required!").setMessage("The app needs this permission " +
                            "to run, it will now be closed.").create().show();
                    finish();
                } else {
                    goToNextActivity(100L);
                }
            } else {
                goToNextActivity(100L);
            }
        }
    }

    /*
     * Moves to the next Activity after the specified delay.
     */
    private void goToNextActivity(long delay)
    {
        Intent intent = new Intent(this, RobotChooser.class);
        try {
            Thread.sleep(delay, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "", e);
        }
        startActivity(intent);
        finish();
    }
}
