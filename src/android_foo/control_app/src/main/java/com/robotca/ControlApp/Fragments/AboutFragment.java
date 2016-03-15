package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robotca.ControlApp.R;

/**
 * Created by Kenneth Spear on 3/15/16.
 */
public class AboutFragment extends Fragment {

    public AboutFragment() {

    }

    /**
     * Called when the activity is  created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, null);

        TextView aboutTxt = (TextView) view.findViewById(R.id.abouttxt);
        aboutTxt.setText(readTxt());

        return view;


}

    private String readTxt() {

        InputStream inputStream = getResources().openRawResource(R.raw.about);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        }
        catch (IOException e) {

            e.printStackTrace();
        }

        return byteArrayOutputStream.toString();

    }
}

