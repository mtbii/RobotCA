package com.robotca.ControlApp.Core;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.robotca.ControlApp.ControlApp;
import com.robotca.ControlApp.Dialogs.AddEditRobotDialogFragment;
import com.robotca.ControlApp.Dialogs.ConfirmDeleteDialogFragment;
import com.robotca.ControlApp.R;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Contains the list of Robot definitions users can create and then connect to.
 *
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotInfoAdapter extends RecyclerView.Adapter<RobotInfoAdapter.ViewHolder> {
    private List<RobotInfo> mDataset;

    private static AppCompatActivity activity;
    private static RobotInfo lastInfo;

    /**
     * Constructor for RobotInfoAdapter.
     * @param activity The parent activity
     * @param dataset The list of previsously created RobotInfos to load
     */
    public RobotInfoAdapter(AppCompatActivity activity, List<RobotInfo> dataset) {
        RobotInfoAdapter.activity = activity;
        mDataset = dataset;
    }

    /**
     * Creates a ViewHolder.
     * @param parent The ViewHolder's parent ViewGroup.
     * @param viewType The ViewHolder's view type
     * @return The created ViewHolder
     */
    public RobotInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.robot_info_view, parent, false);

        return new ViewHolder(v);
        //return viewHolder;
    }

    /**
     * Binds the specified ViewHolder.
     * @param holder The ViewHolder
     * @param position The ViewHolder's position in the list
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mRobotNameTextView.setText(mDataset.get(position).getName());
        holder.mMasterUriTextView.setText(mDataset.get(position).getUri().toString());
    }

    /**
     * Returns the number of ViewHolders inside this RobotInfoAdapter.
     * @return The number
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * Container for the Views inside this RobotInfoAdapter.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // TextView containing the name of the Robot
        public TextView mRobotNameTextView;
        // TextView containing the master URI of the Robot
        public TextView mMasterUriTextView;


        private ImageButton mEditButton;
        private ImageButton mDeleteButton;
        //private ImageSwitcher mImageSwitcher;
        private ImageView mImageView;

        /**
         * Creates a ViewHolder for the specified View.
         * @param v The View
         */
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

            mImageView = (ImageView) v.findViewById(R.id.robot_wifi_image);
            mImageView.setImageResource(R.drawable.wifi_0);

            Timer t = new Timer();

            t.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    try {
                        int position = getAdapterPosition();
                        final RobotInfo info = mDataset.get(position);
                        //mImageView.setLayoutParams(new ActionBar.LayoutParams(mEditButton.getHeight(), mEditButton.getHeight()));

                        if (isPortOpen(info.getUri().getHost(), info.getUri().getPort(), 10000)) {
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mImageView.setImageResource(R.drawable.wifi_4);
                                }
                            });
                        } else {
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mImageView.setImageResource(R.drawable.wifi_0);
                                }
                            });
                        }

                        Thread.sleep(10000);
                    } catch (Exception ignore) {

                    }
                }
            }, 1000, 15000);
        }

        /**
         * Handles clicks on the RobotInfoAdapter.ViewHolder.
         *
         * @param v The clicked View. Can be either the edit button, delete button, or the adapter itself,
         *          in which case a connection is initiated to the RobotInfo contained in this Adapter
         */
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Bundle bundle;
            final RobotInfo info = mDataset.get(position);

            switch (v.getId()) {
                case R.id.robot_edit_button:
                    AddEditRobotDialogFragment editRobotDialogFragment = new AddEditRobotDialogFragment();
                    bundle = new Bundle();
                    info.save(bundle);
                    bundle.putInt(AddEditRobotDialogFragment.POSITION_KEY, position);
                    editRobotDialogFragment.setArguments(bundle);

                    editRobotDialogFragment.show(activity.getSupportFragmentManager(), "editrobotialog");
                    break;

                case R.id.robot_delete_button:
                    ConfirmDeleteDialogFragment confirmDeleteDialogFragment = new ConfirmDeleteDialogFragment();
                    bundle = new Bundle();

                    bundle.putInt(ConfirmDeleteDialogFragment.POSITION_KEY, position);
                    bundle.putString(ConfirmDeleteDialogFragment.NAME_KEY, info.getName());
                    confirmDeleteDialogFragment.setArguments(bundle);

                    confirmDeleteDialogFragment.show(activity.getSupportFragmentManager(), "deleterobotdialog");
                    break;

                default:

                    FragmentManager fragmentManager = activity.getFragmentManager();
                    ConnectionProgressDialogFragment f = new ConnectionProgressDialogFragment(info);
                    f.show(fragmentManager, "ConnectionProgressDialog");

                    break;
            }
        }
    }

    /**
     * Tests whether the specified port is open within the specified timeout.
     * @param ip The IP
     * @param port The port
     * @param timeout Time to wait before assuming the port is closed
     * @return True if the port is open before the timeout ends, false otherwise
     */
    public static boolean isPortOpen(final String ip, final int port, final int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }
        catch(ConnectException ce) {
//            ce.printStackTrace();
            return false;
        }
        catch (Exception ex) {
//            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Dialog Fragment for connecting to a Robot.
     */
    public static class ConnectionProgressDialogFragment extends DialogFragment {

        private static final String TAG = "ConnectionProgress";

        private final RobotInfo INFO;
        private Thread thread;

        /**
         * Default Constructor.
         */
        public ConnectionProgressDialogFragment()
        {
            INFO = lastInfo;

            if (INFO == null)
                throw new IllegalArgumentException("info must be non null!");
        }

        /**
         * Creates a ConnectionProgressDialogFragment for the specified RobotInfo.
         * @param info The RobotInfo with which to connect
         */
        @SuppressLint("ValidFragment")
        public ConnectionProgressDialogFragment(RobotInfo info)
        {
            INFO = info;
            lastInfo = info;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final ProgressDialog progressDialog
                    = ProgressDialog.show(activity, "Connecting", "Connecting to "
                    + INFO.getName() + " (" + INFO.getUri().toString() + ")", true, false);

            run();

            return progressDialog;
        }

        @Override
        public void onDestroy()
        {
            thread.interrupt();

            super.onDestroy();
        }

        /*
         * Starts the connection process.
         */
        private void run()
        {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(!isPortOpen(INFO.getUri().getHost(), INFO.getUri().getPort(), 10000)){
                            throw new Exception("Cannot connect to ROS. Please make sure ROS is running and that the Master URI is correct.");
                        }

                        final Intent intent = new Intent(activity, ControlApp.class);

                        // !!!---- EVIL USE OF STATIC VARIABLE ----!! //
                        // Should not be doing this but there is no other way that I can see -Michael
                        ControlApp.ROBOT_INFO = INFO;

                        dismiss();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.startActivity(intent);
                            }
                        });
                    }
                    catch (final NetworkOnMainThreadException e){
                        dismiss();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, "Invalid Master URI", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore
                        Log.d(TAG, "interrupted");
                    }
                    catch (final Exception e) {

                        if (ConnectionProgressDialogFragment.this.getFragmentManager() != null)
                            dismiss();

                        activity.runOnUiThread(new Runnable() {
                                @Override
            public void run() {
                            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        });
                    }
                }
            });

            thread.start();
        }

    }
}