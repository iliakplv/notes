package com.iliakplv.notes.notes;

import com.iliakplv.notes.utils.StringUtils;

import java.io.Serializable;

public class Label {

    private Serializable id = NotesUtils.DEFAULT_ID;

    private String name;
    private int color;


    public Label(String name, int color) {
        setName(name);
        setColor(color);
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

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = NotesUtils.getValidNoteId(id);
    }
}
