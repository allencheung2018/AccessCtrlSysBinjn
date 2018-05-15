package com.binjn.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.binjn.faceimgresovle.FaceDetRec;

/**
 * Created by xiaomi2 on 2017/12/18 0018.
 */

public class FeatDatabaseHelper extends SQLiteOpenHelper {
    //static
    public static final String DatabaseName = FaceDetRec.dirFeatData + "PersonFeat.db";
    public static int DatabaseVersion = 1;
    public static final String TABLE_NAME = "featureData";
    public static final String PERSON_ID = "personid";
    public static final String HASHCODE = "hashcode";
    public static final String NAME = "name";
    public static final String IDCARD_NO = "idCardNo";
    public static final String FEATDATA_PATH = "path";
    public static final String FEATURE_COUNT = "featurecount";
    public static final String ENTER_COUNT = "entercount";
    public static final String ENTER_TIME = "entertime";
    public static final String CREATE_TIME = "createtime";
    public static final String AVAILABLE = "available";
    public static final String NEEDUPLOAD = "needupload";       //1-需上传;0-无需
    //
    public static final String PERSON_TABLE_PREFIX = "Person";
    public static final String FEATFILES = "featfiles";
    public static final String FACEFILES = "facefiles";
    //
    public static final String TABLE_MANAGEABLEPERSON = "manageablepersons";
    public static final String VISFACEREGISTERED = "visibleFaceRegistered";
    public static final String NIFFACEREGISTERED = "nirFaceRegistered";


    public FeatDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public FeatDatabaseHelper(Context context) {
        super(context, DatabaseName, null, DatabaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_NAME + " (id integer primary key autoincrement,"
                + PERSON_ID+" text," + HASHCODE+" text," + NAME+" text," + IDCARD_NO+" text,"
                + FEATDATA_PATH+" text," + FEATURE_COUNT+" integer," + ENTER_COUNT+" integer,"
                + ENTER_TIME+" text," + AVAILABLE+" integer," + NEEDUPLOAD+" integer," + CREATE_TIME+" text)");
        sqLiteDatabase.execSQL("create table " + TABLE_MANAGEABLEPERSON
                + " (id integer primary key autoincrement," + PERSON_ID+" text," + NAME+" text,"
                + IDCARD_NO+" text," + HASHCODE+" text," + VISFACEREGISTERED+" integer,"
                + NIFFACEREGISTERED+" integer)");
        Log.d("FeatDatabaseHelper", "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
