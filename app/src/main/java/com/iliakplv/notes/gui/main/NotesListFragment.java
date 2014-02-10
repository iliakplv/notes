package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NotesListFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
		NotesDatabaseFacade.DatabaseChangeListener {

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
	private MainActivity mainActivity;
	private NotesListAdapter listAdapter;
	private boolean listeningDatabase = false;

	// TODO replace usages by real labels ids
	private static final int TEMP_ALL_LABELS = NotesDatabaseFacade.ALL_LABELS;

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
		mainActivity.onNoteSelected(dbFacade.getNotesForLabel(TEMP_ALL_LABELS).get(position).getId());
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		return showNoteMenu(i);
	}

	public boolean showNoteMenu(int position) {
		final NotesDatabaseEntry selectedNoteEntry = dbFacade.getNotesForLabel(TEMP_ALL_LABELS).get(position);

		// Show available actions for note
		new AlertDialog.Builder(mainActivity).
				setTitle(getTitleForNote(selectedNoteEntry)).
				setItems(R.array.note_actions, new NoteActionDialogClickListener(selectedNoteEntry)).
				setNegativeButton(R.string.common_cancel, null).
				create().show();

		return true;
	}

	@Override
	public void onDatabaseChanged() {
		if (listeningDatabase && listAdapter != null) {
			mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listeningDatabase && listAdapter != null) {
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


	// List item texts

	public static String getTitleForNote(NotesDatabaseEntry<AbstractNote> entry) {
		return getTitleForNote(entry.getEntry(), entry.getId());
	}

	public static String getTitleForNote(AbstractNote note, int number) {
		final String originalTitle = note.getTitle();
		final String originalBody = note.getBody();

		if (!StringUtils.isBlank(originalTitle)) {
			return originalTitle;
		} else if (!StringUtils.isBlank(originalBody)) {
			return originalBody;
		} else {
			final String suffix = (number > 1) ? " " + String.valueOf(number) : "";
			return NotesApplication.getContext().getString(R.string.empty_note_placeholder) + suffix;
		}
}

	public static String getBodyForNote(AbstractNote note) {
		final String originalTitle = note.getTitle();
		final String originalBody = note.getBody();

		if (!StringUtils.isBlank(originalTitle)) {
			// title not blank - show body under the title
			return originalBody;
		} else {
			// title blank - body or placeholder will be shown as a title
			// don't show body
			return "";
		}
	}


	/**
	 * ******************************************
	 * <p/>
	 * Inner classes
	 * <p/>
	 * *******************************************
	 */

	private class NotesListAdapter extends ArrayAdapter<NotesDatabaseEntry> {

		private final int[] LABELS_IDS = {
				R.id.label_1,
				R.id.label_2,
				R.id.label_3,
				R.id.label_4,
				R.id.label_5};

		private int [] labelsColors;

		public NotesListAdapter() {
			super(mainActivity, 0, dbFacade.getNotesForLabel(TEMP_ALL_LABELS));
			labelsColors = getResources().getIntArray(R.array.label_colors);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(R.layout.note_list_item, parent, false);
			}

			final NotesDatabaseEntry<AbstractNote> entry = dbFacade.getNotesForLabel(TEMP_ALL_LABELS).get(position);
			final TextView title = (TextView) view.findViewById(R.id.title);
			final TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			title.setText(getTitleForNote(entry));
			subtitle.setText(getBodyForNote(entry.getEntry()));

			final List<NotesDatabaseEntry<Label>> labelEntries = dbFacade.getLabelsForNote(entry.getId());
			final int labelsCount = Math.min(LABELS_IDS.length, labelEntries.size());
			for (int i = 0; i < labelsCount; i++) {
				final View labelView = view.findViewById(LABELS_IDS[i]);
				labelView.setBackgroundColor(labelsColors[labelEntries.get(i).getEntry().getColor()]);
				labelView.setVisibility(View.VISIBLE);
			}

			return view;
		}

		@Override
		public int getCount() {
			return dbFacade.getNotesForLabelCount(TEMP_ALL_LABELS);
		}

	}

	private class NoteActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int INFO_INDEX = 0;
		private final int DELETE_INDEX = 1;

		private NotesDatabaseEntry<AbstractNote> noteEntry;

		public NoteActionDialogClickListener(NotesDatabaseEntry noteEntry) {
			if (noteEntry == null) {
				throw new NullPointerException("Note entry is null");
			}
			this.noteEntry = noteEntry;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case INFO_INDEX:
					showNoteInfo();
					break;
				case DELETE_INDEX:
					showDeleteDialog();
					break;
			}
		}

		private void showDeleteDialog() {
			new AlertDialog.Builder(mainActivity).
					setTitle(getTitleForNote(noteEntry)).
					setMessage("\n" + getString(R.string.note_action_delete_confirm_dialog_text) + "\n").
					setNegativeButton(R.string.common_no, null).
					setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							NotesApplication.executeInBackground(new Runnable() {
								@Override
								public void run() {
									NotesDatabaseFacade.getInstance().deleteNote(noteEntry.getId());
								}
							});
						}
					}).create().show();
		}

		private void showNoteInfo() {
			final String timeFormat = "HH:mm";

			final DateTime created = noteEntry.getEntry().getCreateTime();
			final String createdString = created.toLocalDate().toString() + " " +
					created.toLocalTime().toString(timeFormat);

			final DateTime changed = noteEntry.getEntry().getChangeTime();
			final String changedString = changed.toLocalDate().toString() + " " +
					changed.toLocalTime().toString(timeFormat);

			final String info = "\n" + getString(R.string.note_info_created, createdString) +
					"\n\n" + getString(R.string.note_info_modified, changedString) + "\n";

			new AlertDialog.Builder(mainActivity).
					setTitle(getTitleForNote(noteEntry)).
					setMessage(info).
					setNegativeButton(R.string.common_ok, null).
					create().show();
		}
	}

}
