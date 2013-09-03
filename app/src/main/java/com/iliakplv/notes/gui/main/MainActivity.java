package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.iliakplv.notes.BuildConfig;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends ActionBarActivity {

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (BuildConfig.DEBUG) {
			Log.d("MENU", "Clicked MenuItem is " + item.getTitle());
		}
		return super.onOptionsItemSelected(item);
	}

}
