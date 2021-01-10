package com.gargsoft.gargnote.models;

import com.gargsoft.gargnote.adapters.ViewType;

public class Note extends BaseMainItem {
    private String note = "";

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Note(Folder parent) {
        super(parent);
    }

    public ViewType getViewType() {return ViewType.Note; }
}
