package com.iliakplv.notes.gui.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

	final static String ARG_NOTE_ID = "note_id";
	private int noteId = 0;

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();

	private View layout;
	private TextView title;
	private TextView body;
	private TextView createdDate;
	private TextView modifiedDate;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_details, container, false);
		layout = view.findViewById(R.id.details_fragment_layout);
		title = (TextView) view.findViewById(R.id.note_title);
		body = (TextView) view.findViewById(R.id.note_body);
		createdDate = (TextView) view.findViewById(R.id.note_create_date);
		modifiedDate = (TextView) view.findViewById(R.id.note_modify_date);
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
		final NotesDatabaseEntry entry = noteId > 0 ? dbFacade.getNote(noteId) : null;
		if (entry == null) {
			layout.setVisibility(View.GONE);
			return;
		}
		layout.setVisibility(View.VISIBLE);

		final AbstractNote note = entry.getNote();
		final String noteTitle = note.getTitle();
		final String noteBody = note.getBody();
		final boolean hasTitle = !StringUtils.isNullOrEmpty(noteTitle);
		final boolean hasBody = !StringUtils.isNullOrEmpty(noteBody);

		// At least one of note's fields (title or body) will be not empty (empty note can't be stored)
		if (hasTitle && hasBody) {
			// show all
			title.setVisibility(View.VISIBLE);
			title.setText(noteTitle);
			body.setText(noteBody);
		} else if (!hasTitle && hasBody) {
			// show only body
			title.setVisibility(View.GONE);
			body.setText(noteBody);
		} else { // (hasTitle && !hasBody)
			// show only title (like it's a body)
			title.setVisibility(View.GONE);
			body.setText(noteTitle);
		}

		createdDate.setText(note.getCreateTime().toString());
		modifiedDate.setText(note.getChangeTime().toString());
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_NOTE_ID, noteId);
	}
}
