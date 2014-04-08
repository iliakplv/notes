package com.iliakplv.notes.notes;

import com.iliakplv.notes.utils.StringUtils;
import org.joda.time.DateTime;

public abstract class AbstractNote {

	private int id = 0;

	private String title;
	private String body;
	private DateTime createTime;
	private DateTime changeTime;


	public AbstractNote(String title, String body) {
		setTitle(title);
		setBody(body);
		createTime = new DateTime();
		changeTime = new DateTime();
	}


	// Text

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = StringUtils.getNotNull(title);
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = StringUtils.getNotNull(body);
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if (id <= 0) {
			throw new IllegalArgumentException("Id value must be positive!");
		}
		this.id = id;
	}
}
