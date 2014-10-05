package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.iliakplv.notes.R;

public class VoiceSearchInstallDialog  extends DialogFragment {
	private static final String FRAGMENT_TAG = VoiceSearchInstallDialog.class.getSimpleName();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.action_bar_speak)
				.setMessage(R.string.no_voice_search_dialog_text)
				.setNegativeButton(R.string.common_cancel, null)
				.setPositiveButton(R.string.no_voice_search_dialog_install, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.voicesearch"));
						startActivity(browserIntent);
					}
				}).create();
	}

	public static void show(FragmentManager fm) {
		final VoiceSearchInstallDialog dialog = new VoiceSearchInstallDialog();
		dialog.show(fm, FRAGMENT_TAG);
	}
}