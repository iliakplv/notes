package com.iliakplv.notes.gui.main;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.iliakplv.notes.R;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		NotesListFragment notesListFragment = new NotesListFragment();

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.notes_list, notesListFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	public void showDetailsFragment(String text) {
		NoteDetailsFragment noteDetailsFragment = new NoteDetailsFragment();
		noteDetailsFragment.setText("This is text of note \'" + text + "\'");

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.details, noteDetailsFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

}
