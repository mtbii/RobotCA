package com.robotca.ControlApp.Fragments;

/**
 * Fragment containing the FAQ tab in the Help Fragment.
 *
 * Created by kennethspear on 3/28/16.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.robotca.ControlApp.Core.Utils;
import com.robotca.ControlApp.R;

public class PageThreeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pagethree_fragment, container, false);

        WebView webView = (WebView) view.findViewById(R.id.faq_webview);
        webView.loadData(Utils.readText(getActivity(), R.raw.faq), "text/html", null);

        return view;
    }
}

