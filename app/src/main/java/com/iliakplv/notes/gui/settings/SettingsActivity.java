package com.iliakplv.notes.gui.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.iliakplv.notes.R;

public class SettingsActivity extends PreferenceActivity {

	private static final int PREFERENCES = R.xml.preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//noinspection deprecation
		addPreferencesFromResource(PREFERENCES);
		PreferenceManager.setDefaultValues(this, PREFERENCES, false);
	}
}
