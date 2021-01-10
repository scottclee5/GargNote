package com.gargsoft.gargnote.utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.gargsoft.gargnote.models.BaseMainItem;
import com.gargsoft.gargnote.models.CheckList;
import com.gargsoft.gargnote.models.CheckListItem;
import com.gargsoft.gargnote.models.Folder;
import com.gargsoft.gargnote.models.Note;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int CURRENT_VERSION = 1;
    public static final String DATABASE_NAME = "GargNote.db";

    private static DatabaseHelper instance;

    private DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, CURRENT_VERSION);
    }

    public static boolean Initialize(Context context) {
        context.deleteDatabase(DATABASE_NAME);//FIXX - Test DB recreation

        if (null == instance) {
            try {
                instance = new DatabaseHelper(context);

                // This is necessary to create the db.
                instance.getWritableDatabase();
            }
            catch (Exception e) {
                if (null != instance)
                    instance.close();
            }
        }

        return (null != instance);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;

        try {
            sql = "CREATE TABLE baseitem (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "timestamp TEXT NOT NULL," +
                    "folder_id INTEGER REFERENCES baseitem (id)," +
                    "CHECK(id <> folder_id)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE folder (" +
                    "id INTEGER NOT NULL REFERENCES baseitem (id)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE checklist (" +
                    "id INTEGER NOT NULL REFERENCES baseitem (id)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE checklist_item (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "checked INTEGER NOT NULL," +
                    "complete INTEGER NOT NULL," +
                    "timestamp TEXT NOT NULL," +
                    "checklist_id INTEGER NOT NULL REFERENCES checklist (id)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE checklist_dependency (" +
                    "checklist_id INTEGER NOT NULL REFERENCES checklist (id)," +
                    "dependent_checklist_id INTEGER NOT NULL REFERENCES checklist (id)," +
                    "PRIMARY KEY (checklist_id, dependent_checklist_id)," +
                    "CHECK(checklist_id <> dependent_checklist_id)" +
                    ")";

            db.execSQL(sql);

            sql = "CREATE TABLE note (" +
                    "id INTEGER NOT NULL REFERENCES baseitem (id)," +
                    "note TEXT NOT NULL" +
                    ")";

            db.execSQL(sql);
        }
        catch (Exception e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    public static Folder load() {
        SQLiteDatabase db = null;

        try {
            Folder folder = new Folder(null);

            db = instance.getReadableDatabase();

            Map<Integer, Folder> folderMap = getAllFolders(db, folder);

            getAllNotes(db, folder, folderMap);
            getAllCheckLists(db, folder, folderMap);

            return folder;
        }
        catch (Exception e) {
        }
        finally {
            if (null != db)
                db.close();
        }

        return null;
    }

    private static Map<Integer, Folder> getAllFolders(SQLiteDatabase db, Folder rootFolder) {
        String sql = "SELECT a.id, b.name, b.timestamp, b.folder_id FROM folder a INNER JOIN baseitem b ON b.id = a.id";
        Cursor cursor = null;

        try {
            Folder folder;
            Integer parentFolderId;
            Map<Folder, Integer> parentMap = new HashMap<Folder, Integer>();
            Map<Integer, Folder> folderIdMap = new HashMap<Integer, Folder>();

            cursor = db.rawQuery(sql, new String[0]);

            while (cursor.moveToNext()) {
                folder = new Folder(null);

                folder.setId(getInt(cursor, "id"));
                folder.setName(getString(cursor, "name"));
                folder.setTimestamp(getDate(cursor, "timestamp"));

                parentFolderId = getInt(cursor, "folder_id", null);
                parentMap.put(folder, parentFolderId);
                folderIdMap.put(folder.getId(), folder);
            }

            for (Map.Entry<Folder, Integer> item : parentMap.entrySet()) {
                parentFolderId = item.getValue();

                if (null != parentFolderId) {
                    folder = folderIdMap.get(parentFolderId);
                    item.getKey().setParent(folder);
                }
                else {
                    item.getKey().setParent(rootFolder);
                }
            }

            return folderIdMap;
        }
        finally {
            if (null != cursor)
                cursor.close();
        }
    }

    private static void getAllNotes(SQLiteDatabase db, Folder rootFolder, Map<Integer, Folder> folderIdMap) {
        String sql = "SELECT a.id, b.name, b.timestamp, a.note, b.folder_id FROM note a INNER JOIN baseitem b ON b.id = a.id";
        Cursor cursor = null;

        try {
            Note note;
            Folder parent;
            Integer parentFolderId;

            cursor = db.rawQuery(sql, new String[0]);

            while (cursor.moveToNext()) {
                parentFolderId = getInt(cursor, "folder_id", null);

                if (null != parentFolderId) {
                    parent = folderIdMap.get(parentFolderId);
                }
                else {
                    parent = rootFolder;
                }

                note = new Note(parent);

                note.setId(getInt(cursor, "id"));
                note.setName(getString(cursor, "name"));
                note.setTimestamp(getDate(cursor, "timestamp"));
                note.setNote(getString(cursor, "note"));
            }
        }
        finally {
            if (null != cursor)
                cursor.close();
        }
    }

    private static void getAllCheckLists(SQLiteDatabase db, Folder rootFolder, Map<Integer, Folder> folderIdMap) {
        String sql = "SELECT a.id, b.name, b.timestamp, b.folder_id FROM checklist a INNER JOIN baseitem b ON b.id = a.id";
        Cursor cursor = null;
        Map<Integer, CheckList> checkListIdMap = new HashMap<Integer, CheckList>();

        try {
            CheckList checkList;
            Folder parent;
            Integer parentFolderId;

            cursor = db.rawQuery(sql, new String[0]);

            while (cursor.moveToNext()) {
                parentFolderId = getInt(cursor, "folder_id", null);

                if (null != parentFolderId) {
                    parent = folderIdMap.get(parentFolderId);
                }
                else {
                    parent = rootFolder;
                }

                checkList = new CheckList(parent);

                checkList.setId(getInt(cursor, "id"));
                checkList.setName(getString(cursor, "name"));
                checkList.setTimestamp(getDate(cursor, "timestamp"));

                checkListIdMap.put(checkList.getId(), checkList);
            }
        }
        finally {
            if (null != cursor)
                cursor.close();
        }

        getAllCheckListItems(db, checkListIdMap);
        getAllCheckListDependencies(db, checkListIdMap);
    }

    private static void getAllCheckListItems(SQLiteDatabase db, Map<Integer, CheckList> checkListIdMap) {
        String sql = "SELECT id, name, checked, complete, timestamp, checklist_id FROM checklist_item";
        Cursor cursor = null;

        for (CheckList item : checkListIdMap.values())
            item.getItems().clear();

        try {
            CheckListItem checkListItem;

            cursor = db.rawQuery(sql, new String[0]);

            while (cursor.moveToNext()) {
                checkListItem = new CheckListItem(checkListIdMap.get(getInt(cursor, "checklist_id")));

                checkListItem.setId(getInt(cursor, "id"));
                checkListItem.setName(getString(cursor, "name"));
                checkListItem.setChecked(getInt(cursor, "checked") != 0);
                checkListItem.setComplete(getInt(cursor, "complete") != 0);
                checkListItem.setTimestamp(getDate(cursor, "timestamp"));
            }
        }
        finally {
            if (null != cursor)
                cursor.close();
        }
    }

    private static void getAllCheckListDependencies(SQLiteDatabase db, Map<Integer, CheckList> checkListIdMap) {
        String sql = "SELECT checklist_id, dependent_checklist_id FROM checklist_dependency";
        Cursor cursor = null;

        for (CheckList item : checkListIdMap.values())
            item.getDependencies().clear();

        try {
            cursor = db.rawQuery(sql, new String[0]);

            while (cursor.moveToNext()) {
                checkListIdMap.get(getInt(cursor, "checklist_id")).getDependencies().add(checkListIdMap.get(getInt(cursor, "dependent_checklist_id")));
            }
        }
        finally {
            if (null != cursor)
                cursor.close();
        }
    }

    private static int insertBaseItem(SQLiteDatabase db, BaseMainItem item) {
        SQLiteStatement statement = db.compileStatement("INSERT INTO baseitem (name, timestamp, folder_id) VALUES(?, ?, ?)");

        try {
            statement.bindString(1, item.getName());
            statement.bindString(2, getDateString(item.getTimestamp()));

            if (item.getParent().getId() > 0)
                statement.bindLong(3, item.getParent().getId());
            else
                statement.bindNull(3);

            item.setId((int)statement.executeInsert());

            return item.getId();
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    private static void updateBaseItem(SQLiteDatabase db, BaseMainItem item) {
        SQLiteStatement statement = db.compileStatement("UPDATE baseitem set name = ?, timestamp = ?, folder_id = ? WHERE id = ?");

        try {
            statement.bindString(1, item.getName());
            statement.bindString(2, getDateString(item.getTimestamp()));

            if (item.getParent().getId() > 0)
                statement.bindLong(3, item.getParent().getId());
            else
                statement.bindNull(3);

            statement.bindLong(4, item.getId());

            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    private static void deleteBaseItem(SQLiteDatabase db, int id) {
        SQLiteStatement statement = db.compileStatement("DELETE FROM baseitem WHERE id = ?");

        try {
            statement.bindLong(1, id);

            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    public static boolean updateParent(BaseMainItem item) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            SQLiteStatement statement = db.compileStatement("UPDATE baseitem SET folder_id = ? WHERE id = ?");

            try {
                if (item.getParent().getId() > 0)
                    statement.bindLong(1, item.getParent().getId());
                else
                    statement.bindNull(1);

                statement.bindLong(2, item.getId());

                statement.executeUpdateDelete();

                return true;
            }
            finally {
                if (null != statement)
                    statement.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean insertFolder(Folder folder) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                insertBaseItem(db, folder);

                SQLiteStatement statement = db.compileStatement("INSERT INTO folder (id) VALUES(?)");

                try {
                    statement.bindLong(1, folder.getId());
                    statement.executeInsert();
                }
                finally {
                    if (null != statement)
                        statement.close();
                }

                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean updateFolder(Folder folder) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                updateBaseItem(db, folder);
                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean deleteFolder(int id) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                deleteFolder(db, id);
                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static void deleteFolder(SQLiteDatabase db, int id) {
        String sql = "SELECT a.id FROM folder a INNER JOIN baseitem b ON b.id = a.id WHERE b.folder_id = ?";
        Cursor cursor = null;
        ArrayList<Integer> folderIds = new ArrayList<>();

        try {
            cursor = db.rawQuery(sql, new String[] {((Integer)id).toString()});

            while (cursor.moveToNext()) {
                folderIds.add(getInt(cursor, "id", null));
            }
        }
        finally {
            if (null != cursor)
                cursor.close();
        }

        for (int folderId : folderIds)
            deleteFolder(db, folderId);

        SQLiteStatement statement = db.compileStatement("DELETE FROM note WHERE id IN (SELECT a.id FROM note a INNER JOIN baseitem b ON b.id = a.id WHERE b.folder_id = ?)");

        try {
            statement.bindLong(1, id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }

        statement = db.compileStatement("DELETE FROM checklist_item WHERE checklist_id IN (SELECT a.id FROM checklist a INNER JOIN baseitem b ON b.id = a.id WHERE b.folder_id = ?)");

        try {
            statement.bindLong(1, id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }

        statement = db.compileStatement("DELETE FROM checklist_dependency WHERE checklist_id IN (SELECT a.id FROM checklist a INNER JOIN baseitem b ON b.id = a.id WHERE b.folder_id = ?)");

        try {
            statement.bindLong(1, id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }

        statement = db.compileStatement("DELETE FROM checklist_dependency WHERE dependent_checklist_id IN (SELECT a.id FROM checklist a INNER JOIN baseitem b ON b.id = a.id WHERE b.folder_id = ?)");

        try {
            statement.bindLong(1, id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }

        statement = db.compileStatement("DELETE FROM checklist WHERE id IN (SELECT a.id FROM checklist a INNER JOIN baseitem b ON b.id = a.id WHERE b.folder_id = ?)");

        try {
            statement.bindLong(1, id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }

        statement = db.compileStatement("DELETE FROM folder WHERE id = ?");

        try {
            statement.bindLong(1, id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }

        statement = db.compileStatement("DELETE FROM baseitem WHERE id = ? OR folder_id = ?");

        try {
            statement.bindLong(1, id);
            statement.bindLong(2, id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    public static boolean insertNote(Note note) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                insertBaseItem(db, note);

                SQLiteStatement statement = db.compileStatement("INSERT INTO note (id, note) VALUES(?, ?)");

                try {
                    statement.bindLong(1, note.getId());
                    statement.bindString(2, note.getNote());
                    statement.executeInsert();
                }
                finally {
                    if (null != statement)
                        statement.close();
                }

                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean updateNote(Note note) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                updateBaseItem(db, note);
                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean deleteNote(int id) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                SQLiteStatement statement = db.compileStatement("DELETE FROM note WHERE id = ?");

                try {
                    statement.bindLong(1, id);
                    statement.executeUpdateDelete();
                }
                finally {
                    if (null != statement)
                        statement.close();
                }

                deleteBaseItem(db, id);

                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean insertCheckList(CheckList checkList) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                insertBaseItem(db, checkList);

                SQLiteStatement statement = db.compileStatement("INSERT INTO checklist (id) VALUES(?)");

                try {
                    statement.bindLong(1, checkList.getId());
                    statement.executeInsert();
                }
                finally {
                    if (null != statement)
                        statement.close();
                }

                if (checkList.getItems().size() > 0)
                    insertCheckListItems(db, checkList.getItems());

                if (checkList.getDependencies().size() > 0)
                    insertCheckListDependencies(db, checkList.getDependencies(), checkList.getId());

                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            for (CheckListItem item :  checkList.getItems())
                item.setId(0);
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean insertCheckListItem(CheckListItem item) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            insertCheckListItems(db, Arrays.asList(new CheckListItem[] {item}));

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    private static void insertCheckListItems(SQLiteDatabase db, Iterable<CheckListItem> items) {
        SQLiteStatement statement = db.compileStatement("INSERT INTO checklist_item (name, checked, complete, timestamp, checklist_id) VALUES(?, ?, ?, ?, ?)");

        try {
            for (CheckListItem item :  items) {
                statement.clearBindings();

                statement.bindString(1, item.getName());
                statement.bindLong(2, (item.isChecked()) ? 1 : 0);
                statement.bindLong(3, (item.isComplete()) ? 1 : 0);
                statement.bindString(4, getDateString(item.getTimestamp()));
                statement.bindLong(5, item.getParent().getId());

                item.setId((int)statement.executeInsert());
            }
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    public static boolean updateCheckListItem(CheckListItem item) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            updateCheckListItems(db, Arrays.asList(new CheckListItem[] {item}));

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    private static void updateCheckListItems(SQLiteDatabase db, Iterable<CheckListItem> items) {
        SQLiteStatement statement = db.compileStatement("UPDATE checklist_item SET name = ?, checked = ?, complete = ?, timestamp = ? WHERE id = ?");

        try {
            for (CheckListItem item :  items) {
                statement.clearBindings();

                statement.bindString(1, item.getName());
                statement.bindLong(2, (item.isChecked()) ? 1 : 0);
                statement.bindLong(3, (item.isComplete()) ? 1 : 0);
                statement.bindString(4, getDateString(item.getTimestamp()));
                statement.bindLong(5, item.getId());

                statement.executeUpdateDelete();
            }
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    public static boolean updateCheckListItemParent(CheckListItem item) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            SQLiteStatement statement = db.compileStatement("UPDATE checklist_item SET checklist_id = ? WHERE id = ?");

            try {
                statement.bindLong(1, item.getParent().getId());
                statement.bindLong(2, item.getId());

                statement.executeUpdateDelete();

                return true;
            }
            finally {
                if (null != statement)
                    statement.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    private static void insertCheckListDependencies(SQLiteDatabase db, Iterable<CheckList> items, int checkList_id) {
        SQLiteStatement statement = db.compileStatement("INSERT INTO checklist_dependency (checklist_id, dependent_checklist_id) VALUES(?, ?)");

        try {
            for (CheckList item :  items) {
                statement.clearBindings();

                statement.bindLong(1, checkList_id);
                statement.bindLong(2, item.getId());

                statement.executeInsert();
            }
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    public static boolean deleteCheckList(int id) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            deleteCheckListItems(db, id);
            deleteCheckListReferences(db, id);
            deleteCheckListDependencies(db, id, null);

            try {
                SQLiteStatement statement = db.compileStatement("DELETE FROM checklist WHERE id = ?");

                try {
                    statement.bindLong(1, id);
                    statement.executeUpdateDelete();
                }
                finally {
                    if (null != statement)
                        statement.close();
                }

                deleteBaseItem(db, id);

                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean deleteCheckListItem(int id) {
        return deleteCheckListItems(Arrays.asList(new Integer[] {id}));
    }

    public static boolean deleteCheckListItems(Iterable<Integer> itemIds) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                deleteCheckListItems(db, itemIds);
                db.setTransactionSuccessful();
                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    private static void deleteCheckListItems(SQLiteDatabase db, Iterable<Integer> itemIds) {
        SQLiteStatement statement = db.compileStatement("DELETE FROM checklist_item WHERE id = ?");

        try {
            for (int itemId :  itemIds) {
                statement.clearBindings();

                statement.bindLong(1, itemId);
                statement.executeUpdateDelete();
            }
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    private static void deleteCheckListItems(SQLiteDatabase db, int checkList_id) {
        SQLiteStatement statement = db.compileStatement("DELETE FROM checklist_item WHERE checklist_id = ?");

        try {
            statement.bindLong(1, checkList_id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    private static void deleteCheckListReferences(SQLiteDatabase db, int checkList_id) {
        SQLiteStatement statement = db.compileStatement("DELETE FROM checklist_dependency WHERE dependent_checklist_id = ?");

        try {
            statement.bindLong(1, checkList_id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    public static boolean deleteCheckListDependencies(int id, Iterable<Integer> dependencyIds) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                deleteCheckListDependencies(db, id, dependencyIds);
                db.setTransactionSuccessful();
                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    private static void deleteCheckListDependencies(SQLiteDatabase db, int checkList_id, Iterable<Integer> dependencyIds) {
        String sql = "DELETE FROM checklist_dependency WHERE checklist_id = ?";

        if (null != dependencyIds) {
            String inList = TextUtils.join(",", dependencyIds);

            if (inList.length() > 0)
                sql += " AND dependent_checklist_id IN (" + inList + ")";
        }

        SQLiteStatement statement = db.compileStatement(sql);

        try {
            statement.bindLong(1, checkList_id);
            statement.executeUpdateDelete();
        }
        finally {
            if (null != statement)
                statement.close();
        }
    }

    public static boolean updateCheckListDependencies(CheckList checkList) {
        SQLiteDatabase db = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                deleteCheckListDependencies(db, checkList.getId(), null);

                if (checkList.getDependencies().size() > 0)
                    insertCheckListDependencies(db, checkList.getDependencies(), checkList.getId());

                db.setTransactionSuccessful();
                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    public static boolean updateCheckList(CheckList checkList, boolean mainCheckListOnly) {
        SQLiteDatabase db = null;
        ArrayList<CheckListItem> newItemList = null;

        try {
            db = instance.getWritableDatabase();

            db.beginTransaction();

            try {
                updateBaseItem(db, checkList);

                if (!mainCheckListOnly) {
                    String sql = "DELETE FROM checklist_item WHERE checklist_id = ?";

                    if (checkList.getItems().size() > 0) {
                        ArrayList<Integer> inList = new ArrayList<>();

                        for (CheckListItem item : checkList.getItems()) {
                            if (item.getId() > 0)
                                inList.add(item.getId());
                        }

                        if (inList.size() > 0)
                            sql += " AND id NOT IN (" + TextUtils.join(",", inList) + ")";
                    }

                    SQLiteStatement statement = db.compileStatement(sql);

                    try {
                        statement.bindLong(1, checkList.getId());
                        statement.executeUpdateDelete();
                    }
                    finally {
                        if (null != statement)
                            statement.close();
                    }

                    newItemList = new ArrayList<>();

                    ArrayList<CheckListItem> existingItemList = new ArrayList<>();

                    for (CheckListItem item :  checkList.getItems()) {
                        if (item.getId() > 0)
                            existingItemList.add(item);
                        else
                            newItemList.add(item);
                    }

                    if (existingItemList.size() > 0)
                        updateCheckListItems(db, existingItemList);

                    if (newItemList.size() > 0)
                        insertCheckListItems(db, newItemList);

                    deleteCheckListDependencies(db, checkList.getId(), null);

                    if (checkList.getDependencies().size() > 0)
                        insertCheckListDependencies(db, checkList.getDependencies(), checkList.getId());
                }

                db.setTransactionSuccessful();

                return true;
            }
            finally {
                db.endTransaction();
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            if (null != newItemList) {
                for (CheckListItem item :  newItemList)
                    item.setId(0);
            }
        }
        finally {
            if (null != db)
                db.close();
        }

        return false;
    }

    private static String getString(Cursor cursor, String name) {
        return getString(cursor, name, "");
    }

    private static String getString(Cursor cursor, String name, String defaultValue) {
        return getString(cursor, cursor.getColumnIndexOrThrow(name), defaultValue);
    }

    private static String getString(Cursor cursor, int index, String defaultValue) {
        String value = defaultValue;

        if (!cursor.isNull(index)) {
            value = cursor.getString(index);
        }

        return value;
    }

    private static Integer getInt(Cursor cursor, String name) {
        return getInt(cursor, name, 0);
    }

    private static Integer getInt(Cursor cursor, String name, Integer defaultValue) {
        return getInt(cursor, cursor.getColumnIndexOrThrow(name), defaultValue);
    }

    private static Integer getInt(Cursor cursor, int index, Integer defaultValue) {
        Integer value = defaultValue;

        if (!cursor.isNull(index)) {
            value = cursor.getInt(index);
        }

        return value;
    }

    private static Date getDate(Cursor cursor, String name) {
        return getDate(cursor, name, null);
    }

    private static Date getDate(Cursor cursor, String name, Date defaultValue) {
        return getDate(cursor, cursor.getColumnIndexOrThrow(name), defaultValue);
    }

    private static Date getDate(Cursor cursor, int index, Date defaultValue) {
        Date value = defaultValue;

        if (!cursor.isNull(index)) {
            value = parseDate(cursor.getString(index));
        }

        return value;
    }

    private static Date parseDate(String dateString) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            return format.parse(dateString);
        }
        catch (Exception e) {
        }

        return null;
    }

    private static String getDateString(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        return format.format(date);
    }
}
