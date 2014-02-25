package com.iliakplv.notes.gui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerListener {

	private static final String ARG_CURRENT_NOTE_ID = "current_note_id";
	public static final int NO_DETAILS = 0;

	private volatile int currentNoteId = NO_DETAILS;
	private boolean listeningExistingNote = false;

	private NotesListFragment notesListFragment;
	private NavigationDrawerFragment navigationDrawerFragment;
	private CharSequence title;


	private boolean isDetailsShown() {
		return currentNoteId != NO_DETAILS;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setupNavigationDrawer();

		if (savedInstanceState == null) {
			notesListFragment = new NotesListFragment();
			notesListFragment.setArguments(getIntent().getExtras());
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, notesListFragment);
			ft.commit();
		} else {
			onDetailsChanged(savedInstanceState.getInt(ARG_CURRENT_NOTE_ID));
		}

	}

	private void setupNavigationDrawer() {
		navigationDrawerFragment = (NavigationDrawerFragment)
				getFragmentManager().findFragmentById(R.id.navigation_drawer);
		title = getTitle();

		navigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onSelectedLabel(int labelId, String newTitle) {
		title = newTitle;
		if (notesListFragment != null) {
			notesListFragment.showNotesForLabel(labelId);
		}
	}

	public void restoreActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void onNoteSelected(int noteId) {
		onDetailsChanged(noteId);

		if (isDetailsShown()) {
			final NoteDetailsFragment noteDetailsFragment = new NoteDetailsFragment();
			final Bundle args = new Bundle();
			args.putInt(NoteDetailsFragment.ARG_NOTE_ID, noteId);
			noteDetailsFragment.setArguments(args);

			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, noteDetailsFragment);
			ft.addToBackStack(null);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}
	}

	private void onDetailsChanged(int newNoteId) {
		currentNoteId = newNoteId;
		invalidateOptionsMenu();
	}

	public void createNewNote() {
		NotesApplication.executeInBackground(new Runnable() {
			@Override
			public void run() {
				final int newNoteId = NotesDatabaseFacade.getInstance().insertNote(new TextNote());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						onNoteSelected(newNoteId);
					}
				});
			}
		});
	}


	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_CURRENT_NOTE_ID, currentNoteId);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		onDetailsChanged(NO_DETAILS);
	}


	// menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!navigationDrawerFragment.isDrawerOpen()) {
			if (isDetailsShown()) {
				getMenuInflater().inflate(R.menu.note, menu);
			} else {
				getMenuInflater().inflate(R.menu.main, menu);
			}
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_add) {
			createNewNote();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
