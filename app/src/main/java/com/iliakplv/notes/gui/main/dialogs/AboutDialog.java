package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.iliakplv.notes.R;

public class AboutDialog extends DialogFragment {
	private static final String FRAGMENT_TAG = AboutDialog.class.getSimpleName();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.action_about)
				.setMessage("to be done")
				.setNegativeButton(R.string.common_close, null)
				.create();
	}

	public static void show(FragmentManager fm) {
		final AboutDialog dialog = new AboutDialog();
		dialog.show(fm, FRAGMENT_TAG);
	}
}
