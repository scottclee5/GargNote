package com.gargsoft.gargnote.viewholders;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    private Object item;
    private int position;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);

        itemView.setTag(this);
    }

    public void bind(Object item) {
        this.item = item;
    }

    public Object getItem() {
        return item;
    }
}
