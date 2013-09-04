package com.iliakplv.notes.utils;

/**
 * Autor: Ilya Kopylov
 * Date:  22.08.2013
 */
public final class StringUtils {

	public static String getNotNull(String s) {
		return s != null ? s : "";
	}

	public static boolean isNullOrEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}

}
