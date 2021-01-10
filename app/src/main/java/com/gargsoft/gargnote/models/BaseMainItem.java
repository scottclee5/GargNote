package com.gargsoft.gargnote.models;

import com.gargsoft.gargnote.adapters.ViewType;
import com.gargsoft.gargnote.utilities.Helper;

public abstract class BaseMainItem extends BaseItem {
    private Folder parent;

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        if (this.parent != parent) {
            if (null != this.parent)
                this.parent.getChildren().remove(this);

            this.parent = parent;

            if (null != this.parent)
                this.parent.getChildren().add(this);
        }
    }

    public Folder getRootFolder() {
        Folder folder;

        if (this instanceof  Folder)
            folder = (Folder)this;
        else
            folder = getParent();

        while (null != folder && null != folder.getParent())
            folder = folder.getParent();

        // The only way folder could be null here is if we are not a folder and we don't have a parent.
        // That would mean we are not part of anything and the root cannot be determined.
        return folder;
    }

    public BaseMainItem(Folder parent) {
        super();
        setParent(parent);
    }

    public abstract ViewType getViewType();

    public String getFullPath() {
        if (null != parent)
            return Helper.makePath(parent.getPath(), getName());

        return getName();
    }

    public void delete() {
        setParent(null);
    }
}
