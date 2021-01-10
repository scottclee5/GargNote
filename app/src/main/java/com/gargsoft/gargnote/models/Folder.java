package com.gargsoft.gargnote.models;

import com.gargsoft.gargnote.GargNoteApplication;
import com.gargsoft.gargnote.adapters.ViewType;
import com.gargsoft.gargnote.utilities.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class Folder extends BaseMainItem {
    private final ArrayList<BaseMainItem> children = new ArrayList<>();

    public Folder(Folder parent) {
        super(parent);
    }

    public ArrayList<BaseMainItem> getChildren() {
        return children;
    }

    public ViewType getViewType() {return ViewType.Folder; }

    public String getPath() {
        StringBuilder builder = new StringBuilder();

        if (null != getParent()) {
            builder.append(getParent().getPath());

            if (builder.length() > 1)
                builder.append('\\');

            String name = getName();

            if (null != name)
                builder.append(name);
        }
        else
            builder.append('\\');

        return builder.toString();
    }

    public BaseMainItem findChild(int id) {
        return findChild(id, true);
    }

    public BaseMainItem findChild(int id, boolean includeSubFolders) {
        BaseMainItem child;

        for (BaseMainItem item : children) {
            if (item.getId() == id)
                return item;

            if (includeSubFolders && item instanceof Folder) {
                child = ((Folder)item).findChild(id, true);

                if (null != child)
                    return child;
            }
        }

        if (includeSubFolders) {
            BaseMainItem item;

            for (Folder folder : getFolders(true)) {
                item = folder.findChild(id, true);

                if (null != item)
                    return item;
            }
        }

        return null;
    }

    public ArrayList<Folder> getFolders(boolean includeSubFolders) {
        return this.<Folder>getChildrenOfType(includeSubFolders, Folder.class);
    }

    public ArrayList<Note> getNotes(boolean includeSubFolders) {
        return this.<Note>getChildrenOfType(includeSubFolders, Note.class);
    }

    public ArrayList<CheckList> getCheckLists(boolean includeSubFolders) {
        return this.<CheckList>getChildrenOfType(includeSubFolders, CheckList.class);
    }

    private <T> ArrayList<T> getChildrenOfType(boolean includeSubFolders, Class<T> tClass) {
        ArrayList<T> items = new ArrayList<T>();

        for (BaseMainItem item : children) {
            if (tClass.isInstance(item))
                items.add((T)item);

            if (includeSubFolders && item instanceof Folder)
                items.addAll(((Folder)item).<T>getChildrenOfType(true, tClass));
        }

        return items;
    }

    public boolean isChild(BaseMainItem item, boolean includeSubFolders) {
        if (children.contains(item))
            return true;

        if (includeSubFolders) {
            for (Folder folder : getFolders(false)) {
                if (folder.isChild(item, true))
                    return true;
            }
        }

        return false;
    }

    public ArrayList<BaseMainItem> getChildren(boolean searchSubFolders, boolean includeFolders) {
        if (!searchSubFolders && includeFolders)
            return getChildren();

        ArrayList<BaseMainItem> items = new ArrayList<BaseMainItem>();

        for (BaseMainItem item : children) {
            if (includeFolders || !(item instanceof Folder))
                items.add(item);

            if (searchSubFolders && item instanceof Folder)
                items.addAll(((Folder)item).getChildren(true, includeFolders));
        }
        return items;
    }

    @Override
    public void delete() {
        ArrayList<BaseMainItem> items = new ArrayList<>();

        // Must use a temporary array as the deletes throughout the loop will modify our children.
        items.addAll(children);

        for (BaseMainItem item : items)
            item.delete();

        super.delete();
    }
}
