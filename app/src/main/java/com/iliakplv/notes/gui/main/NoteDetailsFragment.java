package com.iliakplv.notes.gui.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.AbstractNote;
import org.joda.time.DateTime;

/**
 * Author: Ilya Kopylov
 * Date:  16.08.2013
 */
public class NoteDetailsFragment extends Fragment {

	private AbstractNote note;

	public NoteDetailsFragment() {
		// do nothing
	}

	public NoteDetailsFragment(AbstractNote note) {
		this.note = note;
	}

	public AbstractNote getNote() {
		return note;
	}

	public void setNote(AbstractNote note) {
		this.note = note;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO refactor
		if (note == null) {
			return null;
		}
		final View view = inflater.inflate(R.layout.note_details, container, false);

		((TextView) view.findViewById(R.id.note_body)).setText(note.getBody());

		final String DATE_SPACING = ".";
		final String TIME_SPACING = ":";

		StringBuilder sb = new StringBuilder();
		DateTime timestamp = note.getCreateTime();

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

		((TextView) view.findViewById(R.id.note_create_date)).setText(sb.toString());


		sb = new StringBuilder();
		timestamp = note.getChangeTime();

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

		((TextView) view.findViewById(R.id.note_modify_date)).setText(sb.toString());


		return view;
	}


}
