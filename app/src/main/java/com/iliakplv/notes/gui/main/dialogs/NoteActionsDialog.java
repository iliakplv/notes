package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

import org.joda.time.DateTime;

public class NoteActionsDialog extends AbstractNoteDialog {

	private static final String FRAGMENT_TAG = "note_actions_dialog";

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
					showNoteInfoDialog();
					break;
				case DELETE_INDEX:
					showNoteDeleteDialog();
					break;
			}
		}

		private void showNoteDeleteDialog() {
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

		private void showNoteInfoDialog() {
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
			NoteLabelsDialog.show(activity.getFragmentManager(), noteId);
		}
	}
}
