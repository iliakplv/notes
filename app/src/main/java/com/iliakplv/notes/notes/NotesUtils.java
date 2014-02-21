package com.iliakplv.notes.notes;


import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.utils.StringUtils;

public final class NotesUtils {

	public static enum NoteSortOrder {
		TitleOrBody,
		CreateDateAscending,
		CreateDateDescending,
		ChangeDateAscending,
		ChangeDateDescending
	}


	private static final String[] COLORS_NAMES =
			NotesApplication.getContext().getResources().getStringArray(R.array.label_colors_names);


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

	public static String getSubtitleForNote(AbstractNote note) {
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

	public static String getTitleForLabel(Label label) {
		if (!StringUtils.isNullOrEmpty(label.getName())) {
			return label.getName();
		} else {
			return "(" + COLORS_NAMES[label.getColor()] + ")";
		}
	}

}
