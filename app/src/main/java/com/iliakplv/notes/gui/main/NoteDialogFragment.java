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
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

/**
 * Author: Ilya Kopylov
 * Date:  16.09.2013
 */
public class NoteDialogFragment extends DialogFragment implements View.OnClickListener {

	public static final String ARG_EDIT_MODE = "edit_mode";
	public static final String ARG_NOTE_ID = "note_id";

	private NotesDatabaseEntry noteEntry;
	private boolean editMode;

	private EditText title;
	private EditText body;
	private Button saveButton;


	public NoteDialogFragment() {
		super();
	}

	private Bundle getActualArguments(Bundle savedInstanceState) {
		return savedInstanceState != null ? savedInstanceState : getArguments();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		editMode = getActualArguments(savedInstanceState).getBoolean(ARG_EDIT_MODE);
		dialog.setTitle(editMode ? R.string.note_dialog_edit_note : R.string.note_dialog_new_note);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_dialog, container, false);

		Bundle args = getActualArguments(savedInstanceState);
		editMode = args.getBoolean(ARG_EDIT_MODE);
		if (editMode) {
			noteEntry = NotesDatabaseFacade.getInstance().getNote(args.getInt(ARG_NOTE_ID));
		}
		initControls(view);
		return view;
	}

	private void initControls(View view) {
		title = (EditText) view.findViewById(R.id.note_dialog_title);
		body = (EditText) view.findViewById(R.id.note_dialog_body);
		saveButton = (Button) view.findViewById(R.id.note_dialog_save);
		saveButton.setOnClickListener(this);
		if (editMode) {
			title.setText(noteEntry.getNote().getTitle());
			body.setText(noteEntry.getNote().getBody());
			final NoteTextWatcher watcher = new NoteTextWatcher();
			title.addTextChangedListener(watcher);
			body.addTextChangedListener(watcher);
			saveButton.setEnabled(false);
		}
		view.findViewById(R.id.note_dialog_cancel).setOnClickListener(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(ARG_EDIT_MODE, editMode);
		if (editMode) {
			outState.putInt(ARG_NOTE_ID, noteEntry.getId());
		}
	}

	@Override
	public void onClick(View button) {
		switch (button.getId()) {
			case R.id.note_dialog_cancel:
				dismiss();
				break;
			case R.id.note_dialog_save:
				final TextNote newNote = getNoteToSave();
				new Thread(new Runnable() {
					@Override
					public void run() {
						final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
						if (editMode) {
							dbFacade.updateNote(noteEntry.getId(), newNote);
						} else {
							dbFacade.insertNote(newNote);
						}
					}
				}).start();
				dismiss();
				break;
		}
	}

	private TextNote getNoteToSave() {
		final String newTitle = title.getText().toString();
		final String newBody = body.getText().toString();
		final TextNote note;

		if (editMode) {
			note = (TextNote) noteEntry.getNote();
			note.setTitle(newTitle);
			note.setBody(newBody);
			note.updateChangeTime();
		} else {
			note = new TextNote(newTitle, newBody);
		}

		return note;
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

			final String originalTitle = noteEntry.getNote().getTitle();
			final String originalBody = noteEntry.getNote().getBody();
			// at least one field (title or body) has been changed
			final boolean ableToSaveNote = (!StringUtils.equals(originalTitle, title.getText().toString()) ||
					!StringUtils.equals(originalBody, body.getText().toString()));

			saveButton.setEnabled(ableToSaveNote);
		}
	}
}