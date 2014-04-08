package com.iliakplv.notes.gui.main;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.Storage;
import com.iliakplv.notes.utils.AppLog;
import com.iliakplv.notes.utils.StringUtils;

public class NoteDetailsFragment extends Fragment {

	private static final String LOG_TAG = NoteDetailsFragment.class.getSimpleName();

	private final static String PREFS_KEY_LINKIFY = "linkify_note_text";
	private final static int LINKIFY_MASK = Linkify.WEB_URLS |
			Linkify.EMAIL_ADDRESSES |
			Linkify.PHONE_NUMBERS;

	final static String ARG_NOTE_ID = "note_id";

	private int noteId = MainActivity.NEW_NOTE;
	private final NotesStorage storage = Storage.getStorage();

	private EditText title;
	private EditText body;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_details, container, false);
		title = (EditText) view.findViewById(R.id.note_title);
		body = (EditText) view.findViewById(R.id.note_body);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			noteId = savedInstanceState.getInt(ARG_NOTE_ID);
		}
		AppLog.d(LOG_TAG, "onCreate() call. Note id = " + noteId);
	}

	@Override
	public void onStart() {
		super.onStart();
		final Bundle args = getArguments();

		if (noteId == MainActivity.NEW_NOTE) {
			// Not restored from savedInstanceState in onCreate()
			if (args != null) {
				noteId = args.getInt(ARG_NOTE_ID);
			}
		}
		AppLog.d(LOG_TAG, "onStart() call. Note id = " + noteId);

		updateNoteDetailsView();
	}


	public void updateNoteDetailsView() {
		final boolean gotNoteToShow = noteId > 0 && storage.getNote(noteId) != null;
		if (gotNoteToShow) {
			final AbstractNote note = storage.getNote(noteId);
			title.setText(note.getTitle());
			body.setText(note.getBody());

			final SharedPreferences sp =
					PreferenceManager.getDefaultSharedPreferences(getActivity());
			if (sp.getBoolean(PREFS_KEY_LINKIFY, false)) {
				Linkify.addLinks(body, LINKIFY_MASK);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		trySaveCurrentNote();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		AppLog.d(LOG_TAG, "onSaveInstanceState() call. Note id = " + noteId);
		outState.putInt(ARG_NOTE_ID, noteId);
	}

	private void trySaveCurrentNote() {
		final String LOG_PREFIX = "trySaveCurrentNote(): ";

		final String newTitle = title.getText().toString();
		final String newBody = body.getText().toString();

		if (noteId == MainActivity.NEW_NOTE) {
			// insert new note if not empty
			if (!StringUtils.isNullOrEmpty(newTitle) ||
					!StringUtils.isNullOrEmpty(newBody)) {
				// (perform on UI thread)
				noteId = storage.insertNote(new TextNote(newTitle, newBody));
				AppLog.d(LOG_TAG, LOG_PREFIX + "New note saved. Id = " + noteId);
			} else {
				AppLog.d(LOG_TAG, LOG_PREFIX + "New note empty. Not saved.");
			}
		} else {
			final AbstractNote note = storage.getNote(noteId);
			if (note != null) {
				final AbstractNote currentNote = note;

				// update current note if changed
				if (!StringUtils.equals(currentNote.getBody(), newBody) ||
						!StringUtils.equals(currentNote.getTitle(), newTitle)) {
					currentNote.setTitle(newTitle);
					currentNote.setBody(newBody);
					currentNote.updateChangeTime();
					final boolean updated = storage.updateNote(noteId, currentNote);
					AppLog.d(LOG_TAG, LOG_PREFIX + "Note data changed. Database "
							+ (updated ? "" : "NOT (!) ") + "updated.");
				} else {
					AppLog.d(LOG_TAG, LOG_PREFIX + "Note data unchanged.");
				}
			} else {
				AppLog.d(LOG_TAG, LOG_PREFIX + "Note entry is null (!)");
			}
		}
	}

}
