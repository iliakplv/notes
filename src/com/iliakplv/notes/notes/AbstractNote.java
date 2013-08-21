package com.iliakplv.notes.notes;


import org.joda.time.DateTime;

/**
 * Author: Ilya Kopylov
 * Date:  20.08.2013
 */
public abstract class AbstractNote {

	private String title;
	private String body;
	private DateTime createTime;
	private DateTime changeTime;


	public AbstractNote(String title, String body) {
		this.title = title;
		this.body = body;
		createTime = new DateTime();
		changeTime = new DateTime();
	}

	public AbstractNote(String body) {
		this(null, body);
	}


	// Text

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}


	// Timestamps

	public DateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(DateTime time) {
		createTime = time;
	}

	public DateTime getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(DateTime time) {
		changeTime = time;
	}

	public void updateChangeTime() {
		changeTime = new DateTime();
	}
}
