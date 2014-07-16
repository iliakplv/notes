package com.iliakplv.notes.gui.main.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.analytics.Event;
import com.iliakplv.notes.analytics.EventTracker;
import com.iliakplv.notes.gui.main.MainActivity;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.utils.StringUtils;

import org.joda.time.DateTime;

import java.io.Serializable;

public class SimpleItemDialog extends AbstractItemDialog {

	private static final String EXTRA_TYPE = "dialog_type";
	private static final String FRAGMENT_TAG_PREFIX = "item_dialog_";


	// Dialogs creation

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final DialogType type = (DialogType) getArguments().getSerializable(EXTRA_TYPE);

		switch (type) {

			// for note
			case NoteActions:
				return createNoteActionsDialog();
			case NoteInfo:
				return createNoteInfoDialog();
			case NoteDelete:
				return createNoteDeleteDialog();
			case NoteNoLabels:
				return createNoteNoLabelsDialog();

			// for label
			case LabelActions:
				return createLabelActionsDialog();
			case LabelDelete:
				return createLabelDeleteDialog();

			default:
				throw new RuntimeException("Unknown dialog type: " + type.toString());
		}
	}

	private Dialog createNoteActionsDialog() {
		final AbstractNote selectedNote = storage.getNote(id);
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote(selectedNote)).
				setItems(R.array.note_actions, new NoteActionDialogClickListener()).
				setNegativeButton(R.string.common_cancel, null).
				create();
	}

	private Dialog createNoteInfoDialog() {
		final String timeFormat = "HH:mm";

		final AbstractNote note = storage.getNote(id);
		final DateTime createTime = note.getCreateTime();
		final DateTime changeTime = note.getChangeTime();

		final String createdString = createTime.toLocalDate().toString() + " " +
				createTime.toLocalTime().toString(timeFormat);
		String info = StringUtils.wrapWithEmptyLines(getString(R.string.note_info_created, createdString));

		if (!createTime.equals(changeTime)) {
			final String changedString = changeTime.toLocalDate().toString() + " " +
					changeTime.toLocalTime().toString(timeFormat);
			info += StringUtils.wrapWithEmptyLines(getString(R.string.note_info_modified, changedString));
		}

		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote(note)).
				setMessage(info).
				setNegativeButton(R.string.common_ok, null).
				create();
	}

	private Dialog createNoteDeleteDialog() {
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote(storage.getNote(id))).
				setMessage(StringUtils.wrapWithEmptyLines(getString(R.string.note_action_delete_confirm_dialog_text))).
				setNegativeButton(R.string.common_no, null).
				setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						NotesApplication.executeInBackground(new Runnable() {
							@Override
							public void run() {
								storage.deleteNote(id);
							}
						});
					}
				}).create();
	}

	private Dialog createNoteNoLabelsDialog() {
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote(storage.getNote(id))).
				setMessage(StringUtils.wrapWithEmptyLines(getString(R.string.note_action_no_labels_dialog_text))).
				setNegativeButton(R.string.common_no, null).
				setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						LabelEditDialog.showCreateAndSet(activity.getFragmentManager(),
								((MainActivity) activity).getNavigationDrawerFragment(),
								id);
					}
				}).create();
	}

	private Dialog createLabelActionsDialog() {
		final Label label = storage.getLabel(id);
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForLabel(label)).
				setItems(R.array.label_actions, new LabelActionDialogClickListener()).
				setNegativeButton(R.string.common_cancel, null).
				create();
	}

	private Dialog createLabelDeleteDialog() {
		final Label label = storage.getLabel(id);
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForLabel(label)).
				setMessage(StringUtils.wrapWithEmptyLines(getString(R.string.label_action_delete_confirm_dialog_text))).
				setNegativeButton(R.string.common_no, null).
				setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						NotesApplication.executeInBackground(new Runnable() {
							@Override
							public void run() {
								storage.deleteLabel(label.getId());
								((MainActivity) activity).getNavigationDrawerFragment().onLabelChanged();
							}
						});
					}
				}).create();
	}


	// New dialog showing

	public static void show(DialogType type, Serializable itemId, FragmentManager fragmentManager) {
		final SimpleItemDialog dialog = new SimpleItemDialog();
		final Bundle args = createArgumentsBundle(itemId);
		args.putSerializable(EXTRA_TYPE, type);
		dialog.setArguments(args);
		dialog.show(fragmentManager, FRAGMENT_TAG_PREFIX + type.toString());
	}

	private void showSimpleDialogForCurrentItem(SimpleItemDialog.DialogType type) {
		SimpleItemDialog.show(type, id, activity.getFragmentManager());
	}


	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	public static enum DialogType {
		// for note
		NoteActions,
		NoteInfo,
		NoteDelete,
		NoteNoLabels,
		// for label
		LabelActions,
		LabelDelete
	}

	private class NoteActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int LABELS_INDEX = 0;
		private final int SHARE_INDEX = 1;
		private final int INFO_INDEX = 2;
		private final int DELETE_INDEX = 3;

		public NoteActionDialogClickListener() {}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case LABELS_INDEX:
					showNoteLabelsDialog();
					break;
				case SHARE_INDEX:
					shareNote();
					break;
				case INFO_INDEX:
					showSimpleDialogForCurrentItem(SimpleItemDialog.DialogType.NoteInfo);
					EventTracker.track(Event.NoteInfoShow);
					break;
				case DELETE_INDEX:
					showSimpleDialogForCurrentItem(SimpleItemDialog.DialogType.NoteDelete);
					break;
			}
		}

		private void showNoteLabelsDialog() {
			final boolean noLabelsCreated = storage.getAllLabels().isEmpty();
			if(noLabelsCreated) {
				showSimpleDialogForCurrentItem(SimpleItemDialog.DialogType.NoteNoLabels);
			} else {
				NoteLabelsDialog.show(activity.getFragmentManager(), id);
			}
		}

		private void shareNote() {
			NotesUtils.shareNote(activity, storage.getNote(id), true);
		}
	}

	private class LabelActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int EDIT_INDEX = 0;
		private final int DELETE_INDEX = 1;

		public LabelActionDialogClickListener() {}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case EDIT_INDEX:
					((MainActivity) activity)
							.getNavigationDrawerFragment()
							.showLabelEditDialog(id);
					break;
				case DELETE_INDEX:
					showSimpleDialogForCurrentItem(DialogType.LabelDelete);
					break;
			}
		}
	}
}
