package com.robotca.ControlApp.Core;

import java.net.URI;

import com.robotca.ControlApp.R;

/**
 * Created by Michael Brunson on 1/23/16.
 */
public class RobotInfo {
    private static int robotCount = 1;
    private String mName;
    private String mMasterUriString;

    public RobotInfo(){
        mName = "Robot" + robotCount++;
        mMasterUriString = "http://localhost:11311";
    }

    public RobotInfo(String mName, String mMasterUri) {
        this.mName = mName;
        this.mMasterUriString = mMasterUri;
    }

    public String getMasterUri() {
        return mMasterUriString;
    }

    public void setMasterUri(String mMasterUri) {
        this.mMasterUriString = mMasterUri;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public URI getUri(){
        return URI.create(getMasterUri());
    }
}
