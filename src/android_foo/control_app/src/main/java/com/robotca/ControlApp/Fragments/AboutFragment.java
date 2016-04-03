package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.robotca.ControlApp.Core.Utils;
import com.robotca.ControlApp.R;

/**
 * Simple AboutFragment showing about info for the app.
 *
 * Created by Kenneth Spear on 3/15/16.
 */
public class AboutFragment extends Fragment {

    /**
     * Default Constructor.
     */
    public AboutFragment() {}

    /**
     * Called when the activity is created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, null);

        WebView webView = (WebView) view.findViewById(R.id.abouttxt);
        webView.loadData(Utils.readText(getActivity(), R.raw.about), "text/html", null);

        return view;
    }
}

