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
import android.widget.Toast;

import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.Storage;
import com.iliakplv.notes.utils.AppLog;
import com.iliakplv.notes.utils.StringUtils;

import java.io.Serializable;

public class NoteDetailsFragment extends Fragment {

	public static final String TAG = NoteDetailsFragment.class.getSimpleName();
	public final static String ARG_NOTE_ID = "note_id";

	private final static String PREFS_KEY_LINKIFY = "linkify_note_text";
	private final static int LINKIFY_MASK = Linkify.WEB_URLS |
			Linkify.EMAIL_ADDRESSES |
			Linkify.PHONE_NUMBERS;

	private final NotesStorage storage = Storage.getStorage();

	private Serializable noteId;
	private boolean newNoteCreationMode;

	private EditText title;
	private EditText body;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		noteId  = getArguments().getSerializable(ARG_NOTE_ID);
		newNoteCreationMode = MainActivity.NEW_NOTE.equals(noteId);
		AppLog.d(TAG, "onCreate() call. Note id = " + noteId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_details, container, false);
		title = (EditText) view.findViewById(R.id.note_title);
		body = (EditText) view.findViewById(R.id.note_body);

		final boolean fromSaveInstanceState = savedInstanceState != null;
		if (!newNoteCreationMode && !fromSaveInstanceState) {
			final AbstractNote note = storage.getNote(noteId);
			if (note != null) {
				title.setText(note.getTitle());
				body.setText(note.getBody());
			}
		}

		linkifyNoteBody();

		return view;
	}

	private void linkifyNoteBody() {
		final SharedPreferences sp =
				PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (sp.getBoolean(PREFS_KEY_LINKIFY, false)) {
			Linkify.addLinks(body, LINKIFY_MASK);
		}
	}

	public void onBackPressed() {
		saveNote();
	}

	private void saveNote() {
		final String LOG_PREFIX = "saveNote(): ";

		final String titleText = title.getText().toString();
		final String bodyText = body.getText().toString();
		
		if (newNoteCreationMode) {
			if (StringUtils.isNullOrEmpty(titleText) && StringUtils.isNullOrEmpty(bodyText)) {
				// new note is empty
				Toast.makeText(getActivity(), R.string.empty_note_not_saved, Toast.LENGTH_SHORT).show();
			} else {
				// create new note
				final Serializable newNoteId = storage.insertNote(new TextNote(titleText, bodyText));
				AppLog.d(TAG, LOG_PREFIX + "New note saved. Id = " + newNoteId);
			}
		} else {
			final AbstractNote note = storage.getNote(noteId);
			if (note != null) {
				if (!StringUtils.equals(note.getTitle(), titleText) ||
						!StringUtils.equals(note.getBody(), bodyText)) {
					// update current note if changed
					note.setTitle(titleText);
					note.setBody(bodyText);
					note.updateChangeTime();
					final boolean updated = storage.updateNote(noteId, note);
					AppLog.d(TAG, LOG_PREFIX + "Note data changed. Database "
							+ (updated ? "" : "NOT (!!!) ") + "updated.");
				} else {
					AppLog.d(TAG, LOG_PREFIX + "Note data not changed.");
				}
			} else {
				AppLog.d(TAG, LOG_PREFIX + "Note entry is null (!!!)");
			}
		}
	}
}
