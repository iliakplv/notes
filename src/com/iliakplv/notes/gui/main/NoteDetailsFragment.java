package com.iliakplv.notes.gui.main;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.iliakplv.notes.R;

/**
 * Autor: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NoteDetailsFragment extends Fragment {

	private String text;

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_details, container, false);
		((TextView) view.findViewById(R.id.test_text)).setText(text);
		return view;
	}


}
