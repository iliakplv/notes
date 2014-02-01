package com.iliakplv.notes.notes;

import android.graphics.Color;

import com.iliakplv.notes.utils.StringUtils;


public class Label {

	private static final int DEFAULT_COLOR = Color.WHITE;

	private String name;
	private int color;


	public Label(String name, int color) {
		setName(name);
		setColor(color);
	}

	public Label(String name) {
		this(name, DEFAULT_COLOR);
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
