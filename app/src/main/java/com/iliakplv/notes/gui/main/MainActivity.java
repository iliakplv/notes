package com.iliakplv.notes.gui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.analytics.Event;
import com.iliakplv.notes.analytics.EventTracker;
import com.iliakplv.notes.gui.settings.SettingsActivity;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.dropbox.DropboxHelper;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.Storage;
import com.iliakplv.notes.notes.storage.StorageDataTransfer;
import com.iliakplv.notes.utils.ConnectivityUtils;
import com.iliakplv.notes.utils.StringUtils;

import java.io.Serializable;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerListener {

	private static final String ARG_DETAILS_SHOWN = "details_fragment_shown";
	private static final String ARG_SELECTED_LABEL_ID = "selected_label_id";
	private static final String ARG_SEARCH_QUERY = "search_query";
	private static final String PREFS_KEY_SORT_ORDER = "sort_order";
	public static final Integer NEW_NOTE = 0;

	private final NotesStorage storage = Storage.getStorage();

	private volatile boolean detailsShown = false;
	private NavigationDrawerFragment navigationDrawerFragment;

	private Serializable selectedLabelId = NavigationDrawerFragment.ALL_LABELS;
	private String searchQuery;

	private CharSequence actionBarTitle;


	private boolean isDetailsShown() {
		return detailsShown;
	}

	public void setDetailsShown(boolean shown) {
		detailsShown = shown;
		invalidateOptionsMenu();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		restoreNotesSortOrder();
		setupNavigationDrawer();

		if (savedInstanceState == null) {
			final NotesListFragment notesListFragment = new NotesListFragment();
			notesListFragment.setArguments(getIntent().getExtras());
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, notesListFragment, NotesListFragment.TAG);
			ft.commit();
		} else {
			setDetailsShown(savedInstanceState.getBoolean(ARG_DETAILS_SHOWN));
			selectedLabelId = savedInstanceState.getSerializable(ARG_SELECTED_LABEL_ID);
			searchQuery = savedInstanceState.getString(ARG_SEARCH_QUERY);
		}

	}

	private void setupNavigationDrawer() {
		navigationDrawerFragment = (NavigationDrawerFragment)
				getFragmentManager().findFragmentById(R.id.navigation_drawer);

		navigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateUi();
	}

	@Override
	public void onLabelSelected(Serializable labelId) {
		selectedLabelId = labelId;
		searchQuery = null;
		closeNoteDetails();
		updateUi();
	}

	private void performSearch(String searchQuery) {
		if (!StringUtils.isBlank(searchQuery)) {
			this.searchQuery = searchQuery.trim();
			updateUi();
		}
	}

	private void updateUi() {
		updateNotesList();
		updateActionBar();
	}

	private void updateNotesList() {
		final NotesListFragment noteListFragment =
				(NotesListFragment) getFragmentManager().findFragmentByTag(NotesListFragment.TAG);
		if (noteListFragment != null) {
			if (searchQuery != null) {
				noteListFragment.showNotesForQuery(searchQuery);
			} else {
				noteListFragment.showNotesForLabel(selectedLabelId);
			}
		}
	}

	private void updateActionBar() {
		if (searchQuery != null) {
			actionBarTitle = " '" + searchQuery + "'";
		} else {
			if (NavigationDrawerFragment.ALL_LABELS.equals(selectedLabelId)) {
				actionBarTitle = getString(R.string.labels_drawer_all_notes);
			} else {
				final Label label = storage.getLabel(selectedLabelId);
				actionBarTitle = label != null ?
						NotesUtils.getTitleForLabel(label) :
						getString(R.string.app_name);
			}
		}
		restoreActionBar();
	}

	public void onNoteSelected(Serializable noteId) {
		setDetailsShown(true);

		final NoteDetailsFragment noteDetailsFragment = new NoteDetailsFragment();
		final Bundle args = new Bundle();
		args.putSerializable(NoteDetailsFragment.ARG_NOTE_ID, noteId);
		noteDetailsFragment.setArguments(args);

		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_container, noteDetailsFragment, NoteDetailsFragment.TAG);
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();

		if (NEW_NOTE.equals(noteId)) {
			EventTracker.track(Event.NoteCreateClick);
		} else {
			EventTracker.track(Event.NoteShow);
		}
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
		outState.putSerializable(ARG_SELECTED_LABEL_ID, selectedLabelId);
		outState.putString(ARG_SEARCH_QUERY, searchQuery);
	}

	@Override
	public void onBackPressed() {
		if (navigationDrawerFragment.isDrawerOpen()) {
			// 1. close drawer if opened
			navigationDrawerFragment.closeDrawer();
		} else if (isDetailsShown()) {
			// 2. close note details if shown
			closeNoteDetails();
		} else if (searchQuery != null || !NavigationDrawerFragment.ALL_LABELS.equals(selectedLabelId)) {
			// 3. return to all labels if any label selected or search performed
			onLabelSelected(NavigationDrawerFragment.ALL_LABELS);
		} else {
			// 4. exit from app
			super.onBackPressed();
		}
	}

	private void closeNoteDetails() {
		if (isDetailsShown()) {
			setDetailsShown(false);
			final NoteDetailsFragment noteDetailsFragment =
					(NoteDetailsFragment) getFragmentManager().findFragmentByTag(NoteDetailsFragment.TAG);
			if (noteDetailsFragment != null) {
				noteDetailsFragment.onBackPressed();
			}
			super.onBackPressed();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			performSearch(intent.getStringExtra(SearchManager.QUERY));
		}
	}

	// menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!navigationDrawerFragment.isDrawerOpen()) {
			if (!isDetailsShown()) {
				getMenuInflater().inflate(R.menu.main_menu, menu);
				inflateSortMenu(menu);
				configureSearchMenu(menu);
				updateDropboxActionTitle(menu);
			}
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	private void configureSearchMenu(Menu menu) {
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));
	}

	private void inflateSortMenu(Menu menu) {
		final int order = 2;
		final SubMenu sortMenu =
				menu.addSubMenu(Menu.NONE, Menu.NONE, order, R.string.action_sort);
		getMenuInflater().inflate(R.menu.main_sort_menu, sortMenu);
	}

	private void updateDropboxActionTitle(Menu menu) {
		final MenuItem dropboxItem = menu.findItem(R.id.action_dropbox);
		if (dropboxItem !=  null) {
			if (Storage.getCurrentStorageType() == Storage.Type.Dropbox) {
				dropboxItem.setTitle(R.string.action_dropbox_refresh);
				dropboxItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			} else {
				dropboxItem.setTitle(R.string.action_dropbox_link);
				dropboxItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			}
		}
	}

	private void restoreActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(actionBarTitle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		switch (itemId) {

			// main menu
			case R.id.action_add:
				createNewNote();
				return true;

			// sort menu
			case R.id.sort_by_title:
				setNotesSortOrder(NotesUtils.NoteSortOrder.Title);
				EventTracker.track(Event.NotesSortOrderSelect);
				return true;
			case R.id.sort_by_create_asc:
				setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateAscending);
				EventTracker.track(Event.NotesSortOrderSelect);
				return true;
			case R.id.sort_by_create_desc:
				setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateDescending);
				EventTracker.track(Event.NotesSortOrderSelect);
				return true;
			case R.id.sort_by_change:
				setNotesSortOrder(NotesUtils.NoteSortOrder.ChangeDate);
				EventTracker.track(Event.NotesSortOrderSelect);
				return true;

			// global menu
			case R.id.action_settings:
				showAppSettings();
				return true;

			// dropbox
			case R.id.action_dropbox:
				performDropboxAction();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private void performDropboxAction() {
		if (ConnectivityUtils.isNetworkConnected()) {
			if (Storage.getCurrentStorageType() == Storage.Type.Dropbox) {
				storage.sync();
				Toast.makeText(this, R.string.action_dropbox_refresh_toast, Toast.LENGTH_SHORT).show();
				EventTracker.track(Event.DropboxSyncManual);
			} else {
				final boolean dataTransferStarted = startDataTransferToDropboxIfNeeded();
				if (!dataTransferStarted) {
					DropboxHelper.tryLinkAccountFromActivity(this);
				}
			}
		} else {
			Toast.makeText(this, R.string.no_connection_toast, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DropboxHelper.onAccountLinkActivityResult(this, requestCode, resultCode, data);
		startDataTransferToDropboxIfNeeded();
	}

	public void showAppSettings() {
		final Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(settingsIntent);
		EventTracker.track(Event.SettingsOpening);
	}

	private void setNotesSortOrder(NotesUtils.NoteSortOrder order) {
		if (storage.setNotesSortOrder(order)) {
			final SharedPreferences.Editor editor = getPreferences(Activity.MODE_PRIVATE).edit();
			editor.putInt(PREFS_KEY_SORT_ORDER, order.ordinal());
			editor.apply();
		}
	}

	private void restoreNotesSortOrder() {
		final SharedPreferences prefs = getPreferences(Activity.MODE_PRIVATE);
		int orderOrdinal = prefs.getInt(PREFS_KEY_SORT_ORDER, -1);
		if (orderOrdinal != -1) {
			setNotesSortOrder(NotesUtils.NoteSortOrder.values()[orderOrdinal]);
		}
	}

	private boolean startDataTransferToDropboxIfNeeded() {
		boolean startDataTransfer =
				Storage.getCurrentStorageType() == Storage.Type.Database &&
				DropboxHelper.hasLinkedAccount();

		if (startDataTransfer) {
			NotesApplication.executeInBackground(new Runnable() {
				@Override
				public void run() {
					StorageDataTransfer.changeStorageType(Storage.Type.Dropbox, true);
					DropboxHelper.initSynchronization();
					MainActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							invalidateOptionsMenu();
						}
					});
				}
			});
		}

		return startDataTransfer;
	}

}
