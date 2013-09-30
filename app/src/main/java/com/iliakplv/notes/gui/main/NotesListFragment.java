package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NotesListFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
		NotesDatabaseFacade.DatabaseChangeListener {

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
	private MainActivity mainActivity;
	private NotesListAdapter listAdapter;

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
		if (getFragmentManager().findFragmentById(R.id.note_details_fragment) != null) { // Dual pane layout
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		dbFacade.addDatabaseChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		dbFacade.removeDatabaseChangeListener(this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mainActivity.onNoteSelected(dbFacade.getAllNotes().get(position).getId());
		getListView().setItemChecked(position, true);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		final NotesDatabaseEntry selectedNoteEntry = dbFacade.getAllNotes().get(i);
		final AbstractNote note = selectedNoteEntry.getNote();

		// Show available actions for note
		new AlertDialog.Builder(getActivity()).
				setTitle(getTitleForNote(note)).
				setItems(R.array.note_actions, new NoteActionDialogClickListener(selectedNoteEntry)).
				setNegativeButton(R.string.note_dialog_cancel, null).
				create().show();

		return true;
	}

	@Override
	public void onDatabaseChanged() {
		if (listAdapter != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listAdapter.notifyDataSetChanged();
				}
			});
		}
	}


	// list item text

	public static String getTitleForNote(AbstractNote note) {
		final String originalTitle = note.getTitle();
		final String originalBody = note.getBody();

		if (!StringUtils.isBlank(originalTitle)) {
			return originalTitle;
		} else if (!StringUtils.isBlank(originalBody)) {
			return originalBody;
		} else {
			return NotesApplication.getContext().getString(R.string.empty_note_placeholder);
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


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private class NotesListAdapter extends ArrayAdapter<NotesDatabaseEntry> {

		public NotesListAdapter() {
			super(NotesListFragment.this.getActivity(), 0, dbFacade.getAllNotes());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(R.layout.note_list_item, parent, false);
			}

			final AbstractNote note = dbFacade.getAllNotes().get(position).getNote();
			((TextView) view.findViewById(R.id.title)).setText(getTitleForNote(note));
			((TextView) view.findViewById(R.id.subtitle)).setText(getBodyForNote(note));

			return view;
		}

		@Override
		public int getCount() {
			return dbFacade.getNotesCount();
		}

	}

	private class NoteActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int INFO_INDEX = 0;
		private final int DELETE_INDEX = 1;

		private NotesDatabaseEntry noteEntry;

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
					Toast.makeText(getActivity(), "[very important info]", Toast.LENGTH_SHORT).show();
					break;
				case DELETE_INDEX:
					// Show delete confirmation dialog
					new AlertDialog.Builder(getActivity()).
							setTitle(getTitleForNote(noteEntry.getNote())).
							setMessage(R.string.note_action_delete_confirm_dialog_text).
							setNegativeButton(R.string.note_action_delete_confirm_dialog_no, null).
							setPositiveButton(R.string.note_action_delete_confirm_dialog_yes, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									new Thread(new Runnable() {
										@Override
										public void run() {
											NotesDatabaseFacade.getInstance().deleteNote(noteEntry.getId());
										}
									}).start();
								}

							}).create().show();
					break;
			}
		}
	}

}
