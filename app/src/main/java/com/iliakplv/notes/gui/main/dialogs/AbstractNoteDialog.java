package com.iliakplv.notes.gui.main.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

public abstract class AbstractNoteDialog extends DialogFragment {

	public static final String EXTRA_NOTE_ID = "note_id";

	protected NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();

	protected Activity activity;
	protected int noteId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = getArguments();
		if (args == null || !args.containsKey(EXTRA_NOTE_ID)) {
			throw new RuntimeException("Note id required");
		}
		noteId = args.getInt(EXTRA_NOTE_ID);
	}

	protected static Bundle createArgumentsBundle(int noteId) {
		final Bundle args = new Bundle();
		args.putInt(EXTRA_NOTE_ID, noteId);
		return args;
	}
}
