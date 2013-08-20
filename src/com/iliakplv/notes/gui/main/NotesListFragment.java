package com.iliakplv.notes.gui.main;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.iliakplv.notes.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Autor: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NotesListFragment extends ListFragment {


	private static final String TITLE = "title";
	private static final String SUBTITLE = "subtitle";

	private String titlesData[] = new String[] { "First", "Second", "One more", "Yet anoter" };

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < titlesData.length; i++) {
			final Map<String, String> map = new HashMap<String, String>();
			map.put(TITLE, titlesData[i]);
			map.put(SUBTITLE, String.valueOf(i));
			data.add(map);
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(), data, R.layout.note_list_item, new String[]{TITLE, SUBTITLE}, new int[] {R.id.title, R.id.subtitle});

		setListAdapter(simpleAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		((MainActivity) getActivity()).showDetailsFragment(titlesData[position]);
	}
}
