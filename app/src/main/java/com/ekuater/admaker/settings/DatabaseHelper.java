package com.ekuater.admaker.settings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Database helper class for {@link SettingsProvider}. Mostly just has a bit
 * {@link #onCreate} to initialize the database.
 *
 * @author LinYong
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String _ID = Settings.NameValueTable._ID;
    public static final String NAME = Settings.NameValueTable.NAME;
    public static final String VALUE = Settings.NameValueTable.VALUE;

    private static final String DATABASE_NAME = "settings.db";
    private static final int DATABASE_VERSION = 1;

    private final HashSet<String> mValidTables = new HashSet<>();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Collections.addAll(mValidTables, DatabaseConfig.TABLES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String table : mValidTables) {
            createTable(db, table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
    }

    public boolean isValidTable(String name) {
        return mValidTables.contains(name);
    }

    public Set<String> getValidTables() {
        return mValidTables;
    }

    private void createTable(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                NAME + " TEXT UNIQUE ON CONFLICT REPLACE," +
                VALUE + " TEXT" +
                ");");
        db.execSQL("CREATE INDEX " + tableName + "Index1 ON "
                + tableName + " (" + NAME + ");");
    }
}
