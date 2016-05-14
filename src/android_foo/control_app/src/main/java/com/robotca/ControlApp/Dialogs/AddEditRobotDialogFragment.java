package com.robotca.ControlApp.Dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.R;

import java.util.UUID;

/**
 * Dialog for adding or editing a Robot.
 * <p/>
 * Created by Michael Brunson on 1/23/16.
 */
public class AddEditRobotDialogFragment extends DialogFragment {

    /**
     * Bundle key for position
     */
    public static final String POSITION_KEY = "POSITION_KEY";

    // Temporary RobotInfo
    private RobotInfo mInfo = new RobotInfo();

    // Use this instance of the interface to deliver action events
    private DialogListener mListener;

    // EditTexts for editing the RobotInfo
    private EditText mNameEditTextView;
    private EditText mMasterUriEditTextView;
    private View mAdvancedOptionsView;
    private EditText mJoystickTopicEditTextView;
    private EditText mLaserScanTopicEditTextView;
    private EditText mCameraTopicEditTextView;
    private EditText mNavSatTopicEditTextView;
    private EditText mOdometryTopicEditTextView;
    private EditText mPoseTopicEditTextView;
    private CheckBox mReverseLaserScanCheckBox;
    private CheckBox mInvertXAxisCheckBox;
    private CheckBox mInvertYAxisCheckBox;
    private CheckBox mInvertAngularVelocityCheckBox;


    // Position of the RobotInfo in the list of RobotInfos
    private int mPosition = -1;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            mPosition = args.getInt(POSITION_KEY, -1);
            mInfo.load(args);
        }
    }

    // Override the Fragment.onAttach() method to instantiate the DialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DialogListener so we can send events to the host
            mListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString()  + " must implement DialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.dialog_add_robot, null);
        mNameEditTextView = (EditText) v.findViewById(R.id.robot_name_edit_text);
        mMasterUriEditTextView = (EditText) v.findViewById(R.id.master_uri_edit_view);

        CheckBox mAdvancedOptionsCheckbox = (CheckBox) v.findViewById(R.id.advanced_options_checkbox_view);
        mAdvancedOptionsView = v.findViewById(R.id.advanced_options_view);
        mJoystickTopicEditTextView = (EditText) v.findViewById(R.id.joystick_topic_edit_text);
        mLaserScanTopicEditTextView = (EditText) v.findViewById(R.id.laser_scan_edit_view);
        mCameraTopicEditTextView = (EditText) v.findViewById(R.id.camera_topic_edit_view);
        mNavSatTopicEditTextView = (EditText) v.findViewById(R.id.navsat_topic_edit_view);
        mOdometryTopicEditTextView = (EditText) v.findViewById(R.id.odometry_topic_edit_view);
        mPoseTopicEditTextView = (EditText) v.findViewById(R.id.pose_topic_edit_view);
        mReverseLaserScanCheckBox = (CheckBox) v.findViewById(R.id.reverse_laser_scan_check_box);
        mInvertXAxisCheckBox = (CheckBox) v.findViewById(R.id.invert_x_axis_check_box);
        mInvertYAxisCheckBox = (CheckBox) v.findViewById(R.id.invert_y_axis_check_box);
        mInvertAngularVelocityCheckBox = (CheckBox) v.findViewById(R.id.invert_angular_velocity_check_box);

        mNameEditTextView.setText(mInfo.getName());
        mMasterUriEditTextView.setText(mInfo.getMasterUri());

        mAdvancedOptionsCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mAdvancedOptionsView.setVisibility(View.VISIBLE);
                } else {
                    mAdvancedOptionsView.setVisibility(View.GONE);
                }
            }
        });

        mJoystickTopicEditTextView.setText(mInfo.getJoystickTopic());
        mLaserScanTopicEditTextView.setText(mInfo.getLaserTopic());
        mCameraTopicEditTextView.setText(mInfo.getCameraTopic());
        mNavSatTopicEditTextView.setText(mInfo.getNavSatTopic());
        mOdometryTopicEditTextView.setText(mInfo.getOdometryTopic());
        mPoseTopicEditTextView.setText(mInfo.getPoseTopic());
        mReverseLaserScanCheckBox.setChecked(mInfo.isReverseLaserScan());
        mInvertXAxisCheckBox.setChecked(mInfo.isInvertX());
        mInvertYAxisCheckBox.setChecked(mInfo.isInvertY());
        mInvertAngularVelocityCheckBox.setChecked(mInfo.isInvertAngularVelocity());

        builder.setTitle(R.string.add_edit_robot)
                .setView(v)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String name = mNameEditTextView.getText().toString().trim();
                        String masterUri = mMasterUriEditTextView.getText().toString().trim();
                        String joystickTopic = mJoystickTopicEditTextView.getText().toString().trim();
                        String laserScanTopic = mLaserScanTopicEditTextView.getText().toString().trim();
                        String cameraTopic = mCameraTopicEditTextView.getText().toString().trim();
                        String navsatTopic = mNavSatTopicEditTextView.getText().toString().trim();
                        String odometryTopic = mOdometryTopicEditTextView.getText().toString().trim();
                        String poseTopic = mPoseTopicEditTextView.getText().toString().trim();
                        boolean reverseLaserScan = mReverseLaserScanCheckBox.isChecked();
                        boolean invertX = mInvertXAxisCheckBox.isChecked();
                        boolean invertY = mInvertYAxisCheckBox.isChecked();
                        boolean invertAngVel = mInvertAngularVelocityCheckBox.isChecked();

                        if (masterUri.equals("")) {
                            Toast.makeText(getActivity(), "Master URI required", Toast.LENGTH_SHORT).show();
                        } else if (joystickTopic.equals("") || laserScanTopic.equals("") || cameraTopic.equals("")
                                || navsatTopic.equals("") || odometryTopic.equals("") || poseTopic.equals("")) {
                            Toast.makeText(getActivity(), "All topic names are required", Toast.LENGTH_SHORT).show();
                        } else if (!name.equals("")) {
                            mListener.onAddEditDialogPositiveClick(new RobotInfo(mInfo.getId(), name,
                                    masterUri, joystickTopic, laserScanTopic, cameraTopic, navsatTopic,
                                    odometryTopic, poseTopic, reverseLaserScan, invertX, invertY, invertAngVel), mPosition);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Robot name required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onAddEditDialogNegativeClick(AddEditRobotDialogFragment.this);
                dialog.cancel();
            }
        });

        return builder.create();
    }

    public interface DialogListener {
        void onAddEditDialogPositiveClick(RobotInfo info, int position);

        void onAddEditDialogNegativeClick(DialogFragment dialog);
    }

}
