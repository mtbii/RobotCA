package com.robotca.ControlApp.Views;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * Improved EditTextPreference that shows the current value of the preference.
 * Use %s or %1$s in the summary text to replace it with the preference's current value.
 *
 * Created by Nathaniel on 4/14/16.
 */
public class BetterEditTextPreference extends EditTextPreference {

    private final String SUMMARY;

    /**
     * Creates a BetterEditTextPreference.
     * @param context The parent Context
     * @param attrs The AttributeSet
     */
    public BetterEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        SUMMARY = getSummary().toString();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        setDisplayValue(prefs.getString(getKey(), "Not Set"));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        setDisplayValue(getText());
    }

    /*
     * Sets the value to display on this Preference.
     */
    private void setDisplayValue(String str)
    {
        setSummary(String.format(SUMMARY, str));
    }
}
