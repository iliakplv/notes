package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends FragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Fragment notesListFragment = new NotesListFragment();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.notes_list, notesListFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	public void showDetailsFragment(AbstractNote note) {
		NoteDetailsFragment noteDetailsFragment = new NoteDetailsFragment(note);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.details, noteDetailsFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

}
