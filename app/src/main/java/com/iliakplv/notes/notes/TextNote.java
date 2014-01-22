package com.iliakplv.notes.notes;

/**
 * Author: Ilya Kopylov
 * Date: 21.08.2013
 */
public class TextNote extends AbstractNote {

	public static final TextNote EMPTY = new TextNote();

	public TextNote(String title, String body) {
		super(title, body);
	}

	public TextNote() {
		super(null, null);
	}

}
