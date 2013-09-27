package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends ActionBarActivity implements NotesDatabaseFacade.NoteChangeListener {

	private static final String CURRENT_NOTE_ID = "current_note_id";
	private static final int NO_DETAILS = 0;
	private int currentNoteId = NO_DETAILS;


	private boolean isSinglePaneLayout() {
		return findViewById(R.id.fragment_container) != null;
	}

	private boolean isDetailsShown() {
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
			}
		} else { // dual pane layout
			final int id = savedInstanceState != null ?
					savedInstanceState.getInt(CURRENT_NOTE_ID) :
					NO_DETAILS;
			onNoteSelected(id);
		}
	}

	public void onNoteSelected(int noteId) {
		onDetailsChanged(noteId);

		final NoteDetailsFragment noteDetailsFragment = (NoteDetailsFragment)
				getSupportFragmentManager().findFragmentById(R.id.note_details_fragment);

		if (isDetailsShown()) {
			if (noteDetailsFragment != null) {
				// TODO update or hide (if noteId == NO_DETAILS) !!!
				noteDetailsFragment.updateNoteDetailsView(noteId);
			} else {
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_NOTE_ID, currentNoteId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (isDetailsShown()) {
					onBackPressed();
				}
				break;
			case R.id.action_add:
				(new NoteDialogFragment(null)).show(getSupportFragmentManager(), "dialog");
				break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		onDetailsChanged(NO_DETAILS);
	}

	private void onDetailsChanged(int newNoteId) {
		final boolean detailsWasShown = isDetailsShown();
		currentNoteId = newNoteId;
		final boolean detailsShownNow = isDetailsShown();

		// show/hide arrow on action bar
		getActionBar().setDisplayHomeAsUpEnabled(detailsShownNow && isSinglePaneLayout());

		// subscribe/unsubscribe to note changes
		if (detailsShownNow != detailsWasShown) { // details view changed
			if (detailsShownNow) {
				NotesDatabaseFacade.getInstance().addNoteChangeListener(this);
			} else {
				NotesDatabaseFacade.getInstance().removeNoteChangeListener(this);
			}
		}
	}


	// listener implementation

	@Override
	public void onNoteChanged() {
		onNoteSelected(getNoteId());
	}

	@Override
	public int getNoteId() {
		return currentNoteId;
	}
}
