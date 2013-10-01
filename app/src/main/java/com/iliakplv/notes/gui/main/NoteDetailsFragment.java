package com.iliakplv.notes.gui.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.iliakplv.notes.BuildConfig;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NoteDetailsFragment extends Fragment {

	private static final String LOG_TAG = NoteDetailsFragment.class.getSimpleName();

	final static String ARG_NOTE_ID = "note_id";

	private int noteId = MainActivity.NO_DETAILS;
	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
	private NotesDatabaseEntry noteEntry;

	private View rootLayout;
	private EditText title;
	private EditText body;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_details, container, false);
		rootLayout = view.findViewById(R.id.details_scroll_view);
		title = (EditText) view.findViewById(R.id.note_title);
		body = (EditText) view.findViewById(R.id.note_body);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		final Bundle args = getArguments();
		if (args != null) {
			noteId = args.getInt(ARG_NOTE_ID);
		}
		updateNoteDetailsView(noteId);
	}


	public void updateNoteDetailsView(int noteId) {
		// try save changes on previously shown note
		trySaveCurrentNote();

		// show new note
		this.noteId = noteId;
		noteEntry = noteId > 0 ? dbFacade.getNote(noteId) : null;
		if (noteEntry == null) {
			rootLayout.setVisibility(View.GONE);
			return;
		}

		rootLayout.setVisibility(View.VISIBLE);
		final AbstractNote note = noteEntry.getNote();
		title.setText(note. getTitle());
		body.setText(note.getBody());
	}

	@Override
	public void onPause() {
		super.onPause();
		// try save changes
		trySaveCurrentNote();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_NOTE_ID, noteId);
	}

	private void trySaveCurrentNote() {
		final String LOG_PREFIX = "trySaveCurrentNote(): ";
		if (BuildConfig.DEBUG) {
			Log.d(LOG_TAG, LOG_PREFIX + "call");
		}
		if (noteEntry != null) {
			final String newTitle = title.getText().toString();
			final String newBody = body.getText().toString();
			final AbstractNote currentNote = noteEntry.getNote();
			if (!StringUtils.equals(currentNote.getBody(), newBody) ||
					!StringUtils.equals(currentNote.getTitle(), newTitle)) {
				currentNote.setTitle(newTitle);
				currentNote.setBody(newBody);
				currentNote.updateChangeTime();
				final boolean updated = dbFacade.updateNote(noteEntry.getId(), currentNote);
				if (BuildConfig.DEBUG) {
					Log.d(LOG_TAG, LOG_PREFIX + "Note data changed. Database " + (updated ? "" : "NOT ") + "updated");
				}
			} else {
				if (BuildConfig.DEBUG) {
					Log.d(LOG_TAG, LOG_PREFIX + "Note data unchanged. End");
				}
			}
		} else {
			if (BuildConfig.DEBUG) {
				Log.d(LOG_TAG, LOG_PREFIX + "Note entry is null. End");
			}
		}
	}
}
