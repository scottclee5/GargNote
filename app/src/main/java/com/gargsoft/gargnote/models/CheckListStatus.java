package com.gargsoft.gargnote.models;

public class CheckListStatus {
    private int checked;
    private int complete;
    private int count;

    public int getChecked() {
        return checked;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public CheckListStatus(int checked, int complete, int count) {
        this.checked = checked;
        this.complete = complete;
        this.count = count;
    }

    public CheckListStatus() {
    }
}
