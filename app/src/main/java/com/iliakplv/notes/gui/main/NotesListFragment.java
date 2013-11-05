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
import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;
import org.joda.time.DateTime;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NotesListFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
		NotesDatabaseFacade.DatabaseChangeListener {

	private final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
	private MainActivity mainActivity;
	private NotesListAdapter listAdapter;
	private boolean listeningDatabase = false;

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
		// TODO list view choice mode
		if (getFragmentManager().findFragmentById(R.id.note_details_fragment) != null) { // Dual pane layout
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		startListeningDatabase();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListeningDatabase();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mainActivity.onNoteSelected(dbFacade.getAllNotes().get(position).getId());
		getListView().setItemChecked(position, true);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
		return onNoteLongClick(i);
	}

	public boolean onNoteLongClick(int position) {
		final NotesDatabaseEntry selectedNoteEntry = dbFacade.getAllNotes().get(position);
		final AbstractNote note = selectedNoteEntry.getNote();

		// Show available actions for note
		new AlertDialog.Builder(getActivity()).
				setTitle(getTitleForNote(note)).
				setItems(R.array.note_actions, new NoteActionDialogClickListener(selectedNoteEntry)).
				setNegativeButton(R.string.common_cancel, null).
				create().show();

		return true;
	}

	@Override
	public void onDatabaseChanged() {
		if (listeningDatabase && listAdapter != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listeningDatabase && listAdapter != null) {
						listAdapter.notifyDataSetChanged();
					}
				}
			});
		}
	}

	private void startListeningDatabase() {
		if (!listeningDatabase) {
			dbFacade.addDatabaseChangeListener(this);
			listeningDatabase = true;
		}
	}

	private void stopListeningDatabase() {
		if (listeningDatabase) {
			dbFacade.removeDatabaseChangeListener(this);
			listeningDatabase = false;
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
			final TextView title = (TextView) view.findViewById(R.id.title);
			final TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			title.setText(getTitleForNote(note));
			subtitle.setText(getBodyForNote(note));

			view.findViewById(R.id.note_list_item_menu).setOnClickListener(new NoteMenuClickListener(position));

//			final boolean selected = getListView().isItemChecked(position);
//			final int titleColor = selected ? R.color.note_list_item_selected : R.color.note_list_item_title;
//			final int subtitleColor = selected ? R.color.note_list_item_selected : R.color.note_list_item_subtitle;
//			title.setTextColor(getResources().getColor(titleColor));
//			subtitle.setTextColor(getResources().getColor(subtitleColor));

			return view;
		}

		@Override
		public int getCount() {
			return dbFacade.getNotesCount();
		}

	}

	private class NoteMenuClickListener implements View.OnClickListener {

		private int position;

		public NoteMenuClickListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			NotesListFragment.this.onNoteLongClick(position);
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
					final String timeFormat = "HH:mm";
					final DateTime created = noteEntry.getNote().getCreateTime();
					final String createdString = created.toLocalDate().toString() + " " +
							created.toLocalTime().toString(timeFormat);
					final DateTime changed = noteEntry.getNote().getChangeTime();
					final String changedString = changed.toLocalDate().toString() + " " +
							changed.toLocalTime().toString(timeFormat);
					final String info = "\n" + getString(R.string.note_info_created, createdString) +
							"\n\n" + getString(R.string.note_info_modified, changedString) + "\n";

					new AlertDialog.Builder(getActivity()).
							setTitle(getTitleForNote(noteEntry.getNote())).
							setMessage(info).
							setNegativeButton(R.string.common_ok, null).
							create().show();
					break;
				case DELETE_INDEX:
					// Show delete confirmation dialog
					new AlertDialog.Builder(getActivity()).
							setTitle(getTitleForNote(noteEntry.getNote())).
							setMessage("\n" + getString(R.string.note_action_delete_confirm_dialog_text) + "\n").
							setNegativeButton(R.string.common_no, null).
							setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {

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
