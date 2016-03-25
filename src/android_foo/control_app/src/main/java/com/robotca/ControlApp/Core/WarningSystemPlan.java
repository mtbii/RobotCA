package com.robotca.ControlApp.Core;
import android.media.AudioManager;
import android.media.ToneGenerator;
import com.robotca.ControlApp.Core.Plans.RobotPlan;
import com.robotca.ControlApp.Core.RobotController;
import java.util.Random;
import sensor_msgs.LaserScan;
/**
 * Created by lightyz on 3/24/16.
 */
public class WarningSystemPlan extends RobotPlan {

    private final Random random;
    private float minRange;

    public WarningSystemPlan(float minRange) {
        this.minRange = Math.max(minRange, 0.2f);
        random = new Random();
    }
    @Override
    protected void start(final RobotController controller) throws Exception {
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,100);
        while(!isInterrupted()) {
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2,5000);
            LaserScan laserScan = controller.getLaserScan();

            float[] ranges = laserScan.getRanges();
            float shortestDistance = ranges[ranges.length / 2];
            System.out.println("the original shortest distance is ... ");
            System.out.print(shortestDistance);

            float shortestDistanceAngle = 0;
            float angle = laserScan.getAngleMin();
            float angleDelta = (float) Math.toRadians(30);
            float angleIncrement = laserScan.getAngleIncrement();

            for (int i = 0; i < laserScan.getRanges().length; i++) {
                if (ranges[i] < shortestDistance && angle > -angleDelta && angle < angleDelta) {
                    shortestDistance = ranges[i];
                    shortestDistanceAngle = angle;
                }

                angle += angleIncrement;
            }

            if (shortestDistance < minRange) {
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2,5000);
            }
        }
    }
}
