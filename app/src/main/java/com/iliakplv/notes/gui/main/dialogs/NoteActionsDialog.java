package com.iliakplv.notes.gui.main.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteActionsDialog extends DialogFragment {

	public static final String EXTRA_NOTE_ID = "note_id";
	private static final String FRAGMENT_TAG = "note_actions_dialog";

	private NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
	private Activity activity;
	private int noteId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		noteId = getArguments().getInt(EXTRA_NOTE_ID);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final NotesDatabaseEntry selectedNoteEntry = dbFacade.getNote(noteId);
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote((AbstractNote) selectedNoteEntry.getEntry())).
				setItems(R.array.note_actions, new NoteActionDialogClickListener(selectedNoteEntry)).
				setNegativeButton(R.string.common_cancel, null).
				create();
	}

	public static void show(FragmentManager fragmentManager, int noteId) {
		NoteActionsDialog dialog = new NoteActionsDialog();
		final Bundle args = new Bundle();
		args.putInt(EXTRA_NOTE_ID, noteId);
		dialog.setArguments(args);
		dialog.show(fragmentManager, FRAGMENT_TAG);
	}


	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	private class NoteActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int LABELS_INDEX = 0;
		private final int INFO_INDEX = 1;
		private final int DELETE_INDEX = 2;

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
				case LABELS_INDEX:
					showNoteLabelsDialog();
					break;
				case INFO_INDEX:
					showNoteInfo();
					break;
				case DELETE_INDEX:
					showDeleteDialog();
					break;
			}
		}

		private void showDeleteDialog() {
			new AlertDialog.Builder(activity).
					setTitle(NotesUtils.getTitleForNote(noteEntry.getEntry())).
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

			new AlertDialog.Builder(activity).
					setTitle(NotesUtils.getTitleForNote(noteEntry.getEntry())).
					setMessage(info).
					setNegativeButton(R.string.common_ok, null).
					create().show();
		}

		private void showNoteLabelsDialog() {

			// TODO [low] implement label creation in NoteLabelsDialog
			if(dbFacade.getAllLabels().size() == 0) {
				Toast.makeText(activity, "No labels created...", Toast.LENGTH_SHORT).show();
				return;
			}

			final NoteLabelsListAdapter labelsAdapter = new NoteLabelsListAdapter(noteEntry.getId());
			new AlertDialog.Builder(activity)
					.setTitle(NotesUtils.getTitleForNote(noteEntry.getEntry()))
					.setAdapter(labelsAdapter, null)
					.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							labelsAdapter.applyNoteLabelsChanges();
						}
					})
					.setNegativeButton(R.string.common_cancel, null)
					.create().show();
		}
	}

	private class NoteLabelsListAdapter extends ArrayAdapter<NotesDatabaseEntry<Label>> {

		private final int[] labelsColors;

		private final int noteId;
		private final List<NotesDatabaseEntry<Label>> allLabels;
		private final boolean[] currentLabels;
		private final boolean[] selectedLabels;

		public NoteLabelsListAdapter(int noteId) {
			super(activity, 0, dbFacade.getAllLabels());
			labelsColors = getResources().getIntArray(R.array.label_colors);

			this.noteId = noteId;
			this.allLabels = dbFacade.getAllLabels();

			final Set<Integer> currentNoteLabelsIds = dbFacade.getLabelsIdsForNote(noteId);
			currentLabels = new boolean[allLabels.size()];
			selectedLabels = new boolean[allLabels.size()];
			for (int i = 0; i < currentLabels.length; i++) {
				final boolean selected = currentNoteLabelsIds.contains(allLabels.get(i).getId());
				currentLabels[i] = selected;
				selectedLabels[i] = selected;
			}
		}

		@Override
		public int getCount() {
			return allLabels.size();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(R.layout.label_list_item_checkbox, parent, false);
			}

			final View color = view.findViewById(R.id.label_color);
			final TextView name = (TextView) view.findViewById(R.id.label_name);

			final Label label = allLabels.get(position).getEntry();
			name.setText(NotesUtils.getTitleForLabel(label));
			color.setBackgroundColor(labelsColors[label.getColor()]);

			final android.widget.CheckBox checkBox = (android.widget.CheckBox) view.findViewById(R.id.checkbox);
			checkBox.setChecked(currentLabels[position]);
			// TODO [low] refactor this
			checkBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selectedLabels[position] = ((CheckBox) v).isChecked();
				}
			});

			return view;
		}

		public void applyNoteLabelsChanges() {
			final Set<Integer> labelsIdsToAdd = new HashSet<Integer>();
			final Set<Integer> labelsIdsToDelete = new HashSet<Integer>();

			for (int i = 0; i < allLabels.size(); i++) {
				final int labelId = allLabels.get(i).getId();
				if (!currentLabels[i] && selectedLabels[i]) {
					labelsIdsToAdd.add(labelId);
				} else if (currentLabels[i] && !selectedLabels[i]) {
					labelsIdsToDelete.add(labelId);
				}
			}

			NotesApplication.executeInBackground(new Runnable() {
				@Override
				public void run() {
					for (int labelId : labelsIdsToDelete) {
						dbFacade.deleteLabelFromNote(noteId, labelId);
					}
					for (int labelId : labelsIdsToAdd) {
						dbFacade.insertLabelToNote(noteId, labelId);
					}
				}
			});
		}
	}
}
