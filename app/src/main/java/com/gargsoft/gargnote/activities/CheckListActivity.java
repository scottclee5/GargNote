package com.gargsoft.gargnote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gargsoft.gargnote.GargNoteApplication;
import com.gargsoft.gargnote.R;
import com.gargsoft.gargnote.adapters.CheckListAdapter;
import com.gargsoft.gargnote.adapters.IViewProvider;
import com.gargsoft.gargnote.controls.EditTextEx;
import com.gargsoft.gargnote.models.BaseMainItem;
import com.gargsoft.gargnote.models.CheckList;
import com.gargsoft.gargnote.models.CheckListItem;
import com.gargsoft.gargnote.models.Folder;
import com.gargsoft.gargnote.models.UserPreferences;
import com.gargsoft.gargnote.utilities.DatabaseHelper;
import com.gargsoft.gargnote.utilities.Helper;
import com.gargsoft.gargnote.viewholders.BaseViewHolder;
import com.gargsoft.gargnote.viewholders.CheckListItemViewHolder;

import java.util.ArrayList;
import java.util.Comparator;

public class CheckListActivity extends AppCompatActivity implements IViewProvider {
    public final static String STARTINFO_ID = "com.gargsoft.gargnote.STARTINFO_ID";
    public final static String STARTINFO_FOLDERID = "com.gargsoft.gargnote.STARTINFO_FOLDERID";

    CheckListAdapter adapter;
    RecyclerView listView;
    CheckListItemViewHolder contextMenuViewHolder;
    private boolean deleting = false;

    private enum checkItemsAction {
        check,
        uncheck,
        toggle
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        listView = (RecyclerView) findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.inflateMenu(R.menu.menu_checklist);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Intent intent = getIntent();
        CheckList checkList = null;

        if (null != intent) {
            int id = intent.getIntExtra(STARTINFO_ID, 0);

            if (id > 0) {
                checkList = (CheckList) GargNoteApplication.getRootFolder().findChild(id);
            } else {
                id = intent.getIntExtra(STARTINFO_FOLDERID, 0);

                Folder folder = GargNoteApplication.getRootFolder();

                if (id > 0)
                    folder = (Folder) folder.findChild(id);

                if (null != folder) {
                    checkList = new CheckList(folder);
                } else {
                    Toast.makeText(this, R.string.unknownfolder, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }

        if (null == checkList) {
            Toast.makeText(this, R.string.unknownchecklist, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EditText nameView = findViewById(R.id.name);

        nameView.setText(checkList.getName());

        adapter = new CheckListAdapter(checkList, this);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_checklist, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        CheckListItem[] allItems = adapter.getItems();
        CheckListItem[] checkedItems = getCheckedItems();

        menu.findItem(R.id.dependencies).setEnabled(canSelectDependencies());
        menu.findItem(R.id.checkAll).setEnabled(allItems.length > 0);
        menu.findItem(R.id.uncheckAll).setEnabled(allItems.length > 0);
        menu.findItem(R.id.toggleCheck).setEnabled(allItems.length > 0);
        menu.findItem(R.id.moveItems).setEnabled(canMoveCopyItems(checkedItems));
        menu.findItem(R.id.deleteItems).setEnabled(checkedItems.length > 0);

        return super.onMenuOpened(featureId, menu);
    }

    public static void start(Context context, int folderId, int id) {
        Intent intent = new Intent(context, CheckListActivity.class);

        intent.putExtra(CheckListActivity.STARTINFO_FOLDERID, folderId);
        intent.putExtra(CheckListActivity.STARTINFO_ID, id);

        context.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        boolean delete = deleting;
        CheckList checkList = adapter.getCheckList();

        if (!deleting) {
            String name = ((EditText) findViewById(R.id.name)).getText().toString().trim();
            Intent intent = getIntent();
            int id = 0;

            if (null != intent)
                id = intent.getIntExtra(STARTINFO_ID, 0);

            if (id > 0 || !name.isEmpty() || checkList.getItems().size() > 0)
                ensureCheckListSaved();
            else
                delete = true;
        }

        if (delete) {
            if (checkList.getId() > 0)
                DatabaseHelper.deleteCheckList(checkList.getId());

            checkList.delete();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public BaseViewHolder getViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_checklistitem, parent, false);
        CheckListItemViewHolder viewHolder = new CheckListItemViewHolder(view, adapter.getCheckList());

        LinearLayout layout = (LinearLayout)view.findViewById(R.id.checklistitem);
        View checkbox = layout.findViewById(R.id.check);

        if (UserPreferences.CheckList.getCheckPosition() == UserPreferences.CheckList.CheckPosition.right) {
            layout.removeView(checkbox);
            layout.addView(checkbox);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseViewHolder viewHolder = (BaseViewHolder) view.getTag();
                checkItem((CheckListItem) viewHolder.getItem(), checkItemsAction.toggle);
            }
        });

        registerForContextMenu(viewHolder.itemView);

        return viewHolder;
    }

    @Override
    public int getViewType(Object item) {
        return 0;
    }

    private void addOrEditItem(final CheckListItem inputItem) {
        final EditTextEx input = new EditTextEx(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(3);
        input.setHorizontallyScrolling(false);
        input.setNoEnterAction(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        input.setLayoutParams(lp);
        input.setGravity(Gravity.TOP | Gravity.START);

        if (null != inputItem) {
            input.append(inputItem.getName());
        }

        final CheckListItem item = inputItem;
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(input)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);

        if (null == inputItem)
            builder.setNeutralButton(R.string.add_more, null);

        final AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = input.getText().toString().trim();

                        if (!name.isEmpty() && ensureCheckListSaved()) {
                            boolean addMore = (((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL) == view);
                            CheckListItem editItem = item;
                            boolean changed = false;

                            if (null == editItem) {
                                editItem = new CheckListItem(adapter.getCheckList());
                                changed = true;
                            } else
                                changed = !editItem.getName().equals(name);

                            if (changed) {
                                editItem.refreshTimestamp();
                                editItem.setName(name);

                                int position;

                                if (editItem.getId() <= 0)
                                    position = adapter.onItemAdded(editItem);
                                else
                                    position = adapter.onItemChanged(editItem);

                                if (position >= 0)
                                    ensureItemVisible(position, false);

                                boolean ok;

                                if (editItem.getId() > 0)
                                    ok = DatabaseHelper.updateCheckListItem(editItem);
                                else
                                    ok = DatabaseHelper.insertCheckListItem(editItem);

                                if (!ok)
                                    Helper.toastSaveFailed(CheckListActivity.this);
                                else
                                    refreshTimestamp();
                            }

                            if (addMore) {
                                input.setText("");
                            } else
                                dialog.dismiss();
                        }
                    }
                };

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(listener);

                button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                button.setOnClickListener(listener);
            }
        });

        if (null == inputItem && UserPreferences.CheckList.isAddMoreOnEnter()) {
            input.setImeOptions(EditorInfo.IME_ACTION_GO);

            input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);

                        button.performClick();
                        return true;
                    }

                    return false;
                }
            });

            input.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);

                        button.performClick();
                        return true;
                    }

                    return false;
                }
            });
        }

        dialog.show();
        Helper.requestDefaultEditFocus(CheckListActivity.this, input);
    }

    private void ensureItemVisible(int position, boolean completelyVisible) {
        LinearLayoutManager lm = (LinearLayoutManager) listView.getLayoutManager();
        int first = (completelyVisible) ? lm.findFirstCompletelyVisibleItemPosition() : lm.findFirstVisibleItemPosition();
        int last = (completelyVisible) ? lm.findLastCompletelyVisibleItemPosition() : lm.findLastVisibleItemPosition();

        if (position < first || position > last)
            listView.scrollToPosition(position);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        contextMenuViewHolder = (CheckListItemViewHolder) view.getTag();

        CheckListItem item = (CheckListItem) contextMenuViewHolder.getItem();
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.contextmenu_checklistitem, menu);
        menu.findItem(R.id.move).setEnabled(canMoveCopyItem(item));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem menuItem) {
        if (null != contextMenuViewHolder) {
            final CheckListItem item = (CheckListItem) contextMenuViewHolder.getItem();

            switch (menuItem.getItemId()) {
                case R.id.edit:
                    addOrEditItem(item);
                    break;

                case R.id.move:
                    selectCheckListAndMoveCopyItem(item);
                    break;

                case R.id.delete:
                    deleteItem(item, true);
                    break;
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.newItem:
                addOrEditItem(null);
                break;

            case R.id.dependencies:
                selectDependencies();
                break;

            case R.id.checkAll:
                checkItems(adapter.getItems(), checkItemsAction.check);
                break;

            case R.id.uncheckAll:
                checkItems(adapter.getItems(), checkItemsAction.uncheck);
                break;

            case R.id.toggleCheck:
                checkItems(adapter.getItems(), checkItemsAction.toggle);
                break;

            case R.id.moveItems:
                selectCheckListAndMoveCopyItems(getCheckedItems());
                break;

            case R.id.deleteItems:
                deleteItems(getCheckedItems(), true);
                break;

            case R.id.delete:
                if (UserPreferences.General.isConfirmDelete()) {
                    Helper.confirmation(this, R.string.confirm_delete_checklist, new Helper.ConfirmationListener() {
                        @Override
                        public void onConfirmation() {
                        deleting = true;
                        finish();
                        }
                    });
                }
                else {
                    deleting = true;
                    finish();
                }

                break;
        }

        return true;
    }

    private boolean canSelectDependencies() {
        return (getAvailableDependencies().size() > 0);
    }

    private ArrayList<CheckList> getAvailableDependencies() {
        ArrayList<CheckList> availableDependencies = new ArrayList<>();

        for (CheckList checkList : GargNoteApplication.getRootFolder().getCheckLists(true)) {
            if (adapter.getCheckList() != checkList && !checkList.isDependency(adapter.getCheckList(), true)) {
                availableDependencies.add(checkList);
            }
        }

        return availableDependencies;
    }

    private void selectDependencies() {
        final ArrayList<CheckList> availableDependencies = getAvailableDependencies();

        if (availableDependencies.size() > 0) {
            ArrayList<Integer> selectedItems = new ArrayList<>();

            availableDependencies.sort(new Comparator<CheckList>() {
                @Override
                public int compare(CheckList x, CheckList y) {
                    return x.getFullPath().compareToIgnoreCase(y.getFullPath());
                }
            });

            ArrayList<Object> availableItems = new ArrayList<>();

            for (CheckList item : availableDependencies) {
                if (adapter.getCheckList().isDependency(item, false))
                    selectedItems.add(availableItems.size());

                availableItems.add(item.getFullPath());
            }

            Helper.selectMultipleItems(this, "Select dependencies", availableItems, selectedItems, new Helper.SelectMultipleItemListener() {
                @Override
                public void onOK(ArrayList<Integer> selectedItems) {
                    if (ensureCheckListSaved()) {
                        adapter.getCheckList().getDependencies().clear();

                        for (int position : selectedItems) {
                            adapter.getCheckList().getDependencies().add(availableDependencies.get(position));
                        }

                        adapter.refresh();

                        if (!DatabaseHelper.updateCheckListDependencies(adapter.getCheckList()))
                            Helper.toastSaveFailed(CheckListActivity.this);
                        else
                            refreshTimestamp();
                    }
                }
            });
        }
    }

    private void deleteItem(CheckListItem item, boolean confirm) {
        deleteItems(new CheckListItem[] {item}, confirm);
    }

    private void deleteItems(final CheckListItem[] items, boolean confirm) {
        if (confirm) {
            int mainCount = 0;
            int otherCount = 0;

            for (CheckListItem item : items) {
                if (adapter.getCheckList().isItem(item))
                    mainCount++;
                else
                    otherCount++;
            }

            if (otherCount > 0) {
                if (UserPreferences.CheckList.isConfirmVirtualDelete()) {
                    Helper.confirmation(this, R.string.confirm_delete_virtualchecklistitem, new Helper.ConfirmationListener() {
                        @Override
                        public void onConfirmation() {
                            deleteItems(items);
                        }
                    });
                }
                else
                    deleteItems(items);
            }
            else if (mainCount > 0) {
                if (UserPreferences.CheckList.isConfirmDelete()) {
                    Helper.confirmation(this, R.string.confirm_delete_checklistitem, new Helper.ConfirmationListener() {
                        @Override
                        public void onConfirmation() {
                            deleteItems(items);
                        }
                    });
                }
                else
                    deleteItems(items);
            }
        }
        else
            deleteItems(items);
    }

    private void deleteItems(CheckListItem[] items) {
        boolean refreshTimestamp = false;

        for (CheckListItem item : items) {
            if (!DatabaseHelper.deleteCheckListItem(item.getId())) {
                Toast.makeText(CheckListActivity.this, R.string.deletefailed, Toast.LENGTH_SHORT).show();
                break;
            }

            item.setParent(null); // This will remove it from the parent.
            adapter.onItemRemoved(item);
            refreshTimestamp = true;
        }

        if (refreshTimestamp)
            refreshTimestamp();
    }

    private boolean canMoveCopyItem(final CheckListItem item) {
        return canMoveCopyItems(new CheckListItem[] {item});
    }

    private boolean canMoveCopyItems(final CheckListItem[] items) {
        // Must have at least 1 item and all items must be a member of this checklist.
        if (items.length > 0 && getAvailableCheckListsForMoveCopy().size() > 0) {
            for (CheckListItem item : items) {
                if (!adapter.getCheckList().isItem(item))
                    return false;
            }

            return true;
        }

        return false;
    }

    private ArrayList<CheckList> getAvailableCheckListsForMoveCopy() {
        ArrayList<CheckList> checkLists = new ArrayList<>();

        checkLists.addAll(GargNoteApplication.getRootFolder().getCheckLists(true));
        checkLists.remove(adapter.getCheckList());

        return checkLists;
    }

    private void selectCheckListAndMoveCopyItem(final CheckListItem item) {
        selectCheckListAndMoveCopyItems(new CheckListItem[] {item});
    }

    private void selectCheckListAndMoveCopyItems(final CheckListItem[] items) {
        final ArrayList<CheckList> checkLists = getAvailableCheckListsForMoveCopy();

        if (checkLists.size() > 0) {
            checkLists.sort(new Comparator<CheckList>() {
                @Override
                public int compare(CheckList checkList, CheckList t1) {
                    return checkList.getFullPath().compareToIgnoreCase(t1.getFullPath());
                }
            });

            ArrayList<Object> names = new ArrayList<>();

            for (CheckList checkList : checkLists)
                names.add(checkList.getFullPath());

            Helper.selectItem(this, getResources().getString(R.string.checklistitem_movecopy), names, -1, getResources().getString(R.string.move), getResources().getString(R.string.copy), new Helper.SelectItemListener() {
                @Override
                public void onItemSelected(int position, boolean secondaryAction) {
                    if (ensureCheckListSaved()) {
                        CheckList newCheckList = checkLists.get(position);
                        boolean refreshTimestamp = false;

                        if (secondaryAction) {
                            try {
                                CheckListItem newItem;

                                for (CheckListItem item : items) {
                                    newItem = (CheckListItem) item.clone();

                                    newItem.setId(0);
                                    newItem.setParent(newCheckList);

                                    if (!DatabaseHelper.insertCheckListItem(newItem)) {
                                        Helper.toastSaveFailed(CheckListActivity.this);
                                        break;
                                    }
                                    else
                                        refreshTimestamp = true;
                                }
                            } catch (CloneNotSupportedException e) {}
                        } else {
                            for (CheckListItem item : items) {
                                item.setParent(newCheckList);
                                adapter.onItemRemoved(item);

                                if (!DatabaseHelper.updateCheckListItemParent(item)) {
                                    Helper.toastSaveFailed(CheckListActivity.this);
                                    break;
                                } else
                                    refreshTimestamp = true;
                            }
                        }

                        if (refreshTimestamp)
                            refreshTimestamp();
                    }
                }
            });
        }
    }

    private boolean ensureCheckListSaved() {
        CheckList checkList = adapter.getCheckList();
        String name = ((EditText)findViewById(R.id.name)).getText().toString().trim();
        boolean retval = false;

        if (name.isEmpty())
            name = getResources().getString(R.string.untitled);

        if (checkList.getId() > 0) {
            boolean changed = false;

            if (!name.equals(checkList.getName())) {
                checkList.refreshTimestamp();
                checkList.setName(name);
            }

            retval = DatabaseHelper.updateCheckList(checkList, true);
        } else {
            checkList.setName(name);
            checkList.refreshTimestamp();

            retval = DatabaseHelper.insertCheckList(checkList);
        }

        if (!retval)
            Helper.toastSaveFailed(this);

        return retval;
    }

    private void refreshTimestamp() {
        CheckList checkList = adapter.getCheckList();

        checkList.refreshTimestamp();
        DatabaseHelper.updateCheckList(checkList, true);
    }

    private void checkItem(CheckListItem item, checkItemsAction action) {
        checkItems(new CheckListItem[] {item}, action);
    }

    private void checkItems(CheckListItem[] items, checkItemsAction action) {
        if (ensureCheckListSaved()) {
            boolean newState;

            for (CheckListItem item : items) {
                switch (action) {
                    case check:
                        newState = true;
                        break;
                    case uncheck:
                        newState = false;
                        break;
                    case toggle:
                        newState = !item.isChecked();
                        break;
                    default:
                        newState = item.isChecked();
                        break;
                }

                if (newState != item.isChecked()) {
                    item.setChecked(newState);

                    if (UserPreferences.CheckList.isSetCompletionOnCheck())
                        item.setComplete(item.isChecked());

                    adapter.onItemChanged(item);

                    if (!DatabaseHelper.updateCheckListItem(item)) {
                        Helper.toastSaveFailed(CheckListActivity.this);
                        break;
                    }
                }
            }
        }
    }

    private CheckListItem[] getCheckedItems() {
        ArrayList<CheckListItem> items = new ArrayList<>();

        for (CheckListItem item : adapter.getItems()) {
            if (item.isChecked())
                items.add(item);
        }

        return items.toArray(new CheckListItem[0]);
    }
}
