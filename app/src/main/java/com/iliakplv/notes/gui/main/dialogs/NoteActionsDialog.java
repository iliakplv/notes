package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.gui.main.MainActivity;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

import org.joda.time.DateTime;

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
					showNoteInfoDialog();
					break;
				case DELETE_INDEX:
					showNoteDeleteDialog();
					break;
			}
		}

		private void showNoteDeleteDialog() {
			// TODO implement as DialogFragment
			new AlertDialog.Builder(activity).
					setTitle(NotesUtils.getTitleForNote(noteEntry.getEntry())).
					setMessage(StringUtils.wrapWithEmptyLines(getString(R.string.note_action_delete_confirm_dialog_text))).
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
			// TODO implement as DialogFragment
			final String timeFormat = "HH:mm";

			final DateTime createTime = noteEntry.getEntry().getCreateTime();
			final DateTime changeTime = noteEntry.getEntry().getChangeTime();

			final String createdString = createTime.toLocalDate().toString() + " " +
					createTime.toLocalTime().toString(timeFormat);
			String info = StringUtils.wrapWithEmptyLines(getString(R.string.note_info_created, createdString));

			if (!createTime.equals(changeTime)) {
				final String changedString = changeTime.toLocalDate().toString() + " " +
						changeTime.toLocalTime().toString(timeFormat);
				info += StringUtils.wrapWithEmptyLines(getString(R.string.note_info_modified, changedString));
			}

			new AlertDialog.Builder(activity).
					setTitle(NotesUtils.getTitleForNote(noteEntry.getEntry())).
					setMessage(info).
					setNegativeButton(R.string.common_ok, null).
					create().show();
		}

		private void showNoteLabelsDialog() {
			final boolean noLabelsCreated = dbFacade.getAllLabels().size() == 0;
			if(noLabelsCreated) {
				showNoLabelsCreatedDialog();
			} else {
				NoteLabelsDialog.show(activity.getFragmentManager(), id);
			}
		}

		private void showNoLabelsCreatedDialog() {
			// TODO implement as DialogFragment
			new AlertDialog.Builder(activity).
					setTitle(NotesUtils.getTitleForNote(noteEntry.getEntry())).
					setMessage(StringUtils.wrapWithEmptyLines(getString(R.string.note_action_no_labels_dialog_text))).
					setNegativeButton(R.string.common_no, null).
					setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							((MainActivity) activity).createNewLabelForNote(id);
						}
					}).create().show();
		}
	}
}
