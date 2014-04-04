package com.iliakplv.notes.gui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.settings.SettingsActivity;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerListener {

	private static final String ARG_DETAILS_SHOWN = "details_fragment_shown";
	private static final String LIST_FRAGMENT_TAG = "notes_list_fragment";
	private static final String PREFS_KEY_SORT_ORDER = "sort_order";
	public static final int NEW_NOTE = 0;

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();

	private volatile boolean detailsShown = false;
	private NavigationDrawerFragment navigationDrawerFragment;
	private CharSequence title;


	// TODO dropbox test

	private static final int REQUEST_LINK_TO_DBX = 0;
	private static final String APP_KEY = "cyla6oz3c3vuje3";
	private static final String APP_SECRET = "blt7jatmxpojwiz";
	private DbxAccountManager mAccountManager;
	private DbxAccount mAccount;

	private void dropboxTest() {
		mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);

		if (mAccountManager.hasLinkedAccount()) {
			Toast.makeText(this, "Account already linked", Toast.LENGTH_LONG).show();
		} else {
			mAccountManager.startLink(this, REQUEST_LINK_TO_DBX);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LINK_TO_DBX) {
			if (resultCode == Activity.RESULT_OK) {
				mAccount = mAccountManager.getLinkedAccount();
				Toast.makeText(this, "Account has been linked", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Account link failed!", Toast.LENGTH_LONG).show();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	// TODO dropbox test


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

		restoreNotesSortOrder();
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

	public NavigationDrawerFragment getNavigationDrawerFragment() {
		return navigationDrawerFragment;
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
				final SubMenu sortMenu =
						menu.addSubMenu(Menu.NONE, Menu.NONE, 1, R.string.action_sort);
				getMenuInflater().inflate(R.menu.main_sort_menu, sortMenu);
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

			// sort menu
			case R.id.sort_by_title:
				setNotesSortOrder(NotesUtils.NoteSortOrder.Title);
				return true;
			case R.id.sort_by_create_asc:
				setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateAscending);
				return true;
			case R.id.sort_by_create_desc:
				setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateDescending);
				return true;
			case R.id.sort_by_change:
				setNotesSortOrder(NotesUtils.NoteSortOrder.ChangeDate);
				return true;

			// menu in details
//			TODO [low] implement showing NoteLabelsDialog
//			case R.id.action_labels:
//				return true;

//			TODO implement sharing

			// global menu
			case R.id.action_settings:
				showAppSettings();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showAppSettings() {
		final Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(settingsIntent);
	}

	private void setNotesSortOrder(NotesUtils.NoteSortOrder order) {
		if (dbFacade.setNotesSortOrder(order)) {
			final SharedPreferences.Editor editor = getPreferences(Activity.MODE_PRIVATE).edit();
			editor.putInt(PREFS_KEY_SORT_ORDER, order.ordinal());
			editor.commit();
		}
	}

	private void restoreNotesSortOrder() {
		final SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		int orderOrdinal = prefs.getInt(PREFS_KEY_SORT_ORDER, -1);
		if (orderOrdinal != -1) {
			setNotesSortOrder(NotesUtils.NoteSortOrder.values()[orderOrdinal]);
		}
	}

}
