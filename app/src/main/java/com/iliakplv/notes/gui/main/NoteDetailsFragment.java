package com.iliakplv.notes.gui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.main.dialogs.NoteLabelsDialog;
import com.iliakplv.notes.gui.main.dialogs.SimpleItemDialog;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.Storage;
import com.iliakplv.notes.utils.AppLog;
import com.iliakplv.notes.utils.StringUtils;

import java.io.Serializable;

public class NoteDetailsFragment extends Fragment {

	private static final String LOG_TAG = NoteDetailsFragment.class.getSimpleName();

	private final static String PREFS_KEY_LINKIFY = "linkify_note_text";
	private final static int LINKIFY_MASK = Linkify.WEB_URLS |
			Linkify.EMAIL_ADDRESSES |
			Linkify.PHONE_NUMBERS;

	final static String ARG_NOTE_ID = "note_id";

	private Serializable noteId = MainActivity.NEW_NOTE;
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
			noteId = savedInstanceState.getSerializable(ARG_NOTE_ID);
		}
		AppLog.d(LOG_TAG, "onCreate() call. Note id = " + noteId);
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		final Bundle args = getArguments();

		if (MainActivity.NEW_NOTE.equals(noteId)) {
			// Not restored from savedInstanceState in onCreate()
			if (args != null) {
				noteId = args.getSerializable(ARG_NOTE_ID);
			}
		}
		AppLog.d(LOG_TAG, "onStart() call. Note id = " + noteId);

		updateNoteDetailsView();
	}


	public void updateNoteDetailsView() {
		final boolean gotNoteToShow = !MainActivity.NEW_NOTE.equals(noteId) && storage.getNote(noteId) != null;
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
		trySaveCurrentNote(false);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		AppLog.d(LOG_TAG, "onSaveInstanceState() call. Note id = " + noteId);
		outState.putSerializable(ARG_NOTE_ID, noteId);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		switch (itemId) {
			case R.id.action_share:
				shareNote();
				return true;

			case R.id.action_labels:
				showLabelsDialog();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void shareNote() {
		NotesUtils.shareNote(getActivity(),
				title.getText().toString(),
				body.getText().toString());
	}

	private void showLabelsDialog() {
		if (trySaveCurrentNote(true)) {
			Toast.makeText(getActivity(), "[note saved]", Toast.LENGTH_SHORT).show();
		}

		final boolean noLabelsCreated = storage.getAllLabels().isEmpty();
		final FragmentManager fragmentManager = getActivity().getFragmentManager();
		if (noLabelsCreated) {
			SimpleItemDialog.show(SimpleItemDialog.DialogType.NoteNoLabels, noteId, fragmentManager);
		} else {
			NoteLabelsDialog.show(fragmentManager, noteId);
		}
		// TODO delete empty note on Back press
	}

	// returns true if new note created
	private boolean trySaveCurrentNote(boolean saveIfEmpty) {
		final String LOG_PREFIX = "trySaveCurrentNote(): ";

		final String newTitle = title.getText().toString();
		final String newBody = body.getText().toString();

		boolean newNoteCreated = false;
		if (MainActivity.NEW_NOTE.equals(noteId)) {
			if (saveIfEmpty ||
					!StringUtils.isNullOrEmpty(newTitle) ||
					!StringUtils.isNullOrEmpty(newBody)) {
				// (perform on UI thread)
				noteId = storage.insertNote(new TextNote(newTitle, newBody));
				newNoteCreated = true;
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
		return newNoteCreated;
	}
}
