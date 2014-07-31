package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.TypedValue;
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
import com.iliakplv.notes.notes.storage.NotesStorage;
import com.iliakplv.notes.notes.storage.NotesStorageListener;
import com.iliakplv.notes.notes.storage.Storage;
import com.iliakplv.notes.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NotesListFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
		NotesStorageListener {

	public static final String TAG = NotesListFragment.class.getSimpleName();

	private final NotesStorage storage = Storage.getStorage();
	private MainActivity mainActivity;
	private NotesListAdapter listAdapter;
	private boolean listeningStorage = false;

	private static final Integer ALL_LABELS = NotesStorage.NOTES_FOR_ALL_LABELS;
	private Serializable currentLabelId = ALL_LABELS;

	private boolean showSearchResults = false;
	private String searchQuery;


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
		getListView().setDivider(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		startListeningStorage();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListeningStorage();
	}

	private List<AbstractNote> getNotesList() {
		return showSearchResults ?
				storage.getNotesForQuery(searchQuery) :
				storage.getNotesForLabel(currentLabelId);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mainActivity.onNoteSelected(getNotesList().get(position).getId());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		return showNoteActionsDialog(i);
	}

	private boolean showNoteActionsDialog(int position) {
		final Serializable noteId = getNotesList().get(position).getId();
		SimpleItemDialog.show(SimpleItemDialog.DialogType.NoteActions,
				noteId,
				mainActivity.getFragmentManager());
		return true;
	}

	@Override
	public void onContentChanged() {
		if (listeningStorage) {
			updateListView();
		}
	}

	public void showNotesForLabel(Serializable labelId) {
		currentLabelId = labelId;
		showSearchResults = false;
		updateListView();
	}

	public void showNotesForQuery(String searchQuery) {
		this.searchQuery = searchQuery;
		showSearchResults = true;
		updateListView();
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

	private void startListeningStorage() {
		if (!listeningStorage) {
			storage.addStorageListener(this);
			listeningStorage = true;
		}
	}

	private void stopListeningStorage() {
		if (listeningStorage) {
			storage.removeStorageListener(this);
			listeningStorage = false;
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
			super(mainActivity, 0, getNotesList());
			labelsColors = getResources().getIntArray(R.array.label_colors);
		}

		@Override
		public int getCount() {
			return getNotesList().size();
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
			final AbstractNote note = getNotesList().get(position);
			final TextView title = (TextView) view.findViewById(R.id.title);
			final TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			title.setText(NotesUtils.getTitleForNoteInList(note));
			if (!NotesUtils.isNoteTitleBlank(note)) {
				title.setTextColor(getResources().getColor(R.color.note_list_item_black));
				title.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(R.dimen.note_list_item_large_text_size));
				subtitle.setTextColor(getResources().getColor(R.color.note_list_item_grey));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(R.dimen.note_list_item_small_text_size));
			} else {
				title.setTextColor(getResources().getColor(R.color.note_list_item_placeholder));
				title.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(R.dimen.note_list_item_small_text_size));
				subtitle.setTextColor(getResources().getColor(R.color.note_list_item_black));
				subtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(R.dimen.note_list_item_large_text_size));
			}
			subtitle.setText(note.getBody().trim());

			// labels
			final boolean showOneLabel = !showSearchResults && !ALL_LABELS.equals(currentLabelId);
			final List<Label> labels;
			if (showOneLabel) {
				labels = new ArrayList<Label>(1);
				labels.add(storage.getLabel(currentLabelId));
			} else {
				labels = storage.getLabelsForNote(note.getId());
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
			if (showOneLabel || labels.size() > LABELS_IDS.length) {
				view.findViewById(R.id.more_labels).setVisibility(View.VISIBLE);
			} else {
				view.findViewById(R.id.more_labels).setVisibility(View.GONE);
			}

			return view;
		}

		private String getLetterForLabelName(String name) {
			return StringUtils.isBlank(name) ? "" : name.trim().substring(0, 1).toUpperCase();
		}
	}

}
