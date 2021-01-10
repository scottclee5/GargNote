package com.gargsoft.gargnote.models;

import java.util.Date;

public abstract class BaseItem implements Cloneable {
    private int id;
    private String name = "";
    private Date timestamp = new Date();

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public BaseItem() {
    }

    public void refreshTimestamp() {
        timestamp = new Date();
    }
}
