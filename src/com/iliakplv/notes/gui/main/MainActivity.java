package com.iliakplv.notes.gui.main;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import org.joda.time.DateTime;

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

	public void showDetailsFragment(AbstractNote note) {
		NoteDetailsFragment noteDetailsFragment = new NoteDetailsFragment();
		noteDetailsFragment.setText(note.getBody() + "\n\n" +
				note.getCreateTime().toDate().toLocaleString() + "\n\n" +
				note.getChangeTime().toDate().toLocaleString() + "\n\n" +
				"id = " + note.getId());

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.details, noteDetailsFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

}
