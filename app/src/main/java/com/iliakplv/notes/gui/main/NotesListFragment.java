package com.iliakplv.notes.gui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import com.iliakplv.notes.utils.StringUtils;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NotesListFragment extends ListFragment implements AdapterView.OnItemLongClickListener {

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
	public void onStart() {
		super.onStart();
		if (getFragmentManager().findFragmentById(R.id.note_details_fragment) != null) { // Dual pane layout
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mainActivity.onNoteSelected(position);
		getListView().setItemChecked(position, true);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		final NotesDatabaseEntry selectedNoteEntry = NotesDatabaseFacade.getAllNotes().get(i);

		new AlertDialog.Builder(getActivity()).
				setTitle(selectedNoteEntry.getNote().getTitleOrPlaceholder()).
				setItems(R.array.note_actions, new NoteActionClickListener(selectedNoteEntry)).
				setNegativeButton(R.string.note_dialog_cancel, null).
				create().show();

		return true;
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private class NotesListAdapter extends ArrayAdapter<NotesDatabaseEntry> {

		public NotesListAdapter() {
			super(NotesListFragment.this.getActivity(), 0, NotesDatabaseFacade.getAllNotes());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = LayoutInflater.from(getContext()).inflate(R.layout.note_list_item, parent, false);
			}

			final AbstractNote note = NotesDatabaseFacade.getAllNotes().get(position).getNote();
			((TextView) view.findViewById(R.id.title)).setText(note.getTitleOrPlaceholder());
			((TextView) view.findViewById(R.id.subtitle)).setText(note.getBody());

			// TODO first line or substring of note's body

			return view;
		}

		@Override
		public int getCount() {
			return NotesDatabaseFacade.getAllNotes().size();
		}
	}

	private class NoteActionClickListener implements DialogInterface.OnClickListener {

		private NotesDatabaseEntry noteEntry;

		public NoteActionClickListener(NotesDatabaseEntry noteEntry) {
			if (noteEntry == null) {
				throw new NullPointerException("Note entry is null");
			}
			this.noteEntry = noteEntry;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			Log.d("Dialog", "i=" + i);
			//		listAdapter.notifyDataSetChanged();
		}
	}

}
