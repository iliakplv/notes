package com.iliakplv.notes.gui.main;

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
import com.iliakplv.notes.notes.db.NotesDatabaseAdapter;

import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NotesListFragment extends ListFragment {

	List<NotesDatabaseEntry> notesEntries;
	NotesListAdapter listAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		fillListData();
		listAdapter = new NotesListAdapter();
		setListAdapter(listAdapter);
		getListView().setOnItemLongClickListener(new NoteLongClickListener());
	}

	private void fillListData() {
		NotesDatabaseAdapter dbAdapter = new NotesDatabaseAdapter(getActivity());
		dbAdapter.open();
		notesEntries = dbAdapter.getAllNotes();
		dbAdapter.close();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final AbstractNote note = notesEntries.get(position).getNote();
		((MainActivity) getActivity()).showDetailsFragment(note);
	}


	/********** Inner classes **********/

	private class NoteLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

			NotesDatabaseAdapter dbAdapter = new NotesDatabaseAdapter(getActivity());
			dbAdapter.open();
			dbAdapter.deleteNote(notesEntries.get(i).getId());
			dbAdapter.close();

			fillListData();
			listAdapter.notifyDataSetChanged();

			return true;
		}
	}

	private class NotesListAdapter extends ArrayAdapter<NotesDatabaseEntry> {

		private static final int LIST_RESOURCE = R.id.notes_list;
		private static final int LIST_ITEM_RESOURCE = R.layout.note_list_item;

		public NotesListAdapter() {
			super(NotesListFragment.this.getActivity(), LIST_RESOURCE, notesEntries);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(LIST_ITEM_RESOURCE, parent, false);
			}
			final AbstractNote note = notesEntries.get(position).getNote();
			((TextView) view.findViewById(R.id.title)).setText(note.getTitle());
			((TextView) view.findViewById(R.id.subtitle)).setText(note.getBody());
			return view;
		}

		@Override
		public int getCount() {
			return notesEntries.size();
		}
	}


}
