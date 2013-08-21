package com.iliakplv.notes.gui.main;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.db.NotesDatabaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NotesListFragment extends ListFragment {

	private static final String TITLE = "title";
	private static final String SUBTITLE = "subtitle";

	List<AbstractNote> notes;
	List<Map<String, String>> listData;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		fillListData();
		SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), listData, R.layout.note_list_item, new String[]{TITLE, SUBTITLE}, new int[] {R.id.title, R.id.subtitle});
		setListAdapter(simpleAdapter);
		getListView().setOnItemLongClickListener(new NoteLongClickListener());
	}

	private void fillListData() {
		NotesDatabaseAdapter dbAdapter = new NotesDatabaseAdapter(getActivity());
		dbAdapter.open();
		notes = dbAdapter.getAllNotes();
		dbAdapter.close();

		listData = new ArrayList<Map<String, String>>();
		for (AbstractNote note : notes) {
			final Map<String, String> map = new HashMap<String, String>();
			map.put(TITLE, note.getTitle());
			map.put(SUBTITLE, note.getBody());
			listData.add(map);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		((MainActivity) getActivity()).showDetailsFragment(notes.get(position));
	}

	private class NoteLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

			NotesDatabaseAdapter dbAdapter = new NotesDatabaseAdapter(getActivity());
			dbAdapter.open();
			dbAdapter.deleteNote(notes.get(i).getId());
			dbAdapter.close();

			fillListData();
			((SimpleAdapter) getListAdapter()).notifyDataSetChanged();

			return true;
		}
	}


}
