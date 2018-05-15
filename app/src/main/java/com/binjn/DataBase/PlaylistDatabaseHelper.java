package com.binjn.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.binjn.accessctrlsysbinjn.MainActivity;

/**
 * Created by xiaomi2 on 2018/3/18 0018.
 */

public class PlaylistDatabaseHelper extends SQLiteOpenHelper {
    //static
    public static final String DatabaseName = MainActivity.playlistDir + "PlaylistAD.db";
    public static int DatabaseVersion = 1;
    public static final String TABLE_NAME = "playlistAD";
    public static final String SRC_ID = "srcid";
    public static final String FILENAME = "filename";
    public static final String FILESIZE = "filesize";
    public static final String PLAYTIME = "playtime";
    public static final String FILEPATH = "filepath";
    public static final String SORT = "sort";
    public static final String TYPE = "type";
    public static final String BEGINTIME = "begintime";
    public static final String ENDTIME = "endtime";
    public static final String PLAYLISTID = "playlistid";
    //
    public static final String PWD_TABLE = "pwdtables";
    public static final String PWD_ID = "pwdid";
    public static final String PWD = "pwd";
    public static final String CREATETIME = "createtime";
    public static final String EXPTIME = "exptime";

    public PlaylistDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public PlaylistDatabaseHelper(Context context) {
        super(context, DatabaseName, null, DatabaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("PlaylistDatabaseHelper", "onCreate - playlistAD");
        sqLiteDatabase.execSQL("create table " + TABLE_NAME + " (id integer primary key autoincrement,"
                + SRC_ID+" text," + FILENAME+" text," + FILESIZE+" integer," + PLAYTIME+" integer,"
                + FILEPATH+" text," + SORT+" integer," + TYPE + " integer,"+ BEGINTIME+" text,"
                + ENDTIME+" text," + PLAYLISTID+" text)");
        Log.d("PlaylistDatabaseHelper", "onCreate - pwdtables");
        sqLiteDatabase.execSQL("create table " + PWD_TABLE
                + "(id integer primary key autoincrement," + PWD_ID+" text," + PWD+" text,"
                + CREATETIME+" text," + EXPTIME+" text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
