package com.iliakplv.notes.utils;

/**
 * Autor: Ilya Kopylov
 * Date:  22.08.2013
 */
public final class StringUtils {

	private static final String EMPTY = "";


	public static String getNotNull(String s) {
		return s != null ? s : EMPTY;
	}

	public boolean isNullOrEmpty(String s) {
		return s == null || s.length() == 0;
	}

}
