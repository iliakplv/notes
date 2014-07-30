package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.iliakplv.notes.R;

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

		String appVersion = "";
		try {
			appVersion = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.wtf("About", "Can't get app version", e);
		}

		((TextView) view.findViewById(R.id.version))
				.setText(context.getString(R.string.about_version, appVersion));

		final TextView contactInfo = (TextView) view.findViewById(R.id.contact);
		Linkify.addLinks(contactInfo, Linkify.EMAIL_ADDRESSES);

		final TextView sourcesInfo = (TextView) view.findViewById(R.id.sources);
		Linkify.addLinks(sourcesInfo, Linkify.WEB_URLS);

		return view;
	}

	public static void show(FragmentManager fm) {
		final AboutDialog dialog = new AboutDialog();
		dialog.show(fm, FRAGMENT_TAG);
	}
}
