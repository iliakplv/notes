package com.iliakplv.notes.gui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.iliakplv.notes.R;


public class NoteDialogFragment extends DialogFragment implements View.OnClickListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.note_dialog_new_note);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_dialog, container, false);
		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO
	}
}