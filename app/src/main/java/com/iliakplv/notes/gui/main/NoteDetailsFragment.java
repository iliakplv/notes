package com.iliakplv.notes.gui.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;
import org.joda.time.DateTime;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NoteDetailsFragment extends Fragment {

	final static String ARG_POSITION = "position";

	private int currentPosition = -1;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			currentPosition = savedInstanceState.getInt(ARG_POSITION);
		}
		return inflater.inflate(R.layout.note_details, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		final Bundle args = getArguments();
		if (args != null) {
			updateNoteDetailsView(args.getInt(ARG_POSITION));
		} else if (currentPosition != -1) {
			updateNoteDetailsView(currentPosition);
		}
	}

	public void updateNoteDetailsView(int position) {
		// TODO refactor this piece of code

		if (position >= NotesDatabaseFacade.getAllNotes().size()){
			return;
		}

		final AbstractNote note = NotesDatabaseFacade.getAllNotes().get(position).getNote();

		final String noteTitle = note.getTitle();
		final String noteBody = note.getBody();
		final boolean hasTitle = !StringUtils.isNullOrEmpty(noteTitle);
		final boolean hasBody = !StringUtils.isNullOrEmpty(noteBody);

		final TextView title = (TextView) getActivity().findViewById(R.id.note_title);
		final TextView body = (TextView) getActivity().findViewById(R.id.note_body);

		// At least one of note's fields (title or body) will be not empty (empty note can't be stored)
		if (hasTitle && hasBody) {
			// show all
			title.setVisibility(View.VISIBLE);
			title.setText(noteTitle);
			body.setText(noteBody);
		} else if (!hasTitle && hasBody) {
			// show only body
			title.setVisibility(View.GONE);
			body.setText(noteBody);
		} else { // (hasTitle && !hasBody)
			// show only title (like it's a body)
			title.setVisibility(View.GONE);
			body.setText(noteTitle);
		}

		((TextView) getActivity().findViewById(R.id.note_body)).setText(noteBody);


		final String DATE_SPACING = ".";
		final String TIME_SPACING = ":";

		StringBuilder sb = new StringBuilder();
		DateTime timestamp = note.getCreateTime();

		sb.append(getString(R.string.note_details_created));
		sb.append(timestamp.getHourOfDay());
		sb.append(TIME_SPACING);
		sb.append(timestamp.getMinuteOfHour());
		sb.append(TIME_SPACING);
		sb.append(timestamp.getSecondOfMinute());
		sb.append(" ");
		sb.append(timestamp.getDayOfMonth());
		sb.append(DATE_SPACING);
		sb.append(timestamp.getMonthOfYear());
		sb.append(DATE_SPACING);
		sb.append(timestamp.getYear());

		((TextView) getActivity().findViewById(R.id.note_create_date)).setText(sb.toString());


		sb = new StringBuilder();
		timestamp = note.getChangeTime();

		sb.append(getString(R.string.note_details_modified));
		sb.append(timestamp.getHourOfDay());
		sb.append(TIME_SPACING);
		sb.append(timestamp.getMinuteOfHour());
		sb.append(TIME_SPACING);
		sb.append(timestamp.getSecondOfMinute());
		sb.append(" ");
		sb.append(timestamp.getDayOfMonth());
		sb.append(DATE_SPACING);
		sb.append(timestamp.getMonthOfYear());
		sb.append(DATE_SPACING);
		sb.append(timestamp.getYear());

		((TextView) getActivity().findViewById(R.id.note_modify_date)).setText(sb.toString());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(ARG_POSITION, currentPosition);
	}

}
