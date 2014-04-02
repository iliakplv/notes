package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.Label;

public class LabelEditDialog extends AbstractLabelDialog {

	public static final int NEW_LABEL = 0;

	private static final String FRAGMENT_TAG = "label_edit_dialog";

	private static final String EXTRA_LABEL_NAME = "label_name";
	private static final String EXTRA_SELECTED_COLOR = "label_color";
	private EditText nameEditText;
	private int selectedColor;

	private static final int[] COLORS_CHECKBOXES_IDS = {
			R.id.color_1,
			R.id.color_2,
			R.id.color_3,
			R.id.color_4,
			R.id.color_5,
			R.id.color_6,
			R.id.color_7,
			R.id.color_8
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View labelDialogView = inflater.inflate(R.layout.label_edit_dialog, null);

		final boolean editMode = labelId != NEW_LABEL;
		final boolean fromSavedInstanceState = savedInstanceState != null;

		final Label label = editMode ?
				dbFacade.getLabel(labelId).get(0).getEntry() :
				new Label("", Label.DEFAULT_COLOR_INDEX);
		final String labelName = fromSavedInstanceState ?
				savedInstanceState.getString(EXTRA_LABEL_NAME) :
				label.getName();
		nameEditText = (EditText) labelDialogView.findViewById(R.id.label_name);
		nameEditText.setText(labelName);
		selectedColor = fromSavedInstanceState ?
				savedInstanceState.getInt(EXTRA_SELECTED_COLOR) :
				label.getColor();

		final LabelEditDialogCheckBoxListener checkBoxListener =
				new LabelEditDialogCheckBoxListener((CheckBox) labelDialogView.findViewById(COLORS_CHECKBOXES_IDS[selectedColor]));
		for (int i = 0; i < COLORS_CHECKBOXES_IDS.length; i++) {
			final View checkBox = labelDialogView.findViewById(COLORS_CHECKBOXES_IDS[i]);
			checkBox.setOnClickListener(checkBoxListener);
			checkBox.setTag(i);
		}

		return new AlertDialog.Builder(activity)
				.setView(labelDialogView)
				.setPositiveButton(R.string.common_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						final String labelName = ((EditText) labelDialogView.findViewById(R.id.label_name)).getText().toString();
						NotesApplication.executeInBackground(new Runnable() {
							@Override
							public void run() {
								label.setName(labelName);
								label.setColor(selectedColor);
								if (editMode) {
									dbFacade.updateLabel(labelId, label);
								} else {
									dbFacade.insertLabel(label);
								}
								((LabelEditDialogCallback) getTargetFragment()).onLabelChanged();
							}
						});
					}
				})
				.setNegativeButton(R.string.common_cancel, null)
				.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(EXTRA_LABEL_NAME, nameEditText.getText().toString());
		outState.putInt(EXTRA_SELECTED_COLOR, selectedColor);
	}

	public static void show(FragmentManager fragmentManager, int labelId, Fragment targetFragment) {
		if (!(targetFragment instanceof LabelEditDialogCallback)) {
			throw new IllegalArgumentException("Target fragment must implement callback interface");
		}
		final LabelEditDialog dialog = new LabelEditDialog();
		dialog.setArguments(createArgumentsBundle(labelId));
		dialog.setTargetFragment(targetFragment, 0);
		dialog.show(fragmentManager, FRAGMENT_TAG);
	}


	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	private class LabelEditDialogCheckBoxListener implements View.OnClickListener {
		private CheckBox currentSelectedCheckBox;

		public LabelEditDialogCheckBoxListener(CheckBox selectedCheckBox) {
			currentSelectedCheckBox = selectedCheckBox;
			currentSelectedCheckBox.setChecked(true);
		}

		@Override
		public void onClick(View newSelectedCheckBox) {
			currentSelectedCheckBox.setChecked(false);
			currentSelectedCheckBox = (CheckBox) newSelectedCheckBox;
			selectedColor = (Integer) newSelectedCheckBox.getTag();
		}
	}

	public static interface LabelEditDialogCallback {
		public void onLabelChanged();
	}
}
