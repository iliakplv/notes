package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
public class NotesListFragment extends ListFragment implements AdapterView.OnItemLongClickListener {

	private MainActivity mainActivity;
	private NotesListAdapter listAdapter;


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listAdapter = new NotesListAdapter();
		setListAdapter(listAdapter);
		getListView().setOnItemLongClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getFragmentManager().findFragmentById(R.id.note_details_fragment) != null) { // Dual pane layout
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mainActivity.onNoteSelected(position);
		getListView().setItemChecked(position, true);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		final int noteIdToDelete = NotesDatabaseFacade.getAllNotes().get(i).getId();
		NotesDatabaseFacade.deleteNote(noteIdToDelete);
		listAdapter.notifyDataSetChanged();
		return true;
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private class NotesListAdapter extends ArrayAdapter<NotesDatabaseEntry> {

		public NotesListAdapter() {
			super(NotesListFragment.this.getActivity(), 0, NotesDatabaseFacade.getAllNotes());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(R.layout.note_list_item, parent, false);
			}

			final AbstractNote note = NotesDatabaseFacade.getAllNotes().get(position).getNote();
			final String noteOriginalTitle = note.getTitle();
			final String noteTitleInList =  StringUtils.isNullOrEmpty(noteOriginalTitle) ?
					getContext().getString(R.string.notes_list_no_title) :
					noteOriginalTitle;
			final TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(noteTitleInList);
			// TODO first line or substring of note's body
			((TextView) view.findViewById(R.id.subtitle)).setText(note.getBody());

			return view;
		}

		@Override
		public int getCount() {
			return NotesDatabaseFacade.getAllNotes().size();
		}
	}

}
