package com.gargsoft.gargnote.viewholders;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.gargsoft.gargnote.R;
import com.gargsoft.gargnote.models.CheckList;
import com.gargsoft.gargnote.models.CheckListStatus;
import com.gargsoft.gargnote.models.UserPreferences;
import com.gargsoft.gargnote.utilities.Helper;

import java.text.DateFormat;

public class CheckListViewHolder extends BaseViewHolder {
    private TextView nameView;
    private TextView statusView;
    private TextView timestampView;

    public CheckListViewHolder(@NonNull View itemView) {
        super(itemView);

        nameView = itemView.findViewById(R.id.name);
        statusView = itemView.findViewById(R.id.status);
        timestampView = itemView.findViewById(R.id.timestamp);
    }

    @Override
    public void bind(Object item) {
        super.bind(item);

        CheckList checkList = (CheckList)item;
        CheckListStatus status = checkList.getStatus(true);
        Boolean red = (status.getComplete() < status.getCount());

        nameView.setText((UserPreferences.General.getDisplayMode() == UserPreferences.General.DisplayMode.Flat && UserPreferences.General.isShowFullPathInFlatMode()) ? checkList.getFullPath() : checkList.getName());
        statusView.setText(String.format("%d/%d", status.getComplete(), status.getCount()));

        statusView.setTextAppearance((red) ? R.style.CheckListStatus_Red : R.style.CheckListStatus_Green);
        statusView.setBackgroundResource((red) ? R.drawable.rounded_rectangle_red : R.drawable.rounded_rectangle_green);

        timestampView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(checkList.getTimestamp()));

    }
}
