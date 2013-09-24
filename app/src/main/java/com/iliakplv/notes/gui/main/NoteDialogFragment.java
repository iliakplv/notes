package com.iliakplv.notes.gui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

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
		final NoteTextWatcher watcher = new NoteTextWatcher();
		title = (EditText) view.findViewById(R.id.note_dialog_title);
		title.addTextChangedListener(watcher);
		body = (EditText) view.findViewById(R.id.note_dialog_body);
		body.addTextChangedListener(watcher);
		saveButton = (Button) view.findViewById(R.id.note_dialog_save);
		saveButton.setOnClickListener(this);
		view.findViewById(R.id.note_dialog_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View button) {
		switch (button.getId()) {
			case R.id.note_dialog_cancel:
				dismiss();
				break;
			case R.id.note_dialog_save:
				final TextNote newNote = new TextNote(title.getText().toString(), body.getText().toString());
				new Thread(new Runnable() {
					@Override
					public void run() {
						NotesDatabaseFacade.insertNote(newNote);
					}
				}).start();
				dismiss();
				break;
		}
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private class NoteTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

		@Override
		public void afterTextChanged(Editable s) {
			final boolean ableToSaveNote =
					!(StringUtils.isBlank(title.getText().toString()) &&
							StringUtils.isBlank(body.getText().toString()));
			saveButton.setEnabled(ableToSaveNote);
		}
	}
}