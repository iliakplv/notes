package com.iliakplv.notes.notes;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.utils.StringUtils;

public final class NotesUtils {

	public static enum NoteSortOrder {
		Title,
		CreateDateAscending,
		CreateDateDescending,
		ChangeDate
	}


	private static final String[] COLORS_NAMES =
			NotesApplication.getContext().getResources().getStringArray(R.array.label_colors_names);


	public static String getTitleForNote(AbstractNote note) {
		if (!isNoteTitleEmpty(note)) {
			return note.getTitle();
		} else if (isNoteEmpty(note)) {
			return NotesApplication.getContext().getString(R.string.empty_note_placeholder);
		} else {
			return NotesApplication.getContext().getString(R.string.empty_note_title_placeholder);
		}
	}

	public static boolean isNoteTitleEmpty(AbstractNote note) {
		return StringUtils.isNullOrEmpty(note.getTitle());
	}

	public static boolean isNoteBodyEmpty(AbstractNote note) {
		return StringUtils.isNullOrEmpty(note.getBody());
	}

	public static boolean isNoteEmpty(AbstractNote note) {
		return isNoteTitleEmpty(note) && isNoteBodyEmpty(note);
	}

	public static String getTitleForLabel(Label label) {
		if (!StringUtils.isNullOrEmpty(label.getName())) {
			return label.getName();
		} else {
			return "(" + COLORS_NAMES[label.getColor()] + ")";
		}
	}

}
