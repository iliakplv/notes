package com.iliakplv.notes.gui.settings;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.iliakplv.notes.R;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final int PREFERENCES = R.xml.preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        addPreferencesFromResource(PREFERENCES);
        PreferenceManager.setDefaultValues(this, PREFERENCES, false);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    public static void start(Activity parent) {
        Intent intent = new Intent(parent, SettingsActivity.class);
        parent.startActivity(intent);
    }
}
