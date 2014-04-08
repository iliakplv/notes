package com.iliakplv.notes.notes;

import com.iliakplv.notes.utils.StringUtils;

public class Label {

	public static final int DEFAULT_COLOR_INDEX = 0;

	private int id = 0;

	private String name;
	private int color;


	public Label(String name, int color) {
		setName(name);
		setColor(color);
	}

	public Label(String name) {
		this(name, DEFAULT_COLOR_INDEX);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = StringUtils.getNotNull(name);
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
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
