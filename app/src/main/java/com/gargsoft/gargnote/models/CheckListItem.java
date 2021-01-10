package com.gargsoft.gargnote.models;

public class CheckListItem extends BaseItem implements Cloneable {
    private boolean checked;
    private boolean complete;
    private CheckList parent;

    public Object clone() throws CloneNotSupportedException {
        CheckListItem clone = (CheckListItem)super.clone();

        clone.parent = null;
        clone.setParent(parent);

        return clone;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public CheckList getParent() {
        return parent;
    }

    public void setParent(CheckList parent) {
        if (parent != this.parent) {
            if (null != this.parent)
                this.parent.getItems().remove(this);

            this.parent = parent;

            if (null != parent)
                parent.getItems().add(this);
        }
    }

    public CheckListItem(CheckList parent) {
        super();

        setParent(parent);
    }
}
