package com.gargsoft.gargnote.viewholders;

import android.view.View;
import android.widget.TextView;
import com.gargsoft.gargnote.R;
import com.gargsoft.gargnote.models.Folder;

public class FolderViewHolder extends BaseViewHolder {
    private TextView nameView;

    public FolderViewHolder(View view) {
        super(view);

        nameView = view.findViewById(R.id.name);
    }

    @Override
    public void bind(Object item) {
        super.bind(item);

        Folder folder = (Folder)item;

        nameView.setText(folder.getName());
    }
}
