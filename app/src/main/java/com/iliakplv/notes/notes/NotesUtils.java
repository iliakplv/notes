package com.iliakplv.notes.notes;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.utils.StringUtils;

import java.io.Serializable;

public final class NotesUtils {

	private NotesUtils() {
		throw new AssertionError("Instance creation not allowed!");
	}

	public static final Serializable DEFAULT_ID = "";

	public static enum NoteSortOrder {
		Title,
		CreateDateAscending,
		CreateDateDescending,
		ChangeDate
	}

	public static final NoteSortOrder DEFAULT_SORT_ORDER = NoteSortOrder.Title;


	private static final String[] COLORS_NAMES =
			NotesApplication.getContext().getResources().getStringArray(R.array.label_colors_names);


	public static String getTitleForNoteInDialog(AbstractNote note) {
		if (isNoteBlank(note)) {
			return NotesApplication.getContext().getString(R.string.empty_note_placeholder);
		} else if (!isNoteTitleBlank(note)) {
			return note.getTitle().trim();
		} else {
			return note.getBody().trim();
		}
	}

	public static String getTitleForNoteInList(AbstractNote note) {
		if (isNoteBlank(note)) {
			return NotesApplication.getContext().getString(R.string.empty_note_placeholder);
		} else {
			return note.getTitle().trim();
		}
	}

	public static boolean isNoteTitleBlank(AbstractNote note) {
		return StringUtils.isBlank(note.getTitle());
	}

	public static boolean isNoteBodyBlank(AbstractNote note) {
		return StringUtils.isBlank(note.getBody());
	}

	public static boolean isNoteBlank(AbstractNote note) {
		return isNoteTitleBlank(note) && isNoteBodyBlank(note);
	}


	public static String getTitleForLabel(Label label) {
		if (!StringUtils.isBlank(label.getName())) {
			return label.getName().trim();
		} else {
			return "(" + COLORS_NAMES[label.getColor()] + ")";
		}
	}

	public static Serializable getValidNoteId(Serializable id) {
		return id != null ? id : DEFAULT_ID;
	}


	public static void shareNote(Context context, AbstractNote note, boolean showToastIfEmpty) {
		shareNote(context, note.getTitle(), note.getBody(), showToastIfEmpty);
	}

	public static void shareNote(Context context, String subject, String text, boolean showToastIfEmpty) {
		final boolean empty = StringUtils.isNullOrEmpty(subject) && StringUtils.isNullOrEmpty(text);
		if (!empty) {
			final Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
			intent.putExtra(Intent.EXTRA_TEXT, text);
			context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_bar_share_title)));
		} else if (showToastIfEmpty) {
			Toast.makeText(context, R.string.empty_note_not_shared, Toast.LENGTH_SHORT).show();
		}
	}
}
