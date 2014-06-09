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

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.settings.SettingsActivity;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.dropbox.DropboxHelper;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.Storage;
import com.iliakplv.notes.notes.storage.StorageDataTransfer;

import java.io.Serializable;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerListener {

	private static final String ARG_DETAILS_SHOWN = "details_fragment_shown";
	private static final String PREFS_KEY_SORT_ORDER = "sort_order";
	public static final Integer NEW_NOTE = 0;

	private final NotesStorage storage = Storage.getStorage();

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
	public void onLabelSelected(Serializable labelId, String newTitle) {
		title = newTitle;
		final NotesListFragment noteListFragment =
				(NotesListFragment) getFragmentManager().findFragmentByTag(NotesListFragment.TAG);
		if (noteListFragment != null) {
			noteListFragment.showNotesForLabel(labelId);
		}
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
		if (isDetailsShown()) {
			setDetailsShown(false);
			final NoteDetailsFragment noteDetailsFragment =
					(NoteDetailsFragment) getFragmentManager().findFragmentByTag(NoteDetailsFragment.TAG);
			if (noteDetailsFragment != null) {
				noteDetailsFragment.onBackPressed();
			}
		}
	}


	// menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!navigationDrawerFragment.isDrawerOpen()) {
			if (isDetailsShown()) {
				getMenuInflater().inflate(R.menu.note_menu, menu);
			} else {
				getMenuInflater().inflate(R.menu.main_menu, menu);
				inflateSortMenu(menu);
			}
			updateDropboxActionTitle(menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	private void inflateSortMenu(Menu menu) {
		final SubMenu sortMenu =
				menu.addSubMenu(Menu.NONE, Menu.NONE, 1, R.string.action_sort);
		getMenuInflater().inflate(R.menu.main_sort_menu, sortMenu);
	}

	private void updateDropboxActionTitle(Menu menu) {
		final MenuItem dropboxItem = menu.findItem(R.id.action_dropbox);
		if (dropboxItem !=  null) {
			if (Storage.getCurrentStorageType() == Storage.Type.Dropbox) {
				dropboxItem.setTitle(R.string.action_dropbox_refresh);
			} else {
				dropboxItem.setTitle(R.string.action_dropbox_link);
			}
		}
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

			// main menu
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
		// TODO refactor !!!
		if (Storage.getCurrentStorageType() == Storage.Type.Dropbox) {
			storage.sync();
		} else {
			final boolean dataTransferStarted = startDataTransferToDropboxIfNeeded();
			if (!dataTransferStarted) {
				DropboxHelper.tryLinkAccountFromActivity(this);
			}
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
	}

	private void setNotesSortOrder(NotesUtils.NoteSortOrder order) {
		if (storage.setNotesSortOrder(order)) {
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

	private boolean startDataTransferToDropboxIfNeeded() {
		// TODO refactor !!!
		boolean startDataTransfer =
				Storage.getCurrentStorageType() == Storage.Type.Database &&
				DropboxHelper.hasLinkedAccount();

		if (startDataTransfer) {
			NotesApplication.executeInBackground(new Runnable() {
				@Override
				public void run() {
					StorageDataTransfer.changeStorageType(Storage.Type.Dropbox, true);
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
