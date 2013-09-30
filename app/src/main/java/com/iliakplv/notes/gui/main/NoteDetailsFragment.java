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

	 // TODO rework!

	final static String ARG_NOTE_ID = "note_id";
	private int noteId = 0;

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();

	private View rootLayout;
	private View titleSeparator;
	private TextView title;
	private TextView body;
	private TextView createdDate;
	private TextView modifiedDate;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_details, container, false);
		rootLayout = view.findViewById(R.id.details_fragment_layout);
		titleSeparator = view.findViewById(R.id.note_title_separator);
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
			rootLayout.setVisibility(View.GONE);
			return;
		}
		rootLayout.setVisibility(View.VISIBLE);

		final AbstractNote note = entry.getNote();
		final String noteTitle = note.getTitle();
		final String noteBody = note.getBody();
		final boolean hasTitle = !StringUtils.isNullOrEmpty(noteTitle);
		final boolean hasBody = !StringUtils.isNullOrEmpty(noteBody);

		title.setText(noteTitle);
		// if note has only title or only body show it in body text view
		setShowTitle(hasTitle && hasBody);
		body.setText(hasBody ? noteBody : noteTitle);

		createdDate.setText(getString(R.string.note_details_created, note.getCreateTime().toString()));
		modifiedDate.setText(getString(R.string.note_details_modified, note.getChangeTime().toString()));
	}

	private void setShowTitle(boolean show) {
		final int contentsVisibility = show ? View.VISIBLE : View.GONE;
		title.setVisibility(contentsVisibility);
		titleSeparator.setVisibility(contentsVisibility);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_NOTE_ID, noteId);
	}
}
