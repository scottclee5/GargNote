package com.gargsoft.gargnote.adapters;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gargsoft.gargnote.models.CheckList;
import com.gargsoft.gargnote.models.CheckListItem;
import com.gargsoft.gargnote.models.UserPreferences;
import com.gargsoft.gargnote.viewholders.BaseViewHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CheckListAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private class Sorter implements Comparator<CheckListItem> {
        @Override
        public int compare(CheckListItem x, CheckListItem y) {
            return x.getName().compareToIgnoreCase(y.getName());
        }
    }

    private ArrayList<CheckListItem> sortedItems;
    private CheckList checkList;
    private IViewProvider viewProvider;

    public CheckList getCheckList() {
        return checkList;
    }
    public CheckListItem[] getItems() {
        return sortedItems.toArray(new CheckListItem[0]);
    }

    public void setCheckList(CheckList checkList) {
        this.checkList = checkList;
        sortedItems = new ArrayList<>();

        sortedItems.addAll(Arrays.asList(checkList.getVirtualItems()));

        sort();
    }

    public CheckListAdapter(CheckList checkList, IViewProvider viewProvider) {
        this.viewProvider = viewProvider;
        setCheckList(checkList);
    }

    public CheckListItem getItem(int position) {
        return sortedItems.get(position);
    }
    public void refresh() {
        setCheckList(getCheckList());
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

    private void sortItems() {
        Collections.sort(sortedItems, new CheckListAdapter.Sorter());
    }

    private void sort() {
        sortItems();
        notifyDataSetChanged();
    }

    public int onItemAdded(CheckListItem item) {
        int position = findInsertionPoint(item);

        sortedItems.add(position, item);
        notifyItemInserted(position);

        return position;
    }

    public int onItemChanged(CheckListItem item) {
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

    public void onItemRemoved(CheckListItem item) {
        int oldPosition = sortedItems.indexOf(item);

        if (oldPosition >= 0) {
            sortedItems.remove(oldPosition);
            notifyItemRemoved(oldPosition);
        }
    }

    private int findInsertionPoint(CheckListItem item) {
        Sorter sorter = new CheckListAdapter.Sorter();
        int position = 0;
        int count = sortedItems.size();

        // Since sortedItems is already sorted, we could do a binary search,
        // but there will not be that many items in the list.  Just run up
        // the list until we find the right point.
        while (position < count && sorter.compare(item, sortedItems.get(position)) >= 0)
            position++;

        return position;
    }
}
