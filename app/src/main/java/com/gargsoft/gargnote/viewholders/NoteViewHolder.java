package com.gargsoft.gargnote.viewholders;

import android.view.View;
import android.widget.TextView;
import com.gargsoft.gargnote.R;
import com.gargsoft.gargnote.models.Note;
import com.gargsoft.gargnote.models.UserPreferences;

import java.text.DateFormat;

public class NoteViewHolder extends BaseViewHolder {
    private TextView nameView;
    private TextView timestampView;

    public NoteViewHolder(View view) {
        super(view);

        nameView = view.findViewById(R.id.name);
        timestampView = view.findViewById(R.id.timestamp);
    }

    @Override
    public void bind(Object item) {
        super.bind(item);

        Note note = (Note)item;

        nameView.setText((UserPreferences.General.getDisplayMode() == UserPreferences.General.DisplayMode.Flat && UserPreferences.General.isShowFullPathInFlatMode()) ? note.getFullPath() : note.getName());
        timestampView.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(note.getTimestamp()));
    }
}
