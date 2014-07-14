package com.iliakplv.notes.analytics;

import com.iliakplv.notes.utils.AppLog;

import java.io.Serializable;

public class EventTracker {
	private static final EventTracker instance = new EventTracker();

	private EventTracker() {}

	public static EventTracker getInstance() {
		return instance;
	}


	public void track(Event event, Serializable... args) {

		AppLog.d("EVENT", event.toString());

	}

}
