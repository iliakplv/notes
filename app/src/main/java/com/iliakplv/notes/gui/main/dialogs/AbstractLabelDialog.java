package com.iliakplv.notes.gui.main.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

public class AbstractLabelDialog extends DialogFragment {

	public static final String EXTRA_LABEL_ID = "label_id";

	protected NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();

	protected Activity activity;
	protected int labelId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = getArguments();
		if (args == null || !args.containsKey(EXTRA_LABEL_ID)) {
			throw new RuntimeException("Label id required");
		}
		labelId = args.getInt(EXTRA_LABEL_ID);
	}

	protected static Bundle createArgumentsBundle(int labelId) {
		final Bundle args = new Bundle();
		args.putInt(EXTRA_LABEL_ID, labelId);
		return args;
	}

}
