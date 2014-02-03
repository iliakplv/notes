package com.iliakplv.notes.notes;


import com.iliakplv.notes.utils.StringUtils;


public class Label {

	private String name;
	private int color;


	public Label(String name, int color) {
		setName(name);
		setColor(color);
	}

	public Label(String name) {
		this(name, LabelColor.NO_COLOR);
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
}
