package com.iliakplv.notes.gui.main;

import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.iliakplv.notes.R;
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

	List<String> titles;
	List<String> subtitles;
	List<Long> createDates;
	List<Long> changeDates;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		titles = new ArrayList<String>();
		subtitles = new ArrayList<String>();
		createDates = new ArrayList<Long>();
		changeDates = new ArrayList<Long>();

		NotesDatabaseAdapter dbAdapter = new NotesDatabaseAdapter(getActivity());
		dbAdapter.open();
		Cursor cursor = dbAdapter.getAllNotesCursor();
		if (cursor.moveToFirst()) {
			do {
				titles.add(cursor.getString(NotesDatabaseAdapter.NOTES_KEY_NAME_COLUMN));
				subtitles.add(cursor.getString(NotesDatabaseAdapter.NOTES_KEY_BODY_COLUMN));
				createDates.add(cursor.getLong(NotesDatabaseAdapter.NOTES_KEY_CREATE_DATE_COLUMN));
				changeDates.add(cursor.getLong(NotesDatabaseAdapter.NOTES_KEY_CHANGE_DATE_COLUMN));
			} while (cursor.moveToNext());
		}
		dbAdapter.close();

		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < titles.size(); i++) {
			final Map<String, String> map = new HashMap<String, String>();
			map.put(TITLE, titles.get(i));
			map.put(SUBTITLE, subtitles.get(i));
			data.add(map);
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), data, R.layout.note_list_item, new String[]{TITLE, SUBTITLE}, new int[] {R.id.title, R.id.subtitle});

		setListAdapter(simpleAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		((MainActivity) getActivity()).showDetailsFragment(subtitles.get(position),
				createDates.get(position),
				changeDates.get(position));
	}
}
