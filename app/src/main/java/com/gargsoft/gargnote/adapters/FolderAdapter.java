package com.gargsoft.gargnote.adapters;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gargsoft.gargnote.models.BaseMainItem;
import com.gargsoft.gargnote.models.CheckListItem;
import com.gargsoft.gargnote.models.Folder;
import com.gargsoft.gargnote.models.UserPreferences;
import com.gargsoft.gargnote.viewholders.BaseViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FolderAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private class Sorter implements Comparator<BaseMainItem> {

        @Override
        public int compare(BaseMainItem x, BaseMainItem y) {
            int result = 0;

            if (UserPreferences.General.getFolderPosition() != UserPreferences.General.FolderPosition.Mixed) {
                if (x instanceof Folder && !(y instanceof Folder))
                    result = -1;
                else if (y instanceof Folder && !(x instanceof Folder))
                    result = 1;

                if (result != 0) {
                    if (UserPreferences.General.getFolderPosition() == UserPreferences.General.FolderPosition.Bottom)
                        result = -result;

                    return result;
                }
            }

            switch (UserPreferences.General.getSortMode()) {
                case TimeStamp:
                    result = x.getTimestamp().compareTo(y.getTimestamp());
                    break;

                case Grouped:
                    Integer xType = FolderAdapter.getItemViewType(x);
                    Integer yType = FolderAdapter.getItemViewType(y);

                    result = xType.compareTo(yType);
                    break;
            }

            // Final secondary sort is always by name.
            if (0 == result) {
                if (UserPreferences.General.getDisplayMode() == UserPreferences.General.DisplayMode.Tree)
                    result = x.getName().compareToIgnoreCase(y.getName());
                else
                    result = x.getFullPath().compareToIgnoreCase(y.getFullPath());
            }

            if (UserPreferences.General.getSortDirection() == UserPreferences.SortDirection.Descending)
                result = -result;

            return result;
        }
    }

    private ArrayList<BaseMainItem> sortedItems;
    private Folder folder;
    private IViewProvider viewProvider;

    public Folder getFolder() {
        return folder;
    }
    public BaseMainItem[] getItems() {
        return sortedItems.toArray(new BaseMainItem[0]);
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
        sortedItems = new ArrayList<>();

        if (UserPreferences.General.getDisplayMode() == UserPreferences.General.DisplayMode.Tree)
            sortedItems.addAll(folder.getChildren());
        else
            sortedItems.addAll(folder.getChildren(true, false));

        sort();
    }

    public void refresh() {
        setFolder(getFolder());
    }

    public FolderAdapter(Folder folder, IViewProvider viewProvider) {
        this.viewProvider = viewProvider;
        setFolder(folder);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewProvider.getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bind(sortedItems.get(position));
    }

    @Override
    public int getItemCount() {
        return sortedItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(sortedItems.get(position));
    }

    private static int getItemViewType(BaseMainItem item) {
        switch (item.getViewType()) {
            case Folder:
                return 0;
            case Note:
                return 1;
            case CheckList:
                return 2;
            default:
                return -1;
        }
    }

    private void sortItems() {
        Collections.sort(sortedItems, new FolderAdapter.Sorter());
    }

    private void sort() {
        sortItems();
        notifyDataSetChanged();
    }

    private int findInsertionPoint(BaseMainItem item) {
        Sorter sorter = new FolderAdapter.Sorter();
        int position = 0;
        int count = sortedItems.size();

        // Since sortedItems is already sorted, we could do a binary search,
        // but there will not be that many items in the list.  Just run up
        // the list until we find the right point.
        while (position < count && sorter.compare(item, sortedItems.get(position)) >= 0)
            position++;

        return position;
    }

    public int onItemAdded(BaseMainItem item) {
        int position = findInsertionPoint(item);

        sortedItems.add(position, item);
        notifyItemInserted(position);

        return position;
    }

    public int onItemChanged(BaseMainItem item) {
        int oldPosition = sortedItems.indexOf(item);

        if (oldPosition >= 0) {
            sortedItems.remove(oldPosition);

            int newPosition = findInsertionPoint(item);

            sortedItems.add(newPosition, item);

            if (oldPosition != newPosition)
                notifyItemMoved(oldPosition, newPosition);

            notifyItemChanged(newPosition);

            return newPosition;
        }

        return -1;
    }

    public void onItemRemoved(BaseMainItem item) {
        int oldPosition = sortedItems.indexOf(item);

        if (oldPosition >= 0) {
            sortedItems.remove(oldPosition);
            notifyItemRemoved(oldPosition);
        }
    }

    public boolean isItem(BaseMainItem item) {
        return sortedItems.contains(item);
    }
}
