package com.iliakplv.notes.gui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.iliakplv.notes.R;

/**
 * Author: Ilya Kopylov
 * Date:  16.09.2013
 */
public class NoteDialogFragment extends DialogFragment implements View.OnClickListener {

	private EditText title;
	private EditText body;
	private Button saveButton;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.note_dialog_new_note);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_dialog, container, false);
		initControls(view);
		return view;
	}

	private void initControls(View view) {
		title = (EditText) view.findViewById(R.id.note_dialog_title);
		body = (EditText) view.findViewById(R.id.note_dialog_body);
		view.findViewById(R.id.note_dialog_cancel).setOnClickListener(this);
		saveButton = (Button) view.findViewById(R.id.note_dialog_save);
		saveButton.setOnClickListener(this);
		saveButton.setEnabled(false);
	}

	@Override
	public void onClick(View button) {
		switch (button.getId()) {
			case R.id.note_dialog_cancel:
				dismiss();
				break;
		}
	}
}