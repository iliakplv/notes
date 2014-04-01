package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.main.dialogs.LabelEditDialog;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

import java.util.List;

public class NavigationDrawerFragment extends Fragment implements LabelEditDialog.LabelEditDialogCallback {

	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
	private static final String PREF_SHOW_DRAWER_ON_START = "show_drawer_on_start";

	private static final int ALL_LABELS = NotesDatabaseFacade.ALL_LABELS;
	private static final int ALL_LABELS_HEADER_POSITION = 0;
	private static final int NO_LABEL_SELECTED = -1;

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
	private MainActivity mainActivity;

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private View fragmentContainerView;
	private ListView labelsListView;
	private LabelsListAdapter labelsListAdapter;

	private int currentSelectedPosition = NO_LABEL_SELECTED;
	private boolean fromSavedInstanceState;
	private boolean userLearnedDrawer;
	private boolean showDrawerOnStart;


	public NavigationDrawerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
		showDrawerOnStart = sp.getBoolean(PREF_SHOW_DRAWER_ON_START, false);

		if (!userLearnedDrawer && showDrawerOnStart) {
			// disable user-learned-drawer behaviour if user have selected to show drawer on start
			setUserLearnedDrawer();
		}
		if (savedInstanceState != null) {
			currentSelectedPosition =
					savedInstanceState.getInt(STATE_SELECTED_POSITION, NO_LABEL_SELECTED);
			fromSavedInstanceState = true;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		labelsListView = (ListView) inflater.inflate(R.layout.navigation_drawer, container, false);

		// header
		final View allLabelsItem = inflater.inflate(R.layout.label_list_item, container, false);
		((TextView) allLabelsItem.findViewById(R.id.label_name)).setTypeface(null, Typeface.BOLD);
		allLabelsItem.findViewById(R.id.label_color).setVisibility(View.GONE);
		labelsListView.addHeaderView(allLabelsItem);

		// footer
		final View addNewLabelItem = inflater.inflate(R.layout.label_list_item, container, false);
		((TextView) addNewLabelItem.findViewById(R.id.label_name)).setText(R.string.labels_drawer_add_label);
		addNewLabelItem.findViewById(R.id.label_color).setVisibility(View.GONE);
		labelsListView.addFooterView(addNewLabelItem);

		// adapter
		labelsListAdapter = new LabelsListAdapter();
		labelsListView.setAdapter(labelsListAdapter);

		// click listener
		labelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItem(position);
			}
		});

		// long click listener
		labelsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

				final List<NotesDatabaseEntry<Label>> labels = dbFacade.getAllLabels();
				final int labelItemIndex = position - 1;

				if (labelItemIndex >= 0 && labelItemIndex < labels.size()) { // not header or footer
					final NotesDatabaseEntry<Label> labelEntry = labels.get(labelItemIndex);
					// TODO implement as DialogFragment
					new AlertDialog.Builder(mainActivity).
							setTitle(NotesUtils.getTitleForLabel(labelEntry.getEntry())).
							setItems(R.array.label_actions, new LabelActionDialogClickListener(labelEntry)).
							setNegativeButton(R.string.common_cancel, null).
							create().show();

				}
				return true;
			}

		});

		return labelsListView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (fromSavedInstanceState && currentSelectedPosition != NO_LABEL_SELECTED) {
			selectItem(currentSelectedPosition);
		}
	}

	public boolean isDrawerOpen() {
		return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
	}

	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		fragmentContainerView = getActivity().findViewById(fragmentId);
		this.drawerLayout = drawerLayout;

		this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		drawerToggle = new ActionBarDrawerToggle(
				getActivity(),
				NavigationDrawerFragment.this.drawerLayout,
				R.drawable.ic_drawer,
				R.string.navigation_drawer_open,
				R.string.navigation_drawer_close
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				getActivity().invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}
				if (!userLearnedDrawer) {
					setUserLearnedDrawer();
				}
				getActivity().invalidateOptionsMenu();
			}
		};

		if (showDrawerOnStart || (!userLearnedDrawer && !fromSavedInstanceState)) {
			this.drawerLayout.openDrawer(fragmentContainerView);
		}

		this.drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				drawerToggle.syncState();
			}
		});
		this.drawerLayout.setDrawerListener(drawerToggle);
	}

	private void setUserLearnedDrawer() {
		userLearnedDrawer = true;
		final SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
	}

	private void selectItem(int position) {
		if (position == labelsListView.getCount() - 1) { // "New label" item position
			LabelEditDialog.show(getFragmentManager(), LabelEditDialog.NEW_LABEL, this);
		} else {
			currentSelectedPosition = position;
			if (drawerLayout != null) {
				drawerLayout.closeDrawer(fragmentContainerView);
			}

			final int labelId;
			final String newTitle;
			if (position == ALL_LABELS_HEADER_POSITION) {
				labelId = ALL_LABELS;
				newTitle = getString(R.string.labels_drawer_all_notes);
			} else {
				final List<NotesDatabaseEntry<Label>> allLabels = dbFacade.getAllLabels();
				final NotesDatabaseEntry<Label> labelEntry = allLabels.get(position - 1);
				labelId = labelEntry.getId();
				newTitle = NotesUtils.getTitleForLabel(labelEntry.getEntry());
			}

			mainActivity.onLabelSelected(labelId, newTitle);
		}
	}

	@Override
	public void onLabelChanged() {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (labelsListAdapter != null) {
					labelsListAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mainActivity = (MainActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerListener.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mainActivity = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (drawerLayout != null && isDrawerOpen()) {
			inflater.inflate(R.menu.drawer_menu, menu);
			showGlobalContextActionBar();
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.app_name);
	}

	private ActionBar getActionBar() {
		return getActivity().getActionBar();
	}


	/**
	 * ******************************************
	 * <p/>
	 * Inner classes
	 * <p/>
	 * *******************************************
	 */

	private class LabelsListAdapter extends ArrayAdapter<NotesDatabaseEntry<Label>> {

		private int[] labelsColors;

		public LabelsListAdapter() {
			super(mainActivity, 0, dbFacade.getAllLabels());
			labelsColors = getResources().getIntArray(R.array.label_colors);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(R.layout.label_list_item, parent, false);
			}

			final NotesDatabaseEntry<Label> entry = dbFacade.getAllLabels().get(position);
			final View color = view.findViewById(R.id.label_color);
			final TextView name = (TextView) view.findViewById(R.id.label_name);
			final int labelColor = labelsColors[entry.getEntry().getColor()];
			name.setText(NotesUtils.getTitleForLabel(entry.getEntry()));
			name.setTextColor(labelColor);
			color.setBackgroundColor(labelColor);

			return view;
		}

		@Override
		public int getCount() {
			return dbFacade.getAllLabels().size();
		}

	}

	private class LabelActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int EDIT_INDEX = 0;
		private final int DELETE_INDEX = 1;

		private NotesDatabaseEntry<Label> labelEntry;

		public LabelActionDialogClickListener(NotesDatabaseEntry<Label> labelEntry) {
			if (labelEntry == null) {
				throw new NullPointerException("Label entry is null");
			}
			this.labelEntry = labelEntry;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case EDIT_INDEX:
					LabelEditDialog.show(getFragmentManager(), labelEntry.getId(), NavigationDrawerFragment.this);
					break;
				case DELETE_INDEX:
					showDeleteDialog();
					break;
			}
		}

		private void showDeleteDialog() {
			// TODO implement as DialogFragment
			new AlertDialog.Builder(mainActivity).
					setTitle(NotesUtils.getTitleForLabel(labelEntry.getEntry())).
					setMessage("\n" + getString(R.string.label_action_delete_confirm_dialog_text) + "\n").
					setNegativeButton(R.string.common_no, null).
					setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							NotesApplication.executeInBackground(new Runnable() {
								@Override
								public void run() {
									dbFacade.deleteLabel(labelEntry.getId());
									mainActivity.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											labelsListAdapter.notifyDataSetChanged();
										}
									});
								}
							});
						}
					}).create().show();
		}
	}

	public static interface NavigationDrawerListener {
		void onLabelSelected(int id, String newTitle);
	}
}
