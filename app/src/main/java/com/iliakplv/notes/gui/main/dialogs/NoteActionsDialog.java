package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;

public class NoteActionsDialog extends AbstractItemDialog {

	private static final String FRAGMENT_TAG = "note_actions_dialog";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final NotesDatabaseEntry selectedNoteEntry = dbFacade.getNote(id);
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote((AbstractNote) selectedNoteEntry.getEntry())).
				setItems(R.array.note_actions, new NoteActionDialogClickListener(selectedNoteEntry)).
				setNegativeButton(R.string.common_cancel, null).
				create();
	}

	public static void show(FragmentManager fragmentManager, int noteId) {
		final NoteActionsDialog dialog = new NoteActionsDialog();
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
					showSimpleDialog(SimpleItemDialog.DialogType.NoteInfo);
					break;
				case DELETE_INDEX:
					showSimpleDialog(SimpleItemDialog.DialogType.NoteDelete);
					break;
			}
		}

		private void showNoteLabelsDialog() {
			final boolean noLabelsCreated = dbFacade.getAllLabels().size() == 0;
			if(noLabelsCreated) {
				showSimpleDialog(SimpleItemDialog.DialogType.NoteNoLabels);
			} else {
				NoteLabelsDialog.show(activity.getFragmentManager(), id);
			}
		}

		private void showSimpleDialog(SimpleItemDialog.DialogType type) {
			SimpleItemDialog.show(type,
					noteEntry.getId(),
					activity.getFragmentManager());
		}
	}
}
