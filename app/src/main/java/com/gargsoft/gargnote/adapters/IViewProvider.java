package com.gargsoft.gargnote.adapters;

import android.view.ViewGroup;

import com.gargsoft.gargnote.viewholders.BaseViewHolder;

public interface IViewProvider {
    public BaseViewHolder getViewHolder(ViewGroup parent, int viewType);
    public int getViewType(Object item);
}
