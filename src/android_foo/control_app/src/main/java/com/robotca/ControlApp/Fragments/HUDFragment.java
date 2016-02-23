package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robotca.ControlApp.R;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import nav_msgs.Odometry;

/**
 * Simple fragment showing info about the Robot's current state.
 *
 *
 * @author Nathaniel Stone
 */
public class HUDFragment extends Fragment implements MessageListener<Odometry>{

    private static final String TAG = "HUDFragment";

//    private OdometryListener odometryListener;

    private View view;
    private TextView speedView, turnrateView;

    private final UpdateUIRunnable UPDATE_UI_RUNNABLE = new UpdateUIRunnable();

    /**
     * Default Constructor.
     */
    public HUDFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_hud, container, false);

            speedView = (TextView) view.findViewById(R.id.hud_speed);
            turnrateView = (TextView) view.findViewById(R.id.hud_turnrate);

            updateUI(0.0, 0.0);
        }

        return view;
    }

    private void updateUI(final double speed, final double turnrate)
    {
        if (!isDetached()) {
            UPDATE_UI_RUNNABLE.speed = speed;
            UPDATE_UI_RUNNABLE.turnrate = turnrate;

            view.post(UPDATE_UI_RUNNABLE);
        }
    }

    @Override
    public void onNewMessage(Odometry message) {
//            Log.d(TAG, "New Message: " + message.getTwist().getTwist().getLinear().getX());

        updateUI(message.getTwist().getTwist().getLinear().getX(),
                message.getTwist().getTwist().getAngular().getZ());
    }

    private class UpdateUIRunnable implements Runnable
    {
        public double speed, turnrate;

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {

            if (isDetached())
                return;

            try {
                speed = (int) (speed * 100.0) / 100.0;
                turnrate = (int) (turnrate * 100.0) / 100.0;

                if (speedView != null)
                    speedView.setText(String.format((String) getText(R.string.speed_string), speed));

                if (turnrateView != null)
                    turnrateView.setText(String.format((String) getText(R.string.turnrate_string), turnrate));
            } catch (IllegalStateException e) {
                // Ignore
            }
        }
    }
}
