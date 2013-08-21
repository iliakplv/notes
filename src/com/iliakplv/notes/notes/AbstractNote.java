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

	private int id = 0;


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
		if (time == null) {
			throw new NullPointerException("Note's create time can not be null");
		}
		createTime = time;
	}

	public DateTime getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(DateTime time) {
		if (time == null) {
			throw new NullPointerException("Note's change time can not be null");
		}
		changeTime = time;
	}

	public void updateChangeTime() {
		changeTime = new DateTime();
	}


	// Id

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if (id < 1) {
			throw new IllegalArgumentException("Note's id can not be less than 1");
		}
		this.id = id;
	}
}
