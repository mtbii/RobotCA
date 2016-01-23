package com.robotca.ControlApp.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.robotca.ControlApp.Core.RobotInfo;
import com.robotca.ControlApp.R;

/**
 * Created by Michael Brunson on 1/23/16.
 */
public class AddEditRobotDialogFragment extends DialogFragment {
    public static final String ROBOT_NAME_KEY = "ROBOT_NAME_KEY";
    public static final String MASTER_URI_KEY = "MASTER_URI_KEY";
    public static final String POSITION_KEY = "POSITION_KEY";

    RobotInfo info = new RobotInfo();

    // Use this instance of the interface to deliver action events
    private DialogListener mListener;
    private EditText mNameEditTextView;
    private EditText mMasterUriEditTextView;
    private int mPosition = -1;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if(args != null) {
            mPosition = args.getInt(POSITION_KEY, -1);
            info.setName(args.getString(ROBOT_NAME_KEY, ""));
            info.setMasterUri(args.getString(MASTER_URI_KEY, ""));
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_robot, null);
        mNameEditTextView = (EditText) v.findViewById(R.id.robot_name_edit_text);
        mMasterUriEditTextView = (EditText) v.findViewById(R.id.master_uri_edit_view);

        mNameEditTextView.setText(info.getName());
        mMasterUriEditTextView.setText(info.getMasterUri());

        builder.setTitle(R.string.add_edit_robot)
                .setView(v)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String name = mNameEditTextView.getText().toString().trim();
                        String masterUri = mMasterUriEditTextView.getText().toString().trim();

                        if (masterUri == null || masterUri.equals("")) {
                            Toast.makeText(getActivity(), "Master URI required", Toast.LENGTH_SHORT).show();
                        }
                        else if (name != null && name != "") {
                            mListener.onAddEditDialogPositiveClick(new RobotInfo(name, masterUri), mPosition);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Robot name required", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onAddEditDialogNegativeClick(AddEditRobotDialogFragment.this);
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    public interface DialogListener {
        public void onAddEditDialogPositiveClick(RobotInfo info, int position);
        public void onAddEditDialogNegativeClick(DialogFragment dialog);
    }

}
