package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
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

	private static final String CURRENT_NOTE_KEY = "current_note";
	private static final String CURRENT_UI_STATE_KEY = "current_iu_state";

	private static final int MIN_DISPLAY_WIDTH = 600;

	private int currentNoteId = 0;
	private UiState currentUiState = UiState.NotesList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

//		final Display display = getWindowManager().getDefaultDisplay();
//		final int width = display.getWidth();
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();    //To change body of overridden methods use File | Settings | File Templates.

		final Fragment notesListFragment = new NotesListFragment();
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_NOTE_KEY, currentNoteId);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentNoteId = savedInstanceState.getInt(CURRENT_NOTE_KEY);
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
		(new NoteDialogFragment()).show(getSupportFragmentManager(), "dialog");
		return super.onOptionsItemSelected(item);
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private static enum UiState {
		NotesList,
		NoteDetails,
		DualPane
	}

}
