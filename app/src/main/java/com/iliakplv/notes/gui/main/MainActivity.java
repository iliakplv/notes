package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.iliakplv.notes.R;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends ActionBarActivity {

	private static final String DETAILS_SHOWN = "note_details_shown";
	private boolean detailsShown = false;


	private boolean isSinglePaneLayout() {
		return findViewById(R.id.fragment_container) != null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (isSinglePaneLayout()) {
			if (savedInstanceState != null) {
				onNoteListNavigate(savedInstanceState.getBoolean(DETAILS_SHOWN));
				return;
			}

			final NotesListFragment noteListFragment = new NotesListFragment();
			noteListFragment.setArguments(getIntent().getExtras());
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, noteListFragment);
			ft.commit();
		}
	}

	public void onNoteSelected(int noteId) {
		final NoteDetailsFragment noteDetailsFragment = (NoteDetailsFragment)
				getSupportFragmentManager().findFragmentById(R.id.note_details_fragment);

		if (noteDetailsFragment != null) { // Dual pane layout
			noteDetailsFragment.updateNoteDetailsView(noteId);
		} else { // Single pane layout
			final NoteDetailsFragment newNoteDetailsFragment = new NoteDetailsFragment();
			final Bundle args = new Bundle();
			args.putInt(NoteDetailsFragment.ARG_NOTE_ID, noteId);
			newNoteDetailsFragment.setArguments(args);

			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, newNoteDetailsFragment);
			ft.addToBackStack(null);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();

			onNoteListNavigate(true);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(DETAILS_SHOWN, detailsShown);
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
				if (detailsShown) {
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
		onNoteListNavigate(false);
	}

	private void onNoteListNavigate(boolean showDetails) {
		detailsShown = showDetails;
		getActionBar().setDisplayHomeAsUpEnabled(detailsShown && isSinglePaneLayout());
	}

}
