package com.iliakplv.notes.analytics;

import com.iliakplv.notes.utils.AppLog;

import java.io.Serializable;

public class EventTracker {

	private static volatile boolean enabled = false;


	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean enabled) {
		EventTracker.enabled = enabled;
	}

	public static void track(Event event, Serializable... args) {
		if (enabled) {
			AppLog.d("EVENT", event.toString());
		}
	}
}
