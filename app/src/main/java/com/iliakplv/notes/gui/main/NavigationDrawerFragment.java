package com.iliakplv.notes.gui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.main.dialogs.LabelEditDialog;
import com.iliakplv.notes.gui.main.dialogs.SimpleItemDialog;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;
import com.iliakplv.notes.notes.storage.Storage;

import java.io.Serializable;
import java.util.List;

public class NavigationDrawerFragment extends Fragment implements
		LabelEditDialog.LabelEditDialogCallback, NotesStorageListener {

	public static final Integer ALL_LABELS = NotesStorage.NOTES_FOR_ALL_LABELS;
	private static final int ALL_LABELS_HEADER_POSITION = 0;

	private final NotesStorage storage = Storage.getStorage();
	private MainActivity mainActivity;

	private ActionBarDrawerToggle drawerToggle;
	private DrawerLayout drawerLayout;
	private View fragmentContainerView;
	private ListView labelsListView;
	private LabelsListAdapter labelsListAdapter;


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

				final List<Label> labels = storage.getAllLabels();
				final int labelItemIndex = position - 1;

				if (labelItemIndex >= 0 && labelItemIndex < labels.size()) { // not header or footer
					final Label label = labels.get(labelItemIndex);
					SimpleItemDialog.show(SimpleItemDialog.DialogType.LabelActions,
							label.getId(),
							mainActivity.getFragmentManager());
				}
				return true;
			}

		});

		return labelsListView;
	}

	@Override
	public void onResume() {
		super.onResume();
		storage.addStorageListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		storage.removeStorageListener(this);
	}

	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		fragmentContainerView = getActivity().findViewById(fragmentId);
		this.drawerLayout = drawerLayout;
		this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		final ActionBar actionBar = getActionBar();
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
				getActivity().invalidateOptionsMenu();
			}
		};

		this.drawerLayout.post(new Runnable() {
			@Override
			public void run() {
				drawerToggle.syncState();
			}
		});
		this.drawerLayout.setDrawerListener(drawerToggle);
	}

	public boolean isDrawerOpen() {
		return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
	}

	public void closeDrawer() {
		if (isDrawerOpen()) {
			drawerLayout.closeDrawer(fragmentContainerView);
		}
	}

	private void selectItem(int position) {
		if (position == labelsListView.getCount() - 1) {
			// 'New label' position
			createNewLabel();
		} else {
			closeDrawer();

			final Serializable labelId;
			if (position == ALL_LABELS_HEADER_POSITION) {
				labelId = ALL_LABELS;
			} else {
				final List<Label> allLabels = storage.getAllLabels();
				final Label label = allLabels.get(position - 1);
				labelId = label.getId();
			}

			mainActivity.onLabelSelected(labelId);
		}
	}

	private void createNewLabel() {
		showLabelEditDialog(LabelEditDialog.NEW_LABEL);
	}

	public void showLabelEditDialog(Serializable labelId) {
		LabelEditDialog.show(mainActivity.getFragmentManager(),
				labelId,
				this);
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
		mainActivity = (MainActivity) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mainActivity = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (drawerLayout != null && isDrawerOpen()) {
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
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	private ActionBar getActionBar() {
		return getActivity().getActionBar();
	}

	@Override
	public void onContentChanged() {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (labelsListAdapter != null) {
					labelsListAdapter.notifyDataSetChanged();
				}
			}
		});
	}


	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	private class LabelsListAdapter extends ArrayAdapter<Label> {

		private int[] labelsColors;

		public LabelsListAdapter() {
			super(mainActivity, 0, storage.getAllLabels());
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

			final Label label = storage.getAllLabels().get(position);
			final View color = view.findViewById(R.id.label_color);
			final TextView name = (TextView) view.findViewById(R.id.label_name);
			final int labelColor = labelsColors[label.getColor()];
			name.setText(NotesUtils.getTitleForLabel(label));
			name.setTextColor(labelColor);
			color.setBackgroundColor(labelColor);

			return view;
		}

		@Override
		public int getCount() {
			return storage.getAllLabels().size();
		}
	}

	public static interface NavigationDrawerListener {
		void onLabelSelected(Serializable id);
	}
}
