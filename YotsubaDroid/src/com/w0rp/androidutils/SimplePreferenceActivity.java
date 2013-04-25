package com.w0rp.androidutils;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public abstract class SimplePreferenceActivity extends PreferenceActivity {
    @SuppressLint("ValidFragment")
    private class Frag extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(getPreferenceResource());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment frag = new Frag();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
            .replace(android.R.id.content, frag).commit();
    }

    public abstract int getPreferenceResource();
}
