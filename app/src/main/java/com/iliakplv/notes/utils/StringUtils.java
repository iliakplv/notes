package com.iliakplv.notes.utils;

import com.iliakplv.notes.NotesApplication;

/**
 * Autor: Ilya Kopylov
 * Date:  22.08.2013
 */
public final class StringUtils {

	public static String getNonEmpty(String string, String placeholder) {
		if (placeholder == null) {
			placeholder = "";
		}
		return StringUtils.isNullOrEmpty(string) ? placeholder : string;
	}

	public static String getNonEmpty(String string, int placeholderId) {
		return getNonEmpty(string, NotesApplication.getContext().getString(placeholderId));
	}

	public static boolean isNullOrEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}

	public static boolean equals(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return true;
		} else if (s1 != null) {
			return s1.equals(s2);
		} else { // s2 != null
			return s2.equals(s1);
		}
	}

}
