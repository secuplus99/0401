package com.hj.nf.myapplication;

/**
 * Created by snell1 on 2017-03-06.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import static com.chad.library.adapter.base.listener.SimpleClickListener.TAG;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    SQLiteDatabase db;
    private static final int DbVersion = 11;
    static final String contact = "contact"; //이름
    static final String sms = "sms"; //이름
    static final String mms = "mms"; //이름
    static final String number = "number"; //이름


    static final String file = "file"; //이름
    private static final String DbName = "calllog.db"; //파일 이름

    private Context context;

    public MySQLiteOpenHelper(Context context) {
        super(context, DbName, null, DbVersion);
        db = getWritableDatabase();
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        //콜로그
        String obd_table = "CREATE TABLE contact(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "c_name STRING, " + "c_num STRING, " + "c_date STRING, " + "c_duration STRING, " + " c_type STRING );";
        db.execSQL(obd_table);
        //연락처
        String contact_table = "CREATE TABLE number(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "n_name STRING, " + " n_num STRING );";
        db.execSQL(contact_table);

        String sms_table = "CREATE TABLE sms(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "s_name STRING, " + "s_num STRING, " + "s_date STRING, " + "s_body STRING, " + "s_thread_id INTEGER, " + "s_read INTEGER, " + "s_type STRING );";
        db.execSQL(sms_table);

        String mms_table = "CREATE TABLE mms(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "m_num STRING, " + "m_date STRING, " + "m_body STRING, " + "m_thread_id INTEGER, " + "m_type STRING );";
        db.execSQL(mms_table);


        String file_table = "CREATE TABLE file(" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "f_path STRING, " + "f_filename STRING );";
        db.execSQL(file_table);

    }


    @Override

    //DB업데이트
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // TODO Auto-generated method stub
        String obd_table = "DROP TABLE IF EXISTS contact;";
        String sms_table = "DROP TABLE IF EXISTS sms;";
        String mms_table = "DROP TABLE IF EXISTS mms;";
        String file_table = "DROP TABLE IF EXISTS file;";
        String contact_table = "DROP TABLE IF EXISTS number;";

        db.execSQL(obd_table);
        db.execSQL(sms_table);
        db.execSQL(mms_table);
        db.execSQL(file_table);
        db.execSQL(contact_table);

        onCreate(db);

    }

    public void open() {
        super.onOpen(db);
        db = getWritableDatabase();
    }

    public synchronized void close() {
        db.close();
        super.close();
    }

    /**
     * SQL insert query
     */

    //DB에 내용 넣기
    public void insert_calllog(String num1, String name1, String date1, String dur1, String type1) {
        ContentValues values = new ContentValues();

        values.put("c_name", name1);
        values.put("c_num", num1);
        values.put("c_date", date1);
        values.put("c_duration", dur1);
        values.put("c_type", type1);

        Long rowid = db.insert(contact, null, values);
    }

    public void insert_sms(String num1, String name1, String date1, String body1, String type1, int thread_id1, int read1) {
        ContentValues values = new ContentValues();

        values.put("s_name", name1);
        values.put("s_num", num1);
        values.put("s_date", date1);
        values.put("s_body", body1);
        values.put("s_type", type1);
        values.put("s_thread_id", thread_id1);
        values.put("s_read", read1);

        Long rowid = db.insert(sms, null, values);
    }

    public void insert_mms(String num1,String date1, String body1, String type1, int thread_id1) {
        ContentValues values = new ContentValues();

        values.put("m_num", num1);
        values.put("m_date", date1);
        values.put("m_body", body1);
        values.put("m_type", type1);
        values.put("m_thread_id", thread_id1);

        Long rowid = db.insert(mms, null, values);
    }


    public void insert_file(String path, String name) {
        ContentValues values = new ContentValues();

        values.put("f_path", path);
        values.put("f_filename", name);

        Long rowid = db.insert(file, null, values);
    }
    //연락처
    public void insert_number(String name, String phone) {
        ContentValues values = new ContentValues();

        values.put("n_name", name);
        values.put("n_num", phone);

        Long rowid = db.insert(number, null, values);
    }

    public int insert(ContentValues values) {
        Log.e(TAG, "데이터 " + values);
        return (int) db.insert(contact, null, values);
    }

    public void update_matchset(int id, int done) {
        ContentValues values = new ContentValues();
        values.put("matchset_id", id);
        values.put("isdone", done);
        db.update(contact, values, "matchset_id = ?"
                , new String[]{String.valueOf(id)});
    }

    public int getCount(Cursor c) {
        return c.getCount();
    }

    /**
     * SQL select query
     */

    public Cursor select_matchset() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(contact);
        return qb.query(db, null, null, null, null, null, null);

    }

    public void delete_sms_mms_contact() {

        //새로 연락처를 설정할때 초기화 // db 비우기
        db.execSQL("DELETE FROM " + contact);
        db.execSQL("DELETE FROM " + sms);
        db.execSQL("DELETE FROM " + mms);
        db.execSQL("DELETE FROM " + number);
        context.deleteDatabase(contact);
        context.deleteDatabase(sms);
        context.deleteDatabase(mms);
        context.deleteDatabase(number);

    }

    public void delete_file_db() {

        db.execSQL("DELETE FROM " + file);
        context.deleteDatabase(file);

    }


    /**
     * select * from Table where id = ?
     */
    public Cursor selectWithdone_matchset(int done) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(contact);

        String[] sargs = {done + ""};
        return qb.query(db, null, "isdone=?", sargs, null, null, null);
    }

    public Cursor getCallcolumns() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(contact);

        return qb.query(db, null, null, null, null, null, null);
    }

    public Cursor getSmscolumns() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(sms);

        return qb.query(db, null, null, null, null, null, null);
    }
    public Cursor getMmscolumns() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(mms);

        return qb.query(db, null, null, null, null, null, null);
    }

    public Cursor getFilecolumns() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(file);

        return qb.query(db, null, null, null, null, null, null);
    }
    public Cursor getNumbercolumns() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(number);

        return qb.query(db, null, null, null, null, null, null);
    }


    public Cursor selectWithtime(String num) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(contact);

        String[] sargs = {num};
        return qb.query(db, null, "c_num=? ", sargs, null, null, null);
    }

    public Cursor selectWithid_matchset(int id) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(contact);

        String[] sargs = {id + ""};
        return qb.query(db, null, "matchset_id=?", sargs, null, null, null);
    }

}
