package com.gargsoft.gargnote;

import android.app.Application;
import android.icu.text.CaseMap;

import com.gargsoft.gargnote.models.CheckList;
import com.gargsoft.gargnote.models.CheckListItem;
import com.gargsoft.gargnote.models.Folder;
import com.gargsoft.gargnote.models.Note;
import com.gargsoft.gargnote.utilities.DatabaseHelper;

public class GargNoteApplication extends Application {
    private static Folder rootFolder;

    public static Folder getRootFolder() {
        return rootFolder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseHelper.Initialize(this);

        Folder rootFolder = new Folder(null);

        CreateTestFull(rootFolder);
        //CreateTestCheckLists(rootFolder);

        this.rootFolder = DatabaseHelper.load();

        if (null == this.rootFolder)
            this.rootFolder = new Folder(null);
    }

    private void CreateTestFull(Folder rootFolder)
    {
        Folder folder;
        Folder subFolder;
        Note note;
        CheckList checkList;
        CheckListItem checkListItem;

        folder = new Folder(rootFolder);
        folder.setName("Folder1");
        DatabaseHelper.insertFolder(folder);

        note = new Note(folder);
        note.setName("Folder1Note1");
        DatabaseHelper.insertNote(note);

        checkList = new CheckList(folder);
        checkList.setName("Folder1CheckList1");
        DatabaseHelper.insertCheckList(checkList);

        folder = new Folder(rootFolder);
        folder.setName("Folder2");
        DatabaseHelper.insertFolder(folder);

        subFolder = new Folder(folder);
        subFolder.setName("Folder2Folder3");
        DatabaseHelper.insertFolder(subFolder);

        note = new Note(rootFolder);
        note.setName("Note1");
        DatabaseHelper.insertNote(note);

        note = new Note(rootFolder);
        note.setName("Note2");
        DatabaseHelper.insertNote(note);

        checkList = new CheckList(rootFolder);
        checkList.setName("CheckList1");
        checkListItem = new CheckListItem(checkList);
        checkListItem.setName("CheckListItem1");
        checkListItem = new CheckListItem(checkList);
        checkListItem.setName("CheckListItem2");
        checkListItem.setChecked(true);
        DatabaseHelper.insertCheckList(checkList);

        for (int index = 3; index <= 15; ++index) {
            note = new Note(rootFolder);
            note.setName(String.format("Note%d", index));
            DatabaseHelper.insertNote(note);
        }
    }

    private void CreateTestCheckLists(Folder rootFolder)
    {
        CheckList checkList;
        CheckListItem checkListItem;

        checkList =  new CheckList(rootFolder);
        checkList.setName("CheckList1");

        checkListItem = new CheckListItem(checkList);
        checkListItem.setName("CheckList1Item1");

        checkListItem = new CheckListItem(checkList);
        checkListItem.setName("CheckList1Item2");

        checkListItem = new CheckListItem(checkList);
        checkListItem.setName("CheckList1Item3");

        DatabaseHelper.insertCheckList(checkList);

        checkList =  new CheckList(rootFolder);
        checkList.setName("CheckList2");

        checkListItem = new CheckListItem(checkList);
        checkListItem.setName("CheckList2Item1");

        checkListItem = new CheckListItem(checkList);
        checkListItem.setName("CheckList2Item2");

        DatabaseHelper.insertCheckList(checkList);
    }
}
