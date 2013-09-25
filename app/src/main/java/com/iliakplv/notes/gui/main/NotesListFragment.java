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
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

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
		mainActivity.onNoteSelected(position);
		getListView().setItemChecked(position, true);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		final NotesDatabaseEntry selectedNoteEntry = dbFacade.getAllNotes().get(i);

		// Show available actions for note
		new AlertDialog.Builder(getActivity()).
				setTitle(selectedNoteEntry.getNote().getTitleOrPlaceholder()).
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
			((TextView) view.findViewById(R.id.title)).setText(note.getTitleOrPlaceholder());
			((TextView) view.findViewById(R.id.subtitle)).setText(note.getBody());

			// TODO first line or substring of note's body

			return view;
		}

		@Override
		public int getCount() {
			return dbFacade.getAllNotes().size();
		}
	}

	private class NoteActionDialogClickListener implements DialogInterface.OnClickListener {

		private final int EDIT_INDEX = 0;
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
				case EDIT_INDEX:
					(new NoteDialogFragment(noteEntry)).show(getFragmentManager(), "dialog");
					break;
				case DELETE_INDEX:
					// TODO confirm dialog
					new Thread(new Runnable() {
						@Override
						public void run() {
							NotesDatabaseFacade.getInstance().deleteNote(noteEntry.getId());
						}
					}).start();
					break;
			}
		}
	}

}
