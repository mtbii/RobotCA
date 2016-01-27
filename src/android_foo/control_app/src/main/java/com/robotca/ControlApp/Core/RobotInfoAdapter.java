package com.robotca.ControlApp.Core;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Dialogs.ConfirmDeleteDialogFragment;
import com.robotca.ControlApp.Dialogs.AddEditRobotDialogFragment;
import com.robotca.ControlApp.R;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotInfoAdapter extends RecyclerView.Adapter<RobotInfoAdapter.ViewHolder> {
    private List<RobotInfo> mDataset;
    private AppCompatActivity activity;

    public RobotInfoAdapter(AppCompatActivity activity, List<RobotInfo> dataset) {
        this.activity = activity;
        mDataset = dataset;
    }

    public RobotInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.robot_info_view, parent, false);

        return new ViewHolder(v);
        //return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mRobotNameTextView.setText(mDataset.get(position).getName());
        holder.mMasterUriTextView.setText(mDataset.get(position).getUri().toString());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mRobotNameTextView;
        public TextView mMasterUriTextView;
        private ImageButton mEditButton;
        private ImageButton mDeleteButton;

        public ViewHolder(View v) {
            super(v);
            v.setClickable(true);
            v.setOnClickListener(this);
            mRobotNameTextView = (TextView) v.findViewById(R.id.robot_name_text_view);
            mMasterUriTextView = (TextView) v.findViewById(R.id.master_uri_text_view);

            mEditButton = (ImageButton) v.findViewById(R.id.robot_edit_button);
            mEditButton.setOnClickListener(this);

            mDeleteButton = (ImageButton) v.findViewById(R.id.robot_delete_button);
            mDeleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Bundle bundle;
            final RobotInfo info = mDataset.get(position);

            switch (v.getId()) {
                case R.id.robot_edit_button:
                    AddEditRobotDialogFragment editRobotDialogFragment = new AddEditRobotDialogFragment();
                    bundle = new Bundle();

                    bundle.putInt(AddEditRobotDialogFragment.POSITION_KEY, position);
                    bundle.putString(AddEditRobotDialogFragment.ROBOT_NAME_KEY, info.getName());
                    bundle.putString(AddEditRobotDialogFragment.MASTER_URI_KEY, info.getMasterUri());
                    editRobotDialogFragment.setArguments(bundle);

                    editRobotDialogFragment.show(activity.getSupportFragmentManager(), "editbuttondialog");
                    break;

                case R.id.robot_delete_button:
                    ConfirmDeleteDialogFragment confirmDeleteDialogFragment = new ConfirmDeleteDialogFragment();
                    bundle = new Bundle();

                    bundle.putInt(ConfirmDeleteDialogFragment.POSITION_KEY, position);
                    bundle.putString(ConfirmDeleteDialogFragment.NAME_KEY, info.getName());
                    confirmDeleteDialogFragment.setArguments(bundle);

                    confirmDeleteDialogFragment.show(activity.getSupportFragmentManager(), "deletebuttondialog");
                    break;

                default:

                    final ProgressDialog mProgressDialog = ProgressDialog.show(activity, "Connecting", "Connecting to " + info.getName() + " (" + info.getUri().toString() + ")", true, false);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

//                                if (!InetAddress.getByName(info.getUri().getHost()).isReachable(10000)) {
//                                    throw new Exception("Cannot connect to ROS. Please make sure ROS is running and that the Master URI is correct.");
//                                }

                                if(!isPortOpen(info.getUri().getHost(), info.getUri().getPort(), 10000)){
                                    throw new Exception("Cannot connect to ROS. Please make sure ROS is running and that the Master URI is correct.");
                                }

                                final Intent intent = new Intent(activity, ControlApp.class);

                                // !!!---- EVIL HACK with STATIC VARIABLE ----!! //
                                // Should not be doing this but there is no other way that I can see -Michael
                                ControlApp.ROBOT_INFO = info;

                                mProgressDialog.dismiss();

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.startActivity(intent);
                                    }
                                });
                            }
                            catch (final NetworkOnMainThreadException e){
                                mProgressDialog.dismiss();

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "Invalid Master URI", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            catch (final Exception e) {
                                mProgressDialog.dismiss();

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }).start();

                    break;
            }
        }
    }

    public static boolean isPortOpen(final String ip, final int port, final int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }

        catch(ConnectException ce){
            ce.printStackTrace();
            return false;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}