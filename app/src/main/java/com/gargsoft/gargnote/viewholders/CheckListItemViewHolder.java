package com.gargsoft.gargnote.viewholders;

import android.graphics.Paint;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.gargsoft.gargnote.R;
import com.gargsoft.gargnote.models.CheckList;
import com.gargsoft.gargnote.models.CheckListItem;

public class CheckListItemViewHolder extends BaseViewHolder {
    private CheckBox checkView;
    private TextView nameView;
    private CheckList displayCheckList;

    public CheckBox getCheckView() {
        return checkView;
    }

    public TextView getNameView() {
        return nameView;
    }

    public CheckListItemViewHolder(@NonNull View itemView, @NonNull CheckList checkList) {
        super(itemView);

        checkView = (CheckBox) itemView.findViewById(R.id.check);
        nameView = (TextView) itemView.findViewById(R.id.name);
        displayCheckList = checkList;
    }

    @Override
    public void bind(Object item) {
        super.bind(item);

        CheckListItem checkListItem = (CheckListItem)item;

        checkView.setChecked(checkListItem.isChecked());
        nameView.setText(checkListItem.getName());

        nameView.setTextAppearance((checkListItem.isComplete()) ? R.style.CheckListItemName_Complete : R.style.CheckListItemName_Incomplete);

        if (checkListItem.isComplete())
            nameView.setPaintFlags(nameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            nameView.setPaintFlags(nameView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        // isItem is faster than isDependency since it doesn't have to search the dependency tree
        if (!displayCheckList.isItem(checkListItem))
            nameView.setPaintFlags(nameView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        else
            nameView.setPaintFlags(nameView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
    }
}
