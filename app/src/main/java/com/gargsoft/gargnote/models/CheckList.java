package com.gargsoft.gargnote.models;

import androidx.annotation.NonNull;

import com.gargsoft.gargnote.GargNoteApplication;
import com.gargsoft.gargnote.adapters.ViewType;
import java.util.ArrayList;
import java.util.Arrays;

public class CheckList extends BaseMainItem {
    private final ArrayList<CheckList> dependencies = new ArrayList<>();
    private final ArrayList<CheckListItem> items = new ArrayList<>();

    public ArrayList<CheckList> getDependencies() {
        return dependencies;
    }

    public ArrayList<CheckListItem> getItems() {
        return items;
    }

    public boolean isItem(CheckListItem item) {
        return items.contains(item);
    }

    public boolean isDependency(CheckList item, boolean includeSubDependencies) {
        if (dependencies.contains(item))
            return true;

        if (includeSubDependencies) {
            for (CheckList checkList : dependencies) {
                if (checkList.isDependency(item, true))
                    return true;
            }
        }

        return false;
    }

    public CheckListItem[] getVirtualItems() {
        ArrayList<CheckListItem> items = new ArrayList<>();

        items.addAll(this.items);

        for (CheckList item : dependencies) {
            items.addAll(Arrays.asList(item.getVirtualItems()));
        }

        return items.toArray(new CheckListItem[0]);
    }

    public CheckListItem[] getCheckedItems(boolean virtual) {
        CheckListItem[] allItems;

        if (virtual)
            allItems = getVirtualItems();
        else
            allItems = getItems().toArray(new CheckListItem[0]);

        ArrayList<CheckListItem> items = new ArrayList<>();

        for (CheckListItem item : allItems) {
            if (item.isChecked())
                items.add(item);
        }

        return items.toArray(new CheckListItem[0]);
    }

    public CheckList(Folder parent) {
        super(parent);
    }

    public @NonNull CheckListStatus getStatus(boolean includeDependencies) {
        int checked = 0;
        int complete = 0;
        int total = items.size();
        CheckListStatus status;

        for (CheckListItem item : items) {
            if (item.isChecked())
                checked++;

            if (item.isComplete())
                complete++;
        }

        if (includeDependencies) {
            for (CheckList item : dependencies) {
                status = item.getStatus(true);

                checked += status.getChecked();
                complete += status.getComplete();
                total += status.getCount();
            }
        }

        return new CheckListStatus(checked, complete, total);
    }

    public ViewType getViewType() {return ViewType.CheckList; }

    @Override
    public void delete() {
        Folder rootFolder = getRootFolder();

        if (null != rootFolder) {
            // Remove this checklist from any dependencies of the other checklists.
            for (CheckList item : rootFolder.getCheckLists(true)) {
                if (item.isDependency(this, false))
                    item.getDependencies().remove(this);
            }
        }

        super.delete();
    }
}
