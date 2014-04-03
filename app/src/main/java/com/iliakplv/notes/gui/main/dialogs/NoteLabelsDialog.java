package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteLabelsDialog extends AbstractItemDialog {

	private static final String FRAGMENT_TAG = "note_labels_dialog";

	private static final String EXTRA_SELECTED_LABELS = "selected_labels";
	private boolean[] selectedLabels;
	private boolean fromSavedInstanceState = false;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			selectedLabels = savedInstanceState.getBooleanArray(EXTRA_SELECTED_LABELS);
			fromSavedInstanceState = true;
		}

		final NoteLabelsListAdapter labelsAdapter = new NoteLabelsListAdapter(id);
		return new AlertDialog.Builder(activity)
				.setTitle(NotesUtils.getTitleForNote(dbFacade.getNote(id).getEntry()))
				.setAdapter(labelsAdapter, null)
				.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						labelsAdapter.applyNoteLabelsChanges();
					}
				})
				.setNegativeButton(R.string.common_cancel, null)
				.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBooleanArray(EXTRA_SELECTED_LABELS, selectedLabels);
	}

	public static void show(FragmentManager fragmentManager, int noteId) {
		final NoteLabelsDialog dialog = new NoteLabelsDialog();
		dialog.setArguments(createArgumentsBundle(noteId));
		dialog.show(fragmentManager, FRAGMENT_TAG);
	}


	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	private class NoteLabelsListAdapter extends ArrayAdapter<NotesDatabaseEntry<Label>> {

		private final int[] labelsColors;

		private final int noteId;
		private final List<NotesDatabaseEntry<Label>> allLabels;
		private final boolean[] currentLabels;

		public NoteLabelsListAdapter(int noteId) {
			super(activity, 0, dbFacade.getAllLabels());
			labelsColors = getResources().getIntArray(R.array.label_colors);

			this.noteId = noteId;
			this.allLabels = dbFacade.getAllLabels();

			final Set<Integer> currentNoteLabelsIds = dbFacade.getLabelsIdsForNote(noteId);
			currentLabels = new boolean[allLabels.size()];
			for (int i = 0; i < currentLabels.length; i++) {
				currentLabels[i] = currentNoteLabelsIds.contains(allLabels.get(i).getId());
			}
			if (!fromSavedInstanceState) {
				selectedLabels = Arrays.copyOf(currentLabels, currentLabels.length);
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

			final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			checkBox.setChecked(selectedLabels[position]);
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
