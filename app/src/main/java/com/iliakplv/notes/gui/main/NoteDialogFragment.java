package com.iliakplv.notes.gui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.iliakplv.notes.R;

/**
 * Author: Ilya Kopylov
 * Date:  16.09.2013
 */
public class NoteDialogFragment extends DialogFragment implements View.OnClickListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_dialog, null);
		return view;
	}

	@Override
	public void onClick(View v) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}