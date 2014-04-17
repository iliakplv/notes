package com.iliakplv.notes.gui.main;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
		trySaveCurrentNote();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		AppLog.d(LOG_TAG, "onSaveInstanceState() call. Note id = " + noteId);
		outState.putSerializable(ARG_NOTE_ID, noteId);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// TODO [low] implement showing NoteLabelsDialog

		if (item.getItemId() == R.id.action_share) {
			shareNote();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void shareNote() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
		intent.putExtra(Intent.EXTRA_TEXT, body.getText().toString());
		startActivity(Intent.createChooser(intent, getString(R.string.action_bar_share_title)));
	}

	private void trySaveCurrentNote() {
		final String LOG_PREFIX = "trySaveCurrentNote(): ";

		final String newTitle = title.getText().toString();
		final String newBody = body.getText().toString();

		if (MainActivity.NEW_NOTE.equals(noteId)) {
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
