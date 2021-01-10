package com.gargsoft.gargnote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gargsoft.gargnote.GargNoteApplication;
import com.gargsoft.gargnote.R;
import com.gargsoft.gargnote.adapters.FolderAdapter;
import com.gargsoft.gargnote.adapters.IViewProvider;
import com.gargsoft.gargnote.adapters.ViewType;
import com.gargsoft.gargnote.models.BaseMainItem;
import com.gargsoft.gargnote.models.CheckList;
import com.gargsoft.gargnote.models.Folder;
import com.gargsoft.gargnote.models.Note;
import com.gargsoft.gargnote.models.UserPreferences;
import com.gargsoft.gargnote.utilities.DatabaseHelper;
import com.gargsoft.gargnote.utilities.Helper;
import com.gargsoft.gargnote.viewholders.BaseViewHolder;
import com.gargsoft.gargnote.viewholders.CheckListViewHolder;
import com.gargsoft.gargnote.viewholders.FolderViewHolder;
import com.gargsoft.gargnote.viewholders.NoteViewHolder;

import java.util.ArrayList;
import java.util.Comparator;

//FIXX - What is the best way when we start activities and come back to update the display?
// If anything is modified down the chain, we need to update the display.  Modifying checklists
// or deleting folders or checklists could affect our display because of the checklist dependencies

public class FolderActivity extends AppCompatActivity implements IViewProvider {
    private interface OnSelectFolderListener {
        public void onFolderSelected(Folder folder);
    }
    public final static String STARTINFO_ID = "com.gargsoft.gargnote.STARTINFO_ID";
    private final static ViewType[] viewTypes = {ViewType.Folder, ViewType.Note, ViewType.CheckList};

    FolderAdapter adapter;
    RecyclerView listView;
    BaseViewHolder contextMenuViewHolder;

    public FolderActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        listView = (RecyclerView)findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        int id = 0;

        if (null != intent)
            id = intent.getIntExtra(STARTINFO_ID, 0);

        Folder rootFolder = GargNoteApplication.getRootFolder();

        if (id > 0) {
            Folder folder = (Folder)rootFolder.findChild(id);

            if (null != folder)
                rootFolder = folder;
        }

        final TextView path = findViewById(R.id.path);

        path.setText(rootFolder.getPath());

        adapter = new FolderAdapter(rootFolder, this);
        listView.setAdapter(adapter);

        ImageButton button = findViewById(R.id.home);

        if (id > 0) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                finishAffinity();
                start(FolderActivity.this, 0);
                }
            });
        }
        else
            button.setVisibility(View.GONE);

        button = findViewById(R.id.newCheckList);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckListActivity.start(FolderActivity.this, adapter.getFolder().getId(), 0);
            }
        });

        button = findViewById(R.id.newFolder);

        if (UserPreferences.General.getDisplayMode() == UserPreferences.General.DisplayMode.Tree) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addOrEditFolder(0);
                }
            });
        }
        else
            button.setVisibility(View.GONE);

        button = findViewById(R.id.newNote);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FIXX
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //FIXX - Is this correct?  Should we always refresh?  What about when a checklist
        // or note is added or modified?  If a checklist is modified down the chain, it could
        // affect this display.
        adapter.refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void start(Context context, int id) {
        Intent intent = new Intent(context, FolderActivity.class);

        intent.putExtra(FolderActivity.STARTINFO_ID, id);

        context.startActivity(intent);
    }

    private void addOrEditFolder(int id) {
        Folder inputFolder = null;
        final EditText input = new EditText(this);

        input.setSingleLine();

        if (id > 0) {
            inputFolder = (Folder)GargNoteApplication.getRootFolder().findChild(id);

            if (null == inputFolder) {
                Toast.makeText(this, R.string.unknownfolder, Toast.LENGTH_SHORT).show();
                return;
            }

            input.append(inputFolder.getName());
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);

        final Folder folder = inputFolder;
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle((id > 0) ? R.string.folder : R.string.new_folder)
            .setView(input)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final DialogInterface finalDialog = dialog;
                Button button = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = input.getText().toString().trim();
                        Folder editFolder = folder;

                        if (!name.isEmpty()) {
                            boolean changed;

                            if (null == editFolder) {
                                editFolder = new Folder(adapter.getFolder());
                                changed = true;
                            }
                            else
                                changed = !editFolder.getName().equals(name);

                            if (changed) {
                                editFolder.refreshTimestamp();
                                editFolder.setName(name);

                                boolean ok = false;
                                boolean newFolder = (editFolder.getId() <= 0);

                                if (!newFolder)
                                    ok = DatabaseHelper.updateFolder(editFolder);
                                else if (DatabaseHelper.insertFolder(editFolder))
                                    ok = true;
                                else
                                    editFolder.setParent(null); // To remove from parent children

                                if (ok) {
                                    int position;

                                    if (newFolder)
                                        position = adapter.onItemAdded(editFolder);
                                    else
                                        position = adapter.onItemChanged(editFolder);

                                    if (position >= 0)
                                        ensureItemVisible(position, false);
                                }
                                else
                                    Helper.toastSaveFailed(FolderActivity.this);
                            }
                        }

                        finalDialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
        Helper.requestDefaultEditFocus(FolderActivity.this, input);
    }

    @Override
    public BaseViewHolder getViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        BaseViewHolder viewHolder;

        switch (getIndexViewType(viewType)) {
            case Folder:
                view = inflater.inflate(R.layout.item_folder, parent, false);
                viewHolder = new FolderViewHolder(view);
                break;
            case Note:
                view = inflater.inflate(R.layout.item_note, parent, false);
                viewHolder = new NoteViewHolder(view);
                break;
            case CheckList:
                view = inflater.inflate(R.layout.item_checklist, parent, false);
                viewHolder = new CheckListViewHolder(view);
                break;
            default:
                return null;
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseViewHolder viewHolder = (BaseViewHolder)view.getTag();
                BaseMainItem item = (BaseMainItem)viewHolder.getItem();

                switch (item.getViewType()) {
                    case Folder:
                        FolderActivity.start(FolderActivity.this, item.getId());
                        break;

                    case Note:
                        break;

                    case CheckList:
                        CheckListActivity.start(FolderActivity.this, item.getParent().getId(), item.getId());
                        break;
                }
            }
        });

        registerForContextMenu(viewHolder.itemView);

        return viewHolder;
    }

    @Override
    public int getViewType(Object item) {
        BaseMainItem baseItem = (BaseMainItem)item;
        return 0;
    }

    private static int getViewTypeIndex(BaseMainItem item) {
        ViewType viewType = item.getViewType();
        int index = 0;

        for (ViewType type : viewTypes) {
            if (type == viewType)
                return index;

            index++;
        }

        return index;
    }

    private static ViewType getIndexViewType(int index) {
        return viewTypes[index];
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        contextMenuViewHolder = (BaseViewHolder)view.getTag();

        BaseMainItem item = (BaseMainItem)contextMenuViewHolder.getItem();
        MenuInflater inflater = getMenuInflater();

        switch (item.getViewType()) {
            case Folder:
                inflater.inflate(R.menu.contextmenu_folder, menu);
                menu.findItem(R.id.move).setEnabled(canMoveItem(item));
                break;

            case Note:
                inflater.inflate(R.menu.contextmenu_note, menu);
                menu.findItem(R.id.move).setEnabled(canMoveItem(item));
                break;

            case CheckList:
                inflater.inflate(R.menu.contextmenu_checklist, menu);
                menu.findItem(R.id.move).setEnabled(canMoveItem(item));
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem menuItem) {
        boolean retval = false;

        if (null != contextMenuViewHolder) {
            BaseMainItem item = (BaseMainItem)contextMenuViewHolder.getItem();

            switch (item.getViewType()) {
                case Folder:
                    retval = HandleFolderMenuItem(contextMenuViewHolder, menuItem);
                    break;

                case Note:
                    retval = HandleNoteMenuItem(contextMenuViewHolder, menuItem);
                    break;

                case CheckList:
                    retval = HandleCheckListMenuItem(contextMenuViewHolder, menuItem);
                    break;
            }
        }

        return retval;
    }

    private boolean HandleFolderMenuItem(BaseViewHolder viewHolder, MenuItem menuItem) {
        final Folder folder = (Folder)viewHolder.getItem();
        boolean retval = false;

        switch (menuItem.getItemId()) {
            case R.id.rename:
                addOrEditFolder(folder.getId());
                retval = true;
                break;

            case R.id.move:
                selectFolderAndMoveItem(folder);
                retval = true;
                break;

            case R.id.delete:
                if (UserPreferences.General.isConfirmDelete()) {
                    Helper.confirmation(this, R.string.confirm_delete_folder, new Helper.ConfirmationListener() {
                        @Override
                        public void onConfirmation() {
                            deleteFolder(folder);
                        }
                    });
                }
                else
                    deleteFolder(folder);

                retval = true;
                break;
        }

        return retval;
    }

    private boolean HandleNoteMenuItem(BaseViewHolder viewHolder, MenuItem menuItem) {
        final Note note = (Note)viewHolder.getItem();
        boolean retval = false;

        switch (menuItem.getItemId()) {
            case R.id.move:
                selectFolderAndMoveItem(note);
                retval = true;
                break;

            case R.id.delete:
                if (UserPreferences.General.isConfirmDelete()) {
                    Helper.confirmation(this, R.string.confirm_delete_note, new Helper.ConfirmationListener() {
                        @Override
                        public void onConfirmation() {
                            deleteNote(note);
                        }
                    });
                }
                else
                    deleteNote(note);

                retval = true;
                break;
        }

        return retval;
    }

    private boolean HandleCheckListMenuItem(BaseViewHolder viewHolder, MenuItem menuItem) {
        final CheckList checkList = (CheckList)viewHolder.getItem();
        boolean retval = false;

        switch (menuItem.getItemId()) {
            case R.id.move:
                selectFolderAndMoveItem(checkList);
                retval = true;
                break;

            case R.id.delete:
                if (UserPreferences.General.isConfirmDelete()) {
                    Helper.confirmation(this, R.string.confirm_delete_checklist, new Helper.ConfirmationListener() {
                        @Override
                        public void onConfirmation() {
                            deleteCheckList(checkList);
                        }
                    });
                }
                else
                    deleteCheckList(checkList);

                retval = true;
                break;
        }

        return retval;
    }

    private boolean canMoveItem(final BaseMainItem item) {
        return (UserPreferences.General.getDisplayMode() == UserPreferences.General.DisplayMode.Tree && getAvailableFolders(getMoveItemIgnoreFolders(item)).size() > 0);
    }

    private static ArrayList<Folder> getMoveItemIgnoreFolders(BaseMainItem item) {
        ArrayList<Folder> ignoreFolders = new ArrayList<>();

        // No point in selecting same parent
        ignoreFolders.add(item.getParent());

        if (item instanceof Folder) {
            // Cannot select same folder or any of its descendants

            ignoreFolders.add((Folder)item);

            for (Folder folder : ((Folder)item).getFolders(true))
                ignoreFolders.add(folder);
        }

        return ignoreFolders;
    }

    private static ArrayList<Folder> getAvailableFolders(Iterable<Folder> ignoreFolders) {
        ArrayList<Folder> folders = new ArrayList<>();
        Folder rootFolder = GargNoteApplication.getRootFolder();

        folders.add(rootFolder);
        folders.addAll(rootFolder.getFolders(true));

        if (null != ignoreFolders) {
            for (Folder folder : ignoreFolders)
                folders.remove(folder);
        }

        return folders;
    }

    private void selectFolderAndMoveItem(final BaseMainItem item) {
        selectFolder(getResources().getString(R.string.move_to_folder), getResources().getString(R.string.move), getMoveItemIgnoreFolders(item), new OnSelectFolderListener() {
            @Override
            public void onFolderSelected(Folder selectedFolder) {
                item.setParent(selectedFolder);
                DatabaseHelper.updateParent(item);
                adapter.onItemRemoved(item);
            }
        });
    }

    private void selectFolder(CharSequence title, CharSequence actionButtonText, Iterable<Folder> ignoreFolders, final OnSelectFolderListener listener) {
        final ArrayList<Folder> folders = getAvailableFolders(ignoreFolders);

        if (folders.size() > 0) {
            folders.sort(new Comparator<Folder>() {
                @Override
                public int compare(Folder folder, Folder t1) {
                    return folder.getPath().compareToIgnoreCase(t1.getPath());
                }
            });

            ArrayList<Object> names = new ArrayList<>();

            for (Folder folder : folders)
                names.add(folder.getPath());

            Helper.selectItem(this, title, names, -1, actionButtonText, null, new Helper.SelectItemListener() {
                @Override
                public void onItemSelected(int position, boolean secondaryAction) {
                    listener.onFolderSelected(folders.get(position));
                }
            });
        }
    }

    private void ensureItemVisible(int position, boolean completelyVisible) {
        LinearLayoutManager lm = (LinearLayoutManager)listView.getLayoutManager();
        int first = (completelyVisible) ? lm.findFirstCompletelyVisibleItemPosition() : lm.findFirstVisibleItemPosition();
        int last = (completelyVisible) ? lm.findLastCompletelyVisibleItemPosition() : lm.findLastVisibleItemPosition();

        if (position < first || position > last)
            listView.scrollToPosition(position);
    }

    private void deleteFolder(Folder folder) {
        ArrayList<CheckList> visuallyAffectedCheckLists = new ArrayList<>();
        ArrayList<CheckList> checkListsToBeDeleted = folder.getCheckLists(true);
        CheckList checkList;

        // If we are deleting a folder, we know that we are not in flat display mode so there won't
        // be any checklists being displayed here that would get deleted by deleting the folder.

        // Find out which checklists in this folder are affected by deleting the checklist.
        for (BaseMainItem item : adapter.getItems()) {
            if (item instanceof CheckList) {
                checkList = (CheckList)item;

                for (CheckList deletingCheckList : checkListsToBeDeleted) {
                    if (checkList.isDependency(deletingCheckList, true))
                        visuallyAffectedCheckLists.add(checkList);
                }
            }
        }

        // Delete the folder.  This will delete the entire chain.
        DatabaseHelper.deleteFolder(folder.getId());
        folder.delete();
        adapter.onItemRemoved(folder);

        for (CheckList item : visuallyAffectedCheckLists)
            adapter.onItemChanged(item);
    }

    private void deleteCheckList(CheckList checkList) {
        ArrayList<CheckList> visuallyAffectedCheckLists = new ArrayList<>();

        for (BaseMainItem item : adapter.getItems()) {
            if (item instanceof CheckList && item != checkList && ((CheckList)item).isDependency(checkList, true))
                visuallyAffectedCheckLists.add((CheckList)item);
        }

        DatabaseHelper.deleteCheckList(checkList.getId());
        checkList.delete();
        adapter.onItemRemoved(checkList);

        for (CheckList item : visuallyAffectedCheckLists)
            adapter.onItemChanged(item);
    }

    private void deleteNote(Note note) {
        DatabaseHelper.deleteNote(note.getId());
        note.delete();
        adapter.onItemRemoved(note);
    }
}
