package com.iliakplv.notes.gui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.main.dialogs.AboutDialog;
import com.iliakplv.notes.gui.main.dialogs.DropboxAnnouncementDialog;
import com.iliakplv.notes.gui.main.dialogs.VoiceSearchInstallDialog;
import com.iliakplv.notes.gui.settings.SettingsActivity;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.Storage;
import com.iliakplv.notes.utils.ConnectivityUtils;
import com.iliakplv.notes.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerListener {

    private static final String ARG_DETAILS_SHOWN = "details_fragment_shown";
    private static final String ARG_SELECTED_LABEL_ID = "selected_label_id";
    private static final String ARG_SEARCH_QUERY = "search_query";
    private static final String PREFS_KEY_SORT_ORDER = "sort_order";
    private static final String PREFS_KEY_SHOW_ANNOUNCEMENT = "announcement";
    private static final int RESULT_SPEECH_TO_TEXT = 42;

    public static final Integer NEW_NOTE = 0;

    private final NotesStorage storage = Storage.getStorage();
    private boolean isDropboxLinked = false;

    private volatile boolean detailsShown = false;
    private NavigationDrawerFragment navigationDrawerFragment;

    private Serializable selectedLabelId = NavigationDrawerFragment.ALL_LABELS;
    private String searchQuery;

    private FirebaseAnalytics firebaseAnalytics;

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

        isDropboxLinked = Storage.getCurrentStorageType() == Storage.Type.Dropbox;
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        restoreNotesSortOrder();
        setupNavigationDrawer();

        final boolean fromSaveInstanceState = savedInstanceState != null;
        if (fromSaveInstanceState) {
            setDetailsShown(savedInstanceState.getBoolean(ARG_DETAILS_SHOWN));
            selectedLabelId = savedInstanceState.getSerializable(ARG_SELECTED_LABEL_ID);
            searchQuery = savedInstanceState.getString(ARG_SEARCH_QUERY);
        } else {
            final NotesListFragment notesListFragment = new NotesListFragment();
            notesListFragment.setArguments(getIntent().getExtras());
            final FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, notesListFragment, NotesListFragment.TAG);
            ft.commit();

            final Intent intent = getIntent();
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                if ("text/plain".equals(intent.getType())) {
                    onShareIntent(intent);
                }
            } else if (isDropboxLinked) {
                final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                if (prefs.getBoolean(PREFS_KEY_SHOW_ANNOUNCEMENT, true)) {
                    logEvent("dropbox_announcement_first");
                    DropboxAnnouncementDialog.show(getFragmentManager());
                    prefs.edit().putBoolean(PREFS_KEY_SHOW_ANNOUNCEMENT, false).apply();
                }
            }
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
        syncStorage();
    }

    private void syncStorage() {
        storage.sync();
    }

    @Override
    public void onLabelSelected(Serializable labelId) {
        logEvent("label_selected");
        selectedLabelId = labelId;
        searchQuery = null;
        closeNoteDetails();
        updateUi();
    }

    private void performSearch(String searchQuery) {
        if (!StringUtils.isBlank(searchQuery)) {
            logEvent("note_search");
            this.searchQuery = searchQuery.trim();
            updateUi();
        }
    }

    private void updateUi() {
        updateNotesList();
        restoreActionBar();
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

    public void onNoteSelected(Serializable noteId) {
        showNoteDetails(noteId, null, null);
    }

    private void onShareIntent(Intent intent) {
        logEvent("share_intent_received");
        showNoteDetails(NEW_NOTE,
                StringUtils.getNotNull(intent.getStringExtra(Intent.EXTRA_SUBJECT)),
                StringUtils.getNotNull(intent.getStringExtra(Intent.EXTRA_TEXT)));
    }

    // set text and/or title from external intent
    private void showNoteDetails(Serializable noteId, String title, String text) {
        setDetailsShown(true);

        final NoteDetailsFragment noteDetailsFragment = new NoteDetailsFragment();
        final Bundle args = new Bundle();
        args.putSerializable(NoteDetailsFragment.ARG_NOTE_ID, noteId);
        args.putSerializable(NoteDetailsFragment.ARG_TITLE, title);
        args.putSerializable(NoteDetailsFragment.ARG_TEXT, text);
        noteDetailsFragment.setArguments(args);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, noteDetailsFragment, NoteDetailsFragment.TAG);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void createNewNote() {
        logEvent("note_create_clicked");
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
        if (isDrawerOpened()) {
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

    public boolean isDrawerOpened() {
        return navigationDrawerFragment != null && navigationDrawerFragment.isDrawerOpen();
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
        if (!isDrawerOpened()) {
            if (!isDetailsShown()) {
                final int menuId = isDropboxLinked ? R.menu.main_menu_db : R.menu.main_menu;
                getMenuInflater().inflate(menuId, menu);
                inflateSortMenu(menu);
                configureSearchMenu(menu);
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
        final int order = 3;
        final SubMenu sortMenu =
                menu.addSubMenu(Menu.NONE, Menu.NONE, order, R.string.action_sort);
        getMenuInflater().inflate(R.menu.main_sort_menu, sortMenu);
    }

    private void restoreActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {

            case R.id.action_add:
                createNewNote();
                return true;
            case R.id.action_speak:
                startVoiceInput();
                break;

            // sort menu
            case R.id.sort_by_title:
                logEvent("sort_by_title");
                setNotesSortOrder(NotesUtils.NoteSortOrder.Title);
                return true;
            case R.id.sort_by_create_asc:
                logEvent("sort_by_create_asc");
                setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateAscending);
                return true;
            case R.id.sort_by_create_desc:
                logEvent("sort_by_create_desc");
                setNotesSortOrder(NotesUtils.NoteSortOrder.CreateDateDescending);
                return true;
            case R.id.sort_by_change:
                logEvent("sort_by_change");
                setNotesSortOrder(NotesUtils.NoteSortOrder.ChangeDate);
                return true;

            case R.id.action_settings:
                logEvent("show_settings");
                showAppSettings();
                return true;
            case R.id.action_about:
                logEvent("show_about");
                AboutDialog.show(getFragmentManager());
                return true;

            // dropbox
            case R.id.action_dropbox:
                logEvent("dropbox_action");
                performDropboxAction();
                return true;
            case R.id.action_dropbox_close:
                logEvent("dropbox_announcement");
                DropboxAnnouncementDialog.show(getFragmentManager());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startVoiceInput() {
        try {
            logEvent("voice_input");
            final Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.action_bar_speak_prompt));
            startActivityForResult(speechIntent, RESULT_SPEECH_TO_TEXT);
        } catch (ActivityNotFoundException e) {
            VoiceSearchInstallDialog.show(getFragmentManager());
        }
    }

    private void performDropboxAction() {
        if (ConnectivityUtils.isNetworkConnected()) {
            syncStorage();
            Toast.makeText(this, R.string.action_dropbox_refresh_toast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.no_connection_toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_SPEECH_TO_TEXT) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!matches.isEmpty()) {
                    showNoteDetails(NEW_NOTE, "", matches.get(0));
                }
            }
        }
    }

    public void showAppSettings() {
        final Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
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

    public void logEvent(String event) {
        firebaseAnalytics.logEvent(event, null);
    }

}
