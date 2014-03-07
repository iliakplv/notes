package com.iliakplv.notes.gui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerListener {

	private static final String ARG_DETAILS_SHOWN = "details_fragment_shown";
	private static final String LIST_FRAGMENT_TAG = "notes_list_fragment";
	public static final int NEW_NOTE = 0;

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();

	private volatile boolean detailsShown = false;
	private NavigationDrawerFragment navigationDrawerFragment;
	private CharSequence title;


	private boolean isDetailsShown() {
		return detailsShown;
	}

	public void setDetailsShown(boolean shown) {
		detailsShown = shown;
		invalidateOptionsMenu();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setupNavigationDrawer();

		if (savedInstanceState == null) {
			final NotesListFragment notesListFragment = new NotesListFragment();
			notesListFragment.setArguments(getIntent().getExtras());
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, notesListFragment, LIST_FRAGMENT_TAG);
			ft.commit();
		} else {
			setDetailsShown(savedInstanceState.getBoolean(ARG_DETAILS_SHOWN));
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
	public void onLabelSelected(int labelId, String newTitle) {
		title = newTitle;
		final NotesListFragment noteListFragment =
				(NotesListFragment) getFragmentManager().findFragmentByTag(LIST_FRAGMENT_TAG);
		if (noteListFragment != null) {
			noteListFragment.showNotesForLabel(labelId);
		}
	}

	public void onNoteSelected(int noteId) {
		setDetailsShown(true);

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

	public void createNewNote() {
		onNoteSelected(NEW_NOTE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(ARG_DETAILS_SHOWN, detailsShown);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		setDetailsShown(false);
	}


	// menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!navigationDrawerFragment.isDrawerOpen()) {
			if (isDetailsShown()) {
				getMenuInflater().inflate(R.menu.note_menu, menu);
			} else {
				getMenuInflater().inflate(R.menu.main_menu, menu);
				final SubMenu sortTypesMenu =
						menu.addSubMenu(Menu.NONE, Menu.NONE, 1, R.string.action_sort);
				getMenuInflater().inflate(R.menu.main_sort_menu, sortTypesMenu);
			}
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	private void restoreActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(title);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		switch (itemId) {

			// menu in list
			case R.id.action_add:
				createNewNote();
				return true;

			// sort types menu
			case R.id.sort_by_title:
				dbFacade.setNotesSortOrder(NotesUtils.NoteSortOrder.Title);
				return true;
			case R.id.sort_by_create_asc:
				dbFacade.setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateAscending);
				return true;
			case R.id.sort_by_create_desc:
				dbFacade.setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateDescending);
				return true;
			case R.id.sort_by_change:
				dbFacade.setNotesSortOrder(NotesUtils.NoteSortOrder.ChangeDate);
				return true;

			// menu in details
			case R.id.action_labels:
				// TODO implement
				return true;

			// global menu
			case R.id.action_settings:
				// TODO implement
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
