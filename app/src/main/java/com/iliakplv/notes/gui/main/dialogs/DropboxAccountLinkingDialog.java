package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.main.MainActivity;

public class DropboxAccountLinkingDialog  extends DialogFragment {
	private static final String FRAGMENT_TAG = DropboxAccountLinkingDialog.class.getSimpleName();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final MainActivity mainActivity = (MainActivity) getActivity();

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.dropbox_link_dialog_title)
				.setMessage(R.string.dropbox_link_dialog_message)
				.setNegativeButton(R.string.dropbox_link_dialog_no, null)
				.setPositiveButton(R.string.dropbox_link_dialog_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mainActivity.tryLinkDropboxAccount();
					}
				}).create();
	}

	public static void show(FragmentManager fm) {
		final DropboxAccountLinkingDialog dialog = new DropboxAccountLinkingDialog();
		dialog.show(fm, FRAGMENT_TAG);
	}
}
