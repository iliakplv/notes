package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends ActionBarActivity
		implements NotesDatabaseFacade.NoteChangeListener, NavigationDrawerFragment.NavigationDrawerCallbacks {

	private static final String ARG_CURRENT_NOTE_ID = "current_note_id";
	public static final int NO_DETAILS = 0;

	private int currentNoteId = NO_DETAILS;
	private boolean listeningExistingNote = false;

	private NavigationDrawerFragment navigationDrawerFragment;
	private CharSequence title;


	private boolean isSinglePaneLayout() {
		return findViewById(R.id.fragment_container) != null;
	}

	private boolean isDetailsShown() {
		// TODO [ui] returns true if current note was deleted in dual pane mode
		return currentNoteId != NO_DETAILS;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (isSinglePaneLayout()) {
			if (savedInstanceState == null) { // show only list
				final NotesListFragment noteListFragment = new NotesListFragment();
				noteListFragment.setArguments(getIntent().getExtras());
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.add(R.id.fragment_container, noteListFragment);
				ft.commit();
			} else {
				onDetailsChanged(savedInstanceState.getInt(ARG_CURRENT_NOTE_ID));
			}
		} else { // dual pane
			final int id = savedInstanceState != null ?
					savedInstanceState.getInt(ARG_CURRENT_NOTE_ID) :
					NO_DETAILS;
			onNoteSelected(id);
		}

		// drawer setup
		navigationDrawerFragment = (NavigationDrawerFragment)
				getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		title = getTitle();

		navigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Toast.makeText(this, "selected", Toast.LENGTH_SHORT).show();
	}

	public void onSectionAttached(int number) {
		switch (number) {
			case 1:
				title = "title 1";
				break;
			case 2:
				title = "title 2";
				break;
			case 3:
				title = "title 3";
				break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startListeningNote();
	}


	public void onNoteSelected(int noteId) {
		onDetailsChanged(noteId);

		if (isDetailsShown()) { // show/update details

			final NoteDetailsFragment noteDetailsFragment = (NoteDetailsFragment)
					getSupportFragmentManager().findFragmentById(R.id.note_details_fragment);

			if (noteDetailsFragment != null) { // dual pane
				noteDetailsFragment.updateNoteDetailsView(noteId);

			} else { // single pane

				final NoteDetailsFragment newNoteDetailsFragment = new NoteDetailsFragment();
				final Bundle args = new Bundle();
				args.putInt(NoteDetailsFragment.ARG_NOTE_ID, noteId);
				newNoteDetailsFragment.setArguments(args);

				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.fragment_container, newNoteDetailsFragment);
				ft.addToBackStack(null);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
		}
	}

	private void onDetailsChanged(int newNoteId) {
		currentNoteId = newNoteId;
		supportInvalidateOptionsMenu();

		// show/hide arrow on action bar
//		TODO home button removed
//		getSupportActionBar().setDisplayHomeAsUpEnabled(isDetailsShown() && isSinglePaneLayout());

		// subscribe/unsubscribe to note changes
		if (isDetailsShown()) {
			startListeningNote();
		} else {
			stopListeningNote();
		}
	}

	public void createNewNote() {
		if (getSupportFragmentManager().findFragmentById(R.id.note_details_fragment) == null) { // single pane
			goBackToList();
		}
		onNoteSelected(NotesDatabaseFacade.getInstance().insertNote(new TextNote()));
	}


	@Override
	protected void onPause() {
		super.onPause();
		stopListeningNote();
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
			getMenuInflater().inflate(R.menu.main, menu);
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

	private void goBackToList() {
		if (isDetailsShown()) {
			onBackPressed();
		}
	}


	// listener

	@Override
	public void onNoteChanged() {
		if (listeningExistingNote) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listeningExistingNote) {
						onNoteSelected(currentNoteId);
					}
				}
			});
		}
	}

	@Override
	public int getNoteId() {
		return currentNoteId;
	}

	private void startListeningNote() {
		if (!listeningExistingNote && isDetailsShown()) {
			NotesDatabaseFacade.getInstance().addNoteChangeListener(this);
			listeningExistingNote = true;
		}
	}

	private void stopListeningNote() {
		if (listeningExistingNote) {
			NotesDatabaseFacade.getInstance().removeNoteChangeListener(this);
			listeningExistingNote = false;
		}
	}
}
