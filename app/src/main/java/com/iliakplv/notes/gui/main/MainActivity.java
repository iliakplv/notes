package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends ActionBarActivity implements NotesDatabaseFacade.NoteChangeListener {

	// TODO [ui] handle creating multiple notes one by one

	private static final String ARG_CURRENT_NOTE_ID = "current_note_id";
	public static final int NO_DETAILS = 0;

	private int currentNoteId = NO_DETAILS;
	private boolean listeningExistingNote = false;


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
		getSupportActionBar().setDisplayHomeAsUpEnabled(isDetailsShown() && isSinglePaneLayout());

		// subscribe/unsubscribe to note changes
		if (isDetailsShown()) {
			startListeningNote();
		} else {
			stopListeningNote();
		}
	}

	public void createNewNote() {
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
		final int menuId = isDetailsShown() ? R.menu.main_note : R.menu.main;
		getMenuInflater().inflate(menuId, menu);
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
				createNewNote();
				break;
		}
		return true;
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
