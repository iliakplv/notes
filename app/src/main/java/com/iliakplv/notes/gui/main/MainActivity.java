package com.iliakplv.notes.gui.main;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.iliakplv.notes.BuildConfig;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends ActionBarActivity {

	private List<NotesDatabaseEntry> notesEntries;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (findViewById(R.id.fragment_container) != null) { // One pane layout
			if (savedInstanceState != null) {
				// If we're being restored from a previous state,
				// then we don't need to do anything and should return or else
				// we could end up with overlapping fragments.
				return;
			}

			final NotesListFragment noteListFragment = new NotesListFragment();
			noteListFragment.setArguments(getIntent().getExtras());
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, noteListFragment);
			ft.commit();
		}
	}

	public void onNoteSelected(int position) {
		final NoteDetailsFragment noteDetailsFragment = (NoteDetailsFragment)
				getSupportFragmentManager().findFragmentById(R.id.note_details_fragment);

		if (noteDetailsFragment != null) { // Dual pane layout
			noteDetailsFragment.updateNoteDetailsView(position);
		} else { // One pane layout
			final NoteDetailsFragment newNoteDetailsFragment = new NoteDetailsFragment();
			final Bundle args = new Bundle();
			args.putInt(NoteDetailsFragment.ARG_POSITION, position);
			newNoteDetailsFragment.setArguments(args);

			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container, newNoteDetailsFragment);
			ft.addToBackStack(null);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}
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


	public List<NotesDatabaseEntry> getUpdatedNotesEntriesList() {
		notesEntries = NotesDatabaseFacade.getAllNotes();
		return notesEntries;
	}

	public List<NotesDatabaseEntry> getNotesEntriesList() {
		if (notesEntries == null) {
			return getUpdatedNotesEntriesList();
		}
		return notesEntries;
	}

}
