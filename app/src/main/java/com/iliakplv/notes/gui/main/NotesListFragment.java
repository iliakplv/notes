package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.main.dialogs.SimpleItemDialog;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NotesListFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
		NotesDatabaseFacade.DatabaseChangeListener {

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
	private MainActivity mainActivity;
	private NotesListAdapter listAdapter;
	private boolean listeningDatabase = false;

	private static final int ALL_LABELS = NotesDatabaseFacade.ALL_LABELS;
	private int currentLabelId = ALL_LABELS;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listAdapter = new NotesListAdapter();
		setListAdapter(listAdapter);
		getListView().setOnItemLongClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		startListeningDatabase();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListeningDatabase();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mainActivity.onNoteSelected(dbFacade.getNotesForLabel(currentLabelId).get(position).getId());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		return showNoteActionsDialog(i);
	}

	private boolean showNoteActionsDialog(int position) {
		final int noteId = dbFacade.getNotesForLabel(currentLabelId).get(position).getId();
		SimpleItemDialog.show(SimpleItemDialog.DialogType.NoteActions,
				noteId,
				mainActivity.getFragmentManager());
		return true;
	}

	@Override
	public void onDatabaseChanged() {
		if (listeningDatabase) {
			updateListView();
		}
	}

	public void showNotesForLabel(int labelId) {
		if (labelId == ALL_LABELS || labelId >= 1) {
			currentLabelId = labelId;
			updateListView();
		} else {
			throw new IllegalArgumentException("Wrong label id value: " + labelId);
		}
	}

	private void updateListView() {
		if (mainActivity != null) {
			mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listAdapter != null) {
						listAdapter.notifyDataSetChanged();
					}
				}
			});
		}
	}

	private void startListeningDatabase() {
		if (!listeningDatabase) {
			dbFacade.addDatabaseChangeListener(this);
			listeningDatabase = true;
		}
	}

	private void stopListeningDatabase() {
		if (listeningDatabase) {
			dbFacade.removeDatabaseChangeListener(this);
			listeningDatabase = false;
		}
	}


	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	private class NotesListAdapter extends ArrayAdapter<AbstractNote> {

		private final int[] LABELS_IDS = {
				R.id.label_1,
				R.id.label_2,
				R.id.label_3,
				R.id.label_4};

		private int [] labelsColors;

		public NotesListAdapter() {
			super(mainActivity, 0, dbFacade.getNotesForLabel(currentLabelId));
			labelsColors = getResources().getIntArray(R.array.label_colors);
		}

		@Override
		public int getCount() {
			return dbFacade.getNotesForLabelCount(currentLabelId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(R.layout.note_list_item, parent, false);
			}

			// texts
			final AbstractNote note = dbFacade.getNotesForLabel(currentLabelId).get(position);
			final TextView title = (TextView) view.findViewById(R.id.title);
			final TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			title.setText(NotesUtils.getTitleForNote(note));
			if (NotesUtils.isNoteTitleEmpty(note)) {
				title.setTextColor(getResources().getColor(R.color.note_list_item_placeholder));
				subtitle.setTextColor(getResources().getColor(R.color.note_list_item_black));
			} else {
				title.setTextColor(getResources().getColor(R.color.note_list_item_black));
				subtitle.setTextColor(getResources().getColor(R.color.note_list_item_grey));
			}
			subtitle.setText(note.getBody());

			// labels
			final boolean showingNotesForAllLabels = currentLabelId == ALL_LABELS;
			final List<Label> labels;
			if (showingNotesForAllLabels) {
				labels = dbFacade.getLabelsForNote(note.getId());
			} else {
				labels = new ArrayList<Label>(1);
				labels.add(dbFacade.getLabel(currentLabelId));
			}
			for (int i = 0; i < LABELS_IDS.length; i++) {
				final TextView labelView = (TextView) view.findViewById(LABELS_IDS[i]);
				if (i < labels.size()) {
					final Label label = labels.get(i);
					labelView.setBackgroundColor(labelsColors[label.getColor()]);
					labelView.setText(getLetterForLabelName(label.getName()));
					labelView.setVisibility(View.VISIBLE);
				} else {
					labelView.setVisibility(View.GONE);
				}
			}
			// show [...] sign
			// if showing notes for selected label or
			// if not enough space to show all labels
			if (!showingNotesForAllLabels || labels.size() > LABELS_IDS.length) {
				view.findViewById(R.id.more_labels).setVisibility(View.VISIBLE);
			} else {
				view.findViewById(R.id.more_labels).setVisibility(View.GONE);
			}

			return view;
		}

		private String getLetterForLabelName(String name) {
			return StringUtils.isNullOrEmpty(name) ? "" : name.substring(0, 1).toUpperCase();
		}
	}

}
