package com.iliakplv.notes.gui.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.iliakplv.notes.R;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
