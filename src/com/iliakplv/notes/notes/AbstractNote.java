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


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		updateChangeTime();
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
		updateChangeTime();
	}

	public DateTime getCreateTime() {
		return createTime;
	}

	public DateTime getChangeTime() {
		return changeTime;
	}

	public void updateChangeTime() {
		this.changeTime = new DateTime();
	}
}
