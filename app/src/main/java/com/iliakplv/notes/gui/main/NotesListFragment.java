package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.app.Fragment;
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
import java.util.List;

public class NotesListFragment extends Fragment implements NotesStorageListener {
	public static final String TAG = NotesListFragment.class.getSimpleName();

	private MainActivity mainActivity;
	private int [] labelsColors;
	private boolean isUiVisible = false;
	private NotesListAdapter listAdapter;
	private TextView status;
	private TextView noNotesText;

	private boolean showSearchResults = false;
	private String searchQuery;

	private static final Integer ALL_LABELS = NotesStorage.NOTES_FOR_ALL_LABELS;
	private final NotesStorage storage = Storage.getStorage();
	private Serializable currentLabelId = ALL_LABELS;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		labelsColors = getResources().getIntArray(R.array.label_colors);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mainActivity = (MainActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_list, container, false);

		// list
		final ListView listView = (ListView) view.findViewById(R.id.notes_list);
		listAdapter = new NotesListAdapter();
		listView.setAdapter(listAdapter);
		listView.setDivider(null);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mainActivity.onNoteSelected(getNotesList().get(position).getId());
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				return showNoteActionsDialog(i);
			}
		});

		// status
		status = (TextView) view.findViewById(R.id.status);
		status.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainActivity.onBackPressed();
			}
		});

		// no notes text
		noNotesText = (TextView) view.findViewById(R.id.no_notes_text);
		noNotesText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mainActivity.createNewNote();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		isUiVisible = true;
		startListeningStorage();
		updateUi();
	}

	@Override
	public void onPause() {
		super.onPause();
		isUiVisible = false;
		stopListeningStorage();
	}

	private List<AbstractNote> getNotesList() {
		return showSearchResults ?
				storage.getNotesForQuery(searchQuery) :
				storage.getNotesForLabel(currentLabelId);
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
		updateUiFromBackgroundThread();
	}

	public void showNotesForLabel(Serializable labelId) {
		currentLabelId = labelId;
		showSearchResults = false;
		updateUi();
	}

	public void showNotesForQuery(String searchQuery) {
		this.searchQuery = searchQuery;
		showSearchResults = true;
		updateUi();
	}


	private void updateListView() {
		if (isUiVisible && listAdapter != null) {
			listAdapter.notifyDataSetChanged();
		}
	}

	private void updateStatus() {
		if (isUiVisible && status != null) {
			if (showSearchResults) {
				status.setVisibility(View.VISIBLE);
				status.setBackgroundColor(getResources().getColor(R.color.status_search_background));
				status.setText(getString(R.string.action_bar_search_results, searchQuery));
			} else if (!ALL_LABELS.equals(currentLabelId) && storage.getLabel(currentLabelId) != null) {
				status.setVisibility(View.VISIBLE);
				final Label label = storage.getLabel(currentLabelId);
				status.setBackgroundColor(labelsColors[label.getColor()]);
				status.setText(getString(R.string.action_bar_label_selected, NotesUtils.getTitleForLabel(label)));
			} else {
				status.setVisibility(View.GONE);
			}
		}
	}

	private void updateNoNotesText() {
		if (isUiVisible && noNotesText != null) {
			if (getNotesList().isEmpty()) {
				noNotesText.setVisibility(View.VISIBLE);
				if (showSearchResults) {
					noNotesText.setText(R.string.no_notes_for_search);
					noNotesText.setClickable(false);
				} else if (!ALL_LABELS.equals(currentLabelId)) {
					noNotesText.setText(R.string.no_notes_for_label);
					noNotesText.setClickable(false);
				} else {
					noNotesText.setText(R.string.no_notes_yet);
					noNotesText.setClickable(true);
				}
			} else {
				noNotesText.setVisibility(View.GONE);
			}
		}
	}

	private void updateUiFromBackgroundThread() {
		if (mainActivity != null) {
			mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateUi();
				}
			});
		}
	}

	private void updateUi() {
		updateListView();
		updateStatus();
		updateNoNotesText();
	}


	private void startListeningStorage() {
		storage.addStorageListener(this);
	}

	private void stopListeningStorage() {
		storage.removeStorageListener(this);
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



		public NotesListAdapter() {
			super(mainActivity, 0, getNotesList());
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
			final List<Label> labels = storage.getLabelsForNote(note.getId());
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
			// show ellipsis if not enough space to show all labels
			if (labels.size() > LABELS_IDS.length) {
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
