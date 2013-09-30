package com.iliakplv.notes.gui.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NoteDetailsFragment extends Fragment {

	 // TODO rework!

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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_NOTE_ID, noteId);
	}

	private void saveNote() {
		if (noteEntry != null) {
			dbFacade.updateNote(noteEntry.getId(), new TextNote(title.getText().toString(), body.getText().toString()));
		}
	}
}
