package com.huiyuenet.faceCheck;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteDataBase extends SQLiteOpenHelper {

    // 数据库名、表名
    private static final String DATABASE_NAME = "FaceDemo.db";
    private static final String TABLE_NAME_USER_DATA = "user_data";
    // 数据库初始版本
    private static final int VERSION = 1;

    // 列名
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_UID = "uid";
    private static final String COLUMN_NAME_USER_NAME = "user_name";
    private static final String COLUMN_NAME_ENCROLL_TIME = "enroll_dateTime";
    private static final String COLUMU_NAME_USER_FACEFEATURE = "facefeature";
    private static final String COLUMU_NAME_USER_PHONENUMBER = "phonenumber";
    private static final String COLUMU_NAME_USER_COMPANY = "company";
    private static final String COLUMU_NAME_USER_ADDRESS = "address";
    private static final String COLUMU_NAME_USER_FACEBITMAP = "facebitmap";


    // 创建用户表sql语句
    private static final String CREATE_USER_DATA_TABLE = "CREATE TABLE " + "if not exists "
            + TABLE_NAME_USER_DATA + " ("
            + COLUMN_NAME_ID + " integer primary key autoincrement,"
            + COLUMN_NAME_UID + " varchar(30), "
            + COLUMN_NAME_USER_NAME + " varchar(30), "
            + COLUMN_NAME_ENCROLL_TIME + " varchar(30),"
            + COLUMU_NAME_USER_FACEFEATURE +  " TEXT,"
            + COLUMU_NAME_USER_PHONENUMBER +  " varchar(30),"
            + COLUMU_NAME_USER_COMPANY + " varchar(30),"
            + COLUMU_NAME_USER_ADDRESS + " varchar(30),"
            + COLUMU_NAME_USER_FACEBITMAP + " TEXT)";

    // 删除用户表sql语句
    private static final String DROP_TABLE_NAME_USER_DATA = "drop table if exists "
            + TABLE_NAME_USER_DATA;

    private static SqliteDataBase sInstance;

    public static synchronized SqliteDataBase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SqliteDataBase(context.getApplicationContext());
        }
        return sInstance;
    }

    private SqliteDataBase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_DATA_TABLE);
        Log.e("DataBase","OnCreat");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_NAME_USER_DATA);
        onCreate(db);
    }

    /**
     * 根据注册用户实体插入注册用户信息
     *
     * @param user_info
     */
    public void insertUserData(FaceUserInfo user_info) {
        if (user_info == null) {
            throw new IllegalArgumentException("user_info is null");
        }

        SQLiteDatabase db = sInstance.getWritableDatabase();
        final String insertSql = "insert into "
                + TABLE_NAME_USER_DATA + " ("
                + COLUMN_NAME_UID + ", "
                + COLUMN_NAME_USER_NAME + ", "
                + COLUMN_NAME_ENCROLL_TIME + ", "
                + COLUMU_NAME_USER_FACEFEATURE+", "
                + COLUMU_NAME_USER_PHONENUMBER + ", "
                + COLUMU_NAME_USER_COMPANY + ", "
                + COLUMU_NAME_USER_ADDRESS + ", "
                + COLUMU_NAME_USER_FACEBITMAP + ") values(?,?,?,?,?,?,?,?)";
        Object[] args = new Object[]{
                user_info.m_Uid,
                user_info.m_UserName,
                user_info.m_EnrollTime,user_info.facefeature,
                user_info.mPhoneNumber,user_info.mCompany,user_info.mAddress,user_info.mFaceBitmapArray};
        db.execSQL(insertSql, args);
        db.close();
    }

    public void removeAll() {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String deleteSql = "delete from " + TABLE_NAME_USER_DATA;
//        Object[] args = new Object[]{};
        db.execSQL(deleteSql);
        db.close();
    }

    public void removeFirstUser(){
    	SQLiteDatabase db = sInstance.getWritableDatabase();
    	String sql = "delete from " + TABLE_NAME_USER_DATA + " where " + COLUMN_NAME_ID + 
    			" = (select min(" + COLUMN_NAME_ID + ") from " + TABLE_NAME_USER_DATA + ")";
        db.execSQL(sql);
        db.close();
    }
    
    /**
     * 根据uid删除一条记录
     *
     * @param uid
     */
    public void removeByUid(String uid) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String deleteSql = "delete from " + TABLE_NAME_USER_DATA + " where " + COLUMN_NAME_UID + " = ?";
        Object[] args = new Object[]{uid};
        db.execSQL(deleteSql, args);
        db.close();
    }

    /**
     * 根据用户名删除一条记录
     *
     * @param name
     */
    public void removeByName(String name) {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        String deleteSql = "delete from " + TABLE_NAME_USER_DATA + " where " + COLUMN_NAME_USER_NAME + " = ?";
        Object[] args = new Object[]{name};
        db.execSQL(deleteSql, args);
        db.close();
    }



    /**
     * 查询全部用户实体
     *
     * @return 返回查询到的用户实体集合
     */
    public List<FaceUserInfo> queryAll() {
        SQLiteDatabase db = sInstance.getReadableDatabase();
        final String querySql = "select * from " + TABLE_NAME_USER_DATA;
        Cursor cursor = db.rawQuery(querySql, null);

        if (cursor == null) {
            throw new IllegalArgumentException("cursor is null");
        }

        List<FaceUserInfo> userArray = new ArrayList<FaceUserInfo>();

        FaceUserInfo user_info = null;
        while (cursor.moveToNext()) {
            user_info = new FaceUserInfo();
            user_info.m_Id = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID));
            user_info.m_Uid = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_UID));
            user_info.m_UserName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USER_NAME));
            user_info.m_EnrollTime = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ENCROLL_TIME));
            user_info.facefeature=cursor.getBlob(cursor.getColumnIndex(COLUMU_NAME_USER_FACEFEATURE));
            user_info.mPhoneNumber=cursor.getString(cursor.getColumnIndex(COLUMU_NAME_USER_PHONENUMBER));
            user_info.mCompany=cursor.getString(cursor.getColumnIndex(COLUMU_NAME_USER_COMPANY));
            user_info.mAddress=cursor.getString(cursor.getColumnIndex(COLUMU_NAME_USER_ADDRESS));
            user_info.mFaceBitmapArray=cursor.getBlob(cursor.getColumnIndex(COLUMU_NAME_USER_FACEBITMAP));
            userArray.add(user_info);
        }
        cursor.close();
        db.close();

        return userArray;
    }

    public int getUserCount(){

        SQLiteDatabase db = sInstance.getReadableDatabase();

        final String querySql = "select count(*) from " + TABLE_NAME_USER_DATA;
        Cursor cursor = db.rawQuery(querySql, null);

        int result = 0;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }
            cursor.close();
            db.close();
        }
        return result;
    }

}
