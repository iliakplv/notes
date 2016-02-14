package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.iliakplv.notes.R;

public class DropboxAnnouncementDialog extends DialogFragment {
	private static final String FRAGMENT_TAG = DropboxAnnouncementDialog.class.getSimpleName();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setMessage(R.string.db_announcement_text)
				.setNegativeButton(R.string.common_ok, null)
				.create();
	}

	public static void show(FragmentManager fm) {
		final DropboxAnnouncementDialog dialog = new DropboxAnnouncementDialog();
		dialog.show(fm, FRAGMENT_TAG);
	}
}
