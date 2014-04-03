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
import com.iliakplv.notes.notes.Label;
import com.iliakplv.notes.notes.NotesUtils;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

import org.joda.time.DateTime;

public class SimpleItemDialog extends AbstractItemDialog {

	private static final String EXTRA_TYPE = "dialog_type";
	private static final String FRAGMENT_TAG_PREFIX = "item_dialog_";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final DialogType type = (DialogType) getArguments().getSerializable(EXTRA_TYPE);

		switch (type) {
			case NoteInfo:
				return createNoteInfoDialog();
			case NoteDelete:
				return createNoteDeleteDialog();
			case NoteNoLabels:
				return createNoteNoLabelsDialog();
			case LabelActions:
				return createLabelActionsDialog();
			case LabelDelete:
				return createLabelDeleteDialog();
			default:
				throw new RuntimeException("Unknown dialog type: " + type.toString());
		}
	}

	private Dialog createNoteDeleteDialog() {
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote(dbFacade.getNote(id).getEntry())).
				setMessage(StringUtils.wrapWithEmptyLines(getString(R.string.note_action_delete_confirm_dialog_text))).
				setNegativeButton(R.string.common_no, null).
				setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						NotesApplication.executeInBackground(new Runnable() {
							@Override
							public void run() {
								NotesDatabaseFacade.getInstance().deleteNote(id);
							}
						});
					}
				}).create();
	}

	private Dialog createNoteInfoDialog() {
		final String timeFormat = "HH:mm";

		final NotesDatabaseEntry<AbstractNote> noteEntry = dbFacade.getNote(id);
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

		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote(noteEntry.getEntry())).
				setMessage(info).
				setNegativeButton(R.string.common_ok, null).
				create();
	}

	private Dialog createNoteNoLabelsDialog() {
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForNote(dbFacade.getNote(id).getEntry())).
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
		final NotesDatabaseEntry<Label> labelEntry = dbFacade.getLabel(id).get(0);
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForLabel(labelEntry.getEntry())).
				setItems(R.array.label_actions, new LabelActionDialogClickListener()).
				setNegativeButton(R.string.common_cancel, null).
				create();
	}

	private Dialog createLabelDeleteDialog() {
		final NotesDatabaseEntry<Label> labelEntry = dbFacade.getLabel(id).get(0);
		return new AlertDialog.Builder(activity).
				setTitle(NotesUtils.getTitleForLabel(labelEntry.getEntry())).
				setMessage(StringUtils.wrapWithEmptyLines(getString(R.string.label_action_delete_confirm_dialog_text))).
				setNegativeButton(R.string.common_no, null).
				setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						NotesApplication.executeInBackground(new Runnable() {
							@Override
							public void run() {
								dbFacade.deleteLabel(labelEntry.getId());
								((MainActivity) activity).getNavigationDrawerFragment().onLabelChanged();
							}
						});
					}
				}).create();
	}


	public static void show(DialogType type, int id, FragmentManager fragmentManager) {
		final SimpleItemDialog dialog = new SimpleItemDialog();
		final Bundle args = createArgumentsBundle(id);
		args.putSerializable(EXTRA_TYPE, type);
		dialog.setArguments(args);
		dialog.show(fragmentManager, FRAGMENT_TAG_PREFIX + type.toString());
	}

	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	public static enum DialogType {
		NoteDelete,
		NoteInfo,
		NoteNoLabels,

		LabelActions,
		LabelDelete
	}

	private class LabelActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int EDIT_INDEX = 0;
		private final int DELETE_INDEX = 1;

		public LabelActionDialogClickListener() {}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			switch (i) {
				case EDIT_INDEX:
					((MainActivity) activity).getNavigationDrawerFragment()
							.showLabelEditDialog(id);
					break;
				case DELETE_INDEX:
					SimpleItemDialog.show(DialogType.LabelDelete,
							id,
							activity.getFragmentManager());
					break;
			}
		}
	}
}
