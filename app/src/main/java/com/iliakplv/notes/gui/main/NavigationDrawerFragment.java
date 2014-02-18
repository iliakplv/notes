package com.iliakplv.notes.gui.main;


import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;


public class NavigationDrawerFragment extends Fragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	public static final int ALL_LABELS = NotesDatabaseFacade.ALL_LABELS;
	public static final int ALL_LABELS_HEADER_POSITION = 0;

	private static final int[] COLORS_CHECKBOXES_IDS = {
			R.id.color_1,
			R.id.color_2,
			R.id.color_3,
			R.id.color_4,
			R.id.color_5,
			R.id.color_6,
			R.id.color_7,
			R.id.color_8
	};


	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
    private MainActivity mainActivity;

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView labelsListView;
    private View fragmentContainerView;

    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;


    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }

//      TODO  selectItem(currentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        labelsListView = (ListView) inflater.inflate(
                R.layout.navigation_drawer, container, false);

	    labelsListView.setAdapter(new LabelsListAdapter());
	    labelsListView.addHeaderView(inflater.inflate(R.layout.label_list_item, container, false));
	    final View addNewLabelItem = inflater.inflate(R.layout.label_list_item, container, false);
	    ((TextView) addNewLabelItem.findViewById(R.id.label_name)).setText(R.string.labels_drawer_add_label);
	    labelsListView.addFooterView(addNewLabelItem);
        labelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		        selectItem(position);
	        }
        });

//      TODO  labelsListView.setItemChecked(currentSelectedPosition, true);
        return labelsListView;
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
                    userLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        if (!userLearnedDrawer && !fromSavedInstanceState) {
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

    private void selectItem(int position) {
        currentSelectedPosition = position;
        if (labelsListView != null) {
            labelsListView.setItemChecked(position, true);
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (mainActivity != null) {
	        if (position == labelsListView.getCount() - 1) {

		        // Last position (Add label)
		        // TODO refactor this

		        // color selection
		        final LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        final View labelDialogView = inflater.inflate(R.layout.label_edit_dialog, null);
		        final LabelEditDialogCheckBoxListener checkBoxListener =
				        new LabelEditDialogCheckBoxListener((CheckBox) labelDialogView.findViewById(COLORS_CHECKBOXES_IDS[0]));
		        for (int  id : COLORS_CHECKBOXES_IDS) {
			        labelDialogView.findViewById(id).setOnClickListener(checkBoxListener);
		        }

		        // label edit dialog
		        new AlertDialog.Builder(mainActivity)
				        .setView(labelDialogView)
				        .setPositiveButton(R.string.common_save, new DialogInterface.OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialogInterface, int i) {
						        final String labelName = ((EditText) labelDialogView.findViewById(R.id.label_name)).getText().toString();
						        NotesApplication.executeInBackground(new Runnable() {
							        @Override
							        public void run() {
								        NotesDatabaseFacade.getInstance().insertLabel(new Label(labelName, getIndexOfSelectedColor(labelDialogView)));
							        }
						        });
					        }
				        })
				        .setNegativeButton(R.string.common_cancel, null)
				        .create()
				        .show();


	        } else {
                mainActivity.onSelectedLabelId(position == ALL_LABELS_HEADER_POSITION ?
		                ALL_LABELS :
		                dbFacade.getAllLabels().get(position - 1).getId());
	        }
        }
    }

	private int getIndexOfSelectedColor(View labelEditDialogView) {
		for (int i = 0; i < COLORS_CHECKBOXES_IDS.length; i++) {
			if (((CheckBox) labelEditDialogView.findViewById(COLORS_CHECKBOXES_IDS[i])).isChecked()) {
				return i;
			}
		}
		return 0;
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
            inflater.inflate(R.menu.drawer, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

//	    if (item.getItemId() == R.id.show_bookmarks) {
//
//		    FragmentManager fragmentManager = getFragmentManager();
//		    fragmentManager.beginTransaction()
//				    .replace(R.id.container,new BookmarksListFragment())
//				    .commit();
//
//		    return true;
//	    }

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
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	private class LabelsListAdapter extends ArrayAdapter<NotesDatabaseEntry<Label>> {


		private int [] labelsColors;

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

			final NotesDatabaseEntry<com.iliakplv.notes.notes.Label> entry = dbFacade.getAllLabels().get(position);
			final View color = view.findViewById(R.id.label_color);
			final TextView name = (TextView) view.findViewById(R.id.label_name);
			name.setText(entry.getEntry().getName());
			color.setBackgroundColor(labelsColors[entry.getEntry().getColor()]);

			return view;
		}

		@Override
		public int getCount() {
			return dbFacade.getAllLabels().size();
		}

	}

	private class LabelEditDialogCheckBoxListener implements View.OnClickListener {

		private CheckBox currentSelectedCheckBox;

		public LabelEditDialogCheckBoxListener(CheckBox selectedCheckBox) {
			currentSelectedCheckBox = selectedCheckBox;
		}

		@Override
		public void onClick(View newSelectedCheckBox) {
			currentSelectedCheckBox.setChecked(false);
			currentSelectedCheckBox = (CheckBox) newSelectedCheckBox;
		}
	}

    public static interface NavigationDrawerListener {

        void onSelectedLabelId(int id);

    }
}
