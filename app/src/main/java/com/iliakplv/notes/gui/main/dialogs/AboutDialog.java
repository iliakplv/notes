package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.iliakplv.notes.R;
import com.iliakplv.notes.utils.Utils;

public class AboutDialog extends DialogFragment {
	private static final String FRAGMENT_TAG = AboutDialog.class.getSimpleName();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.app_name)
				.setView(createView())
				.setNegativeButton(R.string.common_close, null)
				.create();
	}

	public View createView() {
		final Context context = getActivity();
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.about_dialog, null);

		// version
		final String appVersion = Utils.getVersionName();
		((TextView) view.findViewById(R.id.version))
				.setText(context.getString(R.string.about_version, appVersion));

		// google play
		view.findViewById(R.id.google_play).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openGooglePlay();
			}
		});


		// email
		view.findViewById(R.id.contact).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendFeedback();
			}
		});

		// sources
		final TextView sourcesInfo = (TextView) view.findViewById(R.id.sources);
		Linkify.addLinks(sourcesInfo, Linkify.WEB_URLS);

		return view;
	}

	private void openGooglePlay() {
		final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
		startActivity(browserIntent);
	}

	private void sendFeedback() {
		final Context context = getActivity();
		final Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
				Uri.fromParts("mailto", "iliakplv@gmail.com", null));
		final String subject = context.getString(R.string.app_name) + " feedback";
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, getTextForFeedback(context));
		startActivity(Intent.createChooser(emailIntent, context.getString(R.string.about_contact)));
	}

	private String getTextForFeedback(Context context) {
		return context.getString(R.string.about_contact_do_not_remove) + Utils.getDeviceInformation();
	}

	public static void show(FragmentManager fm) {
		final AboutDialog dialog = new AboutDialog();
		dialog.show(fm, FRAGMENT_TAG);
	}
}
